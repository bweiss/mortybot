package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.BotUser;
import net.hatemachine.mortybot.CommandListener;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.exception.BotUserException;
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
            log.info("{}: {}, args: {}", command, e.getMessage(), newArgs);
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
            bot.addBotUser(name, hostmask, flags);
            event.respondWith("User added");
        } catch (BotUserException e) {
            handleBotUserException(e, "addCommand", args);
        } catch (IllegalArgumentException e) {
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
        } catch (IllegalArgumentException e) {
            log.warn("Invalid user flag: {}", flagStr);
            event.respondWith("Invalid flag");
        }

        if (flag != null) {
            try {
                bot.addBotUserFlag(name, flag);
                event.respondWith("Flag added");
            } catch (BotUserException e) {
                handleBotUserException(e, "addFlagCommand", args);
            } catch (IllegalArgumentException e) {
                event.respondWith(e.getMessage());
            }
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
            bot.addBotUserHostmask(name, hostmask);
            event.respondWith("Hostmask added");
        } catch (BotUserException e) {
            handleBotUserException(e, "addHostmaskCommand", args);
        } catch (IllegalArgumentException e) {
            event.respondWith(e.getMessage());
        }
    }

    /**
     * List all bot users.
     */
    private void listCommand() {
        MortyBot bot = event.getBot();
        List<BotUser> users = bot.getBotUsers();

        if (!users.isEmpty()) {
            // todo improve formatting and handling of larger user lists
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
            bot.removeBotUser(name);
            event.respondWith("User removed");
        } catch (BotUserException e) {
            handleBotUserException(e, "removeCommand", args);
        } catch (IllegalArgumentException e) {
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
        } catch (IllegalArgumentException e) {
            log.warn("Invalid user flag: {}", flagStr);
            event.respondWith("Invalid flag");
        }

        if (flag != null) {
            try {
                bot.removeBotUserFlag(name, flag);
                event.respondWith("Flag removed");
            } catch (BotUserException e) {
                handleBotUserException(e, "removeFlagCommand", args);
            } catch (IllegalArgumentException e) {
                event.respondWith(e.getMessage());
            }
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
            bot.removeBotUserHostmask(name, hostmask);
            event.respondWith("Hostmask removed");
        } catch (BotUserException e) {
            handleBotUserException(e, "removeHostmaskCommand", args);
        } catch (IllegalArgumentException e) {
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
            BotUser user = bot.getBotUserByName(name);
            // todo improve format of this response
            event.respondWith(user.toString());
        } catch (BotUserException e) {
            handleBotUserException(e, "addCommand", args);
        } catch (IllegalArgumentException e) {
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
            case FLAG_EXISTS:
                errMsg = "Flag already exists";
                break;
            case FLAG_NOT_FOUND:
                errMsg = "Invalid user flag";
                break;
            case HOSTMASK_EXISTS:
                errMsg = "Hostmask already exists";
                break;
            case HOSTMASK_NOT_FOUND:
                errMsg = "Hostmask not found";
                break;
            case USER_EXISTS:
                errMsg = "User already exists";
                break;
            case USER_NOT_FOUND:
                errMsg = "User not found";
                break;
            default:
                log.error("{} caused unhandled BotUserException {} {}", method, e.getReason(), e.getMessage());
                e.printStackTrace();
                return;
        }
        log.debug("{}: {}, args: {}", method, errMsg, args);
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
