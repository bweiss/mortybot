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
package net.hatemachine.mortybot.services.espn.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Situation(LastPlay lastPlay, int down, int yardLine, int distance, boolean isRedZone, int homeTimeouts, int awayTimeouts) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LastPlay(String id, Type type, String text, int scoreValue, Team team, Probability probability, Drive drive, Start start, End end, int startYardage) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Type(String id, String text, String abbreviation) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Team(String id) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Probability(double tiePercentage, double homeWinPercentage, double awayWinPercentage, double secondsLeft) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Drive(String description, Start start, End end, TimeElapsed timeElapsed, String result) {

            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Start(int yardLine, String text) {}

            @JsonIgnoreProperties(ignoreUnknown = true)
            public record End(int yardLine, String text) {}

            @JsonIgnoreProperties(ignoreUnknown = true)
            public record TimeElapsed(String displayValue) {}
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Start(int yardLine) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record End(int yardLine, Team team) {

            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Team(String id) {}
        }
    }
}
