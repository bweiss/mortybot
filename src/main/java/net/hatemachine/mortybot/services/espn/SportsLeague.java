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

public enum SportsLeague {
    CBB("Men's College Basketball",         CompetitionType.TEAM),
    CFB("Men's College Football",           CompetitionType.TEAM),
    MLB("Major League Baseball",            CompetitionType.TEAM),
    NBA("National Basketball Association",  CompetitionType.TEAM),
    NFL("National Football League",         CompetitionType.TEAM),
    NHL("National Hockey League",           CompetitionType.TEAM),
    UFC("Ultimate Fighting Championship",   CompetitionType.INDIVIDUAL);

    private String description;
    private CompetitionType competitionType;

    SportsLeague(String description, CompetitionType competitionType) {
        this.description = description;
        this.competitionType = competitionType;
    }

    public String getDescription() {
        return description;
    }

    public CompetitionType getCompetitionType() {
        return competitionType;
    }
}
