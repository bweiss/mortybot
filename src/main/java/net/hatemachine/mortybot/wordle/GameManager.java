package net.hatemachine.mortybot.wordle;

import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotState;
import org.pircbotx.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GameManager {

    private static final Logger log = LoggerFactory.getLogger(GameManager.class);

    private static GameManager gameManager = null;

    private final Map<User, Game> gamesMap;

    private GameManager() {
        gamesMap = new HashMap<>();
    }

    public static GameManager getGameManager() {
        if (gameManager == null) {
            gameManager = new GameManager();
        }
        return gameManager;
    }

    public Optional<Game> getGame(User user) {
        Optional<Game> game = Optional.empty();
        if (gamesMap.containsKey(user)) {
            game = Optional.of(gamesMap.get(user));
        }
        return game;
    }

    public List<Game> getActiveGames() {
        return gamesMap.values()
                .stream()
                .filter(Game::isActive)
                .toList();
    }

    public List<Game> getInactiveGames() {
        return gamesMap.values()
                .stream()
                .filter(Game::isInactive)
                .toList();
    }

    public void addGame(Game game) {
        log.debug("Adding game to games map: {}", game);
        gamesMap.put(game.getPlayer(), game);
    }

    public void removeGame(Game game) {
        log.debug("Removing game from games map: {}", game);
        gamesMap.remove(game.getPlayer());
    }

    public void expireGames() {
        log.debug("Checking for expired games");

        for (Game game : getActiveGames()) {
            if (game.isActive()) {
                long maxGameMins = BotState.getBotState()
                        .getIntProperty("wordle.max.duration", BotDefaults.WORDLE_MAX_DURATION);
                Duration duration = Duration.between(game.getStartTime(), LocalDateTime.now());
                Duration maxDuration = Duration.ofMinutes(maxGameMins);

                if (duration.compareTo(maxDuration) > 0) {
                    log.debug("Ending game that has exceeded max duration: {}", game);
                    game.end();
                    // TODO: we should be able to let the player know somehow
                }
            }
        }
    }

    public void purgeInactiveGames() {
        for (Game game : getInactiveGames()) {
            log.debug("Purging inactive game: {}", game);
            gamesMap.remove(game.getPlayer(), game);
        }
    }
}
