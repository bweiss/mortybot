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
import net.hatemachine.mortybot.urban.Definition;
import net.hatemachine.mortybot.urban.UrbanDictionary;
import org.pircbotx.Colors;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;

/**
 * Implements the URBAN command, allowing users to search for terms on Urban Dictionary.
 */
@BotCommand(name = "URB", help = {
        "Looks up definitions on urbandictionary.com",
        "Usage: URB [term] [defnum]"
})
@BotCommand(name = "URBAN", help = {
        "Looks up definitions on urbandictionary.com",
        "Usage: URBAN [term] [defnum]"
})
public class UrbanDictionaryCommand implements Command {

    private static final String RESPONSE_PREFIX = "[" + Colors.BOLD + "urb" + Colors.BOLD + "] ";

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public UrbanDictionaryCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        List<Definition> results;
        String term = "";
        int defNum;

        if (args.isEmpty()) {
            defNum = 1;
        } else {
            try {
                defNum = Integer.parseInt(args.get(args.size() - 1));
                term = String.join(" ", args.subList(0, args.size() - 1));
            } catch (NumberFormatException e) {
                defNum = 1;
                term = String.join(" ", args);
            }
        }

        if (term.isBlank()) {
            results = UrbanDictionary.lookup();
        } else {
            results = UrbanDictionary.lookup(term);
        }

        if (!results.isEmpty() && defNum <= results.size()) {
            Definition def = results.get(defNum - 1);
            String response = RESPONSE_PREFIX + String.format("%s (%d): %s", def.term(), defNum, def.meaning());

            // truncate our response if needed
            int maxLen = BotProperties.getBotProperties()
                    .getIntProperty("urb.max.response.length", BotDefaults.URB_MAX_RESPONSE_LENGTH);
            if (response.length() > maxLen) {
                response = response.substring(0, maxLen - 2) + "...";
            }

            event.respondWith(response);

        } else {
            event.respondWith(RESPONSE_PREFIX + "Definition not found");
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
