package net.hatemachine.mortybot.commands;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.util.WebClient;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class WeatherCommand implements BotCommand {

    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile("^\\d{5}(?:[-\\s]\\d{4})?$");

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
        if (args.isEmpty())
            throw new IllegalArgumentException("too few arguments");

        String zipCode = args.get(0);
        Optional<String> json = fetchWeatherByZip(zipCode);

        json.ifPresent(s -> event.respondWith(formatWeather(s)));
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

    private Optional<String> fetchWeatherByZip(String zipCode) {
        Optional<String> json = Optional.empty();
        URI uri = null;
        var client = new WebClient();
        HttpResponse<String> response = null;
        String apiKey = MortyBot.getStringProperty("weather.api.key", System.getenv("WEATHER_API_KEY"));

        if (isValidZipCode(zipCode)) {
            try {
                uri = new URI("https",
                        null,
                        "//api.openweathermap.org/data/2.5/weather",
                        "zip=" + zipCode + "&units=imperial" + "&appid=" + apiKey,
                        null);
            } catch (URISyntaxException e) {
                log.error(e.getMessage());
            }

            if (uri != null) {
                try {
                    response = client.get(uri.toASCIIString());
                } catch (IOException e) {
                    log.error("Failed to fetch weather for {}: {}", zipCode, e.getMessage());
                } catch (InterruptedException e) {
                    log.error("Interrupted while attempting to fetch weather {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }

            if (response != null) {
                int status = response.statusCode();
                if (status >= 200 && status <= 299) {
                    json = Optional.of(response.body());
                } else {
                    log.warn("Failed to fetch weather (HTTP response status: {})", status);
                }
            }
        }

        return json;
    }

    private boolean isValidZipCode(String zipCode) {
        if (zipCode == null || zipCode.trim().isEmpty()) {
            throw new IllegalArgumentException("null or empty argument: zipCode");
        }
        return ZIP_CODE_PATTERN.matcher(zipCode).matches();
    }

    private String formatWeather(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("null or empty argument: json");
        }

        Configuration conf = Configuration.defaultConfiguration();
        DocumentContext parsedJson = JsonPath.using(conf).parse(json);
        String description = parsedJson.read("$.weather[0].description");
        Double temp = Double.parseDouble(parsedJson.read("$.main.temp").toString());
        Integer humidity = Integer.parseInt(parsedJson.read("$.main.humidity").toString());
        Double windSpeed = Double.parseDouble(parsedJson.read("$.wind.speed").toString());
        String country = parsedJson.read("$.sys.country");
        String city = parsedJson.read("$.name");

        return String.format("%s, %s (%.1f°/%s) H:%d%% W:%.1f%n", city, country, temp, description, humidity, windSpeed);
    }
}
