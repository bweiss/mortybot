package net.hatemachine.mortybot;

import net.hatemachine.mortybot.commands.IpLookupCommand;
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
import java.util.Optional;

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
            commandHandler(event, MessageSource.PUBLIC);
        }
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent event) {
        log.debug("onPrivateMessage event: {}", event);
        if (event.getMessage().startsWith(getCommandPrefix())) {
            commandHandler(event, MessageSource.PRIVATE);
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
    private void commandHandler(final GenericMessageEvent event, MessageSource source) {
        List<String> tokens = Arrays.asList(event.getMessage().split(" "));
        String command = tokens.get(0).substring(getCommandPrefix().length()).toUpperCase();
        List<String> args = tokens.subList(1, tokens.size());
        Optional<Channel> channel = (source.equals(MessageSource.PUBLIC) ? Optional.of(((MessageEvent) event).getChannel()) : Optional.empty());

        log.debug("Command {} triggered by {}, args: {}", command, event.getUser(), args);

        switch (command) {
            case "DEOP" -> deopCommand(source, event.getBot(), event.getUser(), channel, args);
            case "IPLOOKUP" -> runBotCommand(new IpLookupCommand(event, args));
            case "JOIN" -> joinCommand(event.getBot(), args);
            case "MSG" -> msgCommand(event, event.getBot(), args);
            case "OP" -> opCommand(event.getBot(), event.getUser(), channel, args);
            case "PART" -> partCommand(event, channel, args);
            case "QUIT" -> quitCommand(event, args);
            case "TEST" -> runBotCommand(new TestCommand(event, args));
            default -> log.info("Unknown command {} from {}", command, event.getUser());
        }
    }

    private void deopCommand(MessageSource source, MortyBot bot, User user, Optional<Channel> channel, List<String> args) {
        if (source == MessageSource.PUBLIC) {
            bot.sendIRC().mode(channel.toString(), "-o " + (args.isEmpty() ? user.getNick() : args.get(0)));
        }
    }

    private void joinCommand(MortyBot bot, List<String> args) {
        if (args.size() == 1) {
            bot.sendIRC().joinChannel(args.get(0));
        } else if (args.size() > 1) {
            // attempt to join with a key
            bot.sendIRC().joinChannel(args.get(0), args.get(1));
        }
    }

    private void msgCommand(GenericMessageEvent event, MortyBot bot, List<String> args) {
        if (args.size() > 1) {
            String target = args.get(0);
            String message = String.join(" ", args.subList(1, args.size()));
            bot.sendIRC().message(target, message);
            event.respondWith(String.format("-msg(%s) %s", target, message));
        }
    }

    private void opCommand(MortyBot bot, User user, Optional<Channel> channel, List<String> args) {
        // TODO: rewrite me! need to clearly define the parameters and behavior for this command
        channel.ifPresent(value -> bot.sendIRC().mode(value.getName(), "+o " + (args.isEmpty() ? user.getNick() : args.get(0))));
    }

    private void partCommand(GenericMessageEvent event, Optional<Channel> channel, List<String> args) {
        if (args.isEmpty()) {
            channel.ifPresent(value -> event.getBot().sendRaw().rawLine("PART " + value.getName()));
        } else {
            event.getBot().sendRaw().rawLine("PART " + args.get(0));
        }
    }

    private void quitCommand(GenericMessageEvent event, List<String> args) {
        MortyBot bot = event.getBot();
        bot.stopBotReconnect();
        bot.sendIRC().quitServer(args.isEmpty() ? "" : String.join(" ", args));
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
