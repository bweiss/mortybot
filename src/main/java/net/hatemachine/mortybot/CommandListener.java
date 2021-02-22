package net.hatemachine.mortybot;

import net.hatemachine.mortybot.commands.IpLookupCommand;
import net.hatemachine.mortybot.commands.StockCommand;
import net.hatemachine.mortybot.commands.TestCommand;
import org.pircbotx.Channel;
import org.pircbotx.User;
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

    private String commandPrefix;

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
     * and the runBotCommand method (see TestCommand for an example).
     *
     * @param event the event that contained a command
     * @param source the source of the command, public or private message
     */
    private void handleCommand(final GenericMessageEvent event, MessageSource source) {
        List<String> tokens = Arrays.asList(event.getMessage().split(" "));
        String command = tokens.get(0).substring(getCommandPrefix().length()).toUpperCase();
        List<String> args = tokens.subList(1, tokens.size());
        User user = event.getUser();

        log.debug("Command {} triggered by {}, args: {}", command, user, args);

        switch (command) {
            case "DEOP":
                deopCommand(event, source, args);
                break;

            case "IPLOOKUP":
                runBotCommand((BotCommand)BotCommandProxy.newInstance(new IpLookupCommand(event, args)));
                break;

            case "JOIN":
                joinCommand(event, args);
                break;

            case "MSG":
                msgCommand(event, args);
                break;

            case "OP":
                opCommand(event, source, args);
                break;

            case "PART":
                partCommand(event, source, args);
                break;

            case "Q":
            case "STOCK":
                runBotCommand((BotCommand)BotCommandProxy.newInstance(new StockCommand(event, args)));
                break;

            case "QUIT":
                quitCommand(event, args);
                break;

            case "TEST":
                runBotCommand((BotCommand)BotCommandProxy.newInstance(new TestCommand(event, args)));
                break;

            default:
                log.info("Unknown command {} from {}", command, event.getUser());
        }
    }

    private void deopCommand(final GenericMessageEvent event, MessageSource source, List<String> args) {
        MortyBot bot = event.getBot();
        User user = event.getUser();
        // todo implement for privmsg
        if (source == PUBLIC && bot.authorizeRick(user)) {
            Channel channel = ((MessageEvent) event).getChannel();
            bot.sendIRC().mode(channel.toString(), "-o " + (args.isEmpty() ? user.getNick() : args.get(0)));
        }
    }

    private void joinCommand(final GenericMessageEvent event, List<String> args) {
        MortyBot bot = event.getBot();
        if (bot.authorizeRick(event.getUser())) {
            if (args.size() == 1) {
                bot.sendIRC().joinChannel(args.get(0));
            } else if (args.size() > 1) {
                // attempt to join with a key
                bot.sendIRC().joinChannel(args.get(0), args.get(1));
            }
        }
    }

    private void msgCommand(GenericMessageEvent event, List<String> args) {
        MortyBot bot = event.getBot();
        if (bot.authorizeRick(event.getUser()) && args.size() > 1) {
            String target = args.get(0);
            String message = String.join(" ", args.subList(1, args.size()));
            bot.sendIRC().message(target, message);
            event.respondWith(String.format("-msg(%s) %s", target, message));
        }
    }

    private void opCommand(final GenericMessageEvent event, MessageSource source, List<String> args) {
        MortyBot bot = event.getBot();
        User user = event.getUser();
        // todo implement for privmsg
        if (source == PUBLIC && bot.authorizeRick(user)) {
            Channel channel = ((MessageEvent) event).getChannel();
            bot.sendIRC().mode(channel.toString(), "+o " + (args.isEmpty() ? user.getNick() : args.get(0)));
        }
    }

    private void partCommand(final GenericMessageEvent event, MessageSource source, List<String> args) {
        MortyBot bot = event.getBot();
        // todo implement for privmsg
        if (source == PUBLIC && bot.authorizeRick(event.getUser())) {
            if (args.isEmpty()) {
                bot.sendRaw().rawLine("PART " + ((MessageEvent)event).getChannel());
            } else {
                bot.sendRaw().rawLine("PART " + args.get(0));
            }
        }
    }

    private void quitCommand(GenericMessageEvent event, List<String> args) {
        MortyBot bot = event.getBot();
        if (bot.authorizeRick(event.getUser())) {
            bot.stopBotReconnect();
            bot.sendIRC().quitServer(args.isEmpty() ? "" : String.join(" ", args));
        }
    }

    /**
     * Run a bot command implemented with the BotCommand interface.
     *
     * @param command instance of the command you want to run
     */
    private void runBotCommand(final BotCommand command) {
        command.execute();
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }
}
