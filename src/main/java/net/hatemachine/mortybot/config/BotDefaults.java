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

    // MortyBot
    public static final String  PROPERTIES_FILE = "bot.properties";
    public static final String  BOT_NAME = "morty";
    public static final String  BOT_LOGIN = "morty";
    public static final String  BOT_REALNAME = "Aww jeez, Rick!";
    public static final String  BOT_COMMAND_PREFIX = ".";
    public static final String  IRC_SERVER = "irc.hatemachine.net";
    public static final int     IRC_PORT = 6697;
    public static final boolean AUTO_RECONNECT = true;
    public static final int     AUTO_RECONNECT_DELAY = 30000;
    public static final int     AUTO_RECONNECT_ATTEMPTS = 3;
    public static final boolean AUTO_NICK_CHANGE = true;

    // AutoOpListener
    public static final boolean AUTO_OP = true;
    public static final int     AUTO_OP_DELAY = 10000;
    public static final int     AUTO_OP_MAX_MODES = 3;

    // LinkListener
    public static final boolean LINKS_SHORTEN = true;
    public static final boolean LINKS_SHOW_TITLES = true;
    public static final int     LINKS_MAX = 2;
    public static final int     LINKS_MIN_LENGTH = 36;
    public static final int     LINKS_MAX_TITLE_LENGTH = 200;

    // WordleGame
    public static final int     WORDLE_MAX_ATTEMPTS = 6;
    public static final int     WORDLE_WORD_LENGTH = 5;
    public static final boolean WORDLE_SHOW_KEYBOARD = true;
    public static final boolean WORDLE_COMPACT_KEYBOARD = false;
    public static final int     WORDLE_MAX_DURATION_IN_MINUTES = 30;

    // BottleCommand
    public static final int     COMMAND_BOTTLE_MAX_RESULTS = 5;

    // ImdbCommand
    public static final int     COMMAND_IMDB_MAX_RESULTS = 4;

    // RtCommand
    public static final int     COMMAND_RT_MAX_RESULTS = 4;

    // StockCommand
    public static final int     COMMAND_STOCK_MAX_SYMBOLS = 4;

    // UrbCommand
    public static final int     COMMAND_URB_MAX_RESPONSE_LENGTH = 430;
}
