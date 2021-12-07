package net.hatemachine.mortybot.listeners;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.BotCommandProxy;
import net.hatemachine.mortybot.commands.IpLookupCommand;
import net.hatemachine.mortybot.commands.JoinCommand;
import net.hatemachine.mortybot.commands.MessageCommand;
import net.hatemachine.mortybot.commands.OpCommand;
import net.hatemachine.mortybot.commands.PartCommand;
import net.hatemachine.mortybot.commands.QuitCommand;
import net.hatemachine.mortybot.commands.StockCommand;
import net.hatemachine.mortybot.commands.TestCommand;
import net.hatemachine.mortybot.commands.UserCommand;
import net.hatemachine.mortybot.commands.WeatherCommand;
import net.hatemachine.mortybot.exception.BotCommandException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static net.hatemachine.mortybot.listeners.CommandListener.CommandSource.PRIVATE;
import static net.hatemachine.mortybot.listeners.CommandListener.CommandSource.PUBLIC;

public class CommandListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(CommandListener.class);

    private final String commandPrefix;

    public enum Command {
        IPLOOKUP,
        JOIN,
        MSG,
        OP,
        PART,
        QUIT,
        STOCK,
        TEST,
        USER,
        WZ;
    }

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
        log.debug("onMessage event: {}", event);
        if (event.getMessage().startsWith(getCommandPrefix())) {
            handleCommand(event, PUBLIC);
        }
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent event) {
        log.debug("onPrivateMessage event: {}", event);
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
        Command command;
        List<String> args = tokens.subList(1, tokens.size());
        var user = event.getUser();

        try {
            command = Enum.valueOf(Command.class, commandStr);
        } catch (IllegalArgumentException e) {
            log.info("Invalid command {} from {}", user.getNick(), commandStr);
            return;
        }

        log.info("Command {} triggered by {}, args: {}", commandStr, user.getNick(), args);

        switch (command) {
            case IPLOOKUP:
                execBotCommand(new IpLookupCommand(event, source, args));
                break;

            case JOIN:
                execBotCommand(new JoinCommand(event, source, args));
                break;

            case MSG:
                execBotCommand(new MessageCommand(event, source, args));
                break;

            case OP:
                execBotCommand(new OpCommand(event, source, args));
                break;

            case PART:
                execBotCommand(new PartCommand(event, source, args));
                break;

            case STOCK:
                execBotCommand(new StockCommand(event, source, args));
                break;

            case QUIT:
                execBotCommand(new QuitCommand(event, source, args));
                break;

            case TEST:
                execBotCommand(new TestCommand(event, source, args));
                break;

            case USER:
                execBotCommand(new UserCommand(event, source, args));
                break;

            case WZ:
                execBotCommand(new WeatherCommand(event, source, args));
                break;
        }
    }

    /**
     * Execute a bot command implemented with the BotCommand interface.
     * This will pass through a BotCommandProxy instance to validate and authorize.
     *
     * @param command instance of the command you want to run
     */
    private void execBotCommand(final BotCommand command) {
        try {
            BotCommandProxy.newInstance(command).execute();
        } catch (BotCommandException e) {
            log.warn(e.getMessage());
        }
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }
}
