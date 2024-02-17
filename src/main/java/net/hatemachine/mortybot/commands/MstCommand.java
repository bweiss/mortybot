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

import com.fasterxml.jackson.core.JsonProcessingException;
import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.Command;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.services.mst.MSTHelper;
import net.hatemachine.mortybot.services.mst.ShootingEvent;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@BotCommand(name = "MST", restricted = false, help = {
        "Retrieves data from Mass Shooting Tracker",
        "Usage: MST [-y <year>] [number]",
        "If number is given, will display that specific event with 0 being most recent, 1 being next most recent, and so on"
})
public class MstCommand implements Command {

    private static final Logger log = LoggerFactory.getLogger(MstCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public MstCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        LocalDate now = LocalDate.now();
        ArgumentParser parser = ArgumentParsers.newFor("MST").build();
        parser.addArgument("-y", "--year").type(Integer.class).setDefault(now.getYear());
        parser.addArgument("num").type(Integer.class).nargs("?").setDefault(0);
        Namespace ns;
        List<ShootingEvent> shootings;

        try {
            ns = parser.parseArgs(args.toArray(new String[0]));
        } catch (ArgumentParserException e) {
            log.error("Problem parsing command arguments", e);
            parser.handleError(e);
            throw new IllegalArgumentException("Problem parsing command");
        }

        if (ns != null) {
            int year = ns.getInt("year");
            int num = ns.getInt("num");

            MSTHelper helper = new MSTHelper();
            try {
                shootings = helper.shootingsByYear(year);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            if (shootings == null || shootings.isEmpty()) {
                event.respondWith("No results found");
            } else {
                ShootingEvent shootingEvent = shootings.get(num);
                event.respondWith(shootingEvent.toString());
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
