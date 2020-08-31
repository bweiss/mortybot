package net.hatemachine.mortybot;

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

public class MessageListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

    private enum Origin {
        PRIVATE,
        PUBLIC
    }

    public MessageListener() {
        this.commandPrefix = "!";
    }

    public MessageListener(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    @Override
    public void onMessage(final MessageEvent event) {
        if (event.getMessage().startsWith(getCommandPrefix())) {
            commandHandler(event, Origin.PUBLIC);
        } else {
            chatHandler(event);
        }
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent event) {
        if (event.getMessage().startsWith(getCommandPrefix())) {
            commandHandler(event, Origin.PRIVATE);
        } else {
            chatHandler(event);
        }
    }

    private void chatHandler (final GenericMessageEvent event) {
        if (event.getMessage().equalsIgnoreCase("Hello")) {
            event.respond("Hi there!");
        }
        else if (event.getMessage().equalsIgnoreCase("shut up morty")) {
            event.respondWith("Aww jeez, Rick!");
        }
    }

    private void commandHandler(GenericMessageEvent event, Origin origin) {

        MortyBot bot = event.getBot();
        User user = event.getUser();
        Optional<Channel> channel = Optional.of(((MessageEvent) event).getChannel());
        List<String> tokens = Arrays.asList(event.getMessage().split(" "));
        String command = tokens.get(0).substring(getCommandPrefix().length()).toUpperCase();
        List<String> args = tokens.subList(1, tokens.size());

        log.debug("Command {} triggered by {}, origin: {}, args: {}", command, user, origin, args);

        switch (command) {

            case "DEOP":
                if (origin == Origin.PUBLIC) {
                    bot.sendIRC().mode(channel.get().getName(), "-o " + (args.isEmpty() ? user.getNick() : args.get(0)));
                }
                break;

            case "JOIN":
                if (args.size() == 1) {
                    bot.sendIRC().joinChannel(args.get(0));
                } else if (args.size() > 1) {
                    // attempt to join with a key
                    bot.sendIRC().joinChannel(args.get(0), args.get(1));
                }
                break;

            case "MSG":
                if (args.size() > 1) {
                    String target = args.get(0);
                    String message = String.join(" ", args.subList(1, args.size()));
                    bot.sendIRC().message(target, message);
                    event.respondWith(String.format("-msg(%s) %s", target, message));
                }
                break;

            case "OP":
                if (origin == Origin.PUBLIC) {
                    bot.sendIRC().mode(channel.get().getName(), "+o " + (args.isEmpty() ? user.getNick() : args.get(0)));
                }
                break;

            case "PART":
                if (args.size() > 0) {
                    event.getBot().sendRaw().rawLine("PART " + args.get(0));
                } else if (origin == Origin.PUBLIC) {
                    event.getBot().sendRaw().rawLine("PART " + channel.get().getName());
                }
                break;

            case "QUIT":
                event.getBot().stopBotReconnect();
                if (args.size() > 0) {
                    event.getBot().sendIRC().quitServer(String.join(" ", args));
                } else {
                    event.getBot().sendIRC().quitServer();
                }
                break;

            case "TIME":
                String time = new java.util.Date().toString();
                event.respondWith("The time is now " + time);
                break;

            default:
                break;
        }
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    private String commandPrefix;
}
