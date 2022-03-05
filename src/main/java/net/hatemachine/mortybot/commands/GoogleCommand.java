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
package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pircbotx.Colors;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Performs a Google search and displays the title and link for the top result.
 */
public class GoogleCommand implements BotCommand {

    private static final String RESPONSE_PREFIX = "[" + Colors.BOLD + "google" + Colors.BOLD + "] ";
    private static final String SEARCH_URL = "https://www.google.com/search?q=";

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    record SearchResult(String url, String text) {
        @Override
        public String toString() {
            return String.format("%s :: %s", text, url);
        }
    }

    public GoogleCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        if (args.isEmpty()) {
            throw new IllegalArgumentException("Not enough arguments");
        }

        String searchUrl = SEARCH_URL + URLEncoder.encode(String.join(" ", args), StandardCharsets.UTF_8);
        List<SearchResult> results = new ArrayList<>();
        Document page = null;

        try {
            page = Jsoup.connect(searchUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (page != null) {
            Elements resultDivs = page.select("div.g");
            for (Element div : resultDivs) {
                Element aTag = div.select("a").first();
                if (aTag != null) {
                    String url = aTag.attr("href");
                    Element h3Tag = aTag.select("h3").first();
                    if (h3Tag != null) {
                        String text = h3Tag.text();
                        results.add(new SearchResult(url, text));
                    }
                }
            }
        }

        if (results.isEmpty()) {
            event.respondWith("No results");
        } else {
            event.respondWith(RESPONSE_PREFIX + results.get(0).toString());
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