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
package net.hatemachine.mortybot;

import net.hatemachine.mortybot.commands.BottleCommand;
import net.hatemachine.mortybot.commands.HelpCommand;
import net.hatemachine.mortybot.commands.ImdbCommand;
import net.hatemachine.mortybot.commands.IpLookupCommand;
import net.hatemachine.mortybot.commands.JoinCommand;
import net.hatemachine.mortybot.commands.MessageCommand;
import net.hatemachine.mortybot.commands.OpCommand;
import net.hatemachine.mortybot.commands.PartCommand;
import net.hatemachine.mortybot.commands.QuitCommand;
import net.hatemachine.mortybot.commands.RtCommand;
import net.hatemachine.mortybot.commands.StockCommand;
import net.hatemachine.mortybot.commands.TestCommand;
import net.hatemachine.mortybot.commands.UserCommand;
import net.hatemachine.mortybot.commands.WeatherCommand;
import net.hatemachine.mortybot.commands.YearCommand;

public enum Command {

    BOTTLE(BottleCommand.class, new String[] {
            "Searches Bottle Blue Book for bottles",
            "Usage: BOTTLE <query>"
    }),

    HELP(HelpCommand.class, new String[] {
            "Shows help and usage information for bot commands",
            "Usage: HELP [topic]"
    }),

    IMDB(ImdbCommand.class, new String[] {
            "Searches IMDB for movie titles or persons",
            "Usage: IMDB [-l] <query>"
    }),

    IPLOOKUP(IpLookupCommand.class, new String[] {
            "Shows the location of an IP address or hostname",
            "Usage: IPLOOKUP <IP>"
    }),

    JOIN(JoinCommand.class, new String[] {
            "Makes the bot join a channel",
            "Usage: JOIN <channel> [key]"
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

    RT(RtCommand.class, new String[] {
            "Searches Rotten Tomatoes for movie titles",
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

    USER(UserCommand.class, new String[] {
            "Manages bot users",
            "Usage: USER <subcommand> [target] [args]",
            "Subcommands: LIST, SHOW ADD, REMOVE, ADDHOSTMASK, REMOVEHOSTMASK, ADDFLAG, REMOVEFLAG",
            "Available user flags: ADMIN, AOP"
    }),

    WTR(WeatherCommand.class, new String[] {
            "Shows the weather for an area",
            "Usage: WTR <query>"
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
