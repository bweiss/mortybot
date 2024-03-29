/*
 * MortyBot - An IRC bot built on the PircBotX framework.
 * Copyright © 2022 Brian Weiss (brian@hatemachine.net)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.Command;
import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.util.ProgressBar;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.time.LocalDate;
import java.util.List;

/**
 * Implements the YEAR command. This displays a progress bar representing the progress into the current year.
 *
 * Inspired by vect0rx and <a href="https://twitter.com/year_progress">@year_progress</a>.
 */
@BotCommand(name = "YEAR", help = {
        "Shows progress through the current year",
        "Usage: YEAR"
})
public class YearCommand implements Command {

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
        event.respondWith("Year progress: " + bar.show(now.getDayOfYear(), now.lengthOfYear()));
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
