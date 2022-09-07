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
import net.hatemachine.mortybot.dao.BotUserDao;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.util.BotUserHelper;
import net.hatemachine.mortybot.util.IrcUtils;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static net.hatemachine.mortybot.config.BotDefaults.USER_ADD_MASK_TYPE;

/**
 * USER command that allows you to view and manipulate bot users.<br/>
 * <br/>
 * Supported subcommands: LIST, SHOW, ADD, REMOVE, ADDHOSTMASK, REMOVEHOSTMASK, ADDFLAG, REMOVEFLAG
 */
public class UserCommand implements BotCommand {

    private static final int LIST_COMMAND_MAX_USERS_PER_LINE = 20;

    private static final String NOT_ENOUGH_ARGS_STR = "Too few arguments";
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
                case "ADDFLAG" -> addFlagCommand(newArgs);
                case "ADDHOSTMASK" -> addHostmaskCommand(newArgs);
                case "LIST" -> listCommand();
                case "REMOVE" -> removeCommand(newArgs);
                case "REMOVEFLAG" -> removeFlagCommand(newArgs);
                case "REMOVEHOSTMASK" -> removeHostmaskCommand(newArgs);
                case "SETLOCATION" -> setLocationCommand(newArgs);
                case "SHOW" -> showCommand(newArgs);
                default -> log.info("Unknown USER command {} from {}", command, event.getUser().getNick());
            }
        } catch (Exception ex) {
            log.error("Exception encountered: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Adds a user to the bot.
     *
     * @param args remaining arguments to the subcommand (index 0: username, index 1: optional hostmask)
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
            flags = BotUserHelper.buildFlagList(args.get(2));
        }

        List<BotUser> matchingUsers = BotUserHelper.findByHostmask(hostmask);

        if (!matchingUsers.isEmpty()) {
            event.respondWith("Another user already matches that hostmask");
        } else if (botUserDao.getWithName(username) != null) {
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
     * Adds flags to a bot user.
     *
     * @param args remaining arguments to the subcommand (index 0: username, index 1: comma-delimited list
     *             of flags)
     */
    private void addFlagCommand(List<String> args) {
        if (args.size() < 2) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String username = Validate.botUserName(args.get(0));
        List<BotUserFlag> flags = BotUserHelper.buildFlagList(args.get(1));
        BotUser botUser = botUserDao.getWithName(username);

        if (botUser == null) {
            event.respondWith(UNKNOWN_USER_STR);
        } else {
            botUser.setBotUserFlags(flags);
            botUserDao.update(botUser);
            event.respondWith("Flags: " + botUser.getBotUserFlags());
        }
    }

    /**
     * Adds a hostmask to a bot user.
     *
     * @param args remaining arguments to the subcommand (index 0: username, index 1: hostmask)
     */
    private void addHostmaskCommand(List<String> args) {
        if (args.size() < 2) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String username = Validate.botUserName(args.get(0));
        String hostmask = Validate.hostmask(args.get(1));
        BotUser botUser = botUserDao.getWithName(username);
        List<BotUser> matchingUsers = BotUserHelper.findByHostmask(hostmask);

        if (botUser == null) {
            event.respondWith(UNKNOWN_USER_STR);
        } else if (!matchingUsers.isEmpty()) {
            event.respondWith("There is already a user with a matching hostmask");
        } else {
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
     * @param args remaining arguments to the subcommand (index 0: username)
     */
    private void removeCommand(List<String> args) {
        if (args.isEmpty()) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String username = Validate.botUserName(args.get(0));
        BotUser botUser = botUserDao.getWithName(username);

        if (botUser == null) {
            event.respondWith(UNKNOWN_USER_STR);
        } else {
            botUserDao.delete(botUser.getId());
            event.respondWith("User removed");
        }
    }

    /**
     * Removes a flag from a bot user.
     *
     * @param args remaining arguments to the subcommand (index 0: username, index 1: the flag)
     */
    private void removeFlagCommand(List<String> args) {
        if (args.size() < 2) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String username = Validate.botUserName(args.get(0));
        String flagStr = args.get(1);
        BotUser botUser = botUserDao.getWithName(username);

        if (botUser == null) {
            event.respondWith(UNKNOWN_USER_STR);
        } else {
            List<BotUserFlag> flags = botUser.getBotUserFlags();
            for (String s : flagStr.split(",")) {
                BotUserFlag flag = Enum.valueOf(BotUserFlag.class, s.toUpperCase());
                flags.remove(flag);
            }
            botUser.setBotUserFlags(flags);
            botUserDao.update(botUser);
            event.respondWith("Flags: " + botUser.getBotUserFlags());
        }

    }

    /**
     * Removes a hostmask from a user.
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
        BotUser botUser = botUserDao.getWithName(username);

        if (botUser == null) {
            event.respondWith(UNKNOWN_USER_STR);
        } else if (!botUser.getBotUserHostmasks().contains(hostmask)) {
            event.respondWith("No such hostmask");
        } else {
            List<String> hostmasks = botUser.getBotUserHostmasks();
            hostmasks.remove(hostmask);
            botUser.setBotUserHostmasks(hostmasks);
            botUserDao.update(botUser);
            event.respondWith("Hostmask removed");
        }
    }

    /**
     * Shows the details of a bot user.
     *
     * @param args remaining arguments to the subcommand (index 0: username)
     */
    private void showCommand(List<String> args) {
        if (args.isEmpty()) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String username = Validate.botUserName(args.get(0));
        BotUser botUser = botUserDao.getWithName(username);

        if (botUser == null) {
            event.respondWith(UNKNOWN_USER_STR);
        } else {
            event.respondWith(botUser.toString());
        }
    }

    /**
     * Sets a default location for a user (used by the WEATHER command).
     *
     * @param args remaining arguments to the subcommand (index 0: username, index 1: location)
     * @see WeatherCommand
     */
    private void setLocationCommand(List<String> args) {
        if (args.size() < 2) {
            event.respondWith(NOT_ENOUGH_ARGS_STR);
            return;
        }

        String username = Validate.botUserName(args.get(0));
        String location = String.join(" ", args.subList(1, args.size()));
        BotUser botUser = botUserDao.getWithName(username);

        if (botUser == null) {
            event.respondWith(UNKNOWN_USER_STR);
        } else {
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
