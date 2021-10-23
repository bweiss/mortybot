package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.CommandListener;
import net.hatemachine.mortybot.MortyBot;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static net.hatemachine.mortybot.CommandListener.CommandSource.PUBLIC;

public class PartCommand implements BotCommand {

    private static final Logger log = LoggerFactory.getLogger(PartCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public PartCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        MortyBot bot = event.getBot();
        if (source == PUBLIC && args.isEmpty()) {
            String channel = ((MessageEvent) event).getChannel().getName();
            bot.sendRaw().rawLine("PART " + channel);
        } else if (!args.isEmpty()) {
            bot.sendRaw().rawLine("PART " + args.get(0));
        } else {
            log.debug("too few arguments for private command; missing channel");
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
