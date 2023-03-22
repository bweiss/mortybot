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
import net.hatemachine.mortybot.bbb.Bottle;
import net.hatemachine.mortybot.bbb.BottleBlueBook;
import net.hatemachine.mortybot.bbb.SearchResult;
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;
import java.util.Optional;

@BotCommand(name = "BOTTLE", help = {
        "Searches Bottle Blue Book for bottles",
        "Usage: BOTTLE <query>"
})
public class BottleCommand implements Command {

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public BottleCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        if (args.isEmpty())
            throw new IllegalArgumentException("Not enough arguments");

        int maxResults = BotProperties.getBotProperties()
                .getIntProperty("bottle.max.results", BotDefaults.BOTTLE_MAX_RESULTS);
        boolean listResults = false;
        String query;

        if (args.get(0).equals("-l")) {
            listResults = true;
            query = String.join(" ", args.subList(1, args.size()));
        } else {
            query = String.join(" ", args);
        }

        List<SearchResult> results = BottleBlueBook.search(query);

        if (listResults && !results.isEmpty()) {
            int max = Math.min(results.size(), maxResults);
            event.respondWith(String.format("Showing %d of %d results", max, results.size()));
            for (int i = 0; i < max; i++) {
                event.respondWith(results.get(i).toString());
            }
        } else if (!results.isEmpty()) {
            Optional<Bottle> bottle = BottleBlueBook.fetchBottle(results.get(0).url());
            event.respondWith(bottle.isPresent() ? bottle.get().toString() : results.get(0).toString());
        } else {
            event.respondWith("No results");
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
