package net.hatemachine.mortybot.util;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.regex.Pattern;

public class Validate {

    private static final int MAX_BOT_USER_NAME_LENGTH = 16;
    private static final Pattern BOT_USER_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("[a-zA-Z0-9\\[\\]|_-]+");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9~]+");
    private static final Pattern HOSTNAME_PATTERN = Pattern.compile("[a-zA-Z0-9.:-]+");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("[a-zA-Z0-9\\[\\]|_-]+![a-zA-Z0-9~]+@[a-zA-Z0-9.:-]+");
    private static final Pattern HOSTMASK_PATTERN = Pattern.compile("[\\\\*a-zA-Z0-9\\[\\]|_-]+![\\\\*a-zA-Z0-9~]+@[\\\\*a-zA-Z0-9.:-]+");
    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile("^\\d{5}(?:[-\\s]\\d{4})?$");

    private Validate() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isBotUserName(String s) {
        return s.length() <= MAX_BOT_USER_NAME_LENGTH && BOT_USER_NAME_PATTERN.matcher(s).matches();
    }

    public static boolean isNickname(String s) {
        return NICKNAME_PATTERN.matcher(s).matches();
    }

    public static boolean isUsername(String s) {
        return USERNAME_PATTERN.matcher(s).matches();
    }

    public static boolean isHostname(String s) {
        return HOSTNAME_PATTERN.matcher(s).matches();
    }

    public static boolean isAddress(String s) {
        return ADDRESS_PATTERN.matcher(s).matches();
    }

    public static boolean isHostmask(String s) {
        return HOSTMASK_PATTERN.matcher(s).matches();
    }

    public static boolean isNumeric(String s) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(s, pos);
        return s.length() == pos.getIndex();
    }

    private static boolean isZipCode(String zipCode) {
        return ZIP_CODE_PATTERN.matcher(zipCode).matches();
    }

    public static Object notNull(Object o) {
        if (o == null)
            throw new IllegalArgumentException("Object is null");
        return o;
    }

    public static String notNullOrEmpty(String s) {
        if (s == null || s.trim().isEmpty())
            throw new IllegalArgumentException("String is null or empty");
        return s;
    }

    public static String botUserName(String s) {
        if (!isBotUserName(s))
            throw new IllegalArgumentException("Invalid bot user name");
        return s;
    }

    public static String nickname(String s) {
        if (!isNickname(s))
            throw new IllegalArgumentException("Invalid nickname");
        return s;
    }

    public static String username(String s) {
        if (!isUsername(s))
            throw new IllegalArgumentException("Invalid username");
        return s;
    }

    public static String hostname(String s) {
        if (!isHostname(s))
            throw new IllegalArgumentException("Invalid hostname");
        return s;
    }

    public static String address(String s) {
        if (!isAddress(s))
            throw new IllegalArgumentException("Invalid address");
        return s;
    }

    public static String hostmask(String s) {
        if (!isHostmask(s))
            throw new IllegalArgumentException("Invalid hostmask");
        return s;
    }

    public static String zipCode(String s) {
        if (!isZipCode(s))
            throw new IllegalArgumentException("Invalid zip code");
        return s;
    }
}
