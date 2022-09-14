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
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.custom.entity.BotUserFlag;
import net.hatemachine.mortybot.custom.entity.ManagedChannelUserFlag;
import net.hatemachine.mortybot.dao.BotUserDao;
import net.hatemachine.mortybot.dao.ManagedChannelDao;
import net.hatemachine.mortybot.dao.ManagedChannelUserDao;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.model.ManagedChannel;
import net.hatemachine.mortybot.model.ManagedChannelUser;
import net.hatemachine.mortybot.util.BotUserHelper;
import net.hatemachine.mortybot.util.IrcUtils;
import net.hatemachine.mortybot.util.ManagedChannelUserHelper;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.stream.Collectors.joining;
import static net.hatemachine.mortybot.config.BotDefaults.USER_ADD_MASK_TYPE;

/**
 * USER command that allows you to view and manipulate bot users.<br/>
 * <br/>
 * Supported subcommands: LIST, SHOW, ADD, REMOVE, ADDHOSTMASK, REMOVEHOSTMASK, ADDFLAG, REMOVEFLAG, LOCATION
 */
public class UserCommand implements BotCommand {

    private static final int LIST_COMMAND_MAX_USERS_PER_LINE = 20;
    private static final String NOT_ENOUGH_ARGS_STR = "Not enough arguments";
    private static final String UNKNOWN_USER_STR = "Unknown user";

