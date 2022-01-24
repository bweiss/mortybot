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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private StringUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Converts a string containing wildcard characters (*, ?) into a regex string so that it can be
     * used for pattern matching. In the context of the bot, this is mostly useful for matching
     * hostmasks to userhosts (e.g. "*!*@somedomain.com" to "derp!someguy@somedomain.com).
     *
     * Posted on stackoverflow.com by J. Hanney (https://stackoverflow.com/users/7326283/j-hanney)
     *
     * @param wildcardStr String containing wildcards such as * and ?
     * @return String that can be used as regex pattern
     */
    public static String wildcardToRegex(String wildcardStr) {
        Pattern regex = Pattern.compile("[^*?\\\\]+|(\\*)|(\\?)|(\\\\)");
        Matcher m = regex.matcher(wildcardStr);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            if (m.group(1) != null) m.appendReplacement(sb, ".*");
            else if (m.group(2) != null) m.appendReplacement(sb, ".");
            else if (m.group(3) != null) m.appendReplacement(sb, "\\\\\\\\");
            else m.appendReplacement(sb, "\\\\Q" + m.group(0) + "\\\\E");
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
