package net.hatemachine.mortybot.commands;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.List;

public class ImdbCommand implements BotCommand {

    private static final String BASE_URL = "https://www.imdb.com";

    private static final Logger log = LoggerFactory.getLogger(ImdbCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public ImdbCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        if (args.isEmpty())
            throw new IllegalArgumentException("too few arguments");

        String searchUrl = BASE_URL + "/find?q=" + URLEncoder.encode(String.join(" ", args), StandardCharsets.UTF_8);

        Document searchResults = null;
        try {
            searchResults = Jsoup.connect(searchUrl).get();
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        if (searchResults != null) {
            Element findSection = searchResults.select("div.findSection").first();
            String sectionType = findSection.select("h3.findSectionHeader > a").first().attr("name");
            Element resultText = findSection.select("td.result_text").first();
            String name = resultText.text();
            String href = resultText.select("a").attr("href");
            String url = BASE_URL + href;

            if (!sectionType.equals("tt")) {
                // Top result is not a title, just display name and url
                event.respondWith(String.format("%s %s", name, url));
            } else {
                // Top result is a title, fetch the details to get the rating and description.
                try {
                    Document titleDetailsPage = Jsoup.connect(url).get();
                    Element scriptTag = titleDetailsPage.select("script[type=\"application/ld+json\"]").first();

                    if (scriptTag != null) {
                        String json = scriptTag.data();
                        Configuration conf = Configuration.defaultConfiguration();
                        DocumentContext parsedJson = JsonPath.using(conf).parse(json);
                        name = parsedJson.read("$.name");
                        url = BASE_URL + parsedJson.read("$.url");
                        String description = parsedJson.read("$.description");
                        Double rating = parsedJson.read("$.aggregateRating.ratingValue");
                        Integer bestRating = parsedJson.read("$.aggregateRating.bestRating");
                        LocalDate publishDate = LocalDate.parse(parsedJson.read("$.datePublished"), DateTimeFormatter.ISO_LOCAL_DATE);

                        event.respondWith(String.format("[imdb] %s (%d) [%.1f/%d] %s", name, publishDate.get(ChronoField.YEAR), rating, bestRating, url));
                        event.respondWith(String.format("[imdb] %s", description));
                    }
                } catch (IOException e) {
                    log.error("Failed to fetch title details: {}", e.getMessage());
                }
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
