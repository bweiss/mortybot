package net.hatemachine.mortybot;

import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;

public interface BotCommand {
    void execute();
    GenericMessageEvent getEvent();
    List<String> getArgs();
}
