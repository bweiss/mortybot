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
package net.hatemachine.mortybot.listeners;

import net.hatemachine.mortybot.bitly.Bitly;
import net.hatemachine.mortybot.config.BotState;
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
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkListener extends ListenerAdapter {
    // default maximum number of URLs to process in a single message from a user
    // the value for LinkListener.maxLinks in bot.properties will override this if present
    private static final int MAX_LINKS_DEFAULT = 2;

    // default minimum length of a URL for it to be shortened
    private static final int MIN_LENGTH_TO_SHORTEN_DEFAULT = 30;

    // maximum number of characters to show for the title
    private static final int MAX_TITLE_LENGTH_DEFAULT = 200;

    // the regex pattern used to match URLs
    private static final Pattern URL_PATTERN = Pattern.compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&\\/\\/=]*)");

    private static final Logger log = LoggerFactory.getLogger(LinkListener.class);

    @Override
    public void onMessage(final MessageEvent event) throws InterruptedException {
        log.debug("onMessage event: {}", event);
        boolean watchChannels = BotState.getBotState().getBooleanProperty("LinkListener.watchChannels", false);
        if (watchChannels) {
            handleMessage(event);
        }
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent event) throws InterruptedException {
        log.debug("onPrivateMessage event: {}", event);
        boolean watchPrivateMessages = BotState.getBotState().getBooleanProperty("LinkListener.watchPrivateMessages", false);
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
        var bs = BotState.getBotState();
        int maxLinks = bs.getIntProperty("LinkListener.maxLinks", MAX_LINKS_DEFAULT);
        int minLenToShorten = bs.getIntProperty("LinkListener.minLengthToShorten", MIN_LENGTH_TO_SHORTEN_DEFAULT);
        int maxTitleLength = bs.getIntProperty("LinkListener.maxTitleLength", MAX_TITLE_LENGTH_DEFAULT);
        boolean shortenLinksFlag = bs.getBooleanProperty("LinkListener.shortenLinks", false);
        boolean showTitlesFlag = bs.getBooleanProperty("LinkListener.showTitles", true);

        List<String> links = parseMessage(event.getMessage());

        for (int i = 0; i < links.size() && i < maxLinks; i++) {
            String link = links.get(i);
            Optional<String> shortLink = Optional.empty();
            Optional<String> title = Optional.empty();

            if (shortenLinksFlag) {
                if (link.length() < minLenToShorten) {
                    shortLink = Optional.of(link);
                } else {
                    try {
                        shortLink = Bitly.shorten(link);
                    } catch (IOException e) {
                        log.error("Error while attempting to shorten link: {}", e.getMessage());
                    }
                }
            }

            if (showTitlesFlag) {
                title = fetchTitle(link);
            }

            if (shortLink.isPresent() && title.isEmpty() && !Objects.equals(link, shortLink.get())) {
                // shortened link only
                event.respondWith(shortLink.get());
            } else if (title.isPresent() && shortLink.isEmpty()) {
                // title only
                event.respondWith(trimTitle(title.get(), maxTitleLength, "..."));
            } else if (shortLink.isPresent() && title.isPresent()) {
                // short link and title
                event.respondWith(String.format("%s :: %s", shortLink.get(), trimTitle(title.get(), maxTitleLength, "...")));
            }
        }
    }

    /**
     * Parses a string looking for URLs and returns them as a list.
     * Valid URLs are determined by the URL_PATTERN constant.
     *
     * @param s string that may or may not contain links
     * @return a list of link strings
     */
    private List<String> parseMessage(final String s) {
        log.debug("Parsing message for links: {}", s);
        Matcher m = URL_PATTERN.matcher(s);
        List<String> links = new ArrayList<>();
        while (m.find()) {
            links.add(m.group(0));
        }
        log.debug("Found {} links: {}", links.size(), links);
        return links;
    }

    /**
     * Fetch the title of a web link.
     *
     * @param link the link that you want to fetch
     * @return an optional containing the link's title
     */
    private Optional<String> fetchTitle(final String link) {
        log.debug("Fetching title for link: {}", link);
        Document doc = null;
        try {
            doc = Jsoup.connect(link).get();
        } catch (IOException e) {
            log.error("Failed to fetch link: {}", e.getMessage());
        }
        if (doc != null) {
            String title = doc.title();
            log.debug("Title: {}", title);
            return Optional.of(title);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Trim the title down to a maximum length.
     *
     * @param title the title you want to trim
     * @param maxLength the maximum length of the string to return
     * @param suffix the suffix to append if the string was trimmed
     * @return the original string, trimmed if it exceeds the max length
     */
    private String trimTitle(String title, int maxLength, String suffix) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < title.length(); i++) {
            if (i == maxLength) {
                sb.append(suffix);
                return sb.toString();
            } else {
                char c = title.charAt(i);
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
