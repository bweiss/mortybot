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
package net.hatemachine.mortybot.config;

public class BotDefaults {

    private BotDefaults() {
        throw new IllegalStateException("Utility class");
    }

    public static final String  PROPERTIES_FILE = "bot.properties";

    public static final String  BOT_NAME = "morty";
    public static final String  BOT_LOGIN = "morty";
    public static final String  BOT_REAL_NAME = "Aww jeez, Rick!";

    public static final String  IRC_SERVER = "irc.hatemachine.net";
    public static final int     IRC_PORT = 6697;

    public static final boolean AUTO_RECONNECT = false;
    public static final int     AUTO_RECONNECT_DELAY = 30000;
    public static final int     AUTO_RECONNECT_ATTEMPTS = 3;
    public static final boolean AUTO_NICK_CHANGE = true;
    public static final String  AUTO_JOIN_CHANNELS = "";
    public static final String  COMMAND_PREFIX = ".";
}
