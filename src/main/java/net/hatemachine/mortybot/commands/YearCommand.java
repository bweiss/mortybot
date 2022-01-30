package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.util.ProgressBar;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.time.LocalDate;
import java.util.List;

/**
 * Show our progress into the current year.
 *
 * Inspired by vect0rx and https://twitter.com/year_progress.
 */
public class YearCommand implements BotCommand {

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public YearCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        LocalDate now = LocalDate.now();
        ProgressBar bar = new ProgressBar(15, true);
        event.respondWith("Year progress: " + bar.show(now.getDayOfYear(), 365));
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
