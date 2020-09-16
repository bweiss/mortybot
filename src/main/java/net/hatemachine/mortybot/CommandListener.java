package net.hatemachine.mortybot;

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

import static net.hatemachine.mortybot.CommandListener.Source.*;

public class CommandListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(CommandListener.class);

    public enum Source {
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
        log.debug("onMessage triggered: {}", event);
        if (event.getMessage().startsWith(getCommandPrefix())) {
            commandHandler(event, PUBLIC);
        }
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent event) {
        log.debug("onPrivateMessage triggered: {}", event);
        if (event.getMessage().startsWith(getCommandPrefix())) {
            commandHandler(event, PRIVATE);
        }
    }

    /**
     *
     * @param event
     * @param source
     */
    private void commandHandler(final GenericMessageEvent event, Source source) {

        MortyBot bot = event.getBot();
        User user = event.getUser();
        Optional<Channel> channel = (source.equals(PUBLIC) ? Optional.of(((MessageEvent) event).getChannel()) : Optional.empty());
        List<String> tokens = Arrays.asList(event.getMessage().split(" "));
        String command = tokens.get(0).substring(getCommandPrefix().length()).toUpperCase();
        List<String> args = tokens.subList(1, tokens.size());

        log.info("Command {} triggered by {}, args: {}", command, user, args);

        switch (command) {
            case "DEOP" -> deopCommand(source, bot, user, channel, args);
            case "JOIN" -> joinCommand(bot, args);
            case "MSG" -> msgCommand(event, bot, args);
            case "OP" -> opCommand(bot, user, channel, args);
            case "PART" -> partCommand(event, channel, args);
            case "QUIT" -> quitCommand(bot, args);
            case "TEST" -> runCommand(new TestCommand(event), args);
            default -> log.info("Unknown command {} from {}", command, event.getUser());
        }
    }

    private void deopCommand(Source source, MortyBot bot, User user, Optional<Channel> channel, List<String> args) {
        if (source == PUBLIC) {
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
        channel.ifPresent(value -> bot.sendIRC().mode(value.getName(), "+o " + (args.isEmpty() ? user.getNick() : args.get(0))));
    }

    private void partCommand(GenericMessageEvent event, Optional<Channel> channel, List<String> args) {
        if (args.isEmpty()) {
            channel.ifPresent(value -> event.getBot().sendRaw().rawLine("PART " + value.getName()));
        } else {
            event.getBot().sendRaw().rawLine("PART " + args.get(0));
        }
    }

    private void quitCommand(MortyBot bot, List<String> args) {
        bot.stopBotReconnect();
        bot.sendIRC().quitServer(args.isEmpty() ? "" : String.join(" ", args));
    }

    private void runCommand(final BotCommand command, List<String> args) {
        command.execute(args);
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    private String commandPrefix;
}
