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

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import net.hatemachine.mortybot.Command;
import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.util.Validate;
import net.hatemachine.mortybot.util.WebClient;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Implements the STOCK command, allowing users to look up stock quotes.
 */
@BotCommand(name = "STOCK", help = {
        "Looks up the current price of stock symbols",
        "Usage: STOCK <symbol1> [symbol2] ..."
})
public class StockCommand implements Command {

    private static final String BASE_URL = "https://query1.finance.yahoo.com/v7/finance/chart/";

    private static final Logger log = LoggerFactory.getLogger(StockCommand.class);

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
        if (args.isEmpty()) {
            throw new IllegalArgumentException("Not enough arguments");
        }

        BotProperties props = BotProperties.getBotProperties();
        int maxSymbols = props.getIntProperty("stock.max.symbols", BotDefaults.STOCK_MAX_SYMBOLS);

        for (int cnt = 0; cnt < args.size() && cnt < maxSymbols; cnt++) {
            String symbol = args.get(cnt);

            log.info("Fetching stock quote for {}", symbol);

            var webClient = new WebClient();
            Optional<String> json = webClient.get(BASE_URL + symbol);

            if (json.isPresent()) {
                String quote = parseQuote(json.get());
                event.respondWith(quote);
            }
        }
    }

    private static String parseQuote(String json) {
        Validate.notNullOrBlank(json);

        var conf = Configuration.defaultConfiguration();
        var metaNode = JsonPath.using(conf).parse(json).read("$.chart.result[0].meta", JsonNode.class);
        var symbol = metaNode.get("symbol").asText();
        var marketTime = metaNode.get("regularMarketTime").asInt();
        var zId = ZoneId.systemDefault();
        var dt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(marketTime), zId);
        var df = DateTimeFormatter.ofPattern("h:mma");
        var timezone = metaNode.get("timezone").asText();
        var marketPrice = metaNode.get("regularMarketPrice").asDouble();

        return String.format("%s %.2f [%s %s]", symbol, marketPrice, dt.format(df), timezone);
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
