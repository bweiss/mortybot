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
import net.hatemachine.mortybot.BotUserDao;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.exception.BotUserException;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.model.BotUser;
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

public class WeatherCommand implements BotCommand {

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
        MortyBot bot = event.getBot();
        User user = event.getUser();
        BotUserDao botUserDao = bot.getBotUserDao();
        List<BotUser> matchingBotUsers = botUserDao.getAll(user.getHostmask());
        if (!matchingBotUsers.isEmpty()) {
            botUser = matchingBotUsers.get(0);
        }
    }

    @Override
    public void execute() {
        // If user passes the -d option, attempt to set their default location
        if (!args.isEmpty() && args.get(0).equals("-d")) {
            if (botUser == null) {
                event.respondWith("You are not registered with the bot. Please register with the REGISTER command.");
            } else if (args.size() < 2) {
                event.respondWith("Could not set default location (not enough arguments)");
            } else {
                List<String> newArgs = args.subList(1, args.size());
                setDefaultLocation(String.join(" ", newArgs));
            }
        }

        try {
            String location = "";
            if (args.isEmpty() && botUser != null) {
                location = String.join("+", botUser.getLocation().split(" "));
            } else if (!args.isEmpty() && !args.get(0).equals("-d")) {
                location = String.join("+", args);
            }

            if (!location.isBlank()) {
                URL url = new URL(BASE_URL + location + PARAMETERS);

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
        try {
            MortyBot bot = event.getBot();
            BotUserDao botUserDao = bot.getBotUserDao();
            botUser.setLocation(loc);
            botUserDao.update(botUser);
            event.respondWith("Set default location to: " + loc);
        } catch (BotUserException e) {
            if (e.getReason() == BotUserException.Reason.UNKNOWN_USER) {
                String errMsg = "Unknown user";
                log.error("{}: {}", errMsg, botUser);
                event.respondWith(errMsg);
            } else {
                log.error("Failed to update bot user: {}", botUser, e);
                event.respondWith("Something went wrong. Failed to set default location.");
            }
        }
    }
}
