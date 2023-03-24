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

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.Command;
import net.hatemachine.mortybot.CommandWrapper;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

/**
 * Implements the HELP command, allowing users to see available commands and retrieve their help text.
 */
@BotCommand(name = "HELP", help = {
        "Shows help and usage information for bot commands",
        "Usage: HELP [command]"
})
public class HelpCommand implements Command {

    private static final Logger log = LoggerFactory.getLogger(HelpCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public HelpCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        if (args.isEmpty()) {
            respondWithAllCommands();
        } else {
            respondWithCommandHelp();
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

    /**
     * Responds with a list of all commands available to end users.
     */
    private void respondWithAllCommands() {
        var commandMap = CommandListener.getCommandMap();
        var regularCommands = commandMap.values()
                .stream()
                .filter(c -> !c.isRestricted())
                .map(CommandWrapper::getName)
                .toList();
        var adminCommands = commandMap.values()
                .stream()
                .filter(CommandWrapper::isRestricted)
                .map(CommandWrapper::getName)
                .toList();

        if (regularCommands.size() > 0) {
            event.respondWith("Commands: " + String.join(", ", regularCommands));
        }

        if (adminCommands.size() > 0) {
            event.respondWith("Admin commands: " + String.join(", ", adminCommands));
        }

        event.respondWith(String.format("Type %sHELP <command> to get more information about a command",
                BotProperties.getBotProperties().getStringProperty("bot.command.prefix")));
    }

    /**
     * Responds with the help text for a given command.
     */
    private void respondWithCommandHelp() {
        String cmdName = args.get(0).toUpperCase(Locale.ROOT);
        CommandWrapper cmdWrapper = CommandListener.getCommand(cmdName);

        if (cmdWrapper != null) {
            for (String line : cmdWrapper.getHelp()) {
                event.respondWith(line);
            }
        } else {
            event.respondWith("Unknown command");
        }
    }
}
