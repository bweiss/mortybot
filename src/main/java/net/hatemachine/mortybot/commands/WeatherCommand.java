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

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.Command;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.repositories.BotUserRepository;
import net.hatemachine.mortybot.util.WebClient;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Implements the WEATHER command, allowing users to look up the current weather for a location.
 */
@BotCommand(name = "WEATHER", help = {
        "Shows the weather for a location",
        "Usage: WEATHER [-d] [location]",
        "If the -d option is present the bot will attempt to save your default location (requires being registered with the bot)"
})
@BotCommand(name = "WTR", help = {
        "Shows the weather for a location",
        "Usage: WTR [-d] [location]",
        "If the -d option is present the bot will attempt to save your default location (requires being registered with the bot)"
})
public class WeatherCommand implements Command {

    private static final String BASE_URL = "https://wttr.in/";
    private static final String PARAMETERS = "?format=j1";

    private static final Logger log = LoggerFactory.getLogger(WeatherCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public WeatherCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        ArgumentParser parser = ArgumentParsers.newFor("WEATHER").build();
        parser.addArgument("-d", "--default").action(Arguments.storeTrue());
        parser.addArgument("location").nargs("*");

        try {
            var ns = parser.parseArgs(args.toArray(new String[0]));
            var defaultFlag = ns.getBoolean("default");
            var location = String.join(" ", ns.getList("location"));

            var botUserRepository = new BotUserRepository();
            var optionalBotUser = botUserRepository.findByHostmask(event.getUser().getHostmask()).stream().findFirst();

            // If user passes the -d option, attempt to set their default location
            if (defaultFlag.equals(Boolean.TRUE)) {
                if (location.isBlank()) {
                    throw new IllegalArgumentException("location not provided");
                } else if (optionalBotUser.isEmpty()) {
                    event.respondWith("You must register first");
                } else {
                    var botUser = optionalBotUser.get();
                    botUser.setLocation(location);
                    botUserRepository.save(botUser);
                    event.respondWith("Set default location to: " + location);
                }
            }

            // If still no location provided, and we know the user, try to pull it from their default
            if (location.isBlank() && optionalBotUser.isPresent()) {
                location = optionalBotUser.get().getLocation();
            }

            // One last check
            if (location == null || location.isBlank()) {
                throw new IllegalArgumentException("location not provided");
            }

            // Fetch and parse the data, then respond to the user
            var webClient = new WebClient();
            Optional<String> json = webClient.get(BASE_URL + location.replace(" ", "+") + PARAMETERS);

            if (json.isPresent()) {
                event.respondWith(parseWeatherJson(json.get()));
            } else {
                event.respondWith("No data received");
            }

        } catch (ArgumentParserException e) {
            log.error("Failed to parse args: {}", args, e);
            event.respondWith("Something went wrong");
        }
    }

    private String parseWeatherJson(String json) {
        Configuration conf = Configuration.defaultConfiguration();
        DocumentContext parsedJson = JsonPath.using(conf).parse(json);
        Integer humidity = Integer.parseInt(parsedJson.read("$.current_condition[0].humidity"));
        Integer tempC = Integer.parseInt(parsedJson.read("$.current_condition[0].temp_C"));
        Integer tempF = Integer.parseInt(parsedJson.read("$.current_condition[0].temp_F"));
        String weatherDesc = parsedJson.read("$.current_condition[0].weatherDesc[0].value");
        String areaName = parsedJson.read("$.nearest_area[0].areaName[0].value");
        String region = parsedJson.read("$.nearest_area[0].region[0].value");
        String windDir16Point = parsedJson.read("$.current_condition[0].winddir16Point");
        Integer windspeedMiles = Integer.parseInt(parsedJson.read("$.current_condition[0].windspeedMiles"));

        return String.format("[wtr] %s, %s (%d°F/%d°C/%s) [H:%d W:%dmph/%s]", areaName, region, tempF, tempC, weatherDesc, humidity, windspeedMiles, windDir16Point);
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
