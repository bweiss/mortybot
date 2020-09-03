package net.hatemachine.mortybot;

import java.util.List;

public interface BotCommand {
    void execute(List<String> args);
}
