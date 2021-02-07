package net.hatemachine.mortybot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class WebClient {

    private static final Logger log = LoggerFactory.getLogger(WebClient.class);
    private final HttpClient client;

    public WebClient() {
        client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public HttpResponse<String> get(String url) throws IOException, InterruptedException {
        if (url == null || url.trim().isEmpty()) {
            String s = "url cannot be null or empty";
            log.error(s);
            throw new IllegalArgumentException(s);
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "Java HttpClient Bot")
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
