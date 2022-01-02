package net.hatemachine.mortybot.rt;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RTHelper {

    private static final String BASE_URL = "https://www.rottentomatoes.com";
    private static final String SEARCH_URL = BASE_URL + "/search?search=";

    private static final Logger log = LoggerFactory.getLogger(RTHelper.class);

    private RTHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Movie> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("null or empty argument: query");
        }

        log.info("Searching Rotten Tomatoes for \"{}\"", query);

        String searchUrl = SEARCH_URL + URLEncoder.encode(query, StandardCharsets.UTF_8);
        List<Movie> results = new ArrayList<>();
        Document resultsPage = null;

        try {
            resultsPage = Jsoup.connect(searchUrl).get();
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        if (resultsPage != null) {
            Elements movieList = resultsPage.select("search-page-result[type=movie] > ul[slot=list] > search-page-media-row");
            for (Element movie : movieList) {
                String releaseYear = movie.attr("releaseyear");
                String tomatoMeterScore = movie.attr("tomatometerscore");
                String tomatoMeterState = movie.attr("tomatometerstate");
                Element aTag = movie.select("a[slot=title]").first();
                if (aTag != null) {
                    String url = aTag.attr("href");
                    String name = aTag.text();

                    results.add(new Movie(name, releaseYear, url, tomatoMeterScore, tomatoMeterState));
                }
            }
        }

        log.info("Found {} results", results.size());

        return results;
    }
}
