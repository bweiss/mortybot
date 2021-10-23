package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.CommandListener;
import net.hatemachine.mortybot.MortyBot;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;

public class MessageCommand implements BotCommand {

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public MessageCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        MortyBot bot = event.getBot();
        if (args.size() > 1) {
            String target = args.get(0);
            var message = String.join(" ", args.subList(1, args.size()));
            bot.sendIRC().message(target, message);
            event.respondWith(String.format("-msg(%s) %s", target, message));
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
