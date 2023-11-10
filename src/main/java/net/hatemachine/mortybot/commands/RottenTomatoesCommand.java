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
package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.Command;
import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.services.rt.Movie;
import net.hatemachine.mortybot.services.rt.RTHelper;
import net.hatemachine.mortybot.util.Validate;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.pircbotx.Colors;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Implements the RT command. This searches for a movie title on rottentomatoes.com.
 */
@BotCommand(name = "RT", help = {
        "Searches Rotten Tomatoes for movie ratings",
        "Usage: RT [-l] <query>"
})
public class RottenTomatoesCommand implements Command {

    private static final String RESPONSE_PREFIX = "[rt]";
    private static final String TOMATO = Colors.RED + "\uD83C\uDF45" + Colors.NORMAL;
    private static final String SPLAT = Colors.GREEN + "\u273B" + Colors.NORMAL;
    private static final String CERTIFIED_FRESH = TOMATO + Colors.BOLD + "certified-fresh" + Colors.NORMAL + TOMATO;
    private static final String FRESH = TOMATO + "fresh" + TOMATO;
    private static final String ROTTEN = SPLAT + "rotten" + SPLAT;

    private static final Logger log = LoggerFactory.getLogger(RottenTomatoesCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public RottenTomatoesCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        if (args.isEmpty()) {
            throw new IllegalArgumentException("Not enough arguments");
        }

        ArgumentParser parser = ArgumentParsers.newFor("RT").build();
        parser.addArgument("-l", "--list").action(Arguments.storeTrue());
        parser.addArgument("query").nargs("*");
        Namespace ns;

        try {
            ns = parser.parseArgs(args.toArray(new String[0]));
        } catch (ArgumentParserException e) {
            log.error("Problem parsing command arguments", e);
            parser.handleError(e);
            throw new IllegalArgumentException("Problem parsing command");
        }

        if (ns != null) {
            int maxResults = BotProperties.getBotProperties().getIntProperty("rt.max.results", BotDefaults.RT_MAX_RESULTS);
            boolean listFlag = ns.getBoolean("list");
            String query = String.join(" ", ns.getList("query"));

            List<Movie> results = RTHelper.search(query);

            if (results.isEmpty()) {
                event.respondWith("No results found");
            } else {
                if (listFlag) {
                    // -l flag present, list results
                    event.respondWith(String.format(RESPONSE_PREFIX + "Showing top %d results:", Math.min(results.size(), maxResults)));
                    for (int i = 0; i < results.size() && i < maxResults; i++) {
                        event.respondWith(formatResponse(results.get(i)));
                    }
                } else {
                    // display details for top result
                    event.respondWith(formatResponse(results.get(0)));
                }
            }
        }
    }

    private String formatResponse(Movie movie) {
        Validate.notNull(movie);
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%s %s (%s)", RESPONSE_PREFIX, movie.getTitle(), movie.getYear()));

        if (movie.hasScore()) {
            sb.append(String.format(" - %s%%", movie.getScore()));
        }

        if (movie.hasState()) {
            String state = movie.getState();
            sb.append(" [");
            switch (state) {
                case "certified-fresh" -> sb.append(CERTIFIED_FRESH);
                case "fresh" -> sb.append(FRESH);
                case "rotten" -> sb.append(ROTTEN);
                default -> sb.append("--");
            }
            sb.append("]");
        }

        return sb.toString();
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
