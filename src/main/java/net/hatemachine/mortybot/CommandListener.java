package net.hatemachine.mortybot;

import net.hatemachine.mortybot.commands.IpLookupCommand;
import net.hatemachine.mortybot.commands.JoinCommand;
import net.hatemachine.mortybot.commands.MessageCommand;
import net.hatemachine.mortybot.commands.OpCommand;
import net.hatemachine.mortybot.commands.PartCommand;
import net.hatemachine.mortybot.commands.QuitCommand;
import net.hatemachine.mortybot.commands.StockCommand;
import net.hatemachine.mortybot.commands.TestCommand;
import net.hatemachine.mortybot.exception.BotCommandException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static net.hatemachine.mortybot.CommandListener.MessageSource.PRIVATE;
import static net.hatemachine.mortybot.CommandListener.MessageSource.PUBLIC;

public class CommandListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(CommandListener.class);

    private final String commandPrefix;

    public enum MessageSource {
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
     * Simple commands are implemented directly within this class (e.g. the join command) but more
     * complex commands can be implemented in their own class using the BotCommand interface
     * and the execBotCommand method (see TestCommand for an example).
     *
     * @param event the event that contained a command
     * @param source the source of the command, public or private message
     */
    private void handleCommand(final GenericMessageEvent event, MessageSource source) {
        List<String> tokens = Arrays.asList(event.getMessage().split(" "));
        String command = tokens.get(0).substring(getCommandPrefix().length()).toUpperCase();
        List<String> args = tokens.subList(1, tokens.size());
        var user = event.getUser();

        log.info("Command {} triggered by {}, args: {}", command, user, args);

        switch (command) {
            case "IPLOOKUP":
                execBotCommand(new IpLookupCommand(event, source, args));
                break;

            case "JOIN":
                execBotCommand(new JoinCommand(event, source, args));
                break;

            case "MSG":
                execBotCommand(new MessageCommand(event, source, args));
                break;

            case "OP":
                execBotCommand(new OpCommand(event, source, args));
                break;

            case "PART":
                execBotCommand(new PartCommand(event, source, args));
                break;

            case "Q":
            case "STOCK":
                execBotCommand(new StockCommand(event, source, args));
                break;

            case "QUIT":
                execBotCommand(new QuitCommand(event, source, args));
                break;

            case "TEST":
                execBotCommand(new TestCommand(event, source, args));
                break;

            default:
                log.info("Unknown command {} from {}", command, event.getUser());
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
