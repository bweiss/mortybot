/*
 * MortyBot - An IRC bot built on the PircBotX framework.
 * Copyright © 2022 Brian Weiss (brianmweiss@gmail.com)
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
package net.hatemachine.mortybot.rt;

import java.util.Objects;

public class Movie {

    private String title;
    private String year;
    private String url;
    private String score;
    private String state;

    public Movie(String title, String year, String url) {
        this.title = title;
        this.year = year;
        this.url = url;
    }

    public Movie(String title, String year, String url, String score, String state) {
        this.title = title;
        this.year = year;
        this.url = url;
        this.score = score;
        this.state = state;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public boolean hasScore() {
        return (this.score != null && !this.score.trim().isEmpty());
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean hasState() {
        return (this.state != null && !this.state.trim().isEmpty());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return title.equals(movie.title) && year.equals(movie.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, year);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(String.format("%s (%s)", title, year));
        if (this.hasScore()) {
            sb.append(String.format(" - %s%%", score));
        }
        if (this.hasState()) {
            sb.append(String.format(" [%s]", state));
        }
        return sb.toString();
    }
}
