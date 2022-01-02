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
        this.url = year;
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
        if (this.hasState() && this.hasScore()) {
            sb.append(String.format(" - %s%% [%s]", score, state));
        }
        return sb.toString();
    }
}
