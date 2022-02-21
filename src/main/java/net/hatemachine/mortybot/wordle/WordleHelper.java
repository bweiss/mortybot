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
package net.hatemachine.mortybot.wordle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordleHelper {

    protected static final char[] ALPHABET = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };

    private static final String WORD_FILE = "wordle-wordlist.txt";

    private static final Logger log = LoggerFactory.getLogger(WordleHelper.class);
    private static final Random random = new Random();
    private static final List<String> wordList;

    static {
        wordList = new ArrayList<>();

        try (InputStream is = WordleHelper.class.getClassLoader().getResourceAsStream(WORD_FILE);
             InputStreamReader streamReader = new InputStreamReader(is);
             BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                wordList.add(line);
            }

        } catch (IOException e) {
            log.error("Exception encountered reading word file", e);
        }
    }

    public static String getRandomWord() {
        return wordList.get(random.nextInt(wordList.size()));
    }

    public static boolean inWordList(String word) {
        return wordList.contains(word);
    }
}
