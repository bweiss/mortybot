package net.hatemachine.mortybot.bitly;

import com.google.gson.Gson;
import net.hatemachine.mortybot.util.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;

public class Bitly {

    private static final String PROPERTIES_FILE = "bitly.properties";

    private static final Logger log = LoggerFactory.getLogger(Bitly.class);
    private static final Properties properties = new Properties();

    static {
        String propertiesFile = System.getenv("MORTYBOT_HOME") + "/conf/" + PROPERTIES_FILE;
        log.info("Attempting to load properties from {}", propertiesFile);
        try (FileReader reader = new FileReader(propertiesFile)) {
            properties.load(reader);
        } catch (FileNotFoundException e) {
            String msg = "file not found: " + propertiesFile;
            log.error(msg, e.getMessage());
        } catch (IOException e) {
            String msg = "unable to read file: " + propertiesFile;
            log.error(msg, e.getMessage());
        }
    }

    private Bitly() {}

    public static Optional<String> shorten(String url) throws IOException, InterruptedException {
        String apiEndpoint = properties.getProperty("bitly.api.endpoint", System.getenv("BITLY_API_ENDPOINT"));
        String apiKey = properties.getProperty("bitly.api.key", System.getenv("BITLY_API_KEY"));
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
