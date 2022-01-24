/*
 * MortyBot - An IRC bot built on the PircBotX framework.
 * Copyright © 2022 Brian Weiss (brianmweiss@gmail.com)
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
import net.hatemachine.mortybot.BotUser;
import net.hatemachine.mortybot.BotUserDao;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.exception.BotUserException;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.joining;

public class UserCommand implements BotCommand {

    private static final String NOT_ENOUGH_ARGS   = "Too few arguments";

    private static final Logger log = LoggerFactory.getLogger(UserCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public UserCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() throws IllegalArgumentException {
        if (args.isEmpty())
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS);

        String command = args.get(0).toUpperCase();
        List<String> newArgs = args.subList(1, args.size());

        try {
            switch (command) {
                case "ADD":
                    addCommand(newArgs);
                    break;
                case "ADDFLAG":
                    addFlagCommand(newArgs);
                    break;
                case "ADDHOSTMASK":
                    addHostmaskCommand(newArgs);
                    break;
                case "LIST":
                    listCommand();
                    break;
                case "REMOVE":
                    removeCommand(newArgs);
                    break;
                case "REMOVEFLAG":
                    removeFlagCommand(newArgs);
                    break;
                case "REMOVEHOSTMASK":
                    removeHostmaskCommand(newArgs);
                    break;
                case "SHOW":
                    showCommand(newArgs);
                    break;
                default:
                    log.info("Unknown USER command {} from {}", command, event.getUser().getNick());
            }
        } catch (IllegalArgumentException e) {
            log.error("{}: {}, args: {}", command, e.getMessage(), newArgs);
        }
    }

    /**
     * Add a user to the bot.
     *
     * @param args the name and initial hostmask of the user in the form of "name nick!user@host" (may contain wildcards)
     * @throws IllegalArgumentException if there are not enough arguments
     */
    private void addCommand(List<String> args) throws IllegalArgumentException {
        if (args.size() < 2)
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS);

        MortyBot bot = event.getBot();
        String name = args.get(0);
        String hostmask = args.get(1);
        String flags = "";

        if (args.size() > 2) {
            flags = args.get(2);
        }

        try {
            bot.getBotUserDao().add(new BotUser(Validate.botUserName(name), Validate.hostmask(hostmask), flags));
            event.respondWith("User added");
        } catch (BotUserException e) {
            handleBotUserException(e, "addCommand", args);
        } catch (IllegalArgumentException e) {
            log.error("Error adding user: {}", e.getMessage());
            event.respondWith(e.getMessage());
        }
    }

    /**
     * Add a flag to a bot user.
     *
     * @param args the name of the bot user and the flag you want to add
     * @throws IllegalArgumentException if there are not enough arguments
     */
    private void addFlagCommand(List<String> args) throws IllegalArgumentException {
        if (args.size() < 2)
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS);

        MortyBot bot = event.getBot();
        String name = args.get(0);
        String flagStr = args.get(1).toUpperCase(Locale.ROOT);
        BotUser.Flag flag = null;

        try {
            flag = Enum.valueOf(BotUser.Flag.class, flagStr);
            BotUserDao dao = bot.getBotUserDao();
            BotUser botUser = dao.getByName(name);
            botUser.addFlag(flag);
            dao.update(botUser);
            event.respondWith("Flag added");
        } catch (BotUserException e) {
            handleBotUserException(e, "addFlagCommand", args);
        } catch (IllegalArgumentException e) {
            log.error("Error adding flag: {}", e.getMessage());
            event.respondWith(e.getMessage());
        }
    }

    /**
     * Add a hostmask to a bot user.
     *
     * @param args the name of the user and hostmask to add in the form of "name nick!user@host" (may contain wildcards)
     * @throws IllegalArgumentException if there are not enough arguments
     */
    private void addHostmaskCommand(List<String> args) throws IllegalArgumentException {
        if (args.size() < 2)
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS);

        MortyBot bot = event.getBot();
        String name = args.get(0);
        String hostmask = args.get(1);

        try {
            BotUserDao dao = bot.getBotUserDao();
            BotUser botUser = dao.getByName(name);
            botUser.addHostmask(Validate.hostmask(hostmask));
            dao.update(botUser);
            event.respondWith("Hostmask added");
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
        MortyBot bot = event.getBot();
        List<BotUser> users = bot.getBotUserDao().getAll();

        if (!users.isEmpty()) {
            // TODO: improve formatting and handling of larger user lists
            String usernames = users.stream().map(BotUser::getName).collect(joining(", "));
            event.respondWith("Bot Users: " + usernames);
            event.respondWith("USER SHOW <username> to see details");
        }
    }

    /**
     * Remove a user from the bot.
     *
     * @param args the name of the user you want to remove
     * @throws IllegalArgumentException if there are too few arguments
     */
    private void removeCommand(List<String> args) throws IllegalArgumentException {
        if (args.isEmpty())
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS);

        MortyBot bot = event.getBot();
        String name = args.get(0);

        try {
            BotUserDao dao = bot.getBotUserDao();
            BotUser botUser = dao.getByName(name);
            dao.delete(botUser);
            event.respondWith("User removed");
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
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS);

        MortyBot bot = event.getBot();
        String name = args.get(0);
        String flagStr = args.get(1).toUpperCase(Locale.ROOT);
        BotUser.Flag flag = null;

        try {
            flag = Enum.valueOf(BotUser.Flag.class, flagStr);
            BotUserDao dao = bot.getBotUserDao();
            BotUser botUser = dao.getByName(name);
            botUser.removeFlag(flag);
            dao.update(botUser);
            event.respondWith("Flag removed");
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
        if (args.size() < 2)
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS);

        MortyBot bot = event.getBot();
        String name = args.get(0);
        String hostmask = args.get(1);

        try {
            BotUserDao dao = bot.getBotUserDao();
            BotUser botUser = dao.getByName(name);
            botUser.removeHostmask(hostmask);
            dao.update(botUser);
            event.respondWith("Hostmask removed");
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
        if (args.isEmpty())
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS);

        MortyBot bot = event.getBot();
        String name = args.get(0);

        try {
            BotUser botUser = bot.getBotUserDao().getByName(name);
            event.respondWith(botUser.toString());
        } catch (BotUserException e) {
            handleBotUserException(e, "addCommand", args);
        } catch (IllegalArgumentException e) {
            log.error("Error showing user: {}", e.getMessage());
            event.respondWith(e.getMessage());
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
        switch (e.getReason()) {
            case UNKNOWN_USER:
                errMsg = "User not found";
                break;
            case USER_EXISTS:
                errMsg = "User already exists";
                break;
            default:
                log.error("{} caused unhandled BotUserException {} {}", method, e.getReason(), e.getMessage());
                e.printStackTrace();
                return;
        }
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
