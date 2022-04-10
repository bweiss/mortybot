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
package net.hatemachine.mortybot.listeners;

import net.hatemachine.mortybot.ExtendedListenerAdapter;
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotState;
import net.hatemachine.mortybot.events.DccChatMessageEvent;
import net.hatemachine.mortybot.util.DateTimeUtils;
import net.hatemachine.mortybot.wordle.Game;
import net.hatemachine.mortybot.wordle.GameManager;
import net.hatemachine.mortybot.wordle.GameState;
import net.hatemachine.mortybot.wordle.WordleException;
import org.pircbotx.Colors;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.ServerPingEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import static net.hatemachine.mortybot.wordle.WordleException.Reason.WORD_INVALID;

public class WordleListener extends ExtendedListenerAdapter {

    private static final Pattern WORD_PATTERN = Pattern.compile("[a-zA-Z]+");

    private static final Logger log = LoggerFactory.getLogger(WordleListener.class);

    @Override
    public void onMessage(final MessageEvent event) {
        log.debug("MessageEvent triggered: {}", event);
        handleMessage(event);
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent event) {
        log.debug("PrivateMessageEvent triggered: {}", event);
        handleMessage(event);
    }

    @Override
    public void onDccChatMessage(final DccChatMessageEvent event) {
        log.debug("DccChatMessageEvent triggered: {}", event);
        handleMessage(event);
    }

    @Override
    public void onServerPing(final ServerPingEvent event) {
        log.debug("Checking for expired games");
        GameManager gm = GameManager.getGameManager();
        gm.expireGames();
        gm.purgeInactiveGames();
    }

    private void handleMessage(final GenericMessageEvent event) {
        GameManager gm = GameManager.getGameManager();
        Optional<Game> optGame = gm.getGame(event.getUser());

        if (optGame.isPresent()) {
            Game game = optGame.get();
            log.debug("Found an existing game: {}", game);

            if (game.isActive()) {
                String msg = event.getMessage().toLowerCase(Locale.ROOT);

                if (msg.length() == BotDefaults.WORDLE_WORD_LENGTH && WORD_PATTERN.matcher(msg).matches()) {
                    try {
                        log.debug("Trying word: {}", msg);
                        GameState state = game.tryWord(msg);

                        switch (state) {

                            case WON -> {
                                Duration gameDuration = Duration.between(game.getStartTime(), game.getEndTime());
                                event.respond(String.format("You win! The word was %s! [Attempts: %d/%d, Duration: %s]",
                                        Colors.BOLD + game.getWord() + Colors.BOLD,
                                        game.getTried().size(),
                                        game.getMaxAttempts(),
                                        DateTimeUtils.printDuration(gameDuration)));
                            }

                            case LOST -> {
                                Duration gameDuration = Duration.between(game.getStartTime(), game.getEndTime());
                                event.respond(String.format("Game over. The word was %s! [Attempts: %d/%d, Duration: %s]",
                                        Colors.BOLD + game.getWord() + Colors.BOLD,
                                        game.getTried().size(),
                                        game.getMaxAttempts(),
                                        DateTimeUtils.printDuration(gameDuration)));
                            }

                            case ACTIVE -> {
                                boolean quietMode = BotState.getBotState().getBooleanProperty("wordle.quiet.mode",
                                        BotDefaults.WORDLE_QUIET_MODE);
                                if (quietMode) {
                                    game.showLastTried(event);
                                } else {
                                    game.showAllTried(event);
                                    game.showLetters(event);
                                }
                            }

                            default -> {
                                // do nothing
                            }
                        }

                    } catch (WordleException e) {
                        if (e.getReason() == WORD_INVALID) {
                            event.respond(String.format("'%s' not in word list. Try again.", msg));
                        }
                    }
                }
            }
        }
    }
}
