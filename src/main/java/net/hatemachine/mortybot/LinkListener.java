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
import java.util.regex.Pattern;

public class LinkListener extends ListenerAdapter {

    // default maximum number of URLs to process in a single message from a user
    // the value for LinkListener.maxLinks in bot.properties will override this if present
    private static final int MAX_LINKS_DEFAULT = 2;

    // default minimum length of a URL for it to be shortened
    private static final int MIN_LEN_TO_SHORTEN_DEFAULT = 30;

    // the regex pattern used to match URLs
    private static final Pattern URL_PATTERN = Pattern.compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&\\/\\/=]*)");

    private static final Logger log = LoggerFactory.getLogger(LinkListener.class);

    @Override
    public void onMessage(final MessageEvent event) throws InterruptedException {
        log.debug("onMessage event: {}", event);
        boolean watchChannels = MortyBot.getBooleanProperty("LinkListener.watchChannels", false);
        if (watchChannels) {
            handleMessage(event);
        }
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent event) throws InterruptedException {
        log.debug("onPrivateMessage event: {}", event);
        boolean watchPrivateMessages = MortyBot.getBooleanProperty("LinkListener.watchPrivateMessages", false);
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
        var maxLinks = MortyBot.getIntProperty("LinkListener.maxLinks", MAX_LINKS_DEFAULT);
        var minLenToShorten = MortyBot.getIntProperty("LinkListener.minLenToShorten", MIN_LEN_TO_SHORTEN_DEFAULT);
        boolean shortenLinks = MortyBot.getBooleanProperty("LinkListener.shortenLinks", false);
        boolean showTitles = MortyBot.getBooleanProperty("LinkListener.showTitles", true);

        List<String> links = parseMessage(event.getMessage());

        for (var i = 0; i < links.size() && i < maxLinks; i++) {
            String link = links.get(i);
            var responseString = new StringBuilder();
            responseString.append("[");

            if (shortenLinks && link.length() >= minLenToShorten) {
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
                    String title = doc.title();
                    log.debug("Title: {}", title);
                    responseString.append(" ").append(title);
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
        log.debug("Parsing message for links: {}", s);
        var m = URL_PATTERN.matcher(s);
        List<String> links = new ArrayList<>();
        while (m.find()) {
            links.add(m.group(0));
        }
        log.debug("Found {} links: {}", links.size(), links);
        return links;
    }
}
