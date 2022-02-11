/*
 * MortyBot - An IRC bot built on the PircBotX framework.
 * Copyright © 2022 Brian Weiss (brianmweiss@gmail.com)
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class UrbanDictionary {

    private static final String SEARCH_URL = "https://www.urbandictionary.com/define.php?term=";

    public static List<Definition> lookup(String term) {
        String searchUrl = SEARCH_URL + URLEncoder.encode(term, StandardCharsets.UTF_8);
        List<Definition> results = new ArrayList<>();
        Document page = null;

        try {
            page = Jsoup.connect(searchUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (page != null) {
            Elements definitionDivs = page.select("div.definition");
            for (Element div : definitionDivs) {
                Element wordElement = div.select("a.word").first();
                Element meaningDiv = div.select("div.meaning").first();
                if (wordElement != null && meaningDiv != null) {
                    results.add(new Definition(wordElement.text(), meaningDiv.text()));
                }
            }
        }

        return results;
    }
}
