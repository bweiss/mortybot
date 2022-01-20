package net.hatemachine.mortybot;

import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface BotCommand {
    void execute();
    GenericMessageEvent getEvent();
    CommandListener.CommandSource getSource();
    List<String> getArgs();

    default String getName() {
        return this.getClass().getSimpleName();
    }

    default boolean isEnabled() {
        List<String> enabled = new ArrayList<>(Arrays.asList(MortyBot.getStringProperty("CommandListener.enabled.commands").split(",")));
        return (enabled.contains(this.getClass().getSimpleName()));
    }
}
