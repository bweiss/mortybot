package net.hatemachine.mortybot.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class DateTimeUtils {

    private DateTimeUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String printDuration(Duration duration) {
        List<String> parts = new ArrayList<>();

        long days = duration.toDaysPart();
        if (days > 0) {
            parts.add((int)days + "d");
        }

        int hours = duration.toHoursPart();
        if (hours > 0) {
            parts.add(hours + "h");
        }

        int minutes = duration.toMinutesPart();
        if (minutes > 0) {
            parts.add(minutes + "m");
        }

        int seconds = duration.toSecondsPart();
        if (seconds > 0 || parts.isEmpty()) {
            parts.add(seconds + "s");
        }

        return String.join(", ", parts);
    }
}
