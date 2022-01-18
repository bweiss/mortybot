package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.rt.Movie;
import net.hatemachine.mortybot.rt.RTHelper;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;

/**
 * Implements the RT bot command. This searches for movie titles on rottentomatoes.com.
 *
 * Usage: RT [-l] query_str
 *
 * If the -l flag is present as the first argument, it will respond with a list of results.
 * Otherwise, it will respond with the details for the top result.
 */
public class RtCommand implements BotCommand {

    private static final int    MAX_RESULTS_DEFAULT = 4;
    private static final String RESPONSE_PREFIX = "[rt] ";

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public RtCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        if (args.isEmpty())
            throw new IllegalArgumentException("too few arguments");

        boolean listResults = false;
        int maxResults = MortyBot.getIntProperty("RtCommand.maxResults", MAX_RESULTS_DEFAULT);
        String query;

        if (args.get(0).equals("-l")) {
            listResults = true;
            query = String.join(" ", args.subList(1, args.size()));
        } else {
            query = String.join(" ", args);
        }

        List<Movie> results = RTHelper.search(query);

        if (!results.isEmpty()) {
            if (listResults) {
                // -l flag present, list results
                event.respondWith(String.format(RESPONSE_PREFIX + "Showing top %d results:", Math.min(results.size(), maxResults)));
                for (int i = 0; i < results.size() && i < maxResults; i++) {
                    event.respondWith(RESPONSE_PREFIX + results.get(i));
                }
            } else {
                // display details for top result
                Movie topResult = results.get(0);
                event.respondWith(String.format("%s :: %s", RESPONSE_PREFIX + topResult, topResult.getUrl()));
            }
        }
    }

    @Override
    public GenericMessageEvent getEvent() {
        return event;
    }

    @Override
    public CommandListener.CommandSource getSource() {
        return source;
    }

    @Override
    public List<String> getArgs() {
        return args;
    }
}