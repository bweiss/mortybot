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
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.BotUserDao;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.exception.BotUserException;
import net.hatemachine.mortybot.util.IrcUtils;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.stream.Collectors.joining;
import static net.hatemachine.mortybot.config.BotDefaults.USER_ADD_MASK_TYPE;

/**
 * USER command that allows you to view and manipulate bot users.
 *
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
        this.botUserDao = ((MortyBot)event.getBot()).getBotUserDao();
    }

    @Override
    public void execute() throws IllegalArgumentException {
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
        } catch (IllegalArgumentException e) {
            log.error("{}: {}, args: {}", command, e.getMessage(), newArgs);
        }
    }

    /**
     * Add a user to the bot.
     *
     * @param args {@link List} of arguments. Should contain name at minimum and an optional hostmask (may contain wildcards).
     * @throws IllegalArgumentException if there are not enough arguments
     */
    private void addCommand(List<String> args) throws IllegalArgumentException {
        if (args.isEmpty()) {
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS_STR);
        }

        MortyBot bot = event.getBot();
        String name = args.get(0);
        String hostmask = "";
        String flags = "";

        // if no hostmask provided, see if there is a known user with that nick and attempt to pull their hostmask
        if (args.size() == 1) {
            User user = bot.getUserChannelDao().getUser(name);

            if (user != null) {
                try {
                    // this fails if the bot doesn't yet know the user's full hostmask
                    hostmask = IrcUtils.maskAddress(user.getHostmask(),
                            BotProperties.getBotProperties().getIntProperty("user.add.mask.type", USER_ADD_MASK_TYPE));
                } catch (IllegalArgumentException e) {
                    event.respondWith(String.format("Could not determine hostmask for %s. Try specifying manually.", name));
                    return;
                }
            }
        } else {
            hostmask = args.get(1);
        }

        if (args.size() > 2) {
            flags = args.get(2);
        }

        List<BotUser> matchedUsers = botUserDao.getAll(hostmask);

        if (!matchedUsers.isEmpty()) {
            event.respondWith("A user with a matching hostmask already exists");
        } else {
            try {
                BotUser botUser = new BotUser(Validate.botUserName(name), Validate.hostmask(hostmask), Validate.botUserFlags(flags));
                botUserDao.add(botUser);
                event.respondWith("User added");

            } catch (BotUserException e) {
                handleBotUserException(e, "addCommand", args);

            } catch (IllegalArgumentException e) {
                log.error("Error adding user: {}", e.getMessage());
                event.respondWith(e.getMessage());
            }
        }
    }

    /**
     * Add a flag to a bot user.
     *
     * @param args the name of the bot user and the flag you want to add
     * @throws IllegalArgumentException if there are not enough arguments
     */
    private void addFlagCommand(List<String> args) throws IllegalArgumentException {
        if (args.size() < 2) {
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS_STR);
        }

        String username = args.get(0);
        String flag = Validate.botUserFlags(args.get(1));

        try {
            Optional<BotUser> optionalBotUser = botUserDao.getByUsername(username);

            if (optionalBotUser.isPresent()) {
                BotUser botUser = optionalBotUser.get();
                botUser.addFlag(flag);
                botUserDao.update(botUser);
                event.respondWith("Flag(s) added");
            } else {
                event.respondWith(UNKNOWN_USER_STR);
            }

        } catch (BotUserException e) {
            handleBotUserException(e, "addFlagCommand", args);
        }
    }

    /**
     * Add a hostmask to a bot user.
     *
     * @param args the name of the user and hostmask to add in the form of "name nick!user@host" (may contain wildcards)
     * @throws IllegalArgumentException if there are not enough arguments
     */
    private void addHostmaskCommand(List<String> args) throws IllegalArgumentException {
        if (args.size() < 2) {
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS_STR);
        }

        String username = args.get(0);
        String hostmask = args.get(1);

        try {
            Optional<BotUser> optionalBotUser = botUserDao.getByUsername(username);
            if (optionalBotUser.isPresent()) {
                BotUser botUser = optionalBotUser.get();
                botUser.addHostmask(Validate.hostmask(hostmask));
                botUserDao.update(botUser);
                event.respondWith("Hostmask added");
            } else {
                event.respondWith(UNKNOWN_USER_STR);
            }
        } catch (BotUserException e) {
            handleBotUserException(e, "addHostmaskCommand", args);
        } catch (IllegalArgumentException e) {
            log.error("Error adding hostmask: {}", e.getMessage());
            event.respondWith(e.getMessage());
        }
    }

    /**
     * List all bot users.
     */
    private void listCommand() {
        List<BotUser> users = botUserDao.getAll();

        if (!users.isEmpty()) {
            List<List<BotUser>> groups = new ArrayList<>();
            Deque<BotUser> userDeque = new ArrayDeque<>(users);

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
                        g.stream().map(BotUser::getUsername).collect(joining(", "))));
                cnt++;
            }

            event.respondWith("USER SHOW <username> to see details");
        } else {
            event.respondWith("There are no bot users!");
        }
    }

    /**
     * Remove a user from the bot.
     *
     * @param args the name of the user you want to remove
     * @throws IllegalArgumentException if there are too few arguments
     */
    private void removeCommand(List<String> args) throws IllegalArgumentException {
        if (args.isEmpty()) {
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS_STR);
        }

        String username = args.get(0);

        try {
            Optional<BotUser> optionalBotUser = botUserDao.getByUsername(username);
            if (optionalBotUser.isPresent()) {
                BotUser botUser = optionalBotUser.get();
                botUserDao.delete(botUser);
                event.respondWith("User removed");
            } else {
                event.respondWith(UNKNOWN_USER_STR);
            }
        } catch (BotUserException e) {
            handleBotUserException(e, "removeCommand", args);
        } catch (IllegalArgumentException e) {
            log.error("Error removing user: {}", e.getMessage());
            event.respondWith(e.getMessage());
        }
    }

    /**
     * Remove a flag from a bot user.
     *
     * @param args the name of the bot user and the flag that you want to remove
     * @throws IllegalArgumentException if there is an issue removing the flag
     */
    private void removeFlagCommand(List<String> args) throws IllegalArgumentException {
        if (args.size() < 2)
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS_STR);

        String username = args.get(0);
        String flag = args.get(1).toUpperCase(Locale.ROOT);

        try {
            Optional<BotUser> optionalBotUser = botUserDao.getByUsername(username);
            if (optionalBotUser.isPresent()) {
                BotUser botUser = optionalBotUser.get();
                botUser.removeFlag(flag);
                botUserDao.update(botUser);
                event.respondWith("Flag removed");
            } else {
                event.respondWith(UNKNOWN_USER_STR);
            }
        } catch (BotUserException e) {
            handleBotUserException(e, "removeFlagCommand", args);
        } catch (IllegalArgumentException e) {
            log.error("Error removing flag: {}", e.getMessage());
            event.respondWith(e.getMessage());
        }
    }

    /**
     * Remove a hostmask from a user.
     *
     * @param args the name of the user and the hostmask you want to remove
     * @throws IllegalArgumentException if there are too few arguments
     */
    private void removeHostmaskCommand(List<String> args) throws IllegalArgumentException {
        if (args.size() < 2) {
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS_STR);
        }

        String username = args.get(0);
        String hostmask = args.get(1);

        try {
            Optional<BotUser> optionalBotUser = botUserDao.getByUsername(username);
            if (optionalBotUser.isPresent()) {
                BotUser botUser = optionalBotUser.get();
                botUser.removeHostmask(hostmask);
                botUserDao.update(botUser);
                event.respondWith("Hostmask removed");
            } else {
                event.respondWith(UNKNOWN_USER_STR);
            }
        } catch (BotUserException e) {
            handleBotUserException(e, "removeHostmaskCommand", args);
        } catch (IllegalArgumentException e) {
            log.error("Error removing hostmask: {}", e.getMessage());
            event.respondWith(e.getMessage());
        }
    }

    /**
     * Show the details of a bot user.
     *
     * @param args the name of the user you want to show
     * @throws IllegalArgumentException if there are too few arguments
     */
    private void showCommand(List<String> args) throws IllegalArgumentException {
        if (args.isEmpty()) {
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS_STR);
        }

        String username = args.get(0);

        try {
            Optional<BotUser> optionalBotUser = botUserDao.getByUsername(username);
            if (optionalBotUser.isPresent()) {
                BotUser botUser = optionalBotUser.get();
                event.respondWith(botUser.toString());
            } else {
                event.respondWith(UNKNOWN_USER_STR);
            }
        } catch (IllegalArgumentException e) {
            log.error("Error showing user: {}", e.getMessage());
            event.respondWith(e.getMessage());
        }
    }

    private void setLocationCommand(List<String> args) throws IllegalArgumentException {
        if (args.size() < 2) {
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS_STR);
        }

        String username = args.get(0);
        Optional<BotUser> optionalBotUser = botUserDao.getByUsername(username);

        if (optionalBotUser.isPresent()) {
            BotUser botUser = optionalBotUser.get();
            botUser.setLocation(String.join(" ", args.subList(1, args.size())));

            try {
                botUserDao.update(botUser);
                event.respondWith(String.format("%s's location set to %s", botUser.getUsername(), botUser.getLocation()));
            } catch (BotUserException e) {
                String errMsg;
                if (e.getReason() == BotUserException.Reason.UNKNOWN_USER) {
                    errMsg = UNKNOWN_USER_STR;
                } else {
                    errMsg = "Something went wrong updating user";
                }
                log.error(errMsg, e);
                event.respondWith(errMsg);
            }
        } else {
            event.respondWith(UNKNOWN_USER_STR);
        }
    }

    /**
     * Helper method to handle BotUserException and respond appropriately.
     *
     * @param e the BotUserException object
     * @param method the name of the method that ultimately triggered the exception
     * @param args the arguments passed to the method that triggered the exception
     */
    private void handleBotUserException(BotUserException e, String method, List<String> args) {
        String errMsg;
        errMsg = switch (e.getReason()) {
            case UNKNOWN_USER -> UNKNOWN_USER_STR;
            case USER_EXISTS -> "User already exists";
        };
        log.error("{}: {}, args: {}", method, errMsg, args);
        event.respondWith(errMsg);
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
