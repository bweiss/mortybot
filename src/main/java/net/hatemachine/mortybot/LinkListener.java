package net.hatemachine.mortybot;

import net.hatemachine.mortybot.bitly.Bitly;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkListener extends ListenerAdapter {

    // default maximum number of URLs to process in a single message from a user
    // the value for LinkListener.maxLinks in bot.properties will override this if present
    private static final String DEFAULT_MAX_LINKS = "2";

    // the regex pattern used to match URLs
    private static final Pattern URL_PATTERN = Pattern.compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&\\/\\/=]*)");

    private static final Logger log = LoggerFactory.getLogger(LinkListener.class);

    @Override
    public void onMessage(final MessageEvent event) {
        MortyBot bot = event.getBot();
        if (bot.getProperty("LinkListener.watchChannels").equalsIgnoreCase("true")) {
            log.debug("onMessage event: {}", event);
            handleMessage(event);
        }
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent event) {
        MortyBot bot = event.getBot();
        if (bot.getProperty("LinkListener.watchPrivateMessages").equalsIgnoreCase("true")) {
            log.debug("onPrivateMessage event: {}", event);
            handleMessage(event);
        }
    }

    private void handleMessage(final GenericMessageEvent event) {
        MortyBot bot = event.getBot();

        // parse the event message looking for links
        List<String> links = parseMessage(event.getMessage());

        // maximum number of links to process in a single message
        int maxLinks = Integer.parseInt(bot.getProperty("LinkListener.maxLinks", DEFAULT_MAX_LINKS));

        for (int i = 0; i < links.size() && i < maxLinks; i++) {
            String link = links.get(i);

            // build our response string
            StringBuilder responseString = new StringBuilder();
            responseString.append("[");

            // shorten the link if shortenLinks property is true
            if (bot.getProperty("LinkListener.shortenLinks").equalsIgnoreCase("true")) {
                Optional<String> shortLink = Optional.empty();
                try {
                    shortLink = Bitly.shorten(link);
                } catch (IOException | InterruptedException e) {
                    log.error("Error while attempting to shorten link: {}", link);
                } finally {
                    if (shortLink.isPresent()) {
                        responseString.append(shortLink.get());
                    } else {
                        log.warn("Unable to shorten link ({}), falling back to long url", link);
                        responseString.append(link);
                    }
                }
            } else {
                responseString.append(link);
            }

            responseString.append("]");

            // append the title if showTitles property is true
            if (bot.getProperty("LinkListener.showTitles").equalsIgnoreCase("true")) {
                // todo consider moving this into its own method
                // get the page and parse it into a DOM
                Document doc = null;
                try {
                    doc = Jsoup.connect(link).get();
                } catch (IOException e) {
                    log.error("Failed to fetch link: {} - {}", link, e.getMessage());
                }
                if (doc != null) {
                    responseString.append(" ").append(doc.title());
                }
            }

            // respond to the event
            event.respondWith(responseString.toString());
        }
    }

    /**
     * Parses a string looking for URLs and returns them as a list.
     * Valid URLs are determined by the URL_PATTERN constant.
     *
     * @param s string that may or may not contain links
     * @return a list of link strings
     */
    private static List<String> parseMessage(final String s) {
        log.debug("Parsing message for links: {} in ", s);
        Matcher m = URL_PATTERN.matcher(s);
        List<String> links = new ArrayList<>();
        while (m.find()) {
            links.add(m.group(0));
        }
        log.debug("Found {} links {}", links.size(), links);
        return links;
    }
}
