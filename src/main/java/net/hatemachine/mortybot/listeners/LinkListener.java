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
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotState;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.pircbotx.Colors;
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

    private static final Pattern URL_PATTERN = Pattern.compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&\\/\\/=]*)");

    private static final Logger log = LoggerFactory.getLogger(LinkListener.class);

    @Override
    public void onMessage(final MessageEvent event) {
        log.debug("onMessage event: {}", event);
        handleMessage(event);
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent event) {
        log.debug("onPrivateMessage event: {}", event);
        handleMessage(event);
    }

    /**
     * Handle a message event, checking for links.
     *
     * @param event the event being handled
     */
    private void handleMessage(final GenericMessageEvent event) {
        var bs = BotState.getBotState();
        int maxLinks = bs.getIntProperty("links.max", BotDefaults.LINKS_MAX);
        int minLenToShorten = bs.getIntProperty("links.min.length", BotDefaults.LINKS_MIN_LENGTH);
        int maxTitleLength = bs.getIntProperty("links.max.title.length", BotDefaults.LINKS_MAX_TITLE_LENGTH);
        boolean shortenLinksFlag = bs.getBooleanProperty("links.shorten", BotDefaults.LINKS_SHORTEN);
        boolean showTitlesFlag = bs.getBooleanProperty("links.show.titles", BotDefaults.LINKS_SHOW_TITLES);

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
                    } catch (InterruptedException e) {
                        log.warn("Thread interrupted", e);
                        Thread.currentThread().interrupt();
                    } catch (IOException e) {
                        log.error("Exception encountered shortening link", e);
                    }
                }
            }

            if (showTitlesFlag) {
                title = fetchTitle(link);
            }

            // shortened link only
            if (shortLink.isPresent() && title.isEmpty() && !Objects.equals(link, shortLink.get())) {
                event.respondWith(Colors.BOLD + shortLink.get() + Colors.BOLD);

            // title only
            } else if (title.isPresent() && shortLink.isEmpty()) {
                event.respondWith(trimTitle(title.get(), maxTitleLength, "..."));

            // short link and title
            } else if (shortLink.isPresent() && title.isPresent()) {
                event.respondWith(String.format("%s :: %s",
                        Colors.BOLD + shortLink.get() + Colors.BOLD,
                        trimTitle(title.get(), maxTitleLength, "...")));
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
            log.error("Failed to fetch page [URL: {}]", link, e);
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
