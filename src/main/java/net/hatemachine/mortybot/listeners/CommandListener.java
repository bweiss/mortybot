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

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.BotCommandProxy;
import net.hatemachine.mortybot.Command;
import net.hatemachine.mortybot.ExtendedListenerAdapter;
import net.hatemachine.mortybot.dcc.DccManager;
import net.hatemachine.mortybot.events.DccChatMessageEvent;
import net.hatemachine.mortybot.exception.BotCommandException;
import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static net.hatemachine.mortybot.listeners.CommandListener.CommandSource.*;

/**
 * Listen for commands from users.
 * These can come from a number of different sources (e.g. channel messages, private messages, or DCC chat).
 */
public class CommandListener extends ExtendedListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(CommandListener.class);

    private final String commandPrefix;

    public enum CommandSource {
        PRIVATE,
        PUBLIC,
        DCC
    }

    public CommandListener() {
        this.commandPrefix = "!";
    }

    public CommandListener(String commandPrefix) {
        this.commandPrefix = commandPrefix;
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
        DccManager dccManager = DccManager.getManager();

        try {
            Command command = Enum.valueOf(Command.class, commandStr);
            BotCommand botCommand = (BotCommand) command.getBotCommandClass()
                    .getDeclaredConstructor(GenericMessageEvent.class, CommandListener.CommandSource.class, List.class)
                    .newInstance(event, source, args);

            log.info("{} command triggered by {}, source: {}, args: {}", commandStr, user.getNick(), source, args);
            if (source == DCC) {
                dccManager.dispatchMessage(String.format("*** %s command triggered by %s", commandStr, user.getNick()), true);
            }

            BotCommandProxy.newInstance(botCommand).execute();

        } catch (IllegalArgumentException e) {
            log.warn("Invalid command {} from {}", commandStr, user.getNick());
        } catch (BotCommandException e) {
            log.warn(e.getMessage());
        } catch (Exception e) {
            log.error("Exception encountered during command invocation", e);
        }
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }
}
