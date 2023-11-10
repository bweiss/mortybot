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
package net.hatemachine.mortybot.listeners;

import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.repositories.BotChannelRepository;
import net.hatemachine.mortybot.repositories.BotUserRepository;
import net.hatemachine.mortybot.services.bitly.Bitly;
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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.hatemachine.mortybot.listeners.LinkListener.Source.PRIVATE;
import static net.hatemachine.mortybot.listeners.LinkListener.Source.PUBLIC;

public class LinkListener extends ListenerAdapter {

    private static final Pattern URL_PATTERN = Pattern.compile("https?:\\/\\/([.]|\\S+)");

    private static final Logger log = LoggerFactory.getLogger(LinkListener.class);

    enum Source {
        PUBLIC,
        PRIVATE
    }

    @Override
    public void onMessage(final MessageEvent event) {
        log.debug("onMessage event: {}", event);
        handleMessage(event, PUBLIC);
    }

    @Override
    public void onPrivateMessage(final PrivateMessageEvent event) {
        log.debug("onPrivateMessage event: {}", event);
        handleMessage(event, PRIVATE);
    }

    /**
     * Handles a message event, checking for links. If the link appears to be to a tweet then we try to fetch the
     * details and display that. Otherwise, shorten the link and fetch the page title.
     *
     * @param event the event being handled
     */
    private void handleMessage(final GenericMessageEvent event, Source source) {
        BotProperties props = BotProperties.getBotProperties();

        // global settings
        int     maxLinks         = props.getIntProperty("links.max",              BotDefaults.LINKS_MAX);
        int     minLenToShorten  = props.getIntProperty("links.min.length",       BotDefaults.LINKS_MIN_LENGTH);
        int     maxTitleLength   = props.getIntProperty("links.max.title.length", BotDefaults.LINKS_MAX_TITLE_LENGTH);
        boolean shortenLinksFlag = props.getBooleanProperty("links.shorten",      BotDefaults.LINKS_SHORTEN);
        boolean showTitlesFlag   = props.getBooleanProperty("links.show.titles",  BotDefaults.LINKS_SHOW_TITLES);
        String  commandPrefix    = props.getStringProperty("command.prefix",      BotDefaults.BOT_COMMAND_PREFIX);

        // if the source is a public message, see if we have channel specific settings
        if (source == PUBLIC) {
            var channel = ((MessageEvent) event).getChannel();
            var botChannelRepository = new BotChannelRepository();
            var botChannel = botChannelRepository.findByName(channel.getName());

            if (botChannel.isPresent()) {
                shortenLinksFlag = botChannel.get().hasShortenLinksFlag();
                showTitlesFlag = botChannel.get().hasShowLinkTitlesFlag();
            }
        }

        // ignore anything from bot users that have the ignore flag
        var botUserRepository = new BotUserRepository();
        var botUser = botUserRepository.findByHostmask(event.getUser().getHostmask());
        if (botUser.isPresent() && botUser.get().hasIgnoreFlag()) {
            return;
        }

        // ignore commands
        if (event.getMessage().startsWith(commandPrefix)) {
            return;
        }

        // parse the message looking for links
        List<String> links = parseLine(event.getMessage());

        // process any links
        for (int i = 0; i < links.size() && i < maxLinks; i++) {
            String link = links.get(i);
            Optional<String> shortLink = Optional.empty();
            Optional<String> title = Optional.empty();

            if (shortenLinksFlag) {
                if (link.length() < minLenToShorten) {
                    shortLink = Optional.of(link);
                } else {
                    shortLink = Bitly.shorten(link);
                    log.debug("Shortened {} to {}", link, shortLink.orElse(""));
                }
            }

            if (showTitlesFlag) {
                title = fetchTitle(link);
            }

            // shortened link only
            if ((shortLink.isPresent() && !shortLink.get().equals(link)) && !shortLink.get().isBlank() && title.isEmpty()) {
                event.respondWith(Colors.BOLD + shortLink.get() + Colors.BOLD);

            // title only
            } else if (title.isPresent() && shortLink.isEmpty()) {
                event.respondWith(trimTitle(title.get(), maxTitleLength, "..."));

            // short link and title
            } else if (shortLink.isPresent() && title.isPresent()) {
                event.respondWith(String.format("%s :: %s",
                        Colors.BOLD + shortLink.get() + Colors.BOLD,
                        trimTitle(title.get(), maxTitleLength, "...")));

            // nothing to do
            } else {
                log.debug("No title or shortened link, this is a no-op. Original link: {}", link);
            }
        }
    }

    /**
     * Parses a string looking for URLs and returns them as a list.
     * Valid URLs are determined by the URL_PATTERN constant.
     *
     * @param line the line to parse that may contain links
     * @return a list of strings containing links
     */
    private List<String> parseLine(final String line) {
        List<String> links = new ArrayList<>();
        Matcher m = URL_PATTERN.matcher(line);

        log.debug("Parsing line for links: {}", line);

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
        String title = "";

        try {
            doc = Jsoup.connect(link)
                    .ignoreContentType(true)
                    .get();
        } catch (IOException e) {
            log.error("Failed to fetch page [URL: {}]", link, e);
        }

        if (doc != null) {
            title = doc.title();
            log.debug("Title: {}", title);
        }

        if (title.isEmpty() || title.isBlank()) {
            return Optional.empty();
        } else {
            return Optional.of(title);
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
