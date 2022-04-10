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
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.dcc.DccManager;
import net.hatemachine.mortybot.events.DccChatMessageEvent;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.managers.ListenerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * DCC chat listener. Responsible for dispatching chat messages on the party line.
 * Commands are ignored as those are handled by the CommandListener class.
 */
public class DccChatListener extends ExtendedListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(DccChatListener.class);

    @Override
    public void onDccChatMessage(final DccChatMessageEvent event) {
        log.debug("DccChatMessageEvent triggered: {}", event);

        MortyBot bot = event.getBot();
        Optional<String> commandPrefix = Optional.empty();
        DccManager dccManager = DccManager.getManager();
        ListenerManager listenerManager = bot.getConfiguration().getListenerManager();
        Optional<Listener> commandListener = listenerManager.getListeners()
                .stream()
                .filter(CommandListener.class::isInstance)
                .findFirst();

        if (commandListener.isPresent()) {
            commandPrefix = Optional.of(((CommandListener) commandListener.get()).getCommandPrefix());
        }

        if (commandPrefix.isEmpty() || !event.getMessage().startsWith(commandPrefix.get())) {
            dccManager.dispatchMessage(String.format("<%s> %s", event.getUser().getNick(), event.getMessage()));
        }
    }
}
