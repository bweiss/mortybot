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
import net.hatemachine.mortybot.custom.entity.ManagedChannelFlag;
import net.hatemachine.mortybot.dao.ManagedChannelDao;
import net.hatemachine.mortybot.dao.ManagedChannelUserDao;
import net.hatemachine.mortybot.exception.ManagedChannelException;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.model.ManagedChannel;
import net.hatemachine.mortybot.model.ManagedChannelUser;
import net.hatemachine.mortybot.helpers.ManagedChannelHelper;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.Channel;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CHANNEL command that allows you to view and manipulate the bot's managed channels.
 */
@BotCommand(name="CHANNEL", clazz=ChannelCommand.class, help={
        "Manages the bot's channels",
        "Usage: CHANNEL <subcommand> [args]",
        "Subcommands: ADD, ADDBAN, ADDFLAG, LIST, MODES, REMOVE, REMOVEBAN, REMOVEFLAG, SHOW, SHOWBANS"
})
public class ChannelCommand implements Command {

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
        Validate.arguments(args, 1);

        String command = args.get(0).toUpperCase();
        List<String> newArgs = args.subList(1, args.size());

        try {
            switch (command) {
                case "ADD" -> addCommand(newArgs);
                case "ADDBAN" -> addBanCommand(newArgs);
                case "ADDFLAG" -> addFlagCommand(newArgs);
                case "LIST" -> listCommand();
                case "MODES" -> modesCommand(newArgs);
                case "REMOVE" -> removeCommand(newArgs);
                case "REMOVEBAN" -> removeBanCommand(newArgs);
                case "REMOVEFLAG" -> removeFlagCommand(newArgs);
                case "SHOW" -> showCommand(newArgs);
                case "SHOWBANS" -> showBansCommand(newArgs);
                default -> log.info("Unknown CHANNEL subcommand {} from {}", command, event.getUser().getNick());
            }
        } catch (IllegalArgumentException | ManagedChannelException ex) {
            event.respondWith(ex.getMessage());
        } catch (RuntimeException ex) {
            log.error("Exception encountered: {}", ex.getMessage(), ex);
            event.respondWith("Something went wrong");
        }
    }

    /**
     * Adds a new channel to be managed.
     *
     * @param args the remaining arguments to the subcommand
     * @throws IllegalArgumentException if any arguments are missing or invalid
     */
    private void addCommand(final List<String> args) {
        Validate.arguments(args, 1);

        String channelName = Validate.channelName(args.get(0), event.getBot().getServerInfo().getChannelTypes());
        Optional<ManagedChannel> optionalManagedChannel = managedChannelDao.getWithName(channelName);

        if (optionalManagedChannel.isPresent()) {
            event.respondWith("Channel is already managed");
        } else {
            ManagedChannel managedChannel = ManagedChannelHelper.createManagedChannel(channelName);
            event.respondWith(String.format("Added channel %s with flags %s",
                    managedChannel.getName(), formatFlags(managedChannel.getManagedChannelFlags())));
        }
    }

    /**
     * Adds a new ban to a managed channel.
     *
     * @param args the remaining arguments to the subcommand
     * @throws IllegalArgumentException if any arguments are missing or invalid
     * @throws ManagedChannelException if a managed channel with the given name cannot be found
     */
    private void addBanCommand(final List<String> args) throws IllegalArgumentException, ManagedChannelException {
        Validate.arguments(args, 2);

        String channelName = args.get(0);
        String targetHostmask = Validate.hostmask(args.get(1));
        ManagedChannel managedChannel = managedChannelDao.getWithName(channelName)
                .orElseThrow(() -> new ManagedChannelException(ManagedChannelException.Reason.UNKNOWN_CHANNEL, channelName));
        List<String> bans = managedChannel.getBans() == null ? new ArrayList<>() : managedChannel.getBans();

        if (bans.contains(targetHostmask)) {
            event.respondWith("Ban already exists for this channel");
        } else {
            bans.add(targetHostmask);
            managedChannel.setBans(bans);
            managedChannelDao.update(managedChannel);

            if (managedChannel.getManagedChannelFlags().contains(ManagedChannelFlag.ENFORCE_BANS)) {
                Channel channel = event.getBot().getUserChannelDao().getChannel(channelName);
                channel.send().ban(targetHostmask);
            }

            event.respondWith("Ban added");
        }
    }

    /**
     * Adds flags to a managed channel.
     *
     * @param args the remaining arguments to the subcommand
     * @throws IllegalArgumentException if any arguments are missing or invalid
     * @throws ManagedChannelException if managed channel entry cannot be created
     */
    private void addFlagCommand(final List<String> args) throws IllegalArgumentException, ManagedChannelException {
        Validate.arguments(args, 2);

        String channelName = Validate.channelName(args.get(0), event.getBot().getServerInfo().getChannelTypes());
        List<ManagedChannelFlag> newFlags = ManagedChannelHelper.parseFlags(args.get(1));

        ManagedChannel managedChannel = managedChannelDao.getWithName(channelName)
                .or(() -> Optional.of(ManagedChannelHelper.createManagedChannel(channelName)))
                .orElseThrow(() -> new ManagedChannelException(ManagedChannelException.Reason.UNKNOWN_CHANNEL, channelName));

        List<ManagedChannelFlag> flags = managedChannel.getManagedChannelFlags() == null ? new ArrayList<>() : managedChannel.getManagedChannelFlags();

        for (ManagedChannelFlag flag : newFlags) {
            if (!flags.contains(flag)) {
                flags.add(flag);
            }
        }

        managedChannel.setManagedChannelFlags(flags);
        managedChannel = managedChannelDao.update(managedChannel);

        event.respondWith(String.format("Flags for %s: %s",
                managedChannel.getName(), formatFlags(managedChannel.getManagedChannelFlags())));
    }

    /**
     * Lists all managed channels.
     */
    private void listCommand() {
        List<ManagedChannel> channels = managedChannelDao.getAll();

        if (channels == null || channels.isEmpty()) {
            event.respondWith("There are no managed channels");
        } else {
            event.respondWith("Managed channels: " +
                    channels.stream().map(ManagedChannel::getName).collect(Collectors.joining(", ")));
        }
    }

    /**
     * Shows or sets the modes for a managed channel.
     *
     * @param args the remaining arguments to the subcommand
     * @throws IllegalArgumentException if any arguments are missing or invalid
     * @throws ManagedChannelException if managed channel entry cannot be created
     */
    private void modesCommand(final List<String> args) throws IllegalArgumentException, ManagedChannelException {
        Validate.arguments(args, 1);

        String channelName = args.get(0);
        Optional<String> newModes = Optional.empty();
        ManagedChannel managedChannel = managedChannelDao.getWithName(channelName)
                .orElseThrow(() -> new ManagedChannelException(ManagedChannelException.Reason.UNKNOWN_CHANNEL, channelName));

        if (args.size() > 1) {
            newModes = Optional.of(args.get(1));
        }

        if (newModes.isPresent()) {
            managedChannel.setModes(newModes.get());
            managedChannel = managedChannelDao.update(managedChannel);
        }

        event.respondWith(String.format("Modes for %s: %s",
                managedChannel.getName(),
                managedChannel.getModes() == null ? "" : managedChannel.getModes()));
    }
    
    /**
     * Removes a channel from management.
     *
     * @param args the remaining arguments to the subcommand
     * @throws IllegalArgumentException if any arguments are missing or invalid
     * @throws ManagedChannelException if no managed channel entry can be found for this channel
     */
    private void removeCommand(final List<String> args) throws IllegalArgumentException, ManagedChannelException {
        Validate.arguments(args, 1);

        String channelName = Validate.channelName(args.get(0), event.getBot().getServerInfo().getChannelTypes());
        ManagedChannel managedChannel = managedChannelDao.getWithName(channelName)
                .orElseThrow(() -> new ManagedChannelException(ManagedChannelException.Reason.UNKNOWN_CHANNEL, channelName));

        managedChannelDao.delete(managedChannel.getId());
        
        event.respondWith("Channel removed");
    }

    /**
     * Removes a ban from a managed channel.
     *
     * @param args the remaining arguments to the subcommand
     * @throws IllegalArgumentException if any arguments are missing or invalid
     * @throws ManagedChannelException if a managed channel with the given name cannot be found
     */
    private void removeBanCommand(final List<String> args) throws IllegalArgumentException, ManagedChannelException {
        Validate.arguments(args, 2);

        String channelName = args.get(0);
        String targetHostmask = args.get(1);
        ManagedChannel managedChannel = managedChannelDao.getWithName(channelName)
                .orElseThrow(() -> new ManagedChannelException(ManagedChannelException.Reason.UNKNOWN_CHANNEL, channelName));
        List<String> bans = managedChannel.getBans() == null ? new ArrayList<>() : managedChannel.getBans();

        if (!bans.contains(targetHostmask)) {
            event.respondWith("No such ban exists for this channel");
        } else {
            bans.remove(targetHostmask);
            managedChannel.setBans(bans);
            managedChannelDao.update(managedChannel);

            if (managedChannel.getManagedChannelFlags().contains(ManagedChannelFlag.ENFORCE_BANS)) {
                Channel channel = event.getBot().getUserChannelDao().getChannel(channelName);
                channel.send().unBan(targetHostmask);
            }

            event.respondWith("Ban removed");
        }
    }

    /**
     * Removes flags from a managed channel.
     *
     * @param args the remaining arguments to the subcommand
     * @throws IllegalArgumentException if any arguments are missing or invalid
     * @throws ManagedChannelException if no managed channel entry can be found for this channel
     */
    private void removeFlagCommand(final List<String> args) throws IllegalArgumentException, ManagedChannelException {
        Validate.arguments(args, 2);

        String channelName = Validate.channelName(args.get(0), event.getBot().getServerInfo().getChannelTypes());
        List<ManagedChannelFlag> flagsToRemove = ManagedChannelHelper.parseFlags(args.get(1));
        ManagedChannel managedChannel = managedChannelDao.getWithName(channelName)
                .orElseThrow(() -> new ManagedChannelException(ManagedChannelException.Reason.UNKNOWN_CHANNEL, channelName));
        List<ManagedChannelFlag> flags = managedChannel.getManagedChannelFlags() == null ? new ArrayList<>() : managedChannel.getManagedChannelFlags();

        for (ManagedChannelFlag flag : flagsToRemove) {
            flags.remove(flag);
        }

        managedChannel.setManagedChannelFlags(flags);
        managedChannel = managedChannelDao.update(managedChannel);

        event.respondWith(String.format("Flags for %s: %s",
                managedChannel.getName(), formatFlags(managedChannel.getManagedChannelFlags())));
    }

    /**
     * Shows the details of a managed channel.
     *
     * @param args the remaining arguments to the subcommand
     * @throws IllegalArgumentException if any arguments are missing or invalid
     * @throws ManagedChannelException if no managed channel entry can be found for this channel
     */
    private void showCommand(final List<String> args) throws IllegalArgumentException, ManagedChannelException {
        Validate.arguments(args, 1);

        String channelName = Validate.channelName(args.get(0), event.getBot().getServerInfo().getChannelTypes());
        ManagedChannel managedChannel = managedChannelDao.getWithName(channelName)
                .orElseThrow(() -> new ManagedChannelException(ManagedChannelException.Reason.UNKNOWN_CHANNEL, channelName));
        List<ManagedChannelFlag> flagList = managedChannel.getManagedChannelFlags() == null ? new ArrayList<>() : managedChannel.getManagedChannelFlags();
        ManagedChannelUserDao mcuDao = new ManagedChannelUserDao();
        List<ManagedChannelUser> mcuList = mcuDao.getMultipleWithManagedChannelId(managedChannel.getId());

        event.respondWith(String.format("%s -> flags[%s] modes[%s]",
                managedChannel.getName(),
                formatFlags(flagList),
                managedChannel.getModes() == null ? "" : managedChannel.getModes()));

        if (mcuList != null && !mcuList.isEmpty()) {
            List<BotUser> botUsers = mcuList.stream().map(ManagedChannelUser::getBotUser).toList();

            event.respondWith(String.format("Bot users registered on %s: %s",
                    managedChannel.getName(),
                    botUsers.stream().map(BotUser::getName).collect(Collectors.joining(", "))));
            event.respondWith("USER SHOW <name> to see their channel specific flags");
        }
    }

    /**
     * Shows all the bans for a managed channel.
     *
     * @param args the remaining arguments to the subcommand
     * @throws IllegalArgumentException if any arguments are missing or invalid
     * @throws ManagedChannelException if no managed channel entry can be found for this channel
     */
    private void showBansCommand(final List<String> args) throws IllegalArgumentException, ManagedChannelException {
        Validate.arguments(args, 1);

        String channelName = args.get(0);
        ManagedChannel managedChannel = managedChannelDao.getWithName(channelName)
                .orElseThrow(() -> new ManagedChannelException(ManagedChannelException.Reason.UNKNOWN_CHANNEL, channelName));
        List<String> banList = managedChannel.getBans();

        if (banList == null) {
            event.respondWith("There are no bans set for " + managedChannel.getName());
        } else {
            event.respondWith("Bans for " + managedChannel.getName() + ":");
            for (String hostmask : banList) {
                event.respondWith(hostmask);
            }
        }
    }

    /**
     * Formats a list of managed channel flags into a string.
     *
     * @param flagList the list of managed channel flags
     * @return a string of managed channel flags joined by commas, or an empty string if the list was null
     */
    private String formatFlags(List<ManagedChannelFlag> flagList) {
        return flagList == null ? "" : flagList.stream().map(ManagedChannelFlag::name).collect(Collectors.joining(", "));
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
