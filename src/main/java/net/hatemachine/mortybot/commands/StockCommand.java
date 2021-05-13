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
import net.hatemachine.mortybot.CommandListener;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.util.WebClient;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static net.hatemachine.mortybot.util.StringUtils.validateString;

public class StockCommand implements BotCommand {

    // default maximum number of symbols a user can request in a single command
    // the value for StockCommand.maxSymbolsPerCommand in bot.properties will override this if present
    private static final int MAX_SYMBOLS_PER_COMMAND_DEFAULT = 4;

    // the api endpoint to use - changing this will probably break things
    private static final String ENDPOINT_URL = "https://query1.finance.yahoo.com/v7/finance/chart/";

    private static final Logger log = LoggerFactory.getLogger(StockCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.MessageSource source;
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

    public StockCommand(GenericMessageEvent event, CommandListener.MessageSource source, List<String> args) {
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

    private static Optional<String> fetchQuote(String symbol) {
        validateString(symbol);
        log.info("Fetching stock quote for {}", symbol);

        String url = ENDPOINT_URL + symbol;
        var client = new WebClient();
        HttpResponse<String> response = null;

        try {
            response = client.get(url);
        } catch (IOException e) {
            log.error("Could not fetch quote {}", e.getMessage());
        } catch (InterruptedException e) {
            log.error("Interrupted while attempting to fetch quote {}", e.getMessage());
            Thread.currentThread().interrupt();
        }

        if (response != null) {
            int status = response.statusCode();
            if (status >= 200 && status <= 299) {
                String json = response.body();
                return Optional.of(json);
            } else {
                log.warn("Failed to fetch quote (HTTP response status: {}", status);
            }
        }

        return Optional.empty();
    }

    private static String parseQuote(String json) {
        validateString(json);
        var conf = Configuration.defaultConfiguration();
        JsonNode metaNode = JsonPath.using(conf)
                .parse(json)
                .read("$.chart.result[0].meta", JsonNode.class);
        String symbol = metaNode.get("symbol").asText();
        var marketTime = metaNode.get("regularMarketTime").asInt();
        var zId = ZoneId.systemDefault();
        var dt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(marketTime), zId);
        var df = DateTimeFormatter.ofPattern("h:mma");
        String timezone = metaNode.get("timezone").asText();
        Double marketPrice = metaNode.get("regularMarketPrice").asDouble();

        return String.format("[Q] %s %.2f [%s %s]%n", symbol, marketPrice, dt.format(df), timezone);
    }

    @Override
    public GenericMessageEvent getEvent() {
        return event;
    }

    @Override
    public CommandListener.MessageSource getSource() {
        return source;
    }

    @Override
    public List<String> getArgs() {
        return args;
    }
}
