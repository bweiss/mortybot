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
    private static final int MAX_LINKS_DEFAULT = 2;

    // the regex pattern used to match URLs
    private static final Pattern URL_PATTERN = Pattern.compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&\\/\\/=]*)");

    private static final Logger log = LoggerFactory.getLogger(LinkListener.class);

    @Override
    public void onMessage(final MessageEvent event) throws InterruptedException {
        log.debug("onMessage event: {}", event);
        MortyBot bot = event.getBot();
        boolean watchChannels = bot.getBotConfig().getStringProperty("LinkListener.watchChannels").equalsIgnoreCase("true");
        if (watchChannels) {
            handleMessage(event);
        }
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent event) throws InterruptedException {
        log.debug("onPrivateMessage event: {}", event);
        MortyBot bot = event.getBot();
        boolean watchPrivateMessages = bot.getBotConfig().getStringProperty("LinkListener.watchPrivateMessages").equalsIgnoreCase("true");
        if (watchPrivateMessages) {
            handleMessage(event);
        }
    }

    /**
     * Handle a message event, checking for links.
     *
     * @param event the event being handled
     */
    private void handleMessage(final GenericMessageEvent event) throws InterruptedException {
        MortyBot bot = event.getBot();
        BotConfiguration config = bot.getBotConfig();
        int maxLinks = config.getIntProperty("LinkListener.maxLinks", MAX_LINKS_DEFAULT);
        boolean shortenLinks = config.getStringProperty("LinkListener.shortenLinks").equalsIgnoreCase("true");
        boolean showTitles = config.getStringProperty("LinkListener.showTitles").equalsIgnoreCase("true");

        log.debug("Parsing message for links [maxLinks={}, shortenLinks={}, showTitles={}]", maxLinks, shortenLinks, showTitles);

        List<String> links = parseMessage(event.getMessage());

        log.debug("Found {} links", links.size());

        for (int i = 0; i < links.size() && i < maxLinks; i++) {
            String link = links.get(i);
            StringBuilder responseString = new StringBuilder();
            responseString.append("[");

            if (shortenLinks) {
                Optional<String> shortLink = Optional.empty();
                try {
                    log.debug("Shortening link: {}", link);
                    shortLink = Bitly.shorten(link);
                } catch (IOException e) {
                    log.error("Error while attempting to shorten link: {}", e.getMessage());
                } finally {
                    if (shortLink.isPresent()) {
                        log.debug("Shortened link to: {}", shortLink.get());
                        responseString.append(shortLink.get());
                    } else {
                        log.warn("Unable to shorten link, falling back to long url");
                        responseString.append(link);
                    }
                }
            } else {
                responseString.append(link);
            }

            responseString.append("]");

            if (showTitles) {
                Document doc = null;
                try {
                    log.debug("Fetching title for link: {}", link);
                    doc = Jsoup.connect(link).get();
                } catch (IOException e) {
                    log.error("Failed to fetch link: {}", e.getMessage());
                }
                if (doc != null) {
                    log.debug("Title: {}", doc.title());
                    responseString.append(" ").append(doc.title());
                }
            }

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
