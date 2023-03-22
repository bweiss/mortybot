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
package net.hatemachine.mortybot.util;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.BotCommands;
import net.hatemachine.mortybot.Command;
import net.hatemachine.mortybot.CommandWrapper;
import org.reflections.Reflections;

import java.util.*;

import static org.reflections.scanners.Scanners.SubTypes;

public class CommandUtil {

    private static final Map<String, CommandWrapper> commandMap = new TreeMap<>();

    // Scan the commands package for classes that have BotCommand annotations and build a map
    static {
        Reflections reflections = new Reflections("net.hatemachine.mortybot.commands");
        Set<Class<?>> cmdClasses = reflections.get(SubTypes.of(Command.class).asClass());

        for (Class<?> clazz : cmdClasses) {
            List<BotCommand> botCommands = getBotCommandAnnotations(clazz);
            for (BotCommand annotation : botCommands) {
                CommandWrapper cmdWrapper = new CommandWrapper(annotation.name(), clazz, annotation.help());
                commandMap.put(cmdWrapper.getName(), cmdWrapper);
            }
        }
    }

    /**
     * Gets a map of commands available to the bot.
     *
     * @return a map of command strings and wrapper objects containing the specifics of the command
     */
    public static Map<String, CommandWrapper> getCommandMap() {
        return commandMap;
    }

    /**
     * Gets a list of {@link BotCommand} annotations for a particular class.
     *
     * @param clazz the class to retrieve annotations for
     * @return a list of BotCommand annotations for the provided class
     */
    private static List<BotCommand> getBotCommandAnnotations(Class<?> clazz) {
        List<BotCommand> annotations = new ArrayList<>();
        var repeatedAnnotations = clazz.getAnnotation(BotCommands.class);

        if (repeatedAnnotations != null) {
            annotations.addAll(Arrays.asList(repeatedAnnotations.value()));
        } else {
            var annotation = clazz.getAnnotation(BotCommand.class);
            if (annotation != null) {
                annotations.add(annotation);
            }
        }

        return annotations;
    }
}
