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
import net.hatemachine.mortybot.dao.BotUserDao;
import net.hatemachine.mortybot.exception.CommandException;
import net.hatemachine.mortybot.helpers.BotUserHelper;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.model.BotUser;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

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
    private BotUser botUser;

    public WeatherCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;

        User user = event.getUser();
        BotUserHelper botUserHelper = new BotUserHelper();
        List<BotUser> matchingBotUsers = botUserHelper.findByHostmask(user.getHostmask());

        if (!matchingBotUsers.isEmpty()) {
            botUser = matchingBotUsers.get(0);
        }
    }

    @Override
    public void execute() {
        ArgumentParser parser = ArgumentParsers.newFor("WEATHER").build();
        parser.addArgument("-d", "--default").action(Arguments.storeTrue());
        parser.addArgument("location").nargs("*");
        Namespace ns;

        try {
            ns = parser.parseArgs(args.toArray(new String[0]));
        } catch (ArgumentParserException e) {
            log.error("Problem parsing command arguments", e);
            parser.handleError(e);
            throw new CommandException(CommandException.Reason.INVALID_ARGS, "Problem parsing command");
        }

        if (ns != null) {
            boolean defaultFlag = ns.getBoolean("default");
            String location = String.join(" ", ns.getList("location"));

            // If user passes the -d option, attempt to set their default location
            if (defaultFlag) {
                if (location.isBlank()) {
                    throw new CommandException(CommandException.Reason.INVALID_ARGS, "location not provided");
                } else if (botUser == null) {
                    throw new CommandException(CommandException.Reason.UNAUTHORIZED_USER, "user not registered");
                } else {
                    setDefaultLocation(location);
                }
            }

            // If still no location provided, and we know the user, try to pull it from their default
            if (location.isBlank() && botUser != null) {
                location = botUser.getLocation();
            }

            // one last check
            if (location == null || location.isBlank()) {
                throw new CommandException(CommandException.Reason.INVALID_ARGS, "location not provided");
            }

            try {
                URL url = new URL(BASE_URL + String.join("+", location.split(" ")) + PARAMETERS);

                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                HttpRequest request = HttpRequest.newBuilder(url.toURI())
                        .header("User-Agent", "Java HttpClient Bot")
                        .GET()
                        .build();

                HttpResponse<String> response;

                response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response != null && response.statusCode() == 200) {
                    event.respondWith(parseWeatherJson(response.body()));
                }
            } catch (MalformedURLException e) {
                log.error("Invalid URL", e);
            } catch (URISyntaxException e) {
                log.error("Invalid URI", e);
            } catch (IOException e) {
                log.error("Error fetching body", e);
            } catch (InterruptedException e) {
                log.warn("Thread interrupted", e);
                Thread.currentThread().interrupt();
            }
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

    private void setDefaultLocation(String loc) {
        if (botUser != null) {
            BotUserDao botUserDao = new BotUserDao();
            botUser.setLocation(loc);
            botUserDao.update(botUser);
            event.respondWith("Set default location to: " + loc);
        } else {
            log.warn("Unable to set default location, botUser is null");
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
