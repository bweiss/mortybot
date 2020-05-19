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
import java.util.Objects;

public class MessageListener extends ListenerAdapter
{
    private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

    public MessageListener() {
        this.commandPrefix = "!";
    }

    public MessageListener(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    @Override
    public void onMessage(final MessageEvent event)
    {
        if (event.getMessage().startsWith(getCommandPrefix())) {
            commandHandler(event.getChannel(), Objects.requireNonNull(event.getUser()), event);
        } else {
            chatHandler(event.getChannel(), Objects.requireNonNull(event.getUser()), event);
        }
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent event)
    {
        if (event.getMessage().startsWith(getCommandPrefix())) {
            commandHandler(null, Objects.requireNonNull(event.getUser()), event);
        } else {
            chatHandler(null, Objects.requireNonNull(event.getUser()), event);
        }
    }

    private void chatHandler (final Channel channel, final User user, final GenericMessageEvent event)
    {
        if (event.getMessage().equalsIgnoreCase("Hello")) {
            event.respond("Hi there!");
        }
        else if (event.getMessage().equalsIgnoreCase("shut up morty")) {
            event.respondWith("Aww jeez, Rick!");
        }
    }

    private void commandHandler(final Channel channel, final User user, final GenericMessageEvent event)
    {
        List<String> tokens = Arrays.asList(event.getMessage().split(" "));
        String command = tokens.get(0).substring(getCommandPrefix().length());
        List<String> args = tokens.subList(1, tokens.size());
        boolean adminFlag = MortyBot.isAdmin(user.getNick());

        switch (command.toUpperCase())
        {
            case "DEOP":
                if (adminFlag && channel != null) {
                    if (args.size() > 0) {
                        event.getBot().sendIRC().mode(channel.getName(), "-o " + args.get(0));
                    } else {
                        event.getBot().sendIRC().mode(channel.getName(), "-o " + user.getNick());
                    }
                }
                break;

            case "JOIN":
                if (adminFlag) {
                    if (args.size() > 1) {
                        event.getBot().sendIRC().joinChannel(args.get(0), args.get(1));
                    } else if (args.size() == 1) {
                        event.getBot().sendIRC().joinChannel(args.get(0));
                    }
                }
                break;

            case "MSG":
                if (adminFlag) {
                    if (args.size() > 1) {
                        String target = args.get(0);
                        String message = String.join(" ", args.subList(1, args.size()));
                        event.getBot().sendIRC().message(target, message);
                        event.respondWith(String.format("-msg(%s) %s", target, message));
                    }
                }
                break;

            case "OP":
                if (adminFlag && channel != null) {
                    if (args.size() > 0) {
                        event.getBot().sendIRC().mode(channel.getName(), "+o " + args.get(0));
                    } else {
                        event.getBot().sendIRC().mode(channel.getName(), "+o " + user.getNick());
                    }
                }
                break;

            case "PART":
                if (adminFlag) {
                    if (args.size() > 0) {
                        event.getBot().sendRaw().rawLine("PART " + args.get(0));
                    } else if (channel != null) {
                        event.getBot().sendRaw().rawLine("PART " + channel.getName());
                    }
                }
                break;

            case "QUIT":
                if (adminFlag) {
                    event.getBot().stopBotReconnect();
                    if (args.size() > 0) {
                        event.getBot().sendIRC().quitServer(String.join(" ", args));
                    } else {
                        event.getBot().sendIRC().quitServer();
                    }
                }
                break;

            case "TEST":
                if (adminFlag) {
                    event.respondWith("command: " + command);
                    event.respondWith("args: " + args.toString());
                }
                break;

            case "TIME":
                String time = new java.util.Date().toString();
                event.respondWith("The time is now " + time);
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
