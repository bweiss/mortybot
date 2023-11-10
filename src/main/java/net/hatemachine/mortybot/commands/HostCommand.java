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
package net.hatemachine.mortybot.commands;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.hatemachine.mortybot.Command;
import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.Colors;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implements the HOST command. Looks up some basic information about a hostname or IP address using the Shodan API (https://www.shodan.io/).
 *
 * This requires a Shodan API key that must be set in either bot.properties or the SHODAN_API_KEY environment variable.
 */
@BotCommand(name = "HOST", help = {
        "Looks up basic information on a particular hostname or IP address",
        "Usage: HOST <address>"
})
public class HostCommand implements Command {

    private static final String API_ENDPOINT = "https://api.shodan.io/shodan/host/";

    private static final Logger log = LoggerFactory.getLogger(HostCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    record Host(String ip, List<String> hostnames, String asn, String city, String regionCode, String countryCode, String isp, String os, List<Integer> ports) {
        @Override
        public String toString() {
            return String.format("%s :: %s :: %s, %s :: %s :: %s :: ports[%s]",
                    Colors.BOLD + ip + Colors.BOLD,
                    hostnames.stream().sorted().collect(Collectors.joining(", ")),
                    city,
                    regionCode,
                    asn,
                    os,
                    ports.stream().sorted().map(Object::toString).collect(Collectors.joining(", ")));
        }
    }

    public HostCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        if (args.isEmpty()) {
            throw new IllegalArgumentException("Not enough arguments");
        }

        try {
            InetAddress addr = InetAddress.getByName(args.get(0));
            Optional<String> json = doShodanHostLookup(addr.getHostAddress());

            if (json.isPresent()) {
                Host host = parseJson(json.get());
                event.respondWith(host.toString());
            } else {
                event.respondWith("Something went wrong");
            }
        } catch (UnknownHostException e) {
            String errMsg = "Unknown host";
            log.error("{}: {}", errMsg, args.get(0));
            event.respondWith(errMsg);
        }
    }

    /**
     * Performs a host search by IP address and retrieves the JSON response from the Shodan API.
     *
     * @param ip the ip address to lookup
     * @return an optional that may contain json with information about the host
     */
    private Optional<String> doShodanHostLookup(String ip) {
        String apiKey = BotProperties.getBotProperties().getStringProperty("shodan.api.key", System.getenv("SHODAN_API_KEY"));
        Optional<String> jsonOptional = Optional.empty();
        URI uri = null;

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("API key not set");
        }

        try {
            uri = new URI(API_ENDPOINT + ip + "?key=" + apiKey);
        } catch (URISyntaxException e) {
            log.error("Invalid URI", e);
        }

        Validate.notNull(uri);

        try (HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build()) {

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .header("User-Agent", "Java HttpClient Bot")
                    .GET()
                    .build();

            HttpResponse<String> response;

            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response != null && response.statusCode() == 200) {
                jsonOptional = Optional.of(response.body());
            }
        } catch (MalformedURLException e) {
            log.error("Invalid URL", e);
        } catch (IOException e) {
            log.error("Error making HTTP request", e);
        } catch (InterruptedException e) {
            log.warn("Thread interrupted", e);
            Thread.currentThread().interrupt();
        }

        return jsonOptional;
    }

    /**
     * Parse the JSON from the Shodan API call and create a Host object.
     *
     * @param json the json string containing host information
     * @return a host object with the parsed information
     */
    private static Host parseJson(String json) {
        Configuration conf = Configuration.defaultConfiguration();
        DocumentContext parsedJson = JsonPath.using(conf).parse(json);

        String ip = parsedJson.read("$.ip_str");
        List<String> hostnames = parsedJson.read("$.hostnames");
        String asn = parsedJson.read("$.asn");
        String city = parsedJson.read("$.city");
        String regionCode = parsedJson.read("$.region_code");
        String countryCode = parsedJson.read("$.country_code");
        String isp = parsedJson.read("$.isp");
        String os = parsedJson.read("$.data[0].os");
        List<Integer> ports = parsedJson.read("$.ports");

        return new Host(ip, hostnames, asn, city, regionCode, countryCode, isp, os, ports);
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
