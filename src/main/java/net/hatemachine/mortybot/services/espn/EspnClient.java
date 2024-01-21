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
package net.hatemachine.mortybot.services.espn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.hatemachine.mortybot.services.espn.model.Scoreboard;
import net.hatemachine.mortybot.util.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;

public class EspnClient {

    private static final String BASE_ENDPOINT = "http://site.api.espn.com/apis/site/v2/sports";

    // Scoreboard endpoints
    private static final String CBB_SCOREBOARD_ENDPOINT = BASE_ENDPOINT + "/basketball/mens-college-basketball/scoreboard";
    private static final String CFB_SCOREBOARD_ENDPOINT = BASE_ENDPOINT + "/football/college-football/scoreboard";
    private static final String MLB_SCOREBOARD_ENDPOINT = BASE_ENDPOINT + "/baseball/mlb/scoreboard";
    private static final String NBA_SCOREBOARD_ENDPOINT = BASE_ENDPOINT + "/basketball/nba/scoreboard";
    private static final String NFL_SCOREBOARD_ENDPOINT = BASE_ENDPOINT + "/football/nfl/scoreboard";
    private static final String NHL_SCOREBOARD_ENDPOINT = BASE_ENDPOINT + "/hockey/nhl/scoreboard";
    private static final String UFC_SCOREBOARD_ENDPOINT = BASE_ENDPOINT + "/mma/ufc/scoreboard";

    private static final Duration WEBCLIENT_TIMEOUT = Duration.ofSeconds(20);

    private static final String[] WEBCLIENT_HEADERS = {
            "User-Agent", "Java HttpClient Bot",
            "Accept", "application/json"
    };

    private static final Logger log = LoggerFactory.getLogger(EspnClient.class);

    public WebClient webClient;

    public EspnClient() {
        this.webClient = new WebClient(WEBCLIENT_TIMEOUT, WEBCLIENT_HEADERS);
    }

    public Optional<Scoreboard> scoreboard(SportsLeague league) {
        Scoreboard scoreboard = null;

        Optional<String> json = switch (league) {
            case CBB -> webClient.get(CBB_SCOREBOARD_ENDPOINT);
            case CFB -> webClient.get(CFB_SCOREBOARD_ENDPOINT);
            case MLB -> webClient.get(MLB_SCOREBOARD_ENDPOINT);
            case NBA -> webClient.get(NBA_SCOREBOARD_ENDPOINT);
            case NFL -> webClient.get(NFL_SCOREBOARD_ENDPOINT);
            case NHL -> webClient.get(NHL_SCOREBOARD_ENDPOINT);
            case UFC -> webClient.get(UFC_SCOREBOARD_ENDPOINT);
        };

        if (json.isPresent()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                scoreboard = mapper.readValue(json.get(), Scoreboard.class);
            } catch (JsonMappingException e) {
                log.error("Failed to map JSON to Scoreboard class: {}", e.getMessage(), e);
            } catch (JsonProcessingException e) {
                log.error("Error processing JSON: {}", e.getMessage(), e);
            }
        }

        if (scoreboard != null) {
            return Optional.of(scoreboard);
        } else {
            return Optional.empty();
        }
    }
}
