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
package net.hatemachine.mortybot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * Convenience class for making simple web requests.
 */
public class WebClient {

    private enum RequestType {
        GET,
        POST
    }

    private static final int DEFAULT_TIMEOUT = 30;
    private static final String[] DEFAULT_HEADERS = {
            "User-Agent", "Java HttpClient Bot"
    };

    private static final Logger log = LoggerFactory.getLogger(WebClient.class);

    private int timeout;
    private String[] headers;

    /**
     * A simple web client that wraps HttpClient requests for convenience purposes.
     * For more complex requests, use HttpClient directly.
     */
    public WebClient() {
        this(DEFAULT_TIMEOUT, DEFAULT_HEADERS);
    }

    /**
     * A simple web client that wraps HttpClient requests for convenience purposes.
     * For more complex requests, use HttpClient directly.
     *
     * @param timeout request timeout in seconds
     */
    public WebClient(int timeout) {
        this(timeout, DEFAULT_HEADERS);
    }

    /**
     * A simple web client that wraps HttpClient requests for convenience purposes.
     * For more complex requests, use HttpClient directly.
     *
     * @param headers list of strings representing the key value pairs of headers to use
     */
    public WebClient(String[] headers) {
        this(DEFAULT_TIMEOUT, headers);
    }

    /**
     * A simple web client that wraps HttpClient requests for convenience purposes.
     * For more complex requests, use HttpClient directly.
     *
     * @param timeout request timeout in seconds
     * @param headers list of strings representing the key value pairs of headers to use
     */
    public WebClient(int timeout, String[] headers) {
        this.timeout = timeout;
        this.headers = headers;
    }

    /**
     * Performs a GET request.
     *
     * @param url the url to request
     * @return an optional response body
     */
    public Optional<String> get(String url) {
        return doRequest(RequestType.GET, url, null);
    }

    /**
     * Performs a POST request.
     *
     * @param url the url to request
     * @param body string representing the body of the post request
     * @return an optional response body
     */
    public Optional<String> post(String url, String body) {
        return doRequest(RequestType.POST, url, body);
    }

    private Optional<String> doRequest(RequestType type, String url, String body) {
        Validate.notNullOrBlank(url, "url cannot be null or blank");

        try (HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(timeout)).build()) {
            HttpRequest request;

            if (type == RequestType.GET) {
                request = HttpRequest.newBuilder(URI.create(url))
                        .headers(headers)
                        .GET()
                        .build();
            } else if (type == RequestType.POST) {
                request = HttpRequest.newBuilder(URI.create(url))
                        .headers(headers)
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
            } else {
                throw new IllegalArgumentException("Invalid request type");
            }

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response != null && response.statusCode() == 200) {
                return Optional.of(response.body());
            }
        } catch (IOException e) {
            log.error("Error fetching body", e);
        } catch (InterruptedException e) {
            log.warn("Thread interrupted: {}", Thread.currentThread().getName(), e);
            Thread.currentThread().interrupt();
        }

        return Optional.empty();
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String[] getHeaders() {
        return headers;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }
}
