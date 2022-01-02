package net.hatemachine.mortybot.imdb;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Objects;

public class Title {

    private String name;
    private String url;
    private LocalDate publishDate;
    private Double rating;
    private String description;

    public Title(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public boolean hasPublishDate() {
        return this.publishDate != null;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public boolean hasRating() {
        return this.rating != null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean hasDescription() {
        return (this.description != null && !this.description.trim().isEmpty());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Title title = (Title) o;
        return name.equals(title.name) && url.equals(title.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (publishDate != null) {
            sb.append(" ");
            sb.append(String.format("(%d)", publishDate.get(ChronoField.YEAR)));
        }
        if (rating != null) {
            sb.append(" ");
            sb.append(String.format("[%.1f/%d]", rating, 10));
        }
        sb.append(" :: ");
        sb.append(url);
        return sb.toString();
    }
}