package net.hatemachine.mortybot.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import net.hatemachine.mortybot.BotCommand;
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
import java.util.List;
import java.util.Optional;

import static net.hatemachine.mortybot.util.StringUtils.validateString;

public class StockCommand implements BotCommand {

    private static final Logger log = LoggerFactory.getLogger(StockCommand.class);

    private static final String ENDPOINT_URL = "https://query1.finance.yahoo.com/v7/finance/chart/";

    private final GenericMessageEvent event;
    private final List<String> args;

    public StockCommand(GenericMessageEvent event, List<String> args) {
        this.event = event;
        this.args = args;
    }

    @Override
    public void execute() {
        for (String symbol : args) {
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
        WebClient client = new WebClient();
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

        return String.format("[Q] %s %.2f [%s %s]%n", symbol, marketPrice, dt.format(df), timezone);
    }
}
