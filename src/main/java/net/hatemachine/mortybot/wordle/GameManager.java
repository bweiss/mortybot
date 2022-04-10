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
            gamesMap.remove(game.getPlayer());
        }
    }
}
