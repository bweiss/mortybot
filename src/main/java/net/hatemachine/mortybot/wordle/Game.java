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

import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotState;
import net.hatemachine.mortybot.util.DateTimeUtils;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static net.hatemachine.mortybot.wordle.GameState.*;
import static net.hatemachine.mortybot.wordle.LetterState.*;
import static net.hatemachine.mortybot.wordle.WordleHelper.ALPHABET;

public class Game {

    private final User player;
    private final int maxAttempts;
    private final String word;
    private final Map<Character, Letter> letterMap;
    private final Map<Integer, String> tried;
    private GameState state;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;

    public Game(User player) {
        this(player, BotState.getBotState().getIntProperty("wordle.max.attempts", BotDefaults.WORDLE_MAX_ATTEMPTS));
    }

    public Game(User player, int maxAttempts) {
        this.player = player;
        this.maxAttempts = maxAttempts;
        this.word = WordleHelper.getRandomWord();
        this.state = ACTIVE;
        this.startTime = LocalDateTime.now();
        this.letterMap = new TreeMap<>();
        this.tried = new TreeMap<>();
        for (char c : ALPHABET) {
            this.letterMap.put(c, new Letter(c, UNUSED));
        }
    }

    public GameState tryWord(String guess) throws WordleException {
        if (guess.length() != BotDefaults.WORDLE_WORD_LENGTH) {
            throw new IllegalArgumentException(String.format("Word must be %d letters", BotDefaults.WORDLE_WORD_LENGTH));
        }
        if (state != ACTIVE) {
            throw new IllegalStateException("Game is not active, state: " + state);
        }

        guess = guess.toLowerCase(Locale.ROOT);

        if (!WordleHelper.inWordList(guess)) {
            throw new WordleException(WordleException.Reason.WORD_INVALID, guess);
        } else {
            tried.put(tried.size(), guess);
            updateLetterState(guess);

            if (guess.equalsIgnoreCase(word)) {
                state = WON;
                endTime = LocalDateTime.now();
            } else if (tried.size() == maxAttempts) {
                state = LOST;
                endTime = LocalDateTime.now();
            }
        }

        return state;
    }

    public void end() {
        state = INACTIVE;
        endTime = LocalDateTime.now();
    }

    public void showAllTried(final GenericMessageEvent event) {
        event.respondWith(String.format("[wordle] Player: %s, Attempts: %d/%d", player.getNick(), tried.size(), maxAttempts));
        tried.forEach((k, v) -> event.respondWith("     " + formatTriedWord(v)));
    }

    public void showLastTried(final GenericMessageEvent event) {
        if (!tried.isEmpty()) {
            String lastTried = tried.get(tried.size() - 1);
            event.respondWith(String.format("(%d/%d): %s",
                    tried.size(),
                    maxAttempts,
                    formatTriedWord(lastTried)));
        }
    }

    public void showLetters(final GenericMessageEvent event) {
        boolean quietMode = BotState.getBotState().getBooleanProperty("wordle.quiet.mode", BotDefaults.WORDLE_QUIET_MODE);

        event.respondWith("-------------------");

        if (quietMode) {
            // just print out a row of letters showing their state
            event.respondWith(formatLetters());
        } else {
            // print a full keyboard showing letter state
            List<String> rows = formatLettersAsKeyboard();
            for (String row : rows) {
                event.respondWith(row);
            }
        }
    }

    public void showDuration(final GenericMessageEvent event) {
        Duration gameDuration = Duration.between(startTime, endTime);
        event.respondWith("Game duration: " + DateTimeUtils.printDuration(gameDuration));
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

    private String formatTriedWord(String w) {
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

    private String formatLetters() {
        StringBuilder sb = new StringBuilder();
        for (char c : ALPHABET) {
            sb.append(letterMap.get(c));
            sb.append(" ");
        }
        return sb.toString();
    }

    private List<String> formatLettersAsKeyboard() {
        List<String> rows = new ArrayList<>();
        char[] row1 = {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'};
        char[] row2 = {'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l'};
        char[] row3 = {'z', 'x', 'c', 'v', 'b', 'n', 'm'};

        StringBuilder sb1 = new StringBuilder();
        for (char c : row1) {
            sb1.append(letterMap.get(c));
            sb1.append(" ");
        }
        rows.add(sb1.toString());

        StringBuilder sb2 = new StringBuilder(" ");
        for (char c : row2) {
            sb2.append(letterMap.get(c));
            sb2.append(" ");
        }
        rows.add(sb2.toString());

        StringBuilder sb3 = new StringBuilder("   ");
        for (char c : row3) {
            sb3.append(letterMap.get(c));
            sb3.append(" ");
        }
        rows.add(sb3.toString());

        return rows;
    }

    public User getPlayer() {
        return player;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public GameState getState() {
        return state;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public boolean isActive() {
        return state == ACTIVE;
    }

    public boolean isInactive() {
        return state != ACTIVE;
    }
}
