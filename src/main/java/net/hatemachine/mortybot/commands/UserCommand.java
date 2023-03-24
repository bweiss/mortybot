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
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.custom.entity.BotUserFlag;
import net.hatemachine.mortybot.custom.entity.ManagedChannelUserFlag;
import net.hatemachine.mortybot.dao.BotUserDao;
import net.hatemachine.mortybot.dao.ManagedChannelDao;
import net.hatemachine.mortybot.dao.ManagedChannelUserDao;
import net.hatemachine.mortybot.exception.BotUserException;
import net.hatemachine.mortybot.exception.ManagedChannelException;
import net.hatemachine.mortybot.exception.ManagedChannelUserException;
import net.hatemachine.mortybot.helpers.BotUserHelper;
import net.hatemachine.mortybot.helpers.ManagedChannelHelper;
import net.hatemachine.mortybot.helpers.ManagedChannelUserHelper;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.model.ManagedChannel;
import net.hatemachine.mortybot.model.ManagedChannelUser;
import net.hatemachine.mortybot.util.IrcUtils;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static net.hatemachine.mortybot.config.BotDefaults.USER_ADD_MASK_TYPE;
import static net.hatemachine.mortybot.listeners.CommandListener.CommandSource.DCC;

/**
 * Implements the USER command, allowing users to view and manipulate bot users.
 */
@BotCommand(name = "USER", restricted = true, help = {
        "Manages bot users",
        "Usage: USER <subcommand> [target] [args]",
        "Usage: USER <ADDCHANFLAG|REMOVECHANFLAG> <user> <channel> <flag>",
        "Subcommands: ADD, ADDCHANFLAG, ADDFLAG, ADDHOSTMASK, LIST, LOCATION, REMOVE, REMOVECHANFLAG, REMOVEFLAG, REMOVEHOSTMASK, SHOW",
        "Available user flags: ADMIN, DCC, IGNORE",
        "Available managed channel user flags: AUTO_OP, AUTO_VOICE"
})
public class UserCommand implements Command {

    private static final int LIST_COMMAND_MAX_USERS_PER_LINE = 20;

    private static final Logger log = LoggerFactory.getLogger(UserCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;
    private final BotUserDao botUserDao;
    private final BotUserHelper botUserHelper;

    public UserCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
        this.botUserDao = new BotUserDao();
        this.botUserHelper = new BotUserHelper();
    }

    @Override
    public void execute() {
        Validate.commandArguments(args, 1);

        String subCommand = args.get(0).toUpperCase();
        List<String> newArgs = args.subList(1, args.size());

        try {
            switch (subCommand) {
                case "ADD" -> addCommand(newArgs);
                case "ADDCHANFLAG" -> addChannelFlagCommand(newArgs);
                case "ADDFLAG" -> addFlagCommand(newArgs);
                case "ADDHOSTMASK" -> addHostmaskCommand(newArgs);
                case "LIST" -> listCommand();
                case "LOCATION" -> locationCommand(newArgs);
                case "REMOVE" -> removeCommand(newArgs);
                case "REMOVECHANFLAG" -> removeChannelFlagCommand(newArgs);
                case "REMOVEFLAG" -> removeFlagCommand(newArgs);
                case "REMOVEHOSTMASK" -> removeHostmaskCommand(newArgs);
                case "SHOW" -> showCommand(newArgs);
                default -> {
                    log.info("Unknown USER subcommand {} from {}", subCommand, event.getUser().getHostmask());
                    event.respondWith("Invalid subcommand " + subCommand);
                }
            }
        } catch (IllegalArgumentException | BotUserException | ManagedChannelException | ManagedChannelUserException ex) {
            event.respondWith(ex.getMessage());
        } catch (RuntimeException ex) {
            log.error("Exception encountered: {}", ex.getMessage(), ex);
            event.respondWith("Something went wrong");
        }
    }

