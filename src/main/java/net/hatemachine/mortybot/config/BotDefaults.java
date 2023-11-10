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
    public static final boolean IRC_SSL = true;
    public static final boolean AUTO_RECONNECT = true;
    public static final int     AUTO_RECONNECT_DELAY = 30000;
    public static final int     AUTO_RECONNECT_ATTEMPTS = 3;
    public static final boolean AUTO_NICK_CHANGE = true;

    // HibernateUtil
    public static final String  DB_URL = "jdbc:sqlite:mortybot.db";

    // AutoOpListener
    public static final boolean AUTO_OP = true;
    public static final int     AUTO_OP_DELAY = 10000;
    public static final int     AUTO_OP_MAX_MODES = 3;

    // ChatCommand, DccListener
    public static final boolean DCC_CHAT_ENABLED = true;

    // LinkListener
    public static final boolean LINKS_SHORTEN = true;
    public static final boolean LINKS_SHOW_TITLES = true;
    public static final int     LINKS_MAX = 2;
    public static final int     LINKS_MIN_LENGTH = 36;
    public static final int     LINKS_MAX_TITLE_LENGTH = 200;

    // Wordle (Game, WordleListener)
    public static final int     WORDLE_MAX_ATTEMPTS = 6;
    public static final int     WORDLE_WORD_LENGTH = 5;
    public static final boolean WORDLE_QUIET_MODE = false;
    public static final int     WORDLE_MAX_DURATION = 30;

    // BanKickCommand
    public static final int     BAN_MASK_TYPE = 3;
    public static final String  KICK_REASON = "Aww jeez";

    // DictionaryCommand
    public static final int     DICT_MAX_DEFS = 4;

    // ImdbCommand
    public static final int     IMDB_MAX_RESULTS = 4;

    // RegisterCommand
    public static final int     REGISTER_MASK_TYPE = 3;
    public static final String  REGISTER_NORMAL_FLAGS = "DCC";
    public static final String  REGISTER_OWNER_FLAGS = "ADMIN,DCC";

    // RottenTomatoesCommand
    public static final int     RT_MAX_RESULTS = 4;

    // StockCommand
    public static final int     STOCK_MAX_SYMBOLS = 4;

    // UrbanDictionaryCommand
    public static final int     URB_MAX_RESPONSE_LENGTH = 430;

    // UserCommand
    public static final int     USER_ADD_MASK_TYPE = 3;
}
