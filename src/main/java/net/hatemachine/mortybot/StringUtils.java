package net.hatemachine.mortybot;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private StringUtils() {}

    public static boolean isNumeric(String s) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(s, pos);
        return s.length() == pos.getIndex();
    }

    public static boolean isValidString(String s) {
        return (s != null && !s.trim().isEmpty());
    }

    public static String validateString(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("Null or empty argument provided");
        }
        return s;
    }

    /**
     * Converts a string containing wildcard characters (*, ?) into a regex string so it can be
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
        StringBuffer sb = new StringBuffer();
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
