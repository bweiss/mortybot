package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.MortyBot;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;

public class JoinCommand implements BotCommand {

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public JoinCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        MortyBot bot = event.getBot();
        if (args.size() == 1) {
            bot.sendIRC().joinChannel(args.get(0));
        } else if (args.size() > 1) {
            // attempt to join with a key
            bot.sendIRC().joinChannel(args.get(0), args.get(1));
        }
    }

    @Override
    public GenericMessageEvent getEvent() {
        return event;
    }

    @Override
    public CommandListener.CommandSource getSource() {
        return source;
    }

    @Override
    public List<String> getArgs() {
        return args;
    }
}
