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

import net.hatemachine.mortybot.commands.*;

public enum Command {

    BAN(BanKickCommand.class, new String[] {
            "Bans a user from a channel",
            "Usage: BAN <nick|hostmask>",
            "Usage: BAN <nick|hostmask> <channel>",
            "You must specify the channel if command is not from a public source"
    }),

    BANKICK(BanKickCommand.class, new String[] {
            "Bans and kicks a user from a channel",
            "Usage: BANKICK <nick> [reason]",
            "Usage: BANKICK <nick> <channel> [reason]",
            "You must specify the channel if command is not from a public source"
    }),

    BOTTLE(BottleCommand.class, new String[] {
            "Searches Bottle Blue Book for bottles",
            "Usage: BOTTLE <query>"
    }),

    CHANNEL(ChannelCommand.class, new String[] {
            "Manages the bot's channels",
            "Usage: CHANNEL <subcommand> [args]",
            "Subcommands: ADD, ADDBAN, ADDFLAG, LIST, MODES, REMOVE, REMOVEBAN, REMOVEFLAG, SHOW, SHOWBANS"
    }),

    CHAT(ChatCommand.class, new String[] {
            "Tells the bot to initiate a DCC chat request with you or [nick] if specified",
            "Usage: CHAT [nick]"
    }),

    CONFIG(ConfigCommand.class, new String[] {
            "Allows you to view and change bot properties",
            "Usage: CONFIG <property> [value]"
    }),

    DICT(DictionaryCommand.class, new String[] {
            "Gets the dictionary definition for a word",
            "Usage: DICT <word>"
    }),

    GEOIP(GeoIpCommand.class, new String[] {
            "Shows the geographic location of an IP address or hostname",
            "Usage: GEOIP <IP|hostname>"
    }),

    GOO(GoogleCommand.class, new String[] {
            "Performs a Google search and displays the top result",
            "Usage: GOO <query>"
    }),

    HELP(HelpCommand.class, new String[] {
            "Shows help and usage information for bot commands",
            "Usage: HELP [command]"
    }),

    HOST(HostCommand.class, new String[] {
            "Looks up basic information on a particular hostname or IP address",
            "Usage: HOST <address>"
    }),

    IMDB(ImdbCommand.class, new String[] {
            "Searches IMDB for movie titles or persons",
            "Usage: IMDB [-l] <query>"
    }),

    JOIN(JoinCommand.class, new String[] {
            "Makes the bot join a channel",
            "Usage: JOIN <channel> [key]"
    }),

    KICK(BanKickCommand.class, new String[] {
            "Kicks a user from a channel",
            "Usage: KICK <user> [reason]",
            "Usage: KICK <user> <channel> [reason]",
            "You must specify the channel if command is not from a public source"
    }),

    KICKBAN(BanKickCommand.class, new String[] {
            "Kicks and bans a user from a channel",
            "Usage: KICKBAN <user> [reason]",
            "Usage: KICKBAN <user> <channel> [reason]",
            "You must specify the channel if command is not from a public source"
    }),

    MSG(MessageCommand.class, new String[] {
            "Makes the bot send a private message to a channel or user",
            "Usage: MSG <user> <text>"
    }),

    OP(OpCommand.class, new String[] {
            "Makes the bot op a user",
            "Usage: OP [user]"
    }),

    PART(PartCommand.class, new String[] {
            "Makes the bot part a channel",
            "Usage: PART [channel]"
    }),

    QUIT(QuitCommand.class, new String[] {
            "Makes the bot quit and shutdown",
            "Usage: QUIT"
    }),

    REGISTER(RegisterCommand.class, new String[] {
            "Registers yourself with the bot using your current hostname",
            "Usage: REGISTER [name]"
    }),

    RT(RtCommand.class, new String[] {
            "Searches Rotten Tomatoes for movie ratings",
            "Usage: RT [-l] <query>"
    }),

    STOCK(StockCommand.class, new String[] {
            "Looks up the current price of stock symbols",
            "Usage: STOCK <symbol1> [symbol2] ..."
    }),

    TEST(TestCommand.class, new String[] {
            "Used for testing",
            "Usage: TEST <args>"
    }),

    URB(UrbCommand.class, new String[] {
            "Looks up definitions on urbandictionary.com",
            "Usage: URB [term] [defnum]"
    }),

    USER(UserCommand.class, new String[] {
            "Manages bot users",
            "Usage: USER <subcommand> [target] [args]",
            "Subcommands: LIST, SHOW, ADD, REMOVE, ADDHOSTMASK, REMOVEHOSTMASK, ADDFLAG, REMOVEFLAG, SETLOCATION",
            "Available user flags: ADMIN, AOP, DCC, IGNORE"
    }),

    WHO(WhoCommand.class, new String[] {
            "Displays all users connected to the party line (DCC chat only)",
            "Usage: WHO"
    }),

    WORDLE(WordleCommand.class, new String[] {
            "Play a game of Wordle!",
            "Usage: WORDLE",
            "After starting the game, simply type your 5-letter word guesses into the channel where you began the game"
    }),

    WOTD(WotdCommand.class, new String[] {
            "Fetch the word of the day from Merriam-Webster",
            "Usage: WOTD"
    }),

    WTR(WeatherCommand.class, new String[] {
            "Shows the weather for a location",
            "Usage: WTR [-d] [location]",
            "If the -d option is present the bot will attempt to save your default location (requires being registered with the bot)"
    }),

    YEAR(YearCommand.class, new String[] {
            "Shows progress through the current year",
            "Usage: YEAR"
    });

    private final Class<?> botCommandClass;
    private final String[] help;

    Command(Class<?> botCommandClass, String[] help) {
        this.botCommandClass = botCommandClass;
        this.help = help;
    }

    public Class<?> getBotCommandClass() {
        return botCommandClass;
    }

    public String[] getHelp() {
        return help;
    }
}
