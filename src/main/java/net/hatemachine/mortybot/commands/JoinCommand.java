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
import net.hatemachine.mortybot.exception.CommandException;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.MortyBot;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;

/**
 * Implements the JOIN command, allowing users to tell the bot to join a channel.
 */
@BotCommand(name = "JOIN", restricted = true, help = {
        "Makes the bot join a channel",
        "Usage: JOIN <channel> [key]"
})
public class JoinCommand implements Command {

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public JoinCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        if (args.isEmpty()) {
            throw new CommandException(CommandException.Reason.INVALID_ARGS, "Not enough arguments");
        }

        MortyBot bot = event.getBot();

        if (args.size() == 1) {
            bot.sendIRC().joinChannel(args.get(0));
        } else if (args.size() > 1) {
            // attempt to join with a key
            bot.sendIRC().joinChannel(args.get(0), args.get(1));
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