    /**
     * Adds a user to the bot.
     *
     * @param args the remaining arguments to the subcommand
     * @throws IllegalArgumentException if any arguments are missing or invalid
     */
    private void addCommand(List<String> args) throws IllegalArgumentException {
        Validate.commandArguments(args, 1);

        String userName = Validate.botUserName(args.get(0));
        String hostmask = "";
        List<BotUserFlag> flags = new ArrayList<>();

        // if no hostmask provided, see if there is a known user with that nick and attempt to pull their hostmask
        if (args.size() == 1) {
            User user = event.getBot().getUserChannelDao().getUser(userName);

            if (user != null) {
                try {
                    // this fails if the bot doesn't yet know the user's full hostmask
                    hostmask = IrcUtils.maskAddress(user.getHostmask(),
                            BotProperties.getBotProperties().getIntProperty("user.add.mask.type", USER_ADD_MASK_TYPE));
                } catch (IllegalArgumentException ex) {
                    log.warn("Failed to retrieve and mask user's address (hostmask: {}, userName: {}", user.getHostmask(), userName);
                    event.respondWith(String.format("Could not determine hostmask for %s. Try specifying manually.", userName));
                    return;
                }
            }
        } else {
            hostmask = Validate.hostmask(args.get(1));
        }

        if (args.size() > 2) {
            flags = botUserHelper.parseFlags(args.get(2));
        } else {
            flags = botUserHelper.parseFlags(BotProperties.getBotProperties()
                    .getStringProperty("bot.user.default.flags", BotDefaults.BOT_USER_DEFAULT_FLAGS));
        }

        Optional<BotUser> optionalBotUser = botUserDao.getWithName(userName);
        BotUserHelper botUserHelper = new BotUserHelper();
        List<BotUser> matchingUsers = botUserHelper.findByHostmask(hostmask);

        if (optionalBotUser.isPresent()) {
            event.respondWith("Another user already has that name");
        } else if (!matchingUsers.isEmpty()) {
            event.respondWith("Another user already matches that hostmask");
        } else {
            BotUser botUser = new BotUser();
            botUser.setName(userName);
            botUser.setBotUserHostmasks(List.of(hostmask));
            botUser.setBotUserFlags(flags);
            botUser = botUserDao.create(botUser);

            event.respondWith(String.format("User added with hostmask %s and flags %s",
                    botUser.getBotUserHostmasks().get(0), formatFlags(botUser.getBotUserFlags())));
        }
    }

    /**
     * Adds managed channel user flags to a bot user on the specified channel. If the channel is not yet managed
     * then an entry will be created.
     *
     * @param args the remaining arguments to the subcommand
     * @throws IllegalArgumentException if any arguments are missing or invalid
     * @throws BotUserException if a bot user with the provided name cannot be found in the database
     * @throws ManagedChannelException if there is a failure to find or create a managed channel entry
     * @throws ManagedChannelUserException if there is a failure to find or create a managed channel user entry for the channel and user
     */
    private void addChannelFlagCommand(List<String> args) throws IllegalArgumentException, BotUserException, ManagedChannelException, ManagedChannelUserException {
        Validate.commandArguments(args, 3);

        ManagedChannelDao mcDao = new ManagedChannelDao();
        ManagedChannelUserDao mcuDao = new ManagedChannelUserDao();

        String userName = Validate.botUserName(args.get(0));
        String channelName = args.get(1);
        String flagStr = args.get(2);
        BotUser botUser = botUserDao.getWithName(userName)
                .orElseThrow(() -> new BotUserException(BotUserException.Reason.UNKNOWN_USER, userName));

        ManagedChannel managedChannel = mcDao.getWithName(channelName)
                .or(() -> Optional.of(ManagedChannelHelper.createManagedChannel(channelName)))
                .orElseThrow(() -> new ManagedChannelException(ManagedChannelException.Reason.UNKNOWN_CHANNEL, channelName));

        ManagedChannelUser managedChannelUser = mcuDao.getWithManagedChannelIdAndBotUserId(managedChannel.getId(), botUser.getId()).or(() -> {
            var mcu = new ManagedChannelUser();
            mcu.setManagedChannelId(managedChannel.getId());
            mcu.setBotUserId(botUser.getId());
            return Optional.of(mcuDao.create(mcu));
        }).orElseThrow(() -> new ManagedChannelUserException(ManagedChannelUserException.Reason.NO_SUCH_RECORD, managedChannel.getName() + " " + botUser.getName()));

        List<ManagedChannelUserFlag> flags = managedChannelUser.getManagedChannelUserFlags();
        var mcuHelper = new ManagedChannelUserHelper();
        List<ManagedChannelUserFlag> newFlags = mcuHelper.parseFlags(flagStr);

        if (flags == null) {
            flags = newFlags;
        } else {
            for (ManagedChannelUserFlag flag : newFlags) {
                if (!flags.contains(flag)) {
                    flags.add(flag);
                }
            }
        }

        managedChannelUser.setManagedChannelUserFlags(flags);
        managedChannelUser = mcuDao.update(managedChannelUser);

        event.respondWith(String.format("Flags for %s on %s: %s",
                botUser.getName(), managedChannel.getName(), formatChanFlags(managedChannelUser.getManagedChannelUserFlags())));
    }

