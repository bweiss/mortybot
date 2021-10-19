package net.hatemachine.mortybot.util;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern BOT_USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9]+");

    private StringUtils() {}

    /**
     * See if a string is numeric.
     *
     * @param s the string you want to check
     * @return true if the string contains only numeric characters
     */
    public static boolean isNumeric(String s) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(s, pos);
        return s.length() == pos.getIndex();
    }

    /**
     * See if a string is valid (not null or empty).
     *
     * @param s the string you want to check
     * @return true if the string is not null or empty
     */
    public static boolean isValidString(String s) {
        return (s != null && !s.trim().isEmpty());
    }

    /**
     * See if a string can pass for a valid bot username according to BOT_USERNAME_PATTERN
     *
     * @param s the string that you want to check
     * @return true if the string can be used as a valid username with the bot
     */
    public static boolean isValidBotUsername(String s) {
        var matcher = BOT_USERNAME_PATTERN.matcher(s);
        return matcher.matches();
    }

    /**
     * Validate a string ensuring it is not null or empty.
     *
     * @param s the string you want to validate
     * @return the string itself if valid
     * @throws IllegalArgumentException if the string is null or empty
     */
    public static String validateString(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("Null or empty argument provided");
        }
        return s;
    }

    /**
     * Validate that a string is legal for a bot username.
     *
     * @param s the string that you want to check
     * @return the input string if it is valid
     * @throws IllegalArgumentException if the string is not valid as a bot username
     */
    public static String validateBotUsername(String s) {
        String username = validateString(s);
        if (!isValidBotUsername(s)) {
            throw new IllegalArgumentException("Invalid bot username: " + username);
        }
        return s;
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
