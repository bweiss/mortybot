package net.hatemachine.mortybot.imdb;

import java.util.Objects;

public class SearchResult {

    public enum Type {
        NM,
        TT
    }

    private final String name;
    private final String url;
    private final Type type;

    public SearchResult(String name, String url, Type type) {
        this.name = name;
        this.url = url;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchResult that = (SearchResult) o;
        return name.equals(that.name) && url.equals(that.url) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url, type);
    }

    @Override
    public String toString() {
        return String.format("%s :: %s", name, url);
    }
}