    /**
     * Adds flags to a bot user.
     *
     * @param args the remaining arguments to the subcommand
     * @throws BotUserException if no user with the provided name exists
     */
    private void addFlagCommand(List<String> args) throws BotUserException {
        Validate.commandArguments(args, 2);

        String userName = Validate.botUserName(args.get(0));
        BotUser botUser = botUserDao.getWithName(userName)
                .orElseThrow(() -> new BotUserException(BotUserException.Reason.UNKNOWN_USER, userName));
        List<BotUserFlag> flags = botUser.getBotUserFlags() == null ? new ArrayList<>() : botUser.getBotUserFlags();
        List<BotUserFlag> newFlags = botUserHelper.parseFlags(args.get(1));

        for (BotUserFlag flag : newFlags) {
            if (!flags.contains(flag)) {
                flags.add(flag);
            }
        }

        botUser.setBotUserFlags(flags);
        botUser = botUserDao.update(botUser);

        event.respondWith(String.format("Flags for %s: %s", botUser.getName(), formatFlags(botUser.getBotUserFlags())));
    }

    /**
     * Adds a hostmask to a bot user.
     *
     * @param args the remaining arguments to the subcommand
     * @throws IllegalArgumentException if any arguments are missing or invalid
     * @throws BotUserException if a user with the provided name cannot be found
     */
    private void addHostmaskCommand(List<String> args) throws IllegalArgumentException, BotUserException {
        Validate.commandArguments(args, 2);

        String userName = Validate.botUserName(args.get(0));
        String hostmask = Validate.hostmask(args.get(1));
        List<BotUser> matchingUsers = botUserHelper.findByHostmask(hostmask);

        if (!matchingUsers.isEmpty()) {
            event.respondWith("There is already a user with a matching hostmask");
        } else {
            BotUser botUser = botUserDao.getWithName(userName)
                    .orElseThrow(() -> new BotUserException(BotUserException.Reason.UNKNOWN_USER, userName));
            List<String> hostmasks = botUser.getBotUserHostmasks() == null ? new ArrayList<>() : botUser.getBotUserHostmasks();
            hostmasks.add(hostmask);
            botUser.setBotUserHostmasks(hostmasks);
            botUser = botUserDao.update(botUser);
            event.respondWith(String.format("Hostmasks for %s: %s", botUser.getName(), botUser.getBotUserHostmasks()));
        }
    }

    /**
     * Lists all bot users. If the source is anything but DCC then it will show the users on multiple
     * lines according to LIST_COMMAND_MAX_USERS_PER_LINE.
     */
    private void listCommand() {
        List<BotUser> botUsers = botUserDao.getAll();

        if (!botUsers.isEmpty()) {
            List<List<BotUser>> groups = new ArrayList<>();

            if (source == DCC) {
                groups.add(botUsers);
            } else {
                Deque<BotUser> userDeque = new ArrayDeque<>(botUsers);
                
                while (!userDeque.isEmpty()) {
                    List<BotUser> group = new ArrayList<>();
                    for (int i = 0; !userDeque.isEmpty() && i < LIST_COMMAND_MAX_USERS_PER_LINE; i++) {
                        group.add(userDeque.pop());
                    }
                    groups.add(group);
                }
            }

            for (int i = 0; i < groups.size(); i++) {
                List<BotUser> group = groups.get(i);
                event.respondWith(String.format("Bot Users (%d/%d): %s",
                        i + 1, groups.size(), group.stream().map(BotUser::getName).collect(joining(", "))));
            }
            event.respondWith("USER SHOW <username> to see details");
        } else {
            event.respondWith("There are no bot users!");
        }
    }

