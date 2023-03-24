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
import net.hatemachine.mortybot.exception.CommandException;
import net.hatemachine.mortybot.imdb.IMDBHelper;
import net.hatemachine.mortybot.imdb.SearchResult;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;

/**
 * Implements the IMDB command, allowing users to look up titles or persons on the IMDB website.
 */
@BotCommand(name = "IMDB", help = {
        "Searches IMDB for movie titles or persons",
        "Usage: IMDB [-l] <query>",
        "Displays a list of results if the -l option is present"
})
public class ImdbCommand implements Command {

    private static final String RESPONSE_PREFIX = "[imdb] ";

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public ImdbCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        if (args.isEmpty()) {
            throw new CommandException(CommandException.Reason.INVALID_ARGS, "Not enough arguments");
        }

        IMDBHelper helper = new IMDBHelper();
        boolean listResults = false;
        int maxResults = BotProperties.getBotProperties().getIntProperty("imdb.max.results", BotDefaults.IMDB_MAX_RESULTS);
        String query;

        if (args.get(0).equals("-l")) {
            listResults = true;
            query = String.join(" ", args.subList(1, args.size()));
        } else {
            query = String.join(" ", args);
        }

        List<SearchResult> results = helper.search(query);

        if (results.isEmpty()) {
            event.respondWith("No results found");
        } else {
            // -l flag present, list results
            if (listResults) {
                for (int i = 0; i < results.size() && i < maxResults; i++) {
                    event.respondWith(RESPONSE_PREFIX + results.get(i));
                }

            // display details for top result
            } else {
                SearchResult topResult = results.get(0);

                // Person
                if (topResult.getType() == SearchResult.Type.NM) {
                    var person = helper.fetchPerson(topResult.getUrl());
                    if (person.isPresent()) {
                        var p = person.get();
                        event.respondWith(RESPONSE_PREFIX + p);
                        if (p.hasBio()) {
                            event.respondWith(RESPONSE_PREFIX + p.getBio());
                        }
                    }
                }

                // Title
                else if (topResult.getType() == SearchResult.Type.TT) {
                    var title = helper.fetchTitle(topResult.getUrl());
                    if (title.isPresent()) {
                        var t = title.get();
                        event.respondWith(RESPONSE_PREFIX + t);
                        if (t.hasDescription()) {
                            event.respondWith(RESPONSE_PREFIX + t.getDescription());
                        }
                    }
                }
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
