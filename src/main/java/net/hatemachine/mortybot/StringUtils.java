package net.hatemachine.mortybot;

import java.text.NumberFormat;
import java.text.ParsePosition;

public class StringUtils {

    // do not allow instances
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
}
