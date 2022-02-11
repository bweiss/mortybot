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
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.urban.Definition;
import net.hatemachine.mortybot.urban.UrbanDictionary;
import org.pircbotx.Colors;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;

public class UrbCommand implements BotCommand {

    private static final String RESPONSE_PREFIX = "[" + Colors.BOLD + "urb" + Colors.BOLD + "] ";

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public UrbCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        if (args.isEmpty()) {
            throw new IllegalArgumentException("Not enough arguments");
        }

        int defNum;
        String term;

        try {
            defNum = Integer.parseInt(args.get(args.size() - 1));
            term = String.join(" ", args.subList(0, args.size() - 1));
        } catch (NumberFormatException e) {
            defNum = 1;
            term = String.join(" ", args);
        }

        List<Definition> results = UrbanDictionary.lookup(term);

        if (!results.isEmpty() && defNum <= results.size()) {
            Definition def = results.get(defNum - 1);
            event.respondWith(RESPONSE_PREFIX + String.format("%s (%d): %s", def.term(), defNum, def.meaning()));
        } else {
            event.respondWith("Definition not found");
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
