package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;

public class TestCommand implements BotCommand {

    private final GenericMessageEvent event;
    private final List<String> args;

    public TestCommand(GenericMessageEvent event, List<String> args) {
        this.event = event;
        this.args = args;
    }

    @Override
    public void execute() {
        event.respondWith("It worked! args: " + args);
    }
}
