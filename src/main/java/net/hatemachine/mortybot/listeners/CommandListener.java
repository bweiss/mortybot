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
package net.hatemachine.mortybot.listeners;

import net.hatemachine.mortybot.*;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.dcc.DccManager;
import net.hatemachine.mortybot.events.DccChatMessageEvent;
import net.hatemachine.mortybot.exception.CommandException;
import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static net.hatemachine.mortybot.listeners.CommandListener.CommandSource.*;
import static org.reflections.scanners.Scanners.SubTypes;

/**
 * Listens for commands from users.
 * These can come from a number of different sources (e.g. channel messages, private messages, or DCC chat).
 */
public class CommandListener extends ExtendedListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(CommandListener.class);
    private static final Map<String, CommandWrapper> commandMap = new TreeMap<>();

    private final String commandPrefix;

    public enum CommandSource {
        PRIVATE,
        PUBLIC,
        DCC
    }

    public CommandListener() {
        this("!");
    }

    public CommandListener(String commandPrefix) {
        this.commandPrefix = commandPrefix;

        // Scan for command classes
        Reflections reflections = new Reflections("net.hatemachine.mortybot.commands");
        Set<Class<?>> cmdClasses = reflections.get(SubTypes.of(Command.class).asClass());

        // Check for disabled commands
        BotProperties props = BotProperties.getBotProperties();
        List<String> disabled = Arrays.asList(props.getStringProperty("commands.disabled", "").split(","));

        // Build our command map from the annotations
        for (Class<?> clazz : cmdClasses) {
            for (BotCommand annotation : getBotCommandAnnotations(clazz)) {
                if (!disabled.contains(annotation.name())) {
                    CommandWrapper cmdWrapper = new CommandWrapper(annotation.name(), clazz, annotation.restricted(), annotation.help());
                    commandMap.put(cmdWrapper.getName(), cmdWrapper);
                }
            }
        }
    }

    @Override
    public void onMessage(final MessageEvent event) {
        log.debug("MessageEvent triggered: {}", event);
        if (event.getMessage().startsWith(getCommandPrefix())) {
            handleCommand(event, PUBLIC);
        }
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent event) {
        log.debug("PrivateMessageEvent triggered: {}", event);
        if (event.getMessage().startsWith(getCommandPrefix())) {
            handleCommand(event, PRIVATE);
        }
    }

    @Override
    public void onDccChatMessage(final DccChatMessageEvent event) {
        log.debug("DccChatMessageEvent triggered: {}", event);
        if (event.getMessage().startsWith(getCommandPrefix())) {
            handleCommand(event, DCC);
        }
    }

    /**
     * Gets the current command prefix.
     *
     * @return the command prefix
     */
    public String getCommandPrefix() {
        return commandPrefix;
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
     * Gets a single command if it is available.
     *
     * @param commandName the name of the command to retrieve
     * @return the requested command if it exists
     * @see CommandWrapper
     */
    public static CommandWrapper getCommand(String commandName) {
        return commandMap.get(commandName);
    }

    /**
     * Handles a command from a user.
     *
     * @param event the event that contained a command
     * @param source the source of the command, public or private message
     */
    private void handleCommand(final GenericMessageEvent event, CommandSource source) {
        List<String> tokens = Arrays.asList(event.getMessage().split(" "));
        String commandName = tokens.get(0).substring(getCommandPrefix().length()).toUpperCase(Locale.ROOT);
        List<String> args = tokens.subList(1, tokens.size());
        Map<String, CommandWrapper> commandMap = getCommandMap();
        User user = event.getUser();

        if (commandMap.containsKey(commandName)) {
            log.info("{} command triggered by {}, source: {}, args: {}", commandName, user.getNick(), source, args);

            // Dispatch a notification to admins on the party line
            DccManager.getManager().dispatchMessage(String.format("*** %s command triggered by %s [%s]: %s",
                    commandName,
                    user.getNick(),
                    source == PUBLIC ? ((MessageEvent) event).getChannel().getName() : source.toString(),
                    event.getMessage()
            ), true);

            CommandWrapper cmdWrapper = commandMap.get(commandName);

            // Attempt to create a command instance and add it to our wrapper object
            try {
                Command command = (Command) cmdWrapper.getCmdClass()
                        .getDeclaredConstructor(GenericMessageEvent.class, CommandSource.class, List.class)
                        .newInstance(event, source, args);
                cmdWrapper.setInstance(command);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                log.error("Exception encountered trying to create instance of command: {}", e.getMessage(), e);
            }

            // Invoke the command proxy to execute the command if it passes all checks
            try {
                CommandProxy.newInstance(cmdWrapper).execute();
            } catch (CommandException e) {
                if (e.getReason() == CommandException.Reason.INVALID_ARGS) {
                    event.respondWith(e.getMessage());
                } else if (e.getReason() == CommandException.Reason.IGNORED_USER) {
                    log.info("Ignoring command from {}", user.getNick());
                } else if (e.getReason() == CommandException.Reason.UNAUTHORIZED_USER) {
                    event.respondWith("User unauthorized");
                }
            } catch (RuntimeException e) {
                log.error("Exception encountered trying to execute command: {}", commandName, e);
            }
        } else {
            log.info("Invalid command {} from {}", commandName, user.getNick());
        }
    }

    /**
     * Gets a list of {@link BotCommand} annotations for a particular class.
     *
     * @param clazz the class to retrieve annotations for
     * @return a list of BotCommand annotations for the provided class
     */
    private List<BotCommand> getBotCommandAnnotations(Class<?> clazz) {
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
