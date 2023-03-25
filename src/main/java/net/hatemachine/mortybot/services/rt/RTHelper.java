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
package net.hatemachine.mortybot.services.rt;

import com.uwyn.urlencoder.UrlEncoder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RTHelper {

    private static final String BASE_URL = "https://www.rottentomatoes.com";
    private static final String SEARCH_URL = BASE_URL + "/search?search=";

    private static final Logger log = LoggerFactory.getLogger(RTHelper.class);

    private RTHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Movie> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("null or empty argument: query");
        }

        log.info("Searching Rotten Tomatoes for \"{}\"", query);

        String searchUrl = SEARCH_URL + UrlEncoder.encode(query);
        List<Movie> results = new ArrayList<>();
        Document resultsPage = null;

        try {
            resultsPage = Jsoup.connect(searchUrl).get();
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        if (resultsPage != null) {
            Elements movieList = resultsPage.select("search-page-result[type=movie] > ul[slot=list] > search-page-media-row");
            for (Element movie : movieList) {
                String releaseYear = movie.attr("releaseyear");
                String tomatoMeterScore = movie.attr("tomatometerscore");
                String tomatoMeterState = movie.attr("tomatometerstate");
                Element aTag = movie.select("a[slot=title]").first();
                if (aTag != null) {
                    String url = aTag.attr("href");
                    String name = aTag.text();

                    results.add(new Movie(name, releaseYear, url, tomatoMeterScore, tomatoMeterState));
                }
            }
        }

        log.info("Found {} results", results.size());

        return results;
    }
}
