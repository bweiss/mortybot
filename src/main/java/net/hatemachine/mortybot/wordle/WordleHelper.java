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

import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotState;
import net.hatemachine.mortybot.listeners.WordleGameListener;
import org.pircbotx.hooks.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WordleHelper {

    private static final String WORD_FILE = "wordle-wordlist.txt";

    private static final Logger log = LoggerFactory.getLogger(WordleHelper.class);

    private final MortyBot bot;

    public WordleHelper(MortyBot bot) {
        this.bot = bot;
    }

    public List<String> getWordList() {
        List<String> wordList = new ArrayList<>();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(WORD_FILE);
             InputStreamReader streamReader = new InputStreamReader(is);
             BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                wordList.add(line);
            }

        } catch (IOException e) {
            log.error("Exception encountered reading word file", e);
        }

        return wordList;
    }

    public synchronized List<WordleGameListener> getListeners() {
        List<WordleGameListener> listeners = new ArrayList<>();
        for (Listener listener : bot.getConfiguration().getListenerManager().getListeners()) {
            if (listener instanceof WordleGameListener wl) {
                listeners.add(wl);
            }
        }
        return listeners;
    }

    public synchronized void expireGames() {
        for (WordleGameListener listener : getListeners()) {
            WordleGame game = listener.getGame();
            if (game.isActive()) {
                long maxGameMins = BotState.getBotState()
                        .getIntProperty("wordle.max.duration.in.minutes", BotDefaults.WORDLE_MAX_DURATION_IN_MINUTES);
                Duration duration = Duration.between(game.getStartTime(), LocalDateTime.now());
                Duration maxDuration = Duration.ofMinutes(maxGameMins);

                if (duration.compareTo(maxDuration) > 0) {
                    game.printScoreBoard();
                    game.end("Time limit exceeded. Game over.");
                }
            }
        }
    }

    public synchronized void removeInactiveListeners() {
        for (WordleGameListener listener : getListeners()) {
            WordleGame game = listener.getGame();
            if (!game.isActive()) {
                bot.getConfiguration().getListenerManager().removeListener(listener);
            }
        }
    }
}