    /**
     * Sets the location for a bot user (used by the WEATHER command).
     *
     * @param args the remaining arguments to the subcommand
     * @throws IllegalArgumentException if any arguments are missing or invalid
     * @throws BotUserException if a user with the provided name cannot be found
     * @see WeatherCommand
     */
    private void locationCommand(List<String> args) throws IllegalArgumentException, BotUserException {
        Validate.commandArguments(args, 2);

        String userName = Validate.botUserName(args.get(0));
        String location = String.join(" ", args.subList(1, args.size()));
        BotUser botUser = botUserDao.getWithName(userName)
                .orElseThrow(() -> new BotUserException(BotUserException.Reason.UNKNOWN_USER, userName));

        botUser.setLocation(location);
        botUser = botUserDao.update(botUser);

        event.respondWith(String.format("Set %s's location to %s", botUser.getName(), botUser.getLocation()));
    }

    /**
     * Removes a user from the bot.
     *
     * @param args the remaining arguments to the subcommand
     * @throws IllegalArgumentException if any arguments are missing or invalid
     * @throws BotUserException if a user with the provided name cannot be found
     */
    private void removeCommand(List<String> args) throws IllegalArgumentException, BotUserException {
        Validate.commandArguments(args, 1);

        String userName = Validate.botUserName(args.get(0));
        BotUser botUser = botUserDao.getWithName(userName)
                .orElseThrow(() -> new BotUserException(BotUserException.Reason.UNKNOWN_USER, userName));

        botUserDao.delete(botUser.getId());

        event.respondWith("User removed");
    }

    /**
     * Removes managed channel user flags for a bot user on a managed channel.
     *
     * @param args the remaining arguments to the subcommand
     * @throws IllegalArgumentException if any arguments are missing or invalid
     * @throws BotUserException if a user with the provided name cannot be found
     * @throws ManagedChannelException if a channel with the provided name does not have a managed channel entry
     * @throws ManagedChannelUserException if a managed channel user entry cannot be created
     */
    private void removeChannelFlagCommand(List<String> args) throws IllegalArgumentException, BotUserException, ManagedChannelException, ManagedChannelUserException {
        Validate.commandArguments(args, 3);

        var mcDao = new ManagedChannelDao();
        var mcuDao = new ManagedChannelUserDao();
        var mcuHelper = new ManagedChannelUserHelper();

        MortyBot bot = event.getBot();

        String userName = Validate.botUserName(args.get(0));
        String channelName = Validate.channelName(args.get(1), bot.getServerInfo().getChannelTypes());
        String flagStr = args.get(2);

        BotUser botUser = botUserDao.getWithName(userName)
                .orElseThrow(() -> new BotUserException(BotUserException.Reason.UNKNOWN_USER, userName));
        ManagedChannel managedChannel = mcDao.getWithName(channelName)
                .orElseThrow(() -> new ManagedChannelException(ManagedChannelException.Reason.UNKNOWN_CHANNEL, channelName));
        ManagedChannelUser managedChannelUser = mcuDao.getWithManagedChannelIdAndBotUserId(managedChannel.getId(), botUser.getId())
                .orElseThrow(() -> new ManagedChannelUserException(ManagedChannelUserException.Reason.NO_SUCH_RECORD, managedChannel.getName() + " " + botUser.getName()));

        List<ManagedChannelUserFlag> flagList = managedChannelUser.getManagedChannelUserFlags() == null ? new ArrayList<>() : managedChannelUser.getManagedChannelUserFlags();
        flagList.removeAll(mcuHelper.parseFlags(flagStr));
        managedChannelUser.setManagedChannelUserFlags(flagList);
        managedChannelUser = mcuDao.update(managedChannelUser);

        event.respondWith(String.format("Flags for %s on %s: %s",
                botUser.getName(), managedChannel.getName(), formatChanFlags(managedChannelUser.getManagedChannelUserFlags())));
    }

