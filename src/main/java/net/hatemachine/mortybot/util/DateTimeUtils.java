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
package net.hatemachine.mortybot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection of utilities for working with dates and times.
 */
public class DateTimeUtils {

    private static final String[] DATE_FORMATS = {
            "M/d/yyyy", "M/dd/yyyy", "MM/d/yyyy", "MM/dd/yyyy",
            "M-d-yyyy", "M-dd-yyyy", "MM-d-yyyy", "MM-dd-yyyy",
            "M.d.yyyy", "M.dd.yyyy", "MM.d.yyyy", "MM.dd.yyyy",
            "yyyy/M/d", "yyyy/M/dd", "yyyy/MM/d", "yyyy/MM/dd",
            "yyyy-M-d", "yyyy-M-dd", "yyyy-MM-d", "yyyy-MM-dd",
            "yyyy.M.d", "yyyy.M.dd", "yyyy.MM.d", "yyyy.MM.dd",
            "MMM d yyyy", "MMM dd yyyy", "MMM d, yyyy", "MMM dd, yyyy"
    };

    private static final Logger log = LoggerFactory.getLogger(DateTimeUtils.class);

    private DateTimeUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Converts a string representing a date into a LocalDate object. Supports a number of common date formats.
     *
     * @param dateString the string to convert into a date
     * @return the resulting date object
     * @throws IllegalArgumentException if unable to parse the string
     */
    public static LocalDate convertToDate(String dateString) {
        for (String format : DATE_FORMATS) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            try {
                return LocalDate.parse(dateString, formatter);
            } catch (DateTimeParseException ex) {
                // do nothing, try next format
            }
        }

        throw new IllegalArgumentException("Invalid date: " + dateString);
    }

    /**
     * Converts a duration into a human-readable format.
     *
     * @param duration the duration to format
     * @return a string representing the duration
     */
    public static String formatDuration(Duration duration) {
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
