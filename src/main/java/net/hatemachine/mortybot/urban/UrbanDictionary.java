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
package net.hatemachine.mortybot.urban;

import com.uwyn.urlencoder.UrlEncoder;
import net.hatemachine.mortybot.util.Validate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UrbanDictionary {

    private static final String BASE_URL = "https://www.urbandictionary.com";
    private static final String SEARCH_URL = BASE_URL + "/define.php?term=";

    private static final Logger log = LoggerFactory.getLogger(UrbanDictionary.class);

    public static List<Definition> lookup() {
        return doLookup(BASE_URL);
    }

    public static List<Definition> lookup(String term) {
        return doLookup(SEARCH_URL + UrlEncoder.encode(term));
    }

    private static List<Definition> doLookup(String url) {
        Validate.notNullOrBlank(url);
        List<Definition> results = new ArrayList<>();
        Optional<Document> page = fetchPage(url);
        if (page.isPresent()) {
            results = parseResults(page.get());
        } else {
            log.error("Failed to fetch page");
        }
        return results;
    }

    private static Optional<Document> fetchPage(String url) {
        Optional<Document> page = Optional.empty();
        try {
            page = Optional.of(Jsoup.connect(url).get());
        } catch (IOException e) {
            log.error("Exception encountered fetching page, url: {}", url, e);
        }
        return page;
    }

    private static List<Definition> parseResults(Document page) {
        Validate.notNull(page);
        List<Definition> results = new ArrayList<>();

        Elements definitionDivs = page.select("div.definition");
        for (Element div : definitionDivs) {
            Element wordElement = div.select("a.word").first();
            Element meaningDiv = div.select("div.meaning").first();
            if (wordElement != null && meaningDiv != null) {
                results.add(new Definition(wordElement.text(), meaningDiv.text()));
            }
        }

        return results;
    }
}
