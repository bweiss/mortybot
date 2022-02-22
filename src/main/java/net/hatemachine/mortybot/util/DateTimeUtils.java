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
