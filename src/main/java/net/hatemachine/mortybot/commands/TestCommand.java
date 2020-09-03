package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;

public class TestCommand implements BotCommand {

    private final GenericMessageEvent event;

    public TestCommand(final GenericMessageEvent event) {
        this.event = event;
    }

    @Override
    public void execute(List<String> args) {
        event.respondWith("It worked! args: " + args);
    }
}
