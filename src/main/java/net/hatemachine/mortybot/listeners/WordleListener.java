package net.hatemachine.mortybot.listeners;

import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotState;
import net.hatemachine.mortybot.wordle.Game;
import net.hatemachine.mortybot.wordle.GameManager;
import net.hatemachine.mortybot.wordle.GameState;
import net.hatemachine.mortybot.wordle.WordleException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.ServerPingEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import static net.hatemachine.mortybot.wordle.WordleException.Reason.WORD_INVALID;

public class WordleListener extends ListenerAdapter {

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
            log.debug("Found an existing game for {}", game.getPlayer());

            if (game.isActive()) {
                String msg = event.getMessage().toLowerCase(Locale.ROOT);

                if (msg.length() == BotDefaults.WORDLE_WORD_LENGTH && WORD_PATTERN.matcher(msg).matches()) {
                    try {
                        log.debug("Trying word: {}", msg);
                        GameState state = game.tryWord(msg);

                        switch (state) {

                            case WON -> {
                                event.respond(String.format("You win! The word was '%s'", msg));
                                game.showDuration(event);
                            }

                            case LOST -> {
                                event.respond("Max attempts reached. Game over.");
                                game.showDuration(event);
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
