/*
 * MortyBot - An IRC bot built on the PircBotX framework.
 * Copyright © 2022 Brian Weiss (brian@hatemachine.net)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.hatemachine.mortybot.services.imdb;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.uwyn.urlencoder.UrlEncoder;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.util.Validate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class IMDBHelper {

    public static final String BASE_URL = "https://www.imdb.com";
    public static final String SEARCH_URL = BASE_URL + "/find/?q=";

    private static final Logger log = LoggerFactory.getLogger(IMDBHelper.class);

    /**
     * Searches IMDB for matching persons and titles.
     *
     * @param query the search string
     * @return list of the results
     */
    public List<SearchResult> search(String query) {
        Validate.notNullOrBlank(query);
        String searchUrl = SEARCH_URL + UrlEncoder.encode(query);
        List<SearchResult> results = new ArrayList<>();
        Document searchResultPage = null;

        log.info("Searching IMDB for \"{}\"", query);
        log.debug("searchUrl: {}", searchUrl);

        // attempt to connect to imdb.com and fetch the search results page
        try {
            searchResultPage = Jsoup.connect(searchUrl).get();
        } catch (IOException e) {
            log.error("Failed to fetch results page: {}", e.getMessage());
            e.printStackTrace();
        }

        if (searchResultPage != null) {
            Elements findSectionDivs = searchResultPage.select("div.findSection");

            for (Element div : findSectionDivs) {
                // find the section type
                Element aTag = div.select("h3.findSectionHeader > a").first();
                SearchResult.Type sectionType = null;
                if (aTag != null) {
                    try {
                        sectionType = Enum.valueOf(SearchResult.Type.class, aTag.attr("name").toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException e) {
                        log.debug("Invalid section: {}", e.getMessage());
                    }
                }

                // get the results for this section and add them to our list
                if (sectionType != null) {
                    Elements tds = div.select("td.result_text");

                    for (Element td : tds) {
                        String name = td.text();
                        String href = td.select("a").attr("href");

                        // strip the extra referrer params
                        int slashCount = 0;
                        int lastSlash = 0;
                        for (int i = 0; i < href.length() && slashCount < 3; i++) {
                            if (href.charAt(i) == '/') {
                                slashCount++;
                                lastSlash = i;
                            }
                        }

                        String url = BASE_URL + href.substring(0, lastSlash + 1);
                        results.add(new SearchResult(name, url, sectionType));
                    }
                }
            }
        }

        log.info("Found {} results", results.size());
        return results;
    }

    /**
     * Fetch the details for a person by URL.
     *
     * @param url the url for this person
     * @return optional person object
     */
    public Optional<Person> fetchPerson(String url) {
        Validate.notNullOrBlank(url);
        Optional<Person> person = Optional.empty();
        Document personDetailsPage = null;

        log.info("Fetching person details for {}", url);

        try {
            personDetailsPage = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .userAgent("MortyBot/" + MortyBot.VERSION)
                    .timeout(12000)
                    .followRedirects(true)
                    .get();
        } catch (IOException e) {
            log.error("Failed to fetch person details page: {}", e.getMessage(), e);
        }

        if (personDetailsPage != null) {
            Element divNameOverview = personDetailsPage.select("div#name-overview-widget").first();

            if (divNameOverview != null) {
                Element h1 = divNameOverview.select("td > h1").first();

                if (h1 != null) {
                    String name = h1.text();
                    var p = new Person(name, url);
                    Element divBioText = personDetailsPage.select("div.name-trivia-bio-text").first();

                    if (divBioText != null) {
                        Element divInline = divBioText.select("div.inline").first();

                        if (divInline != null) {
                            p.setBio(divInline.ownText());
                        }
                    }

                    person = Optional.of(p);
                }
            }
        }

        return person;
    }

    /**
     * Fetch the details for a title by URL.
     *
     * @param url the url for this title
     * @return optional title object
     */
    public Optional<Title> fetchTitle(String url) {
        Validate.notNullOrBlank(url);
        Optional<Title> title = Optional.empty();
        Document titleDetailsPage = null;

        log.info("Fetching title details for {}", url);

        try {
            titleDetailsPage = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("Exception encountered fetching title details", e);
        }

        if (titleDetailsPage != null) {
            Element scriptTag = titleDetailsPage.select("script[type=\"application/ld+json\"]").first();

            if (scriptTag != null) {
                String json = scriptTag.data();
                Title t = createTitleFromJson(url, json);
                title = Optional.of(t);
            }
        }

        return title;
    }

    private Title createTitleFromJson(String url, String json) {
        Configuration conf = Configuration.defaultConfiguration();
        DocumentContext parsedJson = JsonPath.using(conf).parse(json);
        String name = parsedJson.read("$.name");
        Title title = new Title(name, url);

        try {
            title.setDescription(parsedJson.read("$.description"));
        } catch (PathNotFoundException e) {
            log.warn("No description found");
        }

        try {
            title.setRating(Double.parseDouble(parsedJson.read("$.aggregateRating.ratingValue").toString()));
        } catch (PathNotFoundException e) {
            log.warn("No rating found");
        }

        try {
            title.setPublishDate(LocalDate.parse(parsedJson.read("$.datePublished"), DateTimeFormatter.ISO_LOCAL_DATE));
        } catch (PathNotFoundException e) {
            log.warn("No publish date found");
        }

        return title;
    }
}
