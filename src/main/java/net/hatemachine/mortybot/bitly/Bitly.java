package net.hatemachine.mortybot.bitly;

import com.google.gson.Gson;
import net.hatemachine.mortybot.MortyBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;

public class Bitly {

    private static final Logger log = LoggerFactory.getLogger(Bitly.class);
    private static final Properties props = new Properties();

    private static final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    static {
        String propertiesFile = "bitly.properties";
        try (InputStream inputStream = MortyBot.class.getClassLoader().getResourceAsStream(propertiesFile)) {
            if (inputStream != null) {
                props.load(inputStream);
            }
        } catch (IOException e) {
            log.error("Unable to read properties file {}", propertiesFile, e);
        }
    }

    private Bitly() {}

    public static Optional<String> shorten(String url) throws IOException, InterruptedException {
        if (url == null || url.trim().isEmpty()) {
            String msg = "url cannot be null or empty";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }

        String apiEndpoint = props.getProperty("bitly.api.endpoint.shorten");
        String apiKey = props.getProperty("bitly.api.key");

        if ((apiEndpoint == null || apiEndpoint.trim().isEmpty()) || apiKey == null || apiKey.trim().isEmpty()) {
            String msg = "apiEndpoint and apiKey cannot be null or empty, check properties file";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }

        String requestJson = "{\n" +
                "    \"domain\": \"bit.ly\",  \n" +
                "    \"long_url\": \"" + url + "\"  \n" +
                "}";

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
