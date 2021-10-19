package net.hatemachine.mortybot.util;

import org.pircbotx.Channel;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class IrcUtils {

    public static final Pattern NICKNAME_PATTERN = Pattern.compile("[a-zA-Z0-9\\[\\]|_-]+");
    public static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9~]+");
    public static final Pattern HOSTNAME_PATTERN = Pattern.compile("[a-zA-Z0-9.:-]+");
    public static final Pattern ADDRESS_PATTERN  = Pattern.compile("[a-zA-Z0-9\\[\\]|_-]+![a-zA-Z0-9~]+@[a-zA-Z0-9.:-]+");
    public static final Pattern HOSTMASK_PATTERN = Pattern.compile("[\\\\*a-zA-Z0-9\\[\\]|_-]+![\\\\*a-zA-Z0-9~]+@[\\\\*a-zA-Z0-9.:-]+");

    private IrcUtils() {}

    /**
     * Returns address with a mask specified by type.
     *
     * The idea is to implement the $mask() function from EPIC (http://epicsol.org/mask).
     *
     * $mask(1 nick!khaled@mardam.demon.co.uk)  returns *!*khaled@mardam.demon.co.uk
     * $mask(2 nick!khaled@mardam.demon.co.uk)  returns *!*@mardam.demon.co.uk
     *
     * The available types are:
     *
     * 0: *!user@host.domain
     * 1: *!*user@host.domain
     * 2: *!*@host.domain
     * 3: *!*user@*.domain
     * 4: *!*@*.domain
     * 5: nick!user@host.domain
     * 6: nick!*user@host.domain
     * 7: nick!*@host.domain
     * 8: nick!*user@*.domain
     * 9: nick!*@*.domain
     * 10:*!*user@*
     *
     * @param method an integer representing the masking method
     * @param address the address you want to mask in the form nick!user@host
     * @return the masked address
     */
    public static String maskAddress(int method, String address) {
        if (!isValidAddress(address)) {
            throw new IllegalArgumentException("invalid address: " + address);
        }
        int firstExclamation = address.indexOf('!');
        int firstAt = address.indexOf('@');
        String nickname = address.substring(0, firstExclamation);
        String username = address.substring(firstExclamation + 1, firstAt);
        String hostname = address.substring(firstAt + 1);
        String returnBuffer;

        switch (method) {
            case 0:
                returnBuffer = String.format("*!%s@%s", username, hostname);
                break;
            case 1:
                returnBuffer = String.format("*!*%s@%s", username, hostname);
                break;
            case 2:
                returnBuffer = String.format("*!*@%s", hostname);
                break;
            case 3:
                returnBuffer = String.format("*!*%s@%s", trimUsername(username), maskHostname(hostname));
                break;
            case 4:
                returnBuffer = String.format("*!*@%s", maskHostname(hostname));
                break;
            case 5:
                returnBuffer = String.format("%s!%s@%s", nickname, username, hostname);
                break;
            case 6:
                returnBuffer = String.format("%s!*%s@%s", nickname, trimUsername(username), hostname);
                break;
            case 7:
                returnBuffer = String.format("%s!*@%s", nickname, hostname);
                break;
            case 8:
                returnBuffer = String.format("%s!*%s@%s", nickname, trimUsername(username), maskHostname(hostname));
                break;
            case 9:
                returnBuffer = String.format("%s!*@%s", nickname, maskHostname(hostname));
                break;
            case 10:
                returnBuffer = String.format("*!*%s@*", username);
                break;
            default:
                returnBuffer = "";
                break;
        }

        return returnBuffer;
    }

    /**
     * Mask a hostname using the same logic as the EPIC $mask() function.
     *
     * Examples:
     *  "this.is.a.long.vhost.domain.com" -> "*.domain.com"
     *  "101.102.103.104" -> "101.102.103.*"
     *  "some.foreign.domain.co.uk" -> "*.domain.co.uk"
     *  "2603:300a:1d10:c000:de4a:3eff:fe88:cc5f" -> "2603:300a:*"
     *
     * @param hostname the hostname to be masked
     * @return the hostname with masking applied
     */
    public static String maskHostname(String hostname) {
        ArrayList<Integer> colons = new ArrayList<>();
        ArrayList<Integer> dots = new ArrayList<>();
        int nonDigitCount = 0;
        String hostnameMask = "";

        for (int i = 0; i < hostname.length(); i++) {
            char ch = hostname.charAt(i);
            if (ch == ':') {
                colons.add(i);
            } else if (ch == '.') {
                dots.add(i);
            } else if (!Character.isDigit(ch)) {
                nonDigitCount++;
            }
        }

        if (colons.size() >= 2) {
            hostnameMask = hostname.substring(0, colons.get(1) + 1) + "*";
        } else if (nonDigitCount == 0 && dots.size() == 3) {
            hostnameMask = hostname.substring(0, dots.get(2) + 1) + "*";
        } else if (dots.size() >= 2){
            String domain = hostname.substring(dots.get(dots.size() - 2) + 1);
            if (domain.length() < 6) {
                if (dots.size() >= 3) {
                    hostnameMask = "*" + hostname.substring(dots.get(dots.size() - 3));
                }
            } else {
                hostnameMask = "*" + hostname.substring(dots.get(dots.size() - 2));
            }
        } else {
            hostnameMask = hostname;
        }

        return hostnameMask;
    }

    /**
     * See if a nickname is valid.
     *
     * @param s the string that you want to check
     * @return true if the string is a valid nickname
     */
    public static boolean isValidNickname(String s) {
        var matcher = NICKNAME_PATTERN.matcher(s);
        return matcher.matches();
    }

    /**
     * See if a username is valid.
     *
     * @param s the string that you want to check
     * @return true if the string is a valid username
     */
    public static boolean isValidUsername(String s) {
        var matcher = USERNAME_PATTERN.matcher(s);
        return matcher.matches();
    }

    /**
     * See if a hostname is valid.
     *
     * @param s the string that you want to check
     * @return true if the string is a valid hostname
     */
    public static boolean isValidHostname(String s) {
        var matcher = HOSTNAME_PATTERN.matcher(s);
        return matcher.matches();
    }

    /**
     * See if an address is valid. It should be in the form of "nick!user@host".
     *
     * @param s the string that you want to check
     * @return true if the string is a valid address
     */
    public static boolean isValidAddress(String s) {
        var matcher = ADDRESS_PATTERN.matcher(s);
        return matcher.matches();
    }

    /**
     * See if a hostmask is valid. It should be in the form of "nick!user@host" and may contain wildcards.
     *
     * @param s the string that you want to check
     * @return true if the string is a valid hostmask
     */
    public static boolean isValidHostmask(String s) {
        var matcher = HOSTMASK_PATTERN.matcher(s);
        return matcher.matches();
    }

    /**
     * Validate a nickname.
     *
     * @param s the string that you want to validate
     * @return the input string if it is valid
     * @throws IllegalArgumentException if the nickname is not valid
     */
    public static String validateNickname(String s) {
        if (!isValidNickname(s)) {
            throw new IllegalArgumentException("invalid nickname: " + s);
        }
        return s;
    }

    /**
     * Validate a username.
     *
     * @param s the string that you want to validate
     * @return the input string if it is valid
     * @throws IllegalArgumentException if the username is not valid
     */
    public static String validateUsername(String s) {
        if (!isValidUsername(s)) {
            throw new IllegalArgumentException("invalid username: " + s);
        }
        return s;
    }

    /**
     * Validate a hostname.
     *
     * @param s the string that you want to validate
     * @return the input string if it is valid
     * @throws IllegalArgumentException if the hostname is not valid
     */
    public static String validateHostname(String s) {
        if (!isValidHostname(s)) {
            throw new IllegalArgumentException("invalid hostname: " + s);
        }
        return s;
    }

    /**
     * Validate an address. It should be in the form of "nick!user@host".
     *
     * @param s the string that you want to validate
     * @return the input string if it is valid
     * @throws IllegalArgumentException if the address is not valid
     */
    public static String validateAddress(String s) {
        if (!isValidAddress(s)) {
            throw new IllegalArgumentException("invalid address: " + s);
        }
        return s;
    }

    /**
     * Validate a hostmask. It should be in the form of "nick!user@host" and may contain wildcards.
     *
     * @param s the string that you want to validate
     * @return the input string if it is valid
     * @throws IllegalArgumentException if the address is not valid
     */
    public static String validateHostmask(String s) {
        if (!isValidHostmask(s)) {
            throw new IllegalArgumentException("invalid hostmask: " + s);
        }
        return s;
    }

    /**
     * Find out if a user has operator status on a channel.
     *
     * @param targetUser the user to check for operator status
     * @param channel the channel you want to check
     * @return true if user is oped on channel
     */
    public static boolean userHasOps(String targetUser, Channel channel) {
        return channel.getUsers()
                .stream()
                .anyMatch(u -> u.getNick().equalsIgnoreCase(targetUser));
    }

    /**
     * Trim the front of a username until it's 7 characters or fewer.
     *
     * @param username the username that you want to trim
     * @return the username after trimming
     */
    private static String trimUsername(String username) {
        if (username.length() > 7) {
            int diff = username.length() - 7;
            return username.substring(diff);
        } else {
            return username;
        }
    }
}
