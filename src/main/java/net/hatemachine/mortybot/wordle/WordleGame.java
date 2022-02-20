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
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static net.hatemachine.mortybot.wordle.WordleGame.State.*;
import static net.hatemachine.mortybot.wordle.LetterState.*;

public class WordleGame {

    private static final char[] ALPHABET = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    private final MortyBot bot;
    private final User player;
    private final Channel channel;
    private final int maxAttempts;
    private final Map<Character, Letter> letterMap;
    private final Map<Integer, String> tries;
    private List<String> wordList;
    private String word;
    private State state;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private static final Logger log = LoggerFactory.getLogger(WordleGame.class);

    enum State {
        NEW,
        INITIALIZED,
        ACTIVE,
        INACTIVE
    }

    public WordleGame(MortyBot bot, User player, Channel channel) {
        this(bot, player, channel, BotState.getBotState().getIntProperty("wordle.max.attempts", BotDefaults.WORDLE_MAX_ATTEMPTS));
    }

    public WordleGame(MortyBot bot, User player, Channel channel, int maxAttempts) {
        this.bot = bot;
        this.player = player;
        this.channel = channel;
        this.maxAttempts = maxAttempts;
        this.state = NEW;
        this.letterMap = new TreeMap<>();
        this.tries = new TreeMap<>();
        for (char c : ALPHABET) {
            this.letterMap.put(c, new Letter(c, UNUSED));
        }
    }

    public void init() {
        if (state != NEW) {
            throw new IllegalStateException("Game has already been initialized");
        }

        WordleHelper helper = new WordleHelper(bot);
        Random rand = new Random();
        wordList = helper.getWordList();
        word = wordList.get(rand.nextInt(wordList.size()));
        state = INITIALIZED;
        startTime = LocalDateTime.now();

        log.debug("Game initialized [wordlist size: {}, word: {}]", wordList.size(), word);

        printScoreBoard();
    }

    public void guess(String guess) {
        if (state == NEW) {
            throw new IllegalStateException("Game not initialized. Call init() first.");
        }

        if (state == INACTIVE) {
            throw new IllegalStateException("Game is inactive");
        }

        // this is the first guess, start the game
        if (state == INITIALIZED) {
            state = ACTIVE;
        }

        // see if the word is valid
        if (guess.length() != 5) {
            sendToChannel("Word must be 5 characters in length.");
        } else if (!wordList.contains(guess)) {
            sendToChannel(String.format("'%s' not in wordlist. Try again.", guess));

        // word is good, add it to tried and see what we have
        } else {
            tries.put(tries.size(), guess);
            updateLetterState(guess);

            // TODO: add option to suppress display of the full scoreboard and keyboard on every guess
            printScoreBoard();

            if (guess.equalsIgnoreCase(word)) {
                end("You win! The word was " + word + ".");
            } else if (tries.size() == maxAttempts) {
                end("Max attempts reached. Game over.");
            } else {
                printKeyboard();
            }
        }
    }

    public void end(String msg) {
        state = INACTIVE;
        endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);
        sendToChannel(msg);
        sendToChannel("Game duration: " + formatDuration(duration));
    }

    public void printScoreBoard() {
        sendToChannel(String.format("[wordle] Player: %s - Attempts: %d/%d%n", player.getNick(), tries.size(), maxAttempts));
        for (String w : tries.values()) {
            sendToChannel(formatWord(w));
        }
    }

    public void printKeyboard() {
        boolean showKeyboard = BotState.getBotState().getBooleanProperty("wordle.show.keyboard", BotDefaults.WORDLE_SHOW_KEYBOARD);
        boolean compactKeyboard = BotState.getBotState().getBooleanProperty("wordle.compact.keyboard", BotDefaults.WORDLE_COMPACT_KEYBOARD);

        if (showKeyboard) {
            sendToChannel("---------");

            if (compactKeyboard) {
                StringBuilder sb = new StringBuilder("Letters: ");
                for (char c : ALPHABET) {
                    sb.append(letterMap.get(c));
                    sb.append(" ");
                }
                sendToChannel(sb.toString());
            } else {
                char[] row1 = {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'};
                char[] row2 = {'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l'};
                char[] row3 = {'z', 'x', 'c', 'v', 'b', 'n', 'm'};

                StringBuilder sb1 = new StringBuilder();
                for (char c : row1) {
                    sb1.append(letterMap.get(c));
                    sb1.append(" ");
                }
                sendToChannel(sb1.toString());

                StringBuilder sb2 = new StringBuilder();
                sb2.append(" ");
                for (char c : row2) {
                    sb2.append(letterMap.get(c));
                    sb2.append(" ");
                }
                sendToChannel(sb2.toString());

                StringBuilder sb3 = new StringBuilder();
                sb3.append("  ");
                for (char c : row3) {
                    sb3.append(letterMap.get(c));
                    sb3.append(" ");
                }
                sendToChannel(sb3.toString());
            }
        }
    }

    private void updateLetterState(String w) {
        for (int i = 0; i < w.length(); i++) {
            char c = w.charAt(i);
            var curState = letterMap.get(c).state();

            if (c == word.charAt(i)) {
                letterMap.put(c, new Letter(c, EXACT_MATCH));
            } else if (word.indexOf(c) > -1 && curState != EXACT_MATCH) {
                letterMap.put(c, new Letter(c, IMPRECISE_MATCH));
            } else if (curState != EXACT_MATCH && curState != IMPRECISE_MATCH) {
                letterMap.put(c, new Letter(c, NO_MATCH));
            }
        }
    }

    private String formatWord(String w) {
        List<Letter> letters = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < w.length(); i++) {
            char c = w.charAt(i);
            if (c == word.charAt(i)) {
                letters.add(new Letter(c, EXACT_MATCH));
            } else if (word.indexOf(c) > -1) {
                letters.add(new Letter(c, IMPRECISE_MATCH));
            } else {
                letters.add(new Letter(c, UNUSED));
            }
        }

        for (Letter l : letters) {
            sb.append(l);
            sb.append(" ");
        }

        return sb.toString();
    }

    private String formatDuration(Duration duration) {
        List<String> parts = new ArrayList<>();

        long days = duration.toDaysPart();
        if (days > 0) {
            parts.add((int)days + "d");
        }

        int hours = duration.toHoursPart();
        if (hours > 0) {
            parts.add(hours + "h");
        }

        int minutes = duration.toMinutesPart();
        if (minutes > 0) {
            parts.add(minutes + "m");
        }

        int seconds = duration.toSecondsPart();
        if (seconds > 0 || parts.isEmpty()) {
            parts.add(seconds + "s");
        }

        return String.join(", ", parts);
    }

    private void sendToChannel(String msg) {
        channel.getBot().sendIRC().message(channel.getName(), msg);
    }

    public User getPlayer() {
        return player;
    }

    public Channel getChannel() {
        return channel;
    }

    public boolean isActive() {
        return (state == INITIALIZED || state == ACTIVE);
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }
}
