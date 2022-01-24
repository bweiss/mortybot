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
package net.hatemachine.mortybot.listeners;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.BotCommandProxy;
import net.hatemachine.mortybot.Command;
import net.hatemachine.mortybot.exception.BotCommandException;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static net.hatemachine.mortybot.listeners.CommandListener.CommandSource.PRIVATE;
import static net.hatemachine.mortybot.listeners.CommandListener.CommandSource.PUBLIC;

/**
 * Listen for commands from users. These can come from any source but currently only
 * messages from channels or direct private messages from users are supported.
 * This will likely be expanded to include DCC chat at some point.
 */
public class CommandListener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);

    private final String commandPrefix;

    public enum CommandSource {
        PRIVATE,
        PUBLIC
    }

    public CommandListener() {
        this.commandPrefix = "!";
    }

    public CommandListener(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    @Override
    public void onMessage(final MessageEvent event) {
        LOGGER.debug("onMessage event: {}", event);
        if (event.getMessage().startsWith(getCommandPrefix())) {
            handleCommand(event, PUBLIC);
        }
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent event) {
        LOGGER.debug("onPrivateMessage event: {}", event);
        if (event.getMessage().startsWith(getCommandPrefix())) {
            handleCommand(event, PRIVATE);
        }
    }

    /**
     * Handle a command from a user. For now this can either be a public command from a channel
     * or a private message from a user but could be expanded to other sources (e.g. CTCP or DCC).
     *
     * @param event the event that contained a command
     * @param source the source of the command, public or private message
     */
    private void handleCommand(final GenericMessageEvent event, CommandSource source) {
        List<String> tokens = Arrays.asList(event.getMessage().split(" "));
        String commandStr = tokens.get(0).substring(getCommandPrefix().length()).toUpperCase(Locale.ROOT);
        List<String> args = tokens.subList(1, tokens.size());
        User user = event.getUser();

        LOGGER.info("{} command triggered by {}, args: {}", commandStr, user.getNick(), args);

        try {
            Command command = Enum.valueOf(Command.class, commandStr);
            BotCommand botCommand = (BotCommand) command.getBotCommandClass()
                    .getDeclaredConstructor(GenericMessageEvent.class, CommandListener.CommandSource.class, List.class)
                    .newInstance(event, source, args);

            BotCommandProxy.newInstance(botCommand).execute();

        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid command {} from {}", commandStr, user.getNick());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOGGER.error("Exception encountered during command invocation", e);
        } catch (BotCommandException e) {
            LOGGER.error("Exception encountered during command execution", e);
        }
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }
}
