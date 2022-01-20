package net.hatemachine.mortybot;

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

public enum Command {

    HELP(HelpCommand.class, new String[] {
            "Usage: HELP [topic]"
    }),

    IMDB(ImdbCommand.class, new String[] {
            "Usage: IMDB [-l] <query>"
    }),

    IPLOOKUP(IpLookupCommand.class, new String[] {
            "Usage: IPLOOKUP <IP>"
    }),

    JOIN(JoinCommand.class, new String[] {
            "Usage: JOIN <channel> [key]"
    }),

    MSG(MessageCommand.class, new String[] {
            "Usage: MSG <user> <text>"
    }),

    OP(OpCommand.class, new String[] {
            "Usage: OP [user]"
    }),

    PART(PartCommand.class, new String[] {
            "Usage: PART [channel]"
    }),

    QUIT(QuitCommand.class, new String[] {
            "Usage: QUIT"
    }),

    RT(RtCommand.class, new String[] {
            "Usage: RT [-l] <query>"
    }),

    STOCK(StockCommand.class, new String[] {
            "Usage: STOCK <symbol>"
    }),

    TEST(TestCommand.class, new String[] {
            "Usage: TEST <args>"
    }),

    USER(UserCommand.class, new String[] {
            "Usage: USER <subcommand> [target] [args]",
            "Subcommands: LIST, SHOW ADD, REMOVE, ADDHOSTMASK, REMOVEHOSTMASK, ADDFLAG, REMOVEFLAG"
    }),

    WTR(WeatherCommand.class, new String[] {
            "Usage: WTR <query>"
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