    private static final Logger log = LoggerFactory.getLogger(UserCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;
    private final BotUserDao botUserDao;

    public UserCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
        this.botUserDao = new BotUserDao();
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
                case "ADDCHANFLAG" -> addChannelFlagCommand(newArgs);
                case "ADDFLAG" -> addFlagCommand(newArgs);
                case "ADDHOSTMASK" -> addHostmaskCommand(newArgs);
                case "LIST" -> listCommand();
                case "REMOVE" -> removeCommand(newArgs);
                case "REMOVECHANFLAG" -> removeChannelFlagCommand(newArgs);
                case "REMOVEFLAG" -> removeFlagCommand(newArgs);
                case "REMOVEHOSTMASK" -> removeHostmaskCommand(newArgs);
                case "LOCATION" -> locationCommand(newArgs);
                case "SHOW" -> showCommand(newArgs);
                default -> log.info("Unknown USER subcommand {} from {}", command, event.getUser().getNick());
            }
        } catch (Exception ex) {
            log.error("Exception encountered: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Adds a user to the bot.<br/>
     * Usage: <code>ADD username</code>
     *
     * @param args the remaining arguments to the subcommand (index 0: username, index 1: optional hostmask)
     */
    private void addCommand(List<String> args) {
        if (args.isEmpty()) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        MortyBot bot = event.getBot();
        String username = Validate.botUserName(args.get(0));
        String hostmask = "";
        List<BotUserFlag> flags = new ArrayList<>();

        // if no hostmask provided, see if there is a known user with that nick and attempt to pull their hostmask
        if (args.size() == 1) {
            User user = bot.getUserChannelDao().getUser(username);

            if (user != null) {
                try {
                    // this fails if the bot doesn't yet know the user's full hostmask
                    hostmask = IrcUtils.maskAddress(user.getHostmask(),
                            BotProperties.getBotProperties().getIntProperty("user.add.mask.type", USER_ADD_MASK_TYPE));
                } catch (IllegalArgumentException e) {
                    event.respondWith(String.format("Could not determine hostmask for %s. Try specifying manually.", username));
                    return;
                }
            }
        } else {
            hostmask = Validate.hostmask(args.get(1));
        }

        if (args.size() > 2) {
            flags = BotUserHelper.parseFlags(args.get(2));
        }

        List<BotUser> matchingUsers = BotUserHelper.findByHostmask(hostmask);

        if (!matchingUsers.isEmpty()) {
            event.respondWith("Another user already matches that hostmask");
        } else if (botUserDao.getWithName(username).isPresent()) {
            event.respondWith("Another user already has that name");
        } else {
            BotUser botUser = new BotUser();
            botUser.setName(username);
            botUser.setBotUserHostmasks(List.of(hostmask));
            botUser.setBotUserFlags(flags);
            botUserDao.create(botUser);
            event.respondWith("User added");
        }
    }

    /**
     * Adds managed channel flags for a bot user on the specified channel.<br/>
     * Usage: <code>ADDCHANFLAG username channel flags</code>
     *
     * @param args the remaining arguments to the subcommand (index 0: username, index 1: channel, index 2: comma-delimited list of flags)
     */
    private void addChannelFlagCommand(List<String> args) {
        if (args.size() < 3) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String username = Validate.botUserName(args.get(0));
        String channelName = args.get(1);
        String flagStr = args.get(2);
        BotUser botUser = null;
        Optional<BotUser> optionalBotUser = botUserDao.getWithName(username);
        ManagedChannelDao mcDao = new ManagedChannelDao();
        ManagedChannel managedChannel = null;
        Optional<ManagedChannel> optionalManagedChannel = mcDao.getWithName(channelName);
        List<ManagedChannelUserFlag> flags;

        if (optionalBotUser.isEmpty()) {
            event.respondWith(UNKNOWN_USER_STR);
            return;
        } else {
            botUser = optionalBotUser.get();
        }

        // create a managed channel entry if none exists yet
        if (optionalManagedChannel.isEmpty()) {
            managedChannel = new ManagedChannel();
            managedChannel.setName(channelName);
            mcDao.create(managedChannel);
            // retrieve from the database to get the id
            optionalManagedChannel = mcDao.getWithName(channelName);
        }

        if (optionalManagedChannel.isPresent()) {
            managedChannel = optionalManagedChannel.get();
        } else {
            event.respondWith("Could not find managed channel");
            return;
        }

        ManagedChannelUserDao mcuDao = new ManagedChannelUserDao();
        List<ManagedChannelUser> channelUsers = managedChannel.getManagedChannelUsers();
        if (channelUsers == null) {
             channelUsers = new ArrayList<>();
        }
        ManagedChannelUser managedChannelUser;
        BotUser finalBotUser = botUser;
        Optional<ManagedChannelUser> optionalManagedChannelUser = channelUsers.stream()
                .filter(u -> u.getBotUserId().equals(finalBotUser.getId()))
                .findFirst();

        if (optionalManagedChannelUser.isPresent()) {
            managedChannelUser = optionalManagedChannelUser.get();
        } else {
            managedChannelUser = new ManagedChannelUser();
            managedChannelUser.setManagedChannelId(managedChannel.getId());
            managedChannelUser.setBotUserId(botUser.getId());
            mcuDao.create(managedChannelUser);
            // retrieve from the database to get the id
            mcuDao.getWithManagedChannelIdAndBotUserId(managedChannel.getId(), botUser.getId());
        }

        List<ManagedChannelUserFlag> flagList = managedChannelUser.getManagedChannelUserFlags();
        flags = ManagedChannelUserHelper.parseFlags(flagList != null ? flagList : new ArrayList<>(), flagStr);
        managedChannelUser.setManagedChannelUserFlags(flags);
        mcuDao.update(managedChannelUser);

        event.respondWith(String.format("Flags for %s on %s: %s",
                botUser.getName(), managedChannel.getName(), managedChannelUser.getManagedChannelUserFlags()));
    }

    /**
     * Adds flags to a bot user.<br/>
     * Usage: <code>ADDFLAG username flags</code>
     *
     * @param args the remaining arguments to the subcommand (index 0: username, index 1: comma-delimited list of flags)
     */
    private void addFlagCommand(List<String> args) {
        if (args.size() < 2) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String username = Validate.botUserName(args.get(0));
        String flagStr = args.get(1);
        Optional<BotUser> optionalBotUser = botUserDao.getWithName(username);

        if (optionalBotUser.isEmpty()) {
            event.respondWith(UNKNOWN_USER_STR);
        } else {
            BotUser botUser = optionalBotUser.get();
            List<BotUserFlag> flags = BotUserHelper.parseFlags(botUser.getBotUserFlags(), flagStr);
            botUser.setBotUserFlags(flags);
            botUserDao.update(botUser);
            event.respondWith("Flags: " + botUser.getBotUserFlags());
        }
    }

    /**
     * Adds a hostmask to a bot user.<br/>
     * Usager: <code>ADDHOSTMASK username hostmask</code>
     *
     * @param args the remaining arguments to the subcommand (index 0: username, index 1: hostmask)
     */
    private void addHostmaskCommand(List<String> args) {
        if (args.size() < 2) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String username = Validate.botUserName(args.get(0));
        String hostmask = Validate.hostmask(args.get(1));
        Optional<BotUser> optionalBotUser = botUserDao.getWithName(username);
        List<BotUser> matchingUsers = BotUserHelper.findByHostmask(hostmask);

        if (optionalBotUser.isEmpty()) {
            event.respondWith(UNKNOWN_USER_STR);
        } else if (!matchingUsers.isEmpty()) {
            event.respondWith("There is already a user with a matching hostmask");
        } else {
            BotUser botUser = optionalBotUser.get();
            List<String> hostmasks = botUser.getBotUserHostmasks();
            hostmasks.add(hostmask);
            botUser.setBotUserHostmasks(hostmasks);
            botUserDao.update(botUser);
            event.respondWith("Hostmask added");
        }
    }

    /**
     * Lists all bot users.
     */
    private void listCommand() {
        List<BotUser> botUsers = botUserDao.getAll();

        if (!botUsers.isEmpty()) {
            List<List<BotUser>> groups = new ArrayList<>();
            Deque<BotUser> userDeque = new ArrayDeque<>(botUsers);

            while (!userDeque.isEmpty()) {
                List<BotUser> group = new ArrayList<>();
                for (int i = 0; !userDeque.isEmpty() && i < LIST_COMMAND_MAX_USERS_PER_LINE; i++) {
                    group.add(userDeque.pop());
                }
                groups.add(group);
            }

            int cnt = 1;
            for (List<BotUser> g : groups) {
                event.respondWith(String.format("Bot Users (%d/%d): %s",
                        cnt,
                        groups.size(),
                        g.stream().map(BotUser::getName).collect(joining(", "))));
                cnt++;
            }

            event.respondWith("USER SHOW <username> to see details");
        } else {
            event.respondWith("There are no bot users!");
        }
    }

    /**
     * Removes a user from the bot.
     *
     * @param args the remaining arguments to the subcommand (index 0: username)
     */
    private void removeCommand(List<String> args) {
        if (args.isEmpty()) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String username = Validate.botUserName(args.get(0));
        Optional<BotUser> optionalBotUser = botUserDao.getWithName(username);

        if (optionalBotUser.isEmpty()) {
            event.respondWith(UNKNOWN_USER_STR);
        } else {
            BotUser botUser = optionalBotUser.get();
            botUserDao.delete(botUser.getId());
            event.respondWith("User removed");
        }
    }

    /**
     * Removes managed channel user flags for a bot user on a managed channel.<br/>
     * Usage: <code>REMOVECHANFLAG username channel flags</code>
     *
     * @param args the remaining arguments to the subcommand (index 0: username, index 1: channel, index 2: comma-delimited list of flags)
     */
    private void removeChannelFlagCommand(List<String> args) {
        if (args.size() < 3) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String username = Validate.botUserName(args.get(0));
        String channelName = args.get(1);
        String flagStr = args.get(2);
        Optional<BotUser> optionalBotUser = botUserDao.getWithName(username);
        ManagedChannelDao mcDao = new ManagedChannelDao();
        Optional<ManagedChannel> optionalManagedChannel = mcDao.getWithName(channelName);

        if (optionalBotUser.isEmpty()) {
            event.respondWith(UNKNOWN_USER_STR);
            return;
        }

        if (optionalManagedChannel.isEmpty()) {
            event.respondWith("Not a managed channel");
            return;
        }

        BotUser botUser = optionalBotUser.get();
        ManagedChannel managedChannel = optionalManagedChannel.get();
        ManagedChannelUserDao mcuDao = new ManagedChannelUserDao();
        Optional<ManagedChannelUser> optionalManagedChannelUser = mcuDao.getWithManagedChannelIdAndBotUserId(managedChannel.getId(), botUser.getId());
        ManagedChannelUser mcu;

        if (optionalManagedChannelUser.isEmpty()) {
            mcu = new ManagedChannelUser();
            mcu.setManagedChannel(managedChannel);
            mcu.setBotUser(botUser);
            mcuDao.create(mcu);
            optionalManagedChannelUser = mcuDao.getWithManagedChannelIdAndBotUserId(managedChannel.getId(), botUser.getId());
        }

        if (optionalManagedChannelUser.isPresent()) {
            mcu = optionalManagedChannelUser.get();
            List<ManagedChannelUserFlag> flags = mcu.getManagedChannelUserFlags() != null ? mcu.getManagedChannelUserFlags() : new ArrayList<>();
            List<ManagedChannelUserFlag> flagsToRemove = ManagedChannelUserHelper.parseFlags(flagStr);

            for (ManagedChannelUserFlag flag : flagsToRemove) {
                flags.remove(flag);
            }

            mcu.setManagedChannelUserFlags(flags);
            mcuDao.update(mcu);

            event.respondWith(String.format("Flags for %s on %s: %s",
                    botUser.getName(), managedChannel.getName(), mcu.getManagedChannelUserFlags()));
        }
    }

    /**
     * Removes flags from a bot user.<br/>
     * Usage: <code>REMOVEFLAG username flags</code>
     *
     * @param args the remaining arguments to the subcommand (index 0: username, index 1: comma-delimited list of flags)
     */
    private void removeFlagCommand(List<String> args) {
        if (args.size() < 2) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String username = Validate.botUserName(args.get(0));
        String flagStr = args.get(1);
        Optional<BotUser> optionalBotUser = botUserDao.getWithName(username);

        if (optionalBotUser.isEmpty()) {
            event.respondWith(UNKNOWN_USER_STR);
        } else {
            BotUser botUser = optionalBotUser.get();
            List<BotUserFlag> flags = BotUserHelper.parseFlags(botUser.getBotUserFlags(), flagStr);
            botUser.setBotUserFlags(flags);
            botUserDao.update(botUser);
            event.respondWith("Flags: " + botUser.getBotUserFlags());
        }

    }

    /**
     * Removes a hostmask from a bot user.<br/>
     * Usage: <code>REMOVEHOSTMASK username hostmask</code>
     *
     * @param args remaining arguments to the subcommand (index 0: username, index 1: hostmask)
     */
    private void removeHostmaskCommand(List<String> args) {
        if (args.size() < 2) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String username = Validate.botUserName(args.get(0));
        String hostmask = args.get(1);
        Optional<BotUser> optionalBotUser = botUserDao.getWithName(username);

        if (optionalBotUser.isEmpty()) {
            event.respondWith(UNKNOWN_USER_STR);
        } else {
            BotUser botUser = optionalBotUser.get();

            if (!botUser.getBotUserHostmasks().contains(hostmask)) {
                event.respondWith("No such hostmask");
            } else {
                List<String> hostmasks = botUser.getBotUserHostmasks();
                hostmasks.remove(hostmask);
                botUser.setBotUserHostmasks(hostmasks);
                botUserDao.update(botUser);
                event.respondWith("Hostmask removed");
            }
        }
    }

    /**
     * Shows the details of a bot user.<br/>
     * Usage: <code>SHOW username</code>
     *
     * @param args the remaining arguments to the subcommand (index 0: username)
     */
    private void showCommand(List<String> args) {
        if (args.isEmpty()) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String username = Validate.botUserName(args.get(0));
        Optional<BotUser> optionalBotUser = botUserDao.getWithName(username);

        if (optionalBotUser.isEmpty()) {
            event.respondWith(UNKNOWN_USER_STR);
        } else {
            BotUser botUser = optionalBotUser.get();
            List<ManagedChannelUser> mcuList = botUser.getManagedChannelUsers();
            List<ManagedChannel> mcList = new ArrayList<>();
            ManagedChannelDao mcDao = new ManagedChannelDao();

            if (mcuList != null) {
                for (ManagedChannelUser mcu : mcuList) {
                    Optional<ManagedChannel> optionalManagedChannel = mcDao.get(mcu.getManagedChannelId());
                    optionalManagedChannel.ifPresent(mcList::add);
                }
            }

            event.respondWith(String.format("User: %s - hostmasks%s flags%s channels[%s]",
                    botUser.getName(),
                    botUser.getBotUserHostmasks(),
                    botUser.getBotUserFlags(),
                    mcList.stream().map(ManagedChannel::getName).collect(joining(", "))));
        }
    }

    /**
     * Sets the location for a bot user (used by the WEATHER command).<br/>
     * Usage: <code>SHOW username</code>
     *
     * @param args the remaining arguments to the subcommand (index 0: username, index 1: location)
     * @see WeatherCommand
     */
    private void locationCommand(List<String> args) {
        if (args.size() < 2) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String username = Validate.botUserName(args.get(0));
        String location = String.join(" ", args.subList(1, args.size()));
        Optional<BotUser> optionalBotUser = botUserDao.getWithName(username);

        if (optionalBotUser.isEmpty()) {
            event.respondWith(UNKNOWN_USER_STR);
        } else {
            BotUser botUser = optionalBotUser.get();
            botUser.setLocation(location);
            botUserDao.update(botUser);
            event.respondWith(String.format("Set %s's location to %s", botUser.getName(), botUser.getLocation()));
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
