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
import net.hatemachine.mortybot.services.dict.Dictionary;
import net.hatemachine.mortybot.services.dict.DictionaryEntry;
import net.hatemachine.mortybot.services.dict.MerriamWebsterWeb;
import net.hatemachine.mortybot.exception.CommandException;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Implements the DICT command, allowing users to perform dictionary lookups on the Merriam-Webster website.
 */
@BotCommand(name = "DICT", help = {
        "Gets the dictionary definition for a word",
        "Usage: DICT [-a] <word>"
})
public class DictionaryCommand implements Command {

    private static final Logger log = LoggerFactory.getLogger(DictionaryCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public DictionaryCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        if (args.isEmpty()) {
            throw new CommandException(CommandException.Reason.INVALID_ARGS, "Not enough arguments");
        }

        List<String> newArgs = args;
        boolean showAllDefs = false;
        if (args.get(0).equalsIgnoreCase("-a")) {
            showAllDefs = true;
            newArgs = args.subList(1, args.size());
        }

        int maxDefs = BotProperties.getBotProperties().getIntProperty("dict.max.defs", BotDefaults.DICT_MAX_DEFS);
        Dictionary dict = new MerriamWebsterWeb();
        List<DictionaryEntry> entries = dict.lookup(String.join(" ", newArgs));

        if (entries.isEmpty()) {
            event.respondWith("No results found");
        } else {
            for (DictionaryEntry entry : entries) {
                List<String> defs = entry.definitions();

                log.debug("Dictionary entry for {} has {} definitions", entry.word(), defs.size());
                event.respondWith(entry.toString());

                for (int i = 0; i < defs.size() && (showAllDefs || i < maxDefs); i++) {
                    event.respondWith("#" + (i + 1) + defs.get(i));
                }

                if (!showAllDefs && defs.size() > maxDefs) {
                    event.respondWith(String.format("Max definitions reached (%s of %s shown). Pass the -a option to show all.", maxDefs, defs.size()));
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
