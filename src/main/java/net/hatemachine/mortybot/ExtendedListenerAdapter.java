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
package net.hatemachine.mortybot;

import net.hatemachine.mortybot.events.DccChatMessageEvent;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;

/**
 * Extends PircBotX's ListenerAdapter class so that we can add custom events. Concrete classes that want to
 * handle these events must extend this class instead of ListenerAdapter.
 */
public class ExtendedListenerAdapter extends ListenerAdapter implements Listener {
    @Override
    public void onEvent(Event event) throws Exception {
        if (event instanceof DccChatMessageEvent dccChatMessageEvent) {
            onDccChatMessage(dccChatMessageEvent);
        } else {
            super.onEvent(event);
        }
    }

    public void onDccChatMessage(DccChatMessageEvent event) throws Exception {
        // this method intentionally left empty
    }
}
