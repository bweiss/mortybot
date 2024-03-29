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
import net.hatemachine.mortybot.services.dict.DictionaryEntry;
import net.hatemachine.mortybot.services.dict.MerriamWebsterWeb;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;
import java.util.Optional;

/**
 * Implements the WOTD command, allowing users to look up the day's Word of the Day from the Merriam-Webster website.
 */
@BotCommand(name = "WOTD", help = {
        "Fetch the word of the day from Merriam-Webster",
        "Usage: WOTD"
})
public class WotdCommand implements Command {

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public WotdCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        MerriamWebsterWeb mw = new MerriamWebsterWeb();
        Optional<DictionaryEntry> optEntry = mw.wotd();

        if (optEntry.isPresent()) {
            DictionaryEntry entry = optEntry.get();
            List<String> defs = entry.definitions();
            StringBuilder sb = new StringBuilder();

            sb.append(entry);
            if (!defs.isEmpty()) {
                sb.append(": ");
                sb.append(defs.get(0));
            }

            event.respondWith(sb.toString());
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

