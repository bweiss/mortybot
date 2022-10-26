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
package net.hatemachine.mortybot.bbb;

import net.hatemachine.mortybot.util.Validate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BottleBlueBook {

    private static final String SEARCH_URL = "https://bottlebluebook.com/search/";

    private static final Logger log = LoggerFactory.getLogger(BottleBlueBook.class);

    private BottleBlueBook() {
        throw new IllegalStateException("Utility class");
    }

    public static List<SearchResult> search(String query) {
        Validate.notNullOrBlank(query);
        String searchUrl = SEARCH_URL + URLEncoder.encode(query, StandardCharsets.UTF_8);
        List<SearchResult> results = new ArrayList<>();
        Document resultsPage = null;

        log.info("Searching Bottle Blue Book for \"{}\"", query);

        try {
            resultsPage = Jsoup.connect(searchUrl).get();
        } catch (IOException e) {
            log.error("Exception encountered fetching page", e);
        }

        if (resultsPage != null) {
            Elements listingsBoxInfoDivs = resultsPage.select("div.listings_box_info");

            for (Element div : listingsBoxInfoDivs) {
                Element a = div.select("a").first();
                if (a != null) {
                    String url = a.attr("href");
                    String name = a.text();
                    String proof = null;
                    String size = null;
                    Element leftSpan = div.select("span.pull-left").first();
                    if (leftSpan != null) {
                        proof = leftSpan.text();
                    }
                    Element rightSpan = div.select("span.pull-right").first();
                    if (rightSpan != null) {
                        size = rightSpan.text();
                    }
                    results.add(new SearchResult(name, url, proof, size));
                }
            }
        }

        log.info("Found {} results", results.size());
        return results;
    }

    public static Optional<Bottle> fetchBottle(String url) {
        Validate.notNullOrBlank(url);
        Optional<Bottle> bottle = Optional.empty();
        Document bottlePage = null;

        log.info("Fetching bottle details for: {}", url);

        try {
            bottlePage = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("Exception encountered fetching page", e);
        }

        if (bottlePage != null) {
            Elements boxContentsDivs = bottlePage.select("div.box-contents");
            Element bottleHeadingDiv = boxContentsDivs.select("div#bottle_heading").first();

            if (bottleHeadingDiv != null) {
                Elements lineItems = bottleHeadingDiv.select("li");
                Element h1 = boxContentsDivs.select("h1").first();

                if (h1 != null) {
                    String name = h1.text();
                    String type = "";
                    String bottled = "";
                    String age = "";
                    String proof = "";
                    String size = "";
                    String owner = "";
                    String producer = "";
                    String location = "";

                    for (Element li : lineItems) {
                        Element headingTitleSpan = li.select("span.bottle_heading_title").first();
                        Element headingInfoSpan = li.select("span.bottle_heading_info").first();

                        if (headingTitleSpan != null && headingInfoSpan != null) {
                            String key = headingTitleSpan.text();
                            String value = headingInfoSpan.text();

                            switch (key) {
                                case "Type" -> type = value;
                                case "Bottled" -> bottled = value;
                                case "Age" -> age = value;
                                case "Proof" -> proof = value;
                                case "Size" -> size = value;
                                case "Owner" -> owner = value;
                                case "Producer" -> producer = value;
                                case "Location" -> location = value;
                                default -> log.warn("Invalid key: {}", key);
                            }
                        }
                    }

                    bottle = Optional.of(new Bottle(name, url, type, bottled, age, proof, size, owner, producer, location));
                }
            }
        }

        return bottle;
    }
}
