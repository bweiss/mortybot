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
import net.hatemachine.mortybot.exception.CommandException;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.model.BotChannel;
import net.hatemachine.mortybot.repositories.BotChannelRepository;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implements the CHANNEL command, allowing admin users to view and manipulate the bot's channels.
 */
@BotCommand(name = "CHANNEL", restricted = true, help = {
        "Manages the bot's channels",
        "Usage: CHANNEL ADD <name> <hostmask>",
        "Usage: CHANNEL LIST",
        "Usage: CHANNEL RM <name> [...]",
        "Usage: CHANNEL SET <name> <attribute> [new_val]",
        "Usage: CHANNEL SHOW <name> [...]"
})
public class ChannelCommand implements Command {

    private static final Logger log = LoggerFactory.getLogger(ChannelCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;
    private final BotChannelRepository botChannelRepository;

    public ChannelCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
        this.botChannelRepository = new BotChannelRepository();
    }

    @Override
    public void execute() throws CommandException {
        Validate.arguments(args, 1);

        String subCommand = args.get(0).toUpperCase();
        List<String> newArgs = args.subList(1, args.size());

        log.debug("subCommand: {}, args: {}", subCommand, newArgs);

        switch (subCommand) {
            case "ADD" -> addCommand(newArgs);
            case "LIST" -> listCommand(newArgs);
            case "RM" -> rmCommand(newArgs);
            case "SET" -> setCommand(newArgs);
            case "SHOW" -> showCommand(newArgs);
            default -> event.respondWith("Invalid subcommand: " + subCommand);
        }
    }

    private void addCommand(List<String> args) {
        Validate.arguments(args, 1);

        String channelName = args.get(0);

        var botChannel = new BotChannel(channelName);
        botChannelRepository.save(botChannel);

        event.respondWith("Added channel " + channelName);
    }

    private void listCommand(List<String> args) {
        event.respondWith("Bot channels: " + botChannelRepository.findAll().stream()
                .map(BotChannel::getName)
                .collect(Collectors.joining(", ")));
    }

    private void rmCommand(List<String> args) {
        List<BotChannel> botChannels = botChannelRepository.findAllByName(args);

        botChannelRepository.deleteAllById(botChannels.stream()
                .map(BotChannel::getId)
                .toList());

        event.respondWith("Removed channels: " + botChannels.stream()
                .map(BotChannel::getName)
                .collect(Collectors.joining(", ")));
    }

    private void setCommand(List<String> args) {
        Validate.arguments(args, 2);

        Optional<BotChannel> optionalBotChannel = botChannelRepository.findByName(args.get(0));
        String attr = args.get(1);
        String newVal = args.size() > 2 ? args.get(2) : null;

        if (optionalBotChannel.isPresent()) {
            BotChannel botChannel = optionalBotChannel.get();

            switch (attr.toUpperCase()) {
                case "AUTOJOIN":
                    if (newVal == null) {
                        botChannel.setAutoJoinFlag(!botChannel.hasAutoJoinFlag());
                    } else {
                        botChannel.setAutoJoinFlag(newVal.equalsIgnoreCase("true") || newVal.equalsIgnoreCase("1"));
                    }
                    event.respondWith("autoJoinFlag set to " + botChannel.hasAutoJoinFlag());
                    break;

                case "SHORTEN":
                    if (newVal == null) {
                        botChannel.setShortenLinksFlag(!botChannel.hasShortenLinksFlag());
                    } else {
                        botChannel.setShortenLinksFlag(newVal.equalsIgnoreCase("true") || newVal.equalsIgnoreCase("1"));
                    }
                    event.respondWith("shortenLinksFlag set to " + botChannel.hasShortenLinksFlag());
                    break;

                case "SHOWTITLES":
                    if (newVal == null) {
                        botChannel.setShowLinkTitlesFlag(!botChannel.hasShowLinkTitlesFlag());
                    } else {
                        botChannel.setShowLinkTitlesFlag(newVal.equalsIgnoreCase("true") || newVal.equalsIgnoreCase("1"));
                    }
                    event.respondWith("showLinkTitlesFlag set to " + botChannel.hasShowLinkTitlesFlag());
                    break;

                default:
                    event.respondWith("Unknown attribute: " + attr);
            }

            botChannelRepository.save(botChannel);
        }
    }

    private void showCommand(List<String> args) {
        var botChannels = botChannelRepository.findAllByName(args);
        botChannels.forEach(bc -> event.respondWith(bc.toString()));
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
