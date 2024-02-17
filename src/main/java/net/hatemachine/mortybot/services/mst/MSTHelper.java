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
package net.hatemachine.mortybot.services.mst;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hatemachine.mortybot.util.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MSTHelper {

    private static final String BASE_URL = "https://mass-shooting-tracker-data.s3.us-east-2.amazonaws.com/";

    private static final Logger log = LoggerFactory.getLogger(MSTHelper.class);

    public List<ShootingEvent> shootingsByYear(int year) throws JsonProcessingException {
        LocalDate now = LocalDate.now();
        if (year < 2013 || year > now.getYear()) {
            throw new IllegalArgumentException("year must be between 2013 and " + now.getYear());
        }

        Optional<String> json = fetchJson(year);

        if (json.isPresent()) {
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            return mapper.readValue(Validate.notNullOrBlank(json.get()), new TypeReference<>() {});
        } else {
            return new ArrayList<>();
        }
    }

    private Optional<String> fetchJson(int year) {
        String urlStr = BASE_URL + year + "-data.json";
        Optional<String> json = Optional.empty();
        URI uri;

        try {
            uri = new URI(urlStr);
        } catch (URISyntaxException e) {
            log.error("Couldn't parse URL: {}", urlStr);
            throw new RuntimeException(e);
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .header("User-Agent", "Java HttpClient Bot")
                    .GET()
                    .build();

            HttpResponse<String> response;

            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response != null && response.statusCode() == 200) {
                json = Optional.of(response.body());
            }
        } catch (IOException e) {
            log.error("Failed to fetch HTTP response body");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            log.warn("Thread interrupted!");
            Thread.currentThread().interrupt();
        }

        return json;
    }
}
