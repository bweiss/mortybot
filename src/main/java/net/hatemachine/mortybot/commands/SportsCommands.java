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

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.Command;
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.services.espn.EspnClient;
import net.hatemachine.mortybot.services.espn.SportsLeague;
import net.hatemachine.mortybot.services.espn.model.Competition;
import net.hatemachine.mortybot.services.espn.model.Competitor;
import net.hatemachine.mortybot.services.espn.model.Event;
import net.hatemachine.mortybot.services.espn.model.Scoreboard;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@BotCommand(name = "CBB", help = {
        "Shows men's college basketball scores",
        "Usage: CFB [-a] [team] ...",
        "If -a flag is present, all recent games will be shown (default is active only)"
})
@BotCommand(name = "CFB", help = {
        "Shows college football scores",
        "Usage: CFB [-a] [team] ...",
        "If -a flag is present, all recent games will be shown (default is active only)"
})
@BotCommand(name = "MLB", help = {
        "Shows MLB scores",
        "Usage: MLB [-a] [team] ...",
        "If -a flag is present, all recent games will be shown (default is active only)"
})
@BotCommand(name = "NBA", help = {
        "Shows NBA scores",
        "Usage: NBA [-a] [team] ...",
        "If -a flag is present, all recent games will be shown (default is active only)"
})
@BotCommand(name = "NFL", help = {
        "Shows NFL scores",
        "Usage: NFL [-a] [team] ...",
        "If -a flag is present, all recent games will be shown (default is active only)"
})
@BotCommand(name = "NHL", help = {
        "Shows NHL scores",
        "Usage: NHL [-a] [team] ...",
        "If -a flag is present, all recent games will be shown (default is active only)"
})
@BotCommand(name = "UFC", help = {
        "Shows UFC events",
        "Usage: UFC"
})
public class SportsCommands implements Command {

    private static final Logger log = LoggerFactory.getLogger(SportsCommands.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public SportsCommands(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        // Determine the actual command
        BotProperties props = BotProperties.getBotProperties();
        String cmdPrefix = props.getStringProperty("bot.command.prefix", BotDefaults.BOT_COMMAND_PREFIX);
        String cmdStr = event.getMessage()
                .split(" ")[0]
                .substring(cmdPrefix.length());

        // Determine the league and request the scoreboard
        SportsLeague league = Enum.valueOf(SportsLeague.class, cmdStr.toUpperCase());
        EspnClient espnClient = new EspnClient();
        Optional<Scoreboard> scoreboard = espnClient.scoreboard(league);

        if (scoreboard.isPresent()) {
            if (league == SportsLeague.UFC) {
                showIndividualEventScoreboard(scoreboard.get());
            } else {
                showTeamEventScoreboard(scoreboard.get());
            }
        } else {
            log.warn("Did not get a scoreboard result! league: {}", league);
            event.respondWith("Failed to get scoreboard!");
        }
    }

    private void showTeamEventScoreboard(Scoreboard scoreboard) {
        List<Event> sportingEvents = scoreboard.events();
        boolean recentFlag = true; // this limits us to active scoreboard events
        List<String> newArgs = args;

        if (!args.isEmpty() && args.getFirst().equalsIgnoreCase("-a")) {
            // show everything
            recentFlag = false;
            newArgs = args.subList(1, args.size());
        }

        // Apply any filters we have
        if (!newArgs.isEmpty()) {
            final List<String> myArgs = newArgs;
            sportingEvents = sportingEvents.stream()
                    .filter(evt -> {
                        for (String newArg : myArgs) {
                            if (evt.shortName().contains(newArg.toUpperCase())) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .toList();
        } else if (recentFlag) {
            sportingEvents = sportingEvents.stream()
                    .filter(evt -> evt.competitions().getFirst().recent())
                    .toList();
        }

        // Respond to the user
        if (!sportingEvents.isEmpty()) {
            for (Event sportingEvent : sportingEvents) {
                event.respondWith(formatTeamEvent(sportingEvent));
            }
        } else {
            if (recentFlag || !newArgs.isEmpty()) {
                event.respondWith("No events found. Try passing the -a flag to see all scoreboard events.");
            } else {
                event.respondWith("No events found");
            }
        }
    }

    private void showIndividualEventScoreboard(Scoreboard scoreboard) {
        List<Event> sportingEvents = scoreboard.events();

        if (!sportingEvents.isEmpty()) {
            for (Event sEvent : sportingEvents) {
                event.respondWith(formatIndividualEvent(sEvent));
            }
        } else {
            event.respondWith("No recent events found");
        }
    }

    private List<Event> findEventsByTeamAbbreviation(List<Event> sportingEvents, List<String> abbrList) {
        List<Event> foundEvents = new ArrayList<>();

        for (Event sEvent : sportingEvents) {
            boolean addFlag = false;
            List<Competitor> competitors = sEvent.competitions().getFirst().competitors();

            for (Competitor c : competitors) {
                if (abbrList.contains(c.team().abbreviation())) {
                    addFlag = true;
                }
            }

            if (addFlag) {
                foundEvents.add(sEvent);
            }
        }

        return foundEvents;
    }

    private String formatTeamEvent(Event sportingEvent) {
        Competition competition = sportingEvent.competitions().getFirst();
        List<Competitor> competitors = competition.competitors();

        Optional<Competitor> awayCompetitor = competitors.stream()
                .filter(c -> c.homeAway().equals("away"))
                .findFirst();

        Optional<Competitor> homeCompetitor = competitors.stream()
                .filter(c -> c.homeAway().equals("home"))
                .findFirst();

        if (awayCompetitor.isPresent() && homeCompetitor.isPresent()) {
            Competitor away = awayCompetitor.get();
            Competitor home = homeCompetitor.get();

            return String.format("%4s @ %-4s", away.team().abbreviation(), home.team().abbreviation()) +
                    " | " +
                    String.format("%3s - %-3s", away.score(), home.score()) +
                    " | " +
                    competition.status().type().detail();

        } else {
            String errMsg = "Couldn't figure out home and away teams for event: " + sportingEvent.shortName();
            log.warn(errMsg);
            throw new RuntimeException(errMsg);
        }
    }

    private String formatIndividualEvent(Event sportingEvent) {
        return String.format("%s - %s", sportingEvent.name(), sportingEvent.status().type().detail());
    }

    private String formatIndividualCompetition(Competition competition) {
        List<Competitor> competitors = competition.competitors();

        if (competitors.size() == 2) {
            String compStatus = competition.status().type().name();

            if (compStatus.equals("STATUS_FINAL")) {
                return String.format("%s (%s) vs %s (%s) - %s",
                        competitors.get(0).athlete().fullName(),
                        competitors.get(0).winner() ? "Won" : "Lost",
                        competitors.get(1).athlete().fullName(),
                        competitors.get(1).winner() ? "Won" : "Lost",
                        competition.status().type().detail());
            } else {
                return String.format("%s vs %s - %s",
                        competitors.get(0).athlete().fullName(),
                        competitors.get(1).athlete().fullName(),
                        competition.status().type().detail());
            }
        } else {
            String errMsg = "Wrong number of competitors: " + competitors.size() + " (expected: 2)";
            log.warn(errMsg);
            throw new RuntimeException(errMsg);
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
