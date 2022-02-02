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
package net.hatemachine.mortybot.dict;

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

public class MerriamWebster {

    private static final String DICTIONARY_URL = "https://www.merriam-webster.com/dictionary/";
    private static final String WOTD_URL = "https://www.merriam-webster.com/word-of-the-day";

    private static final Logger log = LoggerFactory.getLogger(MerriamWebster.class);

    private MerriamWebster() {
        throw new IllegalStateException("Utility class");
    }

    public static Optional<DictionaryEntry> dictionary(String term) {
        Validate.notNullOrEmpty(term);
        String url = DICTIONARY_URL + URLEncoder.encode(term, StandardCharsets.UTF_8);
        Document doc = null;
        String word;
        String type;
        String syllables = "--";
        String pronunciation = "--";
        List<String> definitions = new ArrayList<>();
        Optional<DictionaryEntry> optEntry = Optional.empty();

        log.info("Fetching definition for \"{}\"", term);

        try {
            doc = Jsoup.connect(url).get();
        } catch (Exception e) {
            log.error("Exception encountered fetching page", e);
        }

        if (doc != null) {
            Element entryHeaderH1 = doc.select("div.entry-header h1").first();
            Element entryHeaderA = doc.select("div.entry-header a").first();
            Element entryAttrSyllablesSpan = doc.select("div.entry-attr span.word-syllables").first();
            Element entryAttrPronSpan = doc.select("div.entry-attr span.pr").first();
            Elements dtTextSpans = doc.select("div#dictionary-entry-1 span.dtText");

            if (entryHeaderH1 != null && entryHeaderA != null) {
                word = entryHeaderH1.text();
                type = entryHeaderA.text();

                if (entryAttrSyllablesSpan != null) {
                    syllables = entryAttrSyllablesSpan.text();
                }

                if (entryAttrPronSpan != null) {
                    pronunciation = entryAttrPronSpan.text();
                }

                for (Element span : dtTextSpans) {
                    definitions.add(span.text());
                }

                optEntry = Optional.of(new DictionaryEntry(word, type, syllables, pronunciation, definitions));
            }
        }

        return optEntry;
    }

    public static Optional<DictionaryEntry> wotd() {
        Optional<DictionaryEntry> optEntry = Optional.empty();
        Document doc = null;

        log.info("Fetching word of the day");

        try {
            doc = Jsoup.connect(WOTD_URL).get();
        } catch (Exception e) {
            log.error("Exception encountered fetching page", e);
        }

        if (doc != null) {
            Element wordH1 = doc.select("div.word-and-pronunciation h1").first();
            Element typeAttrSpan = doc.select("div.word-attributes span.main-attr").first();
            Element syllableAttrSpan = doc.select("div.word-attributes span.word-syllables").first();
            Element defContainerDiv = doc.select("div.wod-definition-container p").first();

            if (wordH1 != null && typeAttrSpan != null && syllableAttrSpan != null && defContainerDiv != null) {
                optEntry = Optional.of(new DictionaryEntry(wordH1.text(), typeAttrSpan.text(), syllableAttrSpan.text(), "--", List.of(defContainerDiv.text())));
            }
        }

        return optEntry;
    }
}
