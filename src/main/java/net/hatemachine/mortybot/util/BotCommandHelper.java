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
import net.hatemachine.mortybot.config.BotProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BotCommandHelper {

    private static final Logger log = LoggerFactory.getLogger(BotCommandHelper.class);

    public BotCommandHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static List<BotCommand> getCommandAnnotations(Class<?> clazz) {
        List<BotCommand> cmdAnnotations = new ArrayList<>();
        var repeatedAnnotations = clazz.getAnnotation(BotCommands.class);

        if (repeatedAnnotations != null) {
            cmdAnnotations.addAll(Arrays.asList(repeatedAnnotations.value()));
        } else {
            var annotation = clazz.getAnnotation(BotCommand.class);
            if (annotation != null) {
                cmdAnnotations.add(annotation);
            }
        }

        return cmdAnnotations;
    }

    public static Map<String, BotCommand> getCommandMap() {
        Map<String, BotCommand> commandMap = new TreeMap<>();
        BotProperties props = BotProperties.getBotProperties();
        List<String> enabledCmdClasses = Arrays.asList(props.getStringProperty("commands.enabled").split(","));
        String pkgPrefix = "net.hatemachine.mortybot.commands.";

        for (String className : enabledCmdClasses) {
            try {
                Class<?> clazz = Class.forName(pkgPrefix + className);
                var annotations = BotCommandHelper.getCommandAnnotations(clazz);
                annotations.forEach(c -> {
                    commandMap.put(c.name(), c);
                });
            } catch (ClassNotFoundException e) {
                log.error("Invalid class: {}", enabledCmdClasses);
            }
        }

        return commandMap;
    }
}
