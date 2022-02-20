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
package net.hatemachine.mortybot.listeners;

import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.wordle.WordleGame;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Listens for guesses from Wordle players. A listener is created for each game that is started by a player
 * and is specific to that user and channel. They are removed from the bot when a game is finished.
 */
public class WordleGameListener extends ListenerAdapter {

    private static final Pattern WORD_PATTERN = Pattern.compile("[a-zA-Z]+");

    private static final Logger log = LoggerFactory.getLogger(WordleGameListener.class);

    private final WordleGame game;

    public WordleGameListener(MortyBot bot, User player, Channel channel) {
        this.game = new WordleGame(bot, player, channel);
        this.game.init();
    }

    @Override
    public void onMessage(final MessageEvent event) {
        log.debug("onMessage event: {}", event);
        if (game.isActive() &&
                event.getUser() == game.getPlayer() &&
                event.getChannel() == game.getChannel() &&
                event.getMessage().length() == BotDefaults.WORDLE_WORD_LENGTH &&
                WORD_PATTERN.matcher(event.getMessage()).matches()) {
            game.guess(event.getMessage());
        }
    }

    public WordleGame getGame() {
        return game;
    }
}
