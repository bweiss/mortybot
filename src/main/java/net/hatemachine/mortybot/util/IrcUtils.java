package net.hatemachine.mortybot.util;

import org.pircbotx.Channel;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.hatemachine.mortybot.util.StringUtils.validateString;

public class IrcUtils {

    private IrcUtils() {}

    /**
     * Returns address with a mask specified by type.
     *
     * The idea is to implement the $mask() function from EPIC (http://www.epicsol.org).
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
     */
    public static String maskAddress(int method, String address) {
        Pattern p = Pattern.compile("[a-zA-Z0-9\\[\\]|_-]+![a-zA-Z0-9~]+@[a-zA-Z0-9.:-]+");
        Matcher m = p.matcher(validateString(address));
        if (!m.matches())
            throw new IllegalArgumentException("invalid address: " + address);

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
    private static String maskHostname(String hostname) {
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
