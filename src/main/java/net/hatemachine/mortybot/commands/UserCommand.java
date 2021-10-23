package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.BotUser;
import net.hatemachine.mortybot.BotUserType;
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

    private static final BotUserType DEFAULT_USER_TYPE = BotUserType.MORTY;
    private static final String      NOT_ENOUGH_ARGS   = "Too few arguments";

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
                case "ADDHOSTMASK":
                    addHostmaskCommand(newArgs);
                    break;
                case "LIST":
                    listCommand();
                    break;
                case "REMOVE":
                    removeCommand(newArgs);
                    break;
                case "REMOVEHOSTMASK":
                    removeHostmaskCommand(newArgs);
                    break;
                case "SETTYPE":
                    setTypeCommand(newArgs);
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

        String name = args.get(0);
        String hostmask = args.get(1);
        MortyBot bot = event.getBot();

        try {
            bot.addBotUser(name, hostmask, DEFAULT_USER_TYPE);
            event.respondWith("User added");
        } catch (BotUserException e) {
            handleBotUserException(e, "addCommand", args);
        } catch (IllegalArgumentException e) {
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

        String name = args.get(0);
        String hostmask = args.get(1);
        MortyBot bot = event.getBot();

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

        String name = args.get(0);
        MortyBot bot = event.getBot();

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
     * Remove a hostmask from a user.
     *
     * @param args the name of the user and the hostmask you want to remove
     * @throws IllegalArgumentException if there are too few arguments
     */
    private void removeHostmaskCommand(List<String> args) throws IllegalArgumentException {
        if (args.size() < 2)
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS);

        String name = args.get(0);
        String hostmask = args.get(1);
        MortyBot bot = event.getBot();

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
     * Set a bot user's type.
     *
     * @param args the name of the user and the type that you want to set in the form "name type"
     * @throws IllegalArgumentException if there are too few arguments
     */
    private void setTypeCommand(List<String> args) throws IllegalArgumentException {
        if (args.size() < 2)
            throw new IllegalArgumentException(NOT_ENOUGH_ARGS);

        String name = args.get(0);
        String typeStr = args.get(1);
        MortyBot bot = event.getBot();

        try {
            BotUserType type = Enum.valueOf(BotUserType.class, typeStr.toUpperCase(Locale.ROOT));
            bot.setBotUserType(name, type);
            event.respondWith("User type changed");
        } catch (BotUserException e) {
            handleBotUserException(e, "setTypeCommand", args);
        } catch (IllegalArgumentException e) {
            // handle invalid user types and give a more sanitized response
            if (e.getMessage().startsWith("No enum constant")) {
                event.respondWith("Invalid user type");
            } else {
                event.respondWith(e.getMessage());
            }
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

        String name = args.get(0);
        MortyBot bot = event.getBot();

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
                log.error("{} Unhandled BotUserException {} {}", method, e.getReason(), e.getMessage());
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
