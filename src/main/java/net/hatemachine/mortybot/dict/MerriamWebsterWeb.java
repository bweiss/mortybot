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
package net.hatemachine.mortybot.dict;

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

public class MerriamWebsterWeb implements Dictionary {

    private static final String DICTIONARY_URL = "https://www.merriam-webster.com/dictionary/";
    private static final String WOTD_URL = "https://www.merriam-webster.com/word-of-the-day";

    private static final Logger log = LoggerFactory.getLogger(MerriamWebsterWeb.class);

    /**
     * Performs a Merriam-Webster dictionary lookup and returns any definitions found.
     *
     * @param term the term to lookup
     * @return a list of dictionary entries containing any definitions for the given term
     */
    public List<DictionaryEntry> lookup(String term) {
        String url = DICTIONARY_URL + UrlEncoder.encode(Validate.notNullOrBlank(term));
        List<DictionaryEntry> entries = new ArrayList<>();

        log.info("Fetching definition for \"{}\"", term);

        try {
            Document doc = Jsoup.connect(url).get();
            Element content = doc.select("div#left-content").first();

            if (content == null) {
                log.error("Failed to find left-content div!");
            } else {
                Elements entryHeaderDivs = content.select("div.row.entry-header");
                Elements entryAttrDivs = content.select("div.row.entry-attr");
                Elements headwordDivs = content.select("div.row.headword-row");
                List<Element> dictEntryDivs = new ArrayList<>();

                // this seems to be the most reliable way to determine the number of definitions on the page
                boolean end = false;
                for (int i = 1; !end; i++) {
                    Element div = content.select("div#dictionary-entry-" + i).first();
                    if (div == null) {
                        end = true;
                    } else {
                        dictEntryDivs.add(div);
                    }
                }

                for (int i = 0; i < dictEntryDivs.size(); i++) {
                    String word = "--";
                    String type = "--";
                    String attributes = "--";
                    List<String> definitions = new ArrayList<>();
                    Element headerDiv = entryHeaderDivs.get(i);

                    // word and attributes
                    // the top entry is different, so we have to do things like this
                    if (i == 0) {
                        Element h1 = headerDiv.select("h1").first();
                        if (h1 != null) {
                            word = h1.text();
                        }
                        if (entryAttrDivs.size() >= dictEntryDivs.size()) {
                            attributes = entryAttrDivs.get(i).text();
                        }
                    } else {
                        Element p = headerDiv.select("p").first();
                        if (p != null) {
                            word = p.text();
                        }
                        if (headwordDivs.size() >= dictEntryDivs.size()) {
                            attributes = headwordDivs.get(i).text();
                        }
                    }

                    // type
                    Element flSpan = headerDiv.select("span.fl").first();
                    if (flSpan != null) {
                        type = flSpan.text();
                    }

                    // definitions
                    Elements dtTextSpans = dictEntryDivs.get(i).select("span.dtText");
                    for (Element dtSpan : dtTextSpans) {
                        definitions.add(dtSpan.text());
                    }

                    entries.add(new DictionaryEntry(word, type, attributes, definitions));
                }
            }
        } catch (IOException e) {
            log.error("Exception encountered fetching page", e);
        }

        log.info("Found {} dictionary entries for \"{}\"", entries.size(), term);
        return entries;
    }

    /**
     * Retrieves the Word of the Day from the Merriam-Webster website.
     *
     * @return a dictionary entry object containing the definition of the current word of the day
     */
    public Optional<DictionaryEntry> wotd() {
        Optional<DictionaryEntry> optEntry = Optional.empty();

        log.info("Fetching word of the day");

        try {
            Document doc = Jsoup.connect(WOTD_URL).get();
            Element wordDiv = doc.select("div.word-and-pronunciation").first();
            Element typeAttrSpan = doc.select("div.word-attributes span.main-attr").first();
            Element syllableAttrSpan = doc.select("div.word-attributes span.word-syllables").first();
            Element defContainerDiv = doc.select("div.wod-definition-container p").first();

            if (wordDiv != null && typeAttrSpan != null && syllableAttrSpan != null && defContainerDiv != null) {
                optEntry = Optional.of(new DictionaryEntry(wordDiv.child(0).text(), typeAttrSpan.text(), syllableAttrSpan.text(), List.of(defContainerDiv.text())));
            } else {
                log.error("One or more elements were null");
            }
        } catch (IOException e) {
            log.error("Exception encountered fetching page", e);
        }

        return optEntry;
    }
}
