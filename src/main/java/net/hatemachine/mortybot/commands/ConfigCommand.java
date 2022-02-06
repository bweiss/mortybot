package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.Command;
import net.hatemachine.mortybot.config.BotState;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;
import java.util.Properties;

public class ConfigCommand implements BotCommand {

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public ConfigCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        BotState state = BotState.getBotState();
        Properties props = state.getProperties();

        if (args.isEmpty()) {
            for (String line : Command.CONFIG.getHelp()) {
                event.respondWith(line);
            }
        }

        else if (args.size() == 1) {
            String propName = args.get(0);
            if (props.containsKey(propName)) {
                event.respondWith(propName + ": " + props.getProperty(propName));
            } else {
                event.respondWith("No such property");
            }
        }

        else {
            String propName = args.get(0);
            if (props.containsKey(propName)) {
                String newValue = String.join(" ", args.subList(1, args.size()));
                state.setStringProperty(propName, newValue);
                state.save();
                event.respondWith(propName + " -> " + newValue);
            } else {
                event.respondWith("No such property");
            }
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
