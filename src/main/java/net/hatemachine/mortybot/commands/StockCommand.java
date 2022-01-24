/*
 * MortyBot - An IRC bot built on the PircBotX framework.
 * Copyright © 2022 Brian Weiss (brianmweiss@gmail.com)
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

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.util.Validate;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class StockCommand implements BotCommand {

    private static final int MAX_SYMBOLS_PER_COMMAND_DEFAULT = 4;

    private static final String BASE_URL = "https://query1.finance.yahoo.com/v7/finance/chart/";

    private static final Logger LOGGER = LoggerFactory.getLogger(StockCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    static {
        // This is needed to use JsonPath with Jackson
        Configuration.setDefaults(new Configuration.Defaults() {
            private final JsonProvider jsonProvider = new JacksonJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }

    public StockCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        var maxSymbols = MortyBot.getIntProperty("StockCommand.maxSymbolsPerCommand", MAX_SYMBOLS_PER_COMMAND_DEFAULT);
        for (var cnt = 0; cnt < maxSymbols; cnt++) {
            String symbol = args.get(cnt);
            Optional<String> json = fetchQuote(symbol);
            if (json.isPresent()) {
                String quote = parseQuote(json.get());
                event.respondWith(quote);
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

    private static Optional<String> fetchQuote(String symbol) {
        Validate.notNullOrEmpty(symbol);
        Optional<String> quote = Optional.empty();

        LOGGER.info("Fetching stock quote for {}", symbol);

        try {
            URL url = new URL(BASE_URL + symbol);

            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder(url.toURI())
                    .header("User-Agent", "Java HttpClient Bot")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response != null && response.statusCode() == 200) {
                quote = Optional.of(response.body());
            }

        } catch (MalformedURLException e) {
            LOGGER.error("Invalid URL", e);
        } catch (URISyntaxException e) {
            LOGGER.error("Invalid URI", e);
        } catch (IOException e) {
            LOGGER.error("Error fetching body", e);
        } catch (InterruptedException e) {
            LOGGER.warn("Thread interrupted", e);
            Thread.currentThread().interrupt();
        }

        return quote;
    }

    private static String parseQuote(String json) {
        Validate.notNullOrEmpty(json);
        Configuration conf = Configuration.defaultConfiguration();
        JsonNode metaNode = JsonPath.using(conf)
                .parse(json)
                .read("$.chart.result[0].meta", JsonNode.class);
        String symbol = metaNode.get("symbol").asText();
        int marketTime = metaNode.get("regularMarketTime").asInt();
        ZoneId zId = ZoneId.systemDefault();
        ZonedDateTime dt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(marketTime), zId);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("h:mma");
        String timezone = metaNode.get("timezone").asText();
        Double marketPrice = metaNode.get("regularMarketPrice").asDouble();

        return String.format("%s %.2f [%s %s]", symbol, marketPrice, dt.format(df), timezone);
    }
}
