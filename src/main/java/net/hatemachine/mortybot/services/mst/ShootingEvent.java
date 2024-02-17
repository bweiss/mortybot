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

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "date",
        "killed",
        "wounded",
        "city",
        "state",
        "names",
        "sources"
})

public class ShootingEvent {
    @JsonProperty("date")
    private LocalDateTime date;
    @JsonProperty("killed")
    private String killed;
    @JsonProperty("wounded")
    private String wounded;
    @JsonProperty("city")
    private String city;
    @JsonProperty("state")
    private String state;
    @JsonProperty("names")
    private List<String> names;
    @JsonProperty("sources")
    private List<String> sources;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

    /**
     * No args constructor for use in serialization
     */
    public ShootingEvent() {
    }

    /**
     * Represents a mass shooting event.
     *
     * @param date the date the event occurred
     * @param wounded the number of wounded
     * @param names the names involved
     * @param sources links to source articles
     * @param city the city the event occurred in
     * @param state the state the event occurred in
     * @param killed the number of killed
     */
    public ShootingEvent(LocalDateTime date, String killed, String wounded, String city, String state, List<String> names, List<String> sources) {
        super();
        this.date = date;
        this.killed = killed;
        this.wounded = wounded;
        this.city = city;
        this.state = state;
        this.names = names;
        this.sources = sources;
    }

    @JsonProperty("date")
    public LocalDateTime getDate() {
        return date;
    }

    @JsonProperty("date")
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @JsonProperty("killed")
    public String getKilled() {
        return killed;
    }

    @JsonProperty("killed")
    public void setKilled(String killed) {
        this.killed = killed;
    }

    @JsonProperty("wounded")
    public String getWounded() {
        return wounded;
    }

    @JsonProperty("wounded")
    public void setWounded(String wounded) {
        this.wounded = wounded;
    }

    @JsonProperty("city")
    public String getCity() {
        return city;
    }

    @JsonProperty("city")
    public void setCity(String city) {
        this.city = city;
    }

    @JsonProperty("state")
    public String getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("names")
    public List<String> getNames() {
        return names;
    }

    @JsonProperty("names")
    public void setNames(List<String> names) {
        this.names = names;
    }

    @JsonProperty("sources")
    public List<String> getSources() {
        return sources;
    }

    @JsonProperty("sources")
    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        String nullStr = "<null>";
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(((this.date == null) ? nullStr : this.date));
        sb.append("] ");
        sb.append(((this.city == null) ? nullStr : this.city));
        sb.append(", ");
        sb.append(((this.state == null) ? nullStr : this.state));
        sb.append(" - ");
        sb.append("Killed: ");
        sb.append(((this.killed == null) ? nullStr : this.killed));
        sb.append(", ");
        sb.append("Wounded: ");
        sb.append(((this.wounded == null) ? nullStr : this.wounded));
        sb.append(", ");
        sb.append("Names: ");
        sb.append(((this.names == null) ? nullStr : this.names));
        sb.append("[Sources: ");
        sb.append(((this.sources == null) ? nullStr : this.sources));
        sb.append(']');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result * 31) + ((this.date == null) ? 0 :this.date.hashCode()));
        result = ((result * 31) + ((this.wounded == null) ? 0 :this.wounded.hashCode()));
        result = ((result * 31) + ((this.names == null) ? 0 :this.names.hashCode()));
        result = ((result * 31) + ((this.sources == null) ? 0 :this.sources.hashCode()));
        result = ((result * 31) + ((this.city == null) ? 0 :this.city.hashCode()));
        result = ((result * 31) + ((this.state == null) ? 0 :this.state.hashCode()));
        result = result * 31 + this.additionalProperties.hashCode();
        result = ((result * 31) + ((this.killed == null) ? 0 :this.killed.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ShootingEvent rhs)) {
            return false;
        }
        return ((((((((Objects.equals(this.date, rhs.date)) && (Objects.equals(this.wounded, rhs.wounded))) && (Objects.equals(this.names, rhs.names))) && (Objects.equals(this.sources, rhs.sources))) && (Objects.equals(this.city, rhs.city))) && (Objects.equals(this.state, rhs.state))) && (Objects.equals(this.additionalProperties, rhs.additionalProperties))) && (Objects.equals(this.killed, rhs.killed)));
    }
}