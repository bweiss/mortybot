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
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements the CONFIG command, allowing users to view and set bot properties at runtime.
 */
@BotCommand(name = "CONFIG", restricted = true, help = {
        "Allows you to view and change bot properties",
        "Usage: CONFIG <property> [value]"
})
public class ConfigCommand implements Command {

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public ConfigCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        if (args.isEmpty()) {
            BotCommand cmd = this.getClass().getAnnotation(BotCommand.class);
            for (String line : cmd.help()) {
                event.respondWith(line);
            }

        } else if (args.size() == 1) {
            String propName = args.get(0);
            BotProperties props = BotProperties.getBotProperties();
            String prop = props.getStringProperty(propName);

            if (prop != null) {
                // exact match, show current value
                event.respondWith(propName + ": " + prop);
            } else {
                // see if we match any known properties
                List<String> matching = findMatchingProperties(propName);

                if (!matching.isEmpty()) {
                    event.respondWith(String.format("Found %d matching properties:", matching.size()));
                    event.respondWith(String.join(", ", matching));
                } else {
                    event.respondWith("No matching properties");
                }
            }

        } else {
            String propName = args.get(0);
            BotProperties props = BotProperties.getBotProperties();
            String prop = props.getStringProperty(propName);

            if (prop != null) {
                String newValue = String.join(" ", args.subList(1, args.size()));
                props.setStringProperty(propName, newValue);
                props.save();
                event.respondWith(propName + " -> " + newValue);
            } else {
                event.respondWith("No such property");
            }
        }
    }

    /**
     * Find all property keys that match a given string.
     *
     * @param s the string you want to match
     * @return a <code>List</code> of property keys that contain <code>s</code>
     */
    private static List<String> findMatchingProperties(String s) {
        List<String> matching = new ArrayList<>();
        Pattern patt = Pattern.compile(s);
        BotProperties props = BotProperties.getBotProperties();

        props.getAll().forEach((k, v) -> {
            Matcher m = patt.matcher(k.toString());
            if (m.find()) {
                matching.add(k.toString());
            }
        });

        return matching;
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
