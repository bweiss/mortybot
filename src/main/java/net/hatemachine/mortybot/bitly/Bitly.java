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
package net.hatemachine.mortybot.bitly;

import com.google.gson.Gson;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.util.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

public class Bitly {

    private static final Logger log = LoggerFactory.getLogger(Bitly.class);

    private Bitly() {}

    public static Optional<String> shorten(String url) throws IOException, InterruptedException {
        BotProperties props = BotProperties.getBotProperties();
        String apiEndpoint = props.getStringProperty("bitly.api.endpoint", System.getenv("BITLY_API_ENDPOINT"));
        String apiKey = props.getStringProperty("bitly.api.key", System.getenv("BITLY_API_KEY"));
        Validate.notNullOrEmpty(url);
        Validate.notNullOrEmpty(apiEndpoint);
        Validate.notNullOrEmpty(apiKey);
        URI uri = URI.create(url);

        if (uri.getHost().equalsIgnoreCase("bit.ly")) {
            log.debug("bit.ly host encountered, skipping...");
            return Optional.empty();
        }

        String requestJson = "{\n" +
                "    \"domain\": \"bit.ly\",  \n" +
                "    \"long_url\": \"" + uri.toASCIIString() + "\"  \n" +
                "}";

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiEndpoint))
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", "Bearer " + apiKey)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        if (status >= 200 && status <= 299) {
            Gson gson = new Gson();
            Bitlink bitLink = gson.fromJson(response.body(), Bitlink.class);
            return Optional.of(bitLink.getLink());
        } else {
            return Optional.empty();
        }
    }
}
