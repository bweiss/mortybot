package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.CommandListener;
import net.hatemachine.mortybot.MortyBot;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;

public class QuitCommand implements BotCommand {

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public QuitCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        MortyBot bot = event.getBot();
        bot.stopBotReconnect();
        bot.sendIRC().quitServer(args.isEmpty() ? "" : String.join(" ", args));
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
