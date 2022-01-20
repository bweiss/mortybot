package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.Command;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HelpCommand implements BotCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelpCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public HelpCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        if (args.isEmpty()) {
            respondWithAllCommands();
        } else {
            respondWithCommandHelp();
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

    private void respondWithAllCommands() {
        event.respondWith("Commands: " + Arrays.stream(Command.values())
                .filter(c -> {
                    try {
                        BotCommand cmdInstance = (BotCommand) c.getBotCommandClass()
                                .getDeclaredConstructor(GenericMessageEvent.class, CommandListener.CommandSource.class, List.class)
                                .newInstance(event, source, args);
                        return (cmdInstance.isEnabled());
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        LOGGER.error("Exception encountered showing all commands", e);
                    }
                    return false;
                })
                .map(Command::toString)
                .collect(Collectors.joining(", ")));
    }

    private void respondWithCommandHelp() {
        String commandStr = args.get(0).toUpperCase(Locale.ROOT);
        try {
            Command command = Enum.valueOf(Command.class, commandStr);
            BotCommand botCommand = (BotCommand) command.getBotCommandClass()
                    .getDeclaredConstructor(GenericMessageEvent.class, CommandListener.CommandSource.class, List.class)
                    .newInstance(event, source, args);
            if (botCommand.isEnabled()) {
                for (String line : command.getHelp()) {
                    event.respondWith(line);
                }
            } else {
                LOGGER.warn("Command not enabled: {}", commandStr);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid command {}", commandStr);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOGGER.error("Exception encountered showing help for command", e);
        }
    }
}
