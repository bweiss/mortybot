package net.hatemachine.mortybot.imdb;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IMDBHelper {

    private IMDBHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static final String BASE_URL = "https://www.imdb.com";
    public static final String SEARCH_URL = BASE_URL + "/find?q=";

    private static final Logger log = LoggerFactory.getLogger(IMDBHelper.class);

    /**
     * Searches IMDB for a matching persons and titles.
     *
     * @param query the search string
     * @return list of the results
     */
    public static List<SearchResult> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("null or empty argument: query");
        }

        log.info("Searching IMDB for \"{}\"", query);

        String searchUrl = SEARCH_URL + URLEncoder.encode(query, StandardCharsets.UTF_8);
        List<SearchResult> results = new ArrayList<>();
        Document searchResultPage = null;

        // attempt to connect to imdb.com and fetch the search results page
        try {
            searchResultPage = Jsoup.connect(searchUrl).get();
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        if (searchResultPage != null) {
            Elements findSectionDivs = searchResultPage.select("div.findSection");

            for (Element div : findSectionDivs) {
                // find the section type
                Element aTag = div.select("h3.findSectionHeader > a").first();
                String sectionType = null;
                if (aTag != null) {
                    sectionType = aTag.attr("name");
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

                        try {
                            results.add(new SearchResult(name, url, sectionType));
                        } catch (IllegalArgumentException e) {
                            log.error(e.getMessage());
                        }
                    }
                }
            }
        }

        return results;
    }

    /**
     * Fetch the details for a person by URL.
     *
     * @param url the url for this person
     * @return optional person object
     */
    public static Optional<Person> fetchPerson(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("null or empty argument: url");
        }

        log.info("Fetching person details for {}", url);

        Optional<Person> person = Optional.empty();
        Document personDetailsPage = null;

        try {
            personDetailsPage = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("failed to fetch person details page: {}", e.getMessage());
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
    public static Optional<Title> fetchTitle(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("null or empty argument: url");
        }

        log.info("Fetching title details for {}", url);

        Optional<Title> title = Optional.empty();
        Document titleDetailsPage = null;

        try {
            titleDetailsPage = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("Failed to fetch title details page: {}", e.getMessage());
        }

        if (titleDetailsPage != null) {
            Element scriptTag = titleDetailsPage.select("script[type=\"application/ld+json\"]").first();

            if (scriptTag != null) {
                String json = scriptTag.data();
                Configuration conf = Configuration.defaultConfiguration();
                DocumentContext parsedJson = JsonPath.using(conf).parse(json);
                String name = parsedJson.read("$.name");
                Title t = new Title(name, url);

                try {
                    t.setDescription(parsedJson.read("$.description"));
                } catch (PathNotFoundException e) {
                    log.warn("No description found");
                }

                try {
                    t.setRating(parsedJson.read("$.aggregateRating.ratingValue"));
                } catch (PathNotFoundException e) {
                    log.warn("No rating found");
                }

                try {
                    t.setPublishDate(LocalDate.parse(parsedJson.read("$.datePublished"), DateTimeFormatter.ISO_LOCAL_DATE));
                } catch (PathNotFoundException e) {
                    log.warn("No publish date found");
                }

                title = Optional.of(t);
            }
        }

        return title;
    }
}
