package net.hatemachine.mortybot;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;

import static org.pircbotx.Utils.tokenizeLine;

public class MessageListener extends ListenerAdapter {

    public MessageListener() {
        this.commandPrefix = "!";
    }

    public MessageListener(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    @Override
    public void onMessage(final MessageEvent event) {

        if (event.getMessage().startsWith(getCommandPrefix())) {
            commandHandler(event.getChannel(), event.getUser(), event);
        }

        if (event.getMessage().equalsIgnoreCase("Hello")) {
            event.respond("Hi there!");
        }
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent event) {

        if (event.getMessage().startsWith(getCommandPrefix())) {
            commandHandler(null, event.getUser(), event);
        }
    }

    private void commandHandler(final Channel channel, final User user, final GenericMessageEvent event) {

        List<String> tokens = tokenizeLine(event.getMessage());
        String command = tokens.get(0).substring(getCommandPrefix().length());
        List<String> args = tokens.subList(1, tokens.size());

        switch (command.toUpperCase()) {

            case "DEOP":
                if (args.size() > 0) {
                    event.getBot().sendIRC().mode(channel.getName(), "-o " + args.get(0));
                } else {
                    event.getBot().sendIRC().mode(channel.getName(), "-o " + user.getNick());
                }
                break;

            case "JOIN":
                if (args.size() > 1) {
                    event.getBot().sendIRC().joinChannel(args.get(0), args.get(1));
                } else if (args.size() == 1) {
                    event.getBot().sendIRC().joinChannel(args.get(0));
                }
                break;

            case "MSG":
                if (args.size() > 1) {
                    String target = args.get(0);
                    String message = String.join(" ", args.subList(1, args.size()));
                    event.getBot().sendIRC().message(target, message);
                    event.respondWith(String.format("-msg(%s) %s", target, message));
                }
                break;

            case "OP":
                if (args.size() > 0) {
                    event.getBot().sendIRC().mode(channel.getName(), "+o " + args.get(0));
                } else {
                    event.getBot().sendIRC().mode(channel.getName(), "+o " + user.getNick());
                }
                break;

            case "PART":
                if (args.size() > 0) {
                    event.getBot().sendRaw().rawLine("PART " + args.get(0));
                } else {
                    event.getBot().sendRaw().rawLine("PART " + channel.getName());
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
                event.respondWith("command: " + command);
                event.respondWith("args: " + args.toString());
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
