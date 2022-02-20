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

import net.hatemachine.mortybot.wordle.WordleHelper;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ServerPingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WordleCleanupListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(WordleCleanupListener.class);

    @Override
    public void onServerPing(final ServerPingEvent event) {
        log.debug("Expiring games and removing inactive listeners");
        WordleHelper helper = new WordleHelper(event.getBot());
        helper.expireGames();
        helper.removeInactiveListeners();
    }
}
