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
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.ServerResponseEvent;

/**
 * Parses server responses for the 005 numeric and sets them for the bot.
 * The values are made available through the bot object.
 */
public class ServerSupportListener extends ListenerAdapter {

    private static final int RPL_SERVERSUPPORT = 5; // 005 numeric

    @Override
    public void onServerResponse(final ServerResponseEvent event) {
        MortyBot bot = event.getBot();
        if (event.getCode() == RPL_SERVERSUPPORT) {
            String[] tokens = event.getRawLine().split(" ");
            for (int i = 3; i < tokens.length; i++) {
                if (tokens[i].charAt(0) == ':')
                    break;
                String[] param = tokens[i].split("=");
                bot.setServerSupportKey(param[0], param.length > 1 ? param[1] : "");
            }
        }
    }

    @Override
    public void onDisconnect(DisconnectEvent event) {
        MortyBot bot = event.getBot();
        bot.clearServerSupport();
    }
}
