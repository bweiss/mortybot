package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.CommandListener;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;

public class TestCommand implements BotCommand {

    private final GenericMessageEvent event;
    private final CommandListener.MessageSource source;
    private final List<String> args;

    public TestCommand(GenericMessageEvent event, CommandListener.MessageSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        event.respondWith("It worked! args: " + args);
    }

    @Override
    public GenericMessageEvent getEvent() {
        return event;
    }

    @Override
    public CommandListener.MessageSource getSource() {
        return source;
    }

    @Override
    public List<String> getArgs() {
        return args;
    }
}
