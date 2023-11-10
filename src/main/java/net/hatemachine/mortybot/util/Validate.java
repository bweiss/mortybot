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

import com.google.common.collect.ImmutableMap;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class to help validate things.
 */
public class Validate {

    private static final int MAX_BOT_USER_NAME_LENGTH = 16;

    private static final Pattern BOT_USER_NAME_PATTERN = Pattern.compile("[\\w]+");
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("[a-zA-Z0-9\\[\\]|_-]+");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9~]+");
    private static final Pattern HOSTNAME_PATTERN = Pattern.compile("[a-zA-Z0-9.:-]+");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("[a-zA-Z0-9\\[\\]|_-]+![a-zA-Z0-9~]+@[a-zA-Z0-9.:-]+");
    private static final Pattern HOSTMASK_PATTERN = Pattern.compile("[\\\\*a-zA-Z0-9\\[\\]|_-]+![\\\\*a-zA-Z0-9~]+@[\\\\*a-zA-Z0-9.:-]+");
    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile("^\\d{5}(?:[-\\s]\\d{4})?$");

    // Default channel prefixes (to be used when we can't get from 005 numeric)
    private static final String CHANNEL_PREFIXES = "&#";

    // Map of valid channel modes and whether they have additional parameters (e.g. +k channelkey)
    public static final ImmutableMap<Character, Boolean> CHANNEL_MODE_HAS_PARAMS = ImmutableMap.<Character, Boolean> builder()
            .put('o', true) // nickname
            .put('p', false)
            .put('s', false)
            .put('i', false)
            .put('t', false)
            .put('n', false)
            .put('m', false)
            .put('l', true) // integer
            .put('b', true) // hostmask
            .put('v', true) // nickname
            .put('k', true) // channel key
            .build();

    private Validate() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Checks to see if a string is a valid bot user name.
     *
     * @param s the string to be checked
     * @return true if the string is a valid bot user name, otherwise false
     */
    public static boolean isBotUserName(String s) {
        return s.length() <= MAX_BOT_USER_NAME_LENGTH && BOT_USER_NAME_PATTERN.matcher(s).matches();
    }

    /**
     * Checks to see if a string is a valid nickname.
     *
     * @param s the string to be checked
     * @return true if the string is a valid nickname, otherwise false
     */
    public static boolean isNickname(String s) {
        return NICKNAME_PATTERN.matcher(s).matches();
    }

    /**
     * Checks to see if a string is a valid username.
     *
     * @param s the string to be checked
     * @return true if the string is a valid username, otherwise false
     */
    public static boolean isUsername(String s) {
        return USERNAME_PATTERN.matcher(s).matches();
    }

    /**
     * Checks to see if a string is a valid hostname.
     *
     * @param s the string to be checked
     * @return true if the string is a valid hostname, otherwise false
     */
    public static boolean isHostname(String s) {
        return HOSTNAME_PATTERN.matcher(s).matches();
    }

    /**
     * Checks to see if a string is a valid IRC address.
     *
     * @param s the string to be checked
     * @return true if the string is a valid IRC address, otherwise false
     */
    public static boolean isAddress(String s) {
        return ADDRESS_PATTERN.matcher(s).matches();
    }

    /**
     * Checks to see if a string is a valid hostmask.
     *
     * @param s the string to be checked
     * @return true if the string is a valid hostmask, otherwise false
     */
    public static boolean isHostmask(String s) {
        return HOSTMASK_PATTERN.matcher(s).matches();
    }

    /**
     * Checks to see if a string is a numeric value.
     *
     * @param s the string to be checked
     * @return true if the string is a numeric, otherwise false
     */
    public static boolean isNumeric(String s) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(s, pos);
        return s.length() == pos.getIndex();
    }

    /**
     * Checks to see if a string is a valid ZIP code.
     *
     * @param s the string to be checked
     * @return true if the string is a valid ZIP code, otherwise false
     */
    public static boolean isZipCode(String s) {
        return ZIP_CODE_PATTERN.matcher(s).matches();
    }

    /**
     * Checks to see if a string is a valid channel name.
     *
     * @param s the string to be checked
     * @return true if the string is a valid channel name, otherwise false
     */
    public static boolean isChannelName(String s) {
        return isChannelName(s, CHANNEL_PREFIXES);
    }

    /**
     * Checks to see if a string is a valid channel name.
     *
     * @param s the string to be checked
     * @param prefixes a string representing characters that represent a valid channel prefix
     * @return true if the string is a valid channel name, otherwise false
     */
    // TODO adhere to RFC1459 and check for ^G (ASCII 7) and comma in addition to the prefixes
    public static boolean isChannelName(String s, String prefixes) {
        if (prefixes == null || prefixes.trim().isBlank()) {
            prefixes = CHANNEL_PREFIXES;
        }

        return prefixes.chars().anyMatch(c -> c == s.charAt(0));
    }

    /**
     * Validates a list of arguments to ensure it is not null, empty, or has null or blank items.
     *
     * @param args the list or arguments to validate
     * @param minSize the minimum size of the list
     * @throws IllegalArgumentException if list is null, empty, or has null or blank items
     */
    public static void arguments(List<String> args, Integer minSize) {
        if (args == null || args.isEmpty() || args.size() < minSize) {
            throw new IllegalArgumentException("Not enough arguments");
        }

        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            if (arg == null || arg.isBlank()) {
                throw new IllegalArgumentException("Item in position " + i + " is null or blank");
            }
        }
    }

    /**
     * Checks that an object is not null.
     *
     * @param o the object to check
     * @return the object if it is not null
     * @throws IllegalArgumentException if the object is null
     */
    public static Object notNull(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
        return o;
    }

    /**
     * Checks that an string is not null or blank.
     *
     * @param s the string to check
     * @return the string if it is not null or blank
     * @throws IllegalArgumentException if the string is null or blank
     */
    public static String notNullOrBlank(String s) {
        return notNullOrBlank(s, "String cannot be null or blank");
    }

    /**
     * Checks if a string is null or blank.
     *
     * @param s the string to check
     * @param message the message to include in the thrown exception
     * @return the string if it is not null or blank
     * @throws IllegalArgumentException if the string is null or blank
     */
    public static String notNullOrBlank(String s, String message) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return s;
    }

    /**
     * Checks if a list of strings is null or empty.
     *
     * @param list the list to check
     * @return the list if it is not null or empty
     */
    public static List<String> notNullOrEmpty(List<String> list) {
        return notNullOrEmpty(list, "List cannot be null or empty");
    }

    /**
     * Checks if a list of strings is null or empty.
     *
     * @param list the list to check
     * @param message the message to include in the thrown exception
     * @return the list if it is not null or empty
     */
    public static List<String> notNullOrEmpty(List<String> list, String message) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return list;
    }

    /**
     * Checks that a string is a valid bot user name.
     *
     * @param s the string to check
     * @return the string if it is a valid bot user name
     * @throws IllegalArgumentException if the string is not a valid bot user name
     */
    public static String botUserName(String s) {
        if (!isBotUserName(s)) {
            throw new IllegalArgumentException("Invalid bot user name");
        }
        return s;
    }

    /**
     * Checks that a string is a valid IRC nickname.
     *
     * @param s the string to check
     * @return the string if it is a valid IRC nickname
     * @throws IllegalArgumentException if the string is not a valid IRC nickname
     */
    public static String nickname(String s) {
        if (!isNickname(s)) {
            throw new IllegalArgumentException("Invalid nickname");
        }
        return s;
    }

    /**
     * Checks that a string is a valid username.
     *
     * @param s the string to check
     * @return the string if it is a valid username
     * @throws IllegalArgumentException if the string is not a valid username
     */
    public static String username(String s) {
        if (!isUsername(s)) {
            throw new IllegalArgumentException("Invalid username");
        }
        return s;
    }

    /**
     * Checks that a string is a valid hostname.
     *
     * @param s the string to check
     * @return the string if it is a valid hostname
     * @throws IllegalArgumentException if the string is not a valid hostname
     */
    public static String hostname(String s) {
        if (!isHostname(s)) {
            throw new IllegalArgumentException("Invalid hostname");
        }
        return s;
    }

    /**
     * Checks that a string is a valid IRC address.
     *
     * @param s the string to check
     * @return the string if it is a valid IRC address
     * @throws IllegalArgumentException if the string is not a valid IRC address
     */
    public static String address(String s) {
        if (!isAddress(s)) {
            throw new IllegalArgumentException("Invalid address");
        }
        return s;
    }

    /**
     * Checks that a string is a valid IRC hostmask.
     *
     * @param s the string to check
     * @return the string if it is a valid IRC hostmask
     * @throws IllegalArgumentException if the string is not a valid IRC hostmask
     */
    public static String hostmask(String s) {
        if (!isHostmask(s)) {
            throw new IllegalArgumentException("Invalid hostmask");
        }
        return s;
    }

    /**
     * Checks that a string is a valid ZIP code
     *
     * @param s the string to check
     * @return the string if it is a valid ZIP code
     * @throws IllegalArgumentException if the string is not a valid ZIP code
     */
    public static String zipCode(String s) {
        if (!isZipCode(s)) {
            throw new IllegalArgumentException("Invalid zip code");
        }
        return s;
    }

    /**
     * Checks that a string is a valid IRC channel name.
     *
     * @param s the string to check
     * @return the string if it is a valid IRC channel name
     * @throws IllegalArgumentException if the string is not a valid IRC channel name
     */
    public static String channelName(String s) {
        return channelName(s, CHANNEL_PREFIXES);
    }

    /**
     * Checks that a string is a valid IRC nickname.
     *
     * @param s the string to check
     * @return the string if it is a valid IRC nickname
     * @throws IllegalArgumentException if the string is not a valid IRC nickname
     */
    public static String channelName(String s, String prefixes) {
        if (!isChannelName(s, prefixes)) {
            throw new IllegalArgumentException("Invalid channel name");
        }
        return s;
    }
}
