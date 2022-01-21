package net.hatemachine.mortybot.listeners;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.BotCommandProxy;
import net.hatemachine.mortybot.Command;
import net.hatemachine.mortybot.exception.BotCommandException;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static net.hatemachine.mortybot.listeners.CommandListener.CommandSource.PRIVATE;
import static net.hatemachine.mortybot.listeners.CommandListener.CommandSource.PUBLIC;

/**
 * Listen for commands from users. These can come from any source but currently only
 * messages from channels or direct private messages from users are supported.
 * This will likely be expanded to include DCC chat at some point.
 */
public class CommandListener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);

    private final String commandPrefix;

    public enum CommandSource {
        PRIVATE,
        PUBLIC
    }

    public CommandListener() {
        this.commandPrefix = "!";
    }

    public CommandListener(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    @Override
    public void onMessage(final MessageEvent event) {
        LOGGER.debug("onMessage event: {}", event);
        if (event.getMessage().startsWith(getCommandPrefix())) {
            handleCommand(event, PUBLIC);
        }
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent event) {
        LOGGER.debug("onPrivateMessage event: {}", event);
        if (event.getMessage().startsWith(getCommandPrefix())) {
            handleCommand(event, PRIVATE);
        }
    }

    /**
     * Handle a command from a user. For now this can either be a public command from a channel
     * or a private message from a user but could be expanded to other sources (e.g. CTCP or DCC).
     *
     * @param event the event that contained a command
     * @param source the source of the command, public or private message
     */
    private void handleCommand(final GenericMessageEvent event, CommandSource source) {
        List<String> tokens = Arrays.asList(event.getMessage().split(" "));
        String commandStr = tokens.get(0).substring(getCommandPrefix().length()).toUpperCase(Locale.ROOT);
        List<String> args = tokens.subList(1, tokens.size());
        User user = event.getUser();

        LOGGER.info("{} command triggered by {}, args: {}", commandStr, user.getNick(), args);

        try {
            Command command = Enum.valueOf(Command.class, commandStr);
            BotCommand botCommand = (BotCommand) command.getBotCommandClass()
                    .getDeclaredConstructor(GenericMessageEvent.class, CommandListener.CommandSource.class, List.class)
                    .newInstance(event, source, args);
            execBotCommand(botCommand);

        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid command {} from {}", commandStr, user.getNick());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOGGER.error("Exception encountered during command invocation", e);
        }
    }

    /**
     * Execute a bot command implemented with the BotCommand interface.
     * This will pass through a BotCommandProxy instance to validate and authorize.
     *
     * @param botCommand instance of the command you want to run
     */
    private void execBotCommand(final BotCommand botCommand) {
        try {
            BotCommandProxy.newInstance(botCommand).execute();
        } catch (BotCommandException e) {
            LOGGER.error("Exception encountered during command execution", e);
        }
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }
}
