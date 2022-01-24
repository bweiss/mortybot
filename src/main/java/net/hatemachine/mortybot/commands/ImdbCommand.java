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
package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.imdb.IMDBHelper;
import net.hatemachine.mortybot.imdb.SearchResult;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;

/**
 * Implements the IMDB bot command.
 *
 * Usage: IMDB [-l] query_str
 *
 * If the -l flag is present as the first argument, it will respond with a list of results.
 * Otherwise, it will respond with the details for the top result.
 */
public class ImdbCommand implements BotCommand {

    private static final int    MAX_RESULTS_DEFAULT = 4;
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
        if (args.isEmpty())
            throw new IllegalArgumentException("too few arguments");

        boolean listResults = false;
        int maxResults = MortyBot.getIntProperty("ImdbCommand.maxResults", MAX_RESULTS_DEFAULT);
        String query;

        if (args.get(0).equals("-l")) {
            listResults = true;
            query = String.join(" ", args.subList(1, args.size()));
        } else {
            query = String.join(" ", args);
        }

        List<SearchResult> results = IMDBHelper.search(query);

        if (!results.isEmpty()) {
            if (listResults) {
                // -l flag present, list results
                for (int i = 0; i < results.size() && i < maxResults; i++) {
                    event.respondWith(RESPONSE_PREFIX + results.get(i));
                }
            } else {
                // display details for top result
                SearchResult topResult = results.get(0);

                // Person
                if (topResult.getType() == SearchResult.Type.NM) {
                    var person = IMDBHelper.fetchPerson(topResult.getUrl());
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
                    var title = IMDBHelper.fetchTitle(topResult.getUrl());
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
