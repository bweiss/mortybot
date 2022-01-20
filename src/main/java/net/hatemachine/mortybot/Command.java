package net.hatemachine.mortybot;

public enum Command {

    HELP(new String[] {
            "Usage: HELP [topic]"
    }),

    IMDB(new String[] {
            "Usage: IMDB [-l] <query>"
    }),

    IPLOOKUP(new String[] {
            "Usage: IPLOOKUP <IP>"
    }),

    JOIN(new String[] {
            "Usage: JOIN <channel> [key]"
    }),

    MSG(new String[] {
            "Usage: MSG <user> <text>"
    }),

    OP(new String[] {
            "Usage: OP [user]"
    }),

    PART(new String[] {
            "Usage: PART [channel]"
    }),

    QUIT(new String[] {
            "Usage: QUIT"
    }),

    RT(new String[] {
            "Usage: RT [-l] <query>"
    }),

    STOCK(new String[] {
            "Usage: STOCK <symbol>"
    }),

    TEST(new String[] {
            "Usage: TEST <args>"
    }),

    USER(new String[] {
            "Usage: USER <subcommand> [target] [args]",
            "Subcommands: LIST, SHOW ADD, REMOVE, ADDHOSTMASK, REMOVEHOSTMASK, ADDFLAG, REMOVEFLAG"
    }),

    WTR(new String[] {
            "Usage: WTR <query>"
    });

    private final String[] help;

    Command(String[] help) {
        this.help = help;
    }

    public String[] getHelp() {
        return help;
    }
}
