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
import java.util.stream.Collectors;

public class MessageListener extends ListenerAdapter
{
    private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

    public enum Origin {
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
        log.debug("onMessage triggered: {}", event);
        messageHandler(event, Origin.PUBLIC);
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent event) {
        log.debug("onPrivateMessage triggered: {}", event);
        messageHandler(event, Origin.PRIVATE);
    }

    private void messageHandler(final GenericMessageEvent event, Origin origin) {
        if (event.getMessage().startsWith(getCommandPrefix())) {
            commandHandler(event, origin);
        } else {
            chatHandler(event, origin);
        }
    }

    private void chatHandler (final GenericMessageEvent event, Origin origin) {
        if (event.getMessage().equalsIgnoreCase("Hello")) {
            event.respond("Hi there!");
        }
        else if (event.getMessage().equalsIgnoreCase("shut up morty")) {
            event.respondWith("Aww jeez, Rick!");
        }
    }

    private void commandHandler(final GenericMessageEvent event, Origin origin)
    {
        MortyBot bot = event.getBot();
        User user = event.getUser();
        String userhost = String.format("%s!%s@%s", user.getNick(), user.getLogin(), user.getHostname());
        Optional<Channel> channel = (origin.equals(Origin.PUBLIC) ? Optional.of(((MessageEvent) event).getChannel()) : Optional.empty());
        List<String> tokens = Arrays.asList(event.getMessage().split(" "));
        String command = tokens.get(0).substring(getCommandPrefix().length()).toUpperCase();
        List<String> args = tokens.subList(1, tokens.size());

        log.info("Command {} triggered by {}, args: {}", command, user, args);

        switch (command)
        {
            case "DEOP":
                if (origin == Origin.PUBLIC) {
                    bot.sendIRC().mode(channel.toString(), "-o " + (args.isEmpty() ? user.getNick() : args.get(0)));
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
                if (channel.isPresent()) {
                    bot.sendIRC().mode(channel.get().getName(), "+o " + (args.isEmpty() ? user.getNick() : args.get(0)));
                }
                break;

            case "PART":
                if (args.size() > 0) {
                    event.getBot().sendRaw().rawLine("PART " + args.get(0));
                } else if (channel.isPresent()) {
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

            case "TEST":
                doCommand(new TestCommand(event), args);
                break;

            case "TIME":
                String time = new java.util.Date().toString();
                event.respondWith("The time is now " + time);
                break;

            case "WHOAMI":
                String target = args.isEmpty() ? userhost : args.get(0);
                List<BotUser> users = bot.findBotUsersByUserhost(target);
                if (users.isEmpty()) {
                    event.respondWith("You did not match any bot users");
                } else {
                    event.respondWith(String.format("You match the following bot users: %s",
                            users.stream().map(BotUser::getName).collect(Collectors.joining(", "))));
                }
                break;

            default:
                log.info("Unknown command {} from {}", command, event.getUser());
                break;
        }
    }

    private void doCommand(final BotCommand command, List<String> args) {
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
