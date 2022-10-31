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
import net.hatemachine.mortybot.config.BotProperties;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.reflections.scanners.Scanners.SubTypes;

/**
 * Helper class for working with bot commands and BotCommand annotations.
 *
 * @see BotCommand
 * @see BotCommands
 */
public class BotCommandHelper {

    private static final Logger log = LoggerFactory.getLogger(BotCommandHelper.class);

    public BotCommandHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gets a list of {@link BotCommand} annotations for a class.
     *
     * @param clazz the class to retrieve annotations for
     * @return a list of BotCommand annotations
     */
    public static List<BotCommand> getBotCommandAnnotations(Class<?> clazz) {
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

    /**
     * Gets a map of bot commands and their BotCommand annotations.
     *
     * @return a map of bot commands
     */
    public static Map<String, BotCommand> getBotCommandMap() {
        Map<String, BotCommand> commandMap = new TreeMap<>();
        BotProperties props = BotProperties.getBotProperties();
        List<String> enabledCmdClasses = Arrays.asList(props.getStringProperty("commands.enabled").split(","));
        Reflections reflections = new Reflections("net.hatemachine.mortybot.commands");
        Set<Class<?>> cmdClasses = reflections.get(SubTypes.of(Command.class).asClass());

        for (var clazz : cmdClasses) {
            List<BotCommand> botCommands = BotCommandHelper.getBotCommandAnnotations(clazz);
            botCommands.forEach(c -> {
                if (enabledCmdClasses.contains(clazz.getSimpleName())) {
                    commandMap.put(c.name(), c);
                }
            });
        }

        return commandMap;
    }
}
