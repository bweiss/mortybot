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
import net.hatemachine.mortybot.custom.entity.ManagedChannelFlag;
import net.hatemachine.mortybot.dao.ManagedChannelDao;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.model.ManagedChannel;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CHANNEL command that allows you to view and manipulate the bot's managed channels.<br/>
 * <br/>
 * Supported subcommands: ADD, REMOVE, LIST, SHOW, ADDFLAG, REMOVEFLAG
 */
public class ChannelCommand implements BotCommand {

    private static final String NOT_ENOUGH_ARGS_STR = "Not enough arguments";
    private static final String UNKNOWN_CHANNEL_STR = "Unknown channel";

    private static final Logger log = LoggerFactory.getLogger(ChannelCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;
    private final ManagedChannelDao managedChannelDao;

    public ChannelCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
        this.managedChannelDao = new ManagedChannelDao();
    }

    @Override
    public void execute() {
        if (args.isEmpty()) {
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS_STR);
        }

        String command = args.get(0).toUpperCase();
        List<String> newArgs = args.subList(1, args.size());

        try {
            switch (command) {
                case "ADD" -> addCommand(newArgs);
                case "REMOVE" -> removeCommand(newArgs);
                case "LIST" -> listCommand();
                case "SHOW" -> showCommand(newArgs);
                case "ADDFLAG" -> addFlagCommand(newArgs);
                case "REMOVEFLAG" -> removeFlagCommand(newArgs);
                default -> log.info("Unknown CHANNEL subcommand {} from {}", command, event.getUser().getNick());
            }
        } catch (Exception ex) {
            log.error("Exception encountered: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Adds a new channel to be managed.<br/>
     * Usage: <code>ADD channel_name</code>
     *
     * @param args the remaining arguments to the subcommand (index 0: channel name)
     */
    private void addCommand(final List<String> args) {
        if (args.isEmpty()) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String channelName = args.get(0);
        Optional<ManagedChannel> optionalManagedChannel = managedChannelDao.getWithName(channelName);

        if (optionalManagedChannel.isPresent()) {
            event.respondWith("Channel already added");
        } else {
            var managedChannel = new ManagedChannel();
            managedChannel.setName(channelName);
            managedChannelDao.create(managedChannel);
            event.respondWith("Channel added");
        }
    }

    /**
     * Removes a channel from management.</br>
     * Usage: <code>REMOVE channel_name</code>
     *
     * @param args the remaining arguments to the subcommand (index 0: channel name)
     */
    private void removeCommand(final List<String> args) {
        if (args.isEmpty()) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String channelName = args.get(0);
        Optional<ManagedChannel> optionalManagedChannel = managedChannelDao.getWithName(channelName);

        if (optionalManagedChannel.isEmpty()) {
            event.respondWith(UNKNOWN_CHANNEL_STR);
        } else {
            ManagedChannel managedChannel = optionalManagedChannel.get();
            managedChannelDao.delete(managedChannel.getId());
            event.respondWith("Channel removed");
        }
    }

    /**
     * Lists all managed channels.<br/>
     * Usage: <code>LIST</code>
     */
    private void listCommand() {
        List<ManagedChannel> channels = managedChannelDao.getAll();

        if (channels.isEmpty()) {
            event.respondWith("There are no managed channels");
        } else {
            event.respondWith("Managed channels: " +
                    channels.stream().map(ManagedChannel::getName).collect(Collectors.joining(", ")));
        }
    }

    /**
     * Shows the details of a managed channel.<br/>
     * Usage: <code>SHOW channel_name</code>
     *
     * @param args the remaining arguments to the subcommand (index 0: channel name)
     */
    private void showCommand(final List<String> args) {
        if (args.isEmpty()) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String channelName = args.get(0);
        Optional<ManagedChannel> optionalManagedChannel = managedChannelDao.getWithName(channelName);

        if (optionalManagedChannel.isEmpty()) {
            event.respondWith(UNKNOWN_CHANNEL_STR);
        } else {
            ManagedChannel managedChannel = optionalManagedChannel.get();
            event.respondWith("Channel: " + managedChannel.getName() + " - flags" + managedChannel.getManagedChannelFlags());
        }
    }

    /**
     * Adds flags to a managed channel.<br/>
     * Usage: <code>ADDFLAG channel_name flags</code>
     *
     * @param args the remaining arguments to the subcommand (index 0: channel name, index 1: comma-delimited list of flags)
     */
    private void addFlagCommand(final List<String> args) {
        if (args.size() < 2) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String channelName = args.get(0);
        String[] flagStr = args.get(1).split(",");
        Optional<ManagedChannel> optionalManagedChannel = managedChannelDao.getWithName(channelName);
        ManagedChannel managedChannel = null;

        // if the channel isn't managed yet, create an entry for it
        if (optionalManagedChannel.isEmpty()) {
            managedChannel = new ManagedChannel();
            managedChannel.setName(channelName);
            managedChannelDao.create(managedChannel);
            // retrieve from the database so we get the id
            optionalManagedChannel = managedChannelDao.getWithName(channelName);
            if (optionalManagedChannel.isPresent()) {
                managedChannel = optionalManagedChannel.get();
            }
        }

        if (managedChannel != null) {
            List<ManagedChannelFlag> flags = managedChannel.getManagedChannelFlags();
            if (flags == null) {
                flags = new ArrayList<>();
            }

            for (String s : flagStr) {
                try {
                    ManagedChannelFlag flag = Enum.valueOf(ManagedChannelFlag.class, s.toUpperCase());

                    if (!flags.contains(flag)) {
                        flags.add(flag);
                    }
                } catch (IllegalArgumentException ex) {
                    log.warn("Invalid flag {}", s.toUpperCase());
                }
            }

            managedChannel.setManagedChannelFlags(flags);
            managedChannelDao.update(managedChannel);

            event.respondWith("Flags: " + managedChannel.getManagedChannelFlags());
        } else {
            log.error("managedChannel is null!");
            event.respondWith("Something went wrong");
        }
    }

    /**
     * Removes flags from a managed channel.<br/>
     * Usage: <code>REMOVEFLAG channel_name flags</code>
     *
     * @param args the remaining arguments to the subcommand (index 0: channel name, index 1: comma-delimited list of flags)
     */
    private void removeFlagCommand(final List<String> args) {
        if (args.size() < 2) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String channelName = args.get(0);
        String[] flagStr = args.get(1).split(",");
        Optional<ManagedChannel> optionalManagedChannel = managedChannelDao.getWithName(channelName);

        if (optionalManagedChannel.isEmpty()) {
            event.respondWith(UNKNOWN_CHANNEL_STR);
        } else {
            ManagedChannel managedChannel = optionalManagedChannel.get();
            List<ManagedChannelFlag> flags = managedChannel.getManagedChannelFlags();
            if (flags == null) {
                flags = new ArrayList<>();
            }

            for (String s : flagStr) {
                try {
                    ManagedChannelFlag flag = Enum.valueOf(ManagedChannelFlag.class, s.toUpperCase());
                    flags.remove(flag);
                } catch (IllegalArgumentException ex) {
                    log.warn("Invalid flag {}", s.toUpperCase());
                }
            }

            managedChannel.setManagedChannelFlags(flags);
            managedChannelDao.update(managedChannel);

            event.respondWith("Flags: " + managedChannel.getManagedChannelFlags());
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