    /**
     * Removes flags from a bot user.
     *
     * @param args the remaining arguments to the subcommand
     * @throws IllegalArgumentException if any arguments are missing or invalid
     * @throws BotUserException if a user with the provided name cannot be found
     */
    private void removeFlagCommand(List<String> args) throws IllegalArgumentException, BotUserException {
        Validate.commandArguments(args, 2);

        String userName = Validate.botUserName(args.get(0));
        String flagStr = args.get(1);
        BotUser botUser = botUserDao.getWithName(userName)
                .orElseThrow(() -> new BotUserException(BotUserException.Reason.UNKNOWN_USER, userName));
        List<BotUserFlag> flags = botUser.getBotUserFlags() == null ? new ArrayList<>() : botUser.getBotUserFlags();

        flags.removeAll(botUserHelper.parseFlags(flagStr));
        botUser.setBotUserFlags(flags);
        botUser = botUserDao.update(botUser);

        event.respondWith(String.format("Flags for %s: %s", botUser.getName(), formatFlags(botUser.getBotUserFlags())));
    }

    /**
     * Removes a hostmask from a bot user.
     *
     * @param args remaining arguments to the subcommand
     * @throws IllegalArgumentException if any arguments are missing or invalid
     * @throws BotUserException if a user with the provided name cannot be found
     */
    private void removeHostmaskCommand(List<String> args) throws IllegalArgumentException, BotUserException {
        Validate.commandArguments(args, 2);

        String userName = Validate.botUserName(args.get(0));
        String hostmask = args.get(1);
        BotUser botUser = botUserDao.getWithName(userName)
                .orElseThrow(() -> new BotUserException(BotUserException.Reason.UNKNOWN_USER, userName));
        List<String> hostmasks = botUser.getBotUserHostmasks() == null ? new ArrayList<>() : botUser.getBotUserHostmasks();

        hostmasks.remove(hostmask);
        botUser.setBotUserHostmasks(hostmasks);
        botUser = botUserDao.update(botUser);

        event.respondWith(String.format("Hostmasks for %s: %s", botUser.getName(), botUser.getBotUserHostmasks()));
    }

    /**
     * Shows the details of a bot user.
     *
     * @param args the remaining arguments to the subcommand
     * @throws IllegalArgumentException if any arguments are missing or invalid
     * @throws BotUserException if a user with the provided name cannot be found
     */
    private void showCommand(List<String> args) throws IllegalArgumentException, BotUserException {
        Validate.commandArguments(args, 1);

        String userName = Validate.botUserName(args.get(0));
        BotUser botUser = botUserDao.getWithName(userName)
                .orElseThrow(() -> new BotUserException(BotUserException.Reason.UNKNOWN_USER, userName));
        var channelFlagMap = botUserHelper.getChannelFlagMap(botUser);

        event.respondWith(String.format("%s -> hostmasks[%s] flags[%s] location[%s]",
                botUser.getName(),
                botUser.getBotUserHostmasks() == null ? "" : String.join(", ", botUser.getBotUserHostmasks()),
                formatFlags(botUser.getBotUserFlags()),
                botUser.getLocation() == null ? "" : botUser.getLocation()));

        channelFlagMap.forEach((k, v) -> event.respondWith(String.format("%s -> %s", k.getName(), v.toString())));
    }

    /**
     * Formats a list of bot user flags into a string.
     *
     * @param flagList the list of bot user flags
     * @return a string of bot user flags joined by commas, or an empty string if the list was null
     */
    private String formatFlags(List<BotUserFlag> flagList) {
        return flagList == null ? "" : flagList.stream().map(BotUserFlag::name).collect(Collectors.joining(", "));
    }

    /**
     * Formats a list of managed channel user flags into a string.
     *
     * @param flagList the list of managed channel user flags
     * @return a string of managed channel user flags joined by commas, or an empty string if the list was null
     */
    private String formatChanFlags(List<ManagedChannelUserFlag> flagList) {
        return flagList == null ? "" : flagList.stream().map(ManagedChannelUserFlag::name).collect(Collectors.joining(", "));
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
