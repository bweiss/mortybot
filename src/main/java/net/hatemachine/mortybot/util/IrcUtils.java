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

import java.util.ArrayList;

public class IrcUtils {

    private IrcUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns address with a mask specified by type. <br/><br/>
     *
     * The idea is to implement the $mask() function from EPIC (<a href="http://epicsol.org/mask">...</a>). <br/><br/>
     *
     * $mask(1 nick!khaled@mardam.demon.co.uk)  returns *!*khaled@mardam.demon.co.uk <br/>
     * $mask(2 nick!khaled@mardam.demon.co.uk)  returns *!*@mardam.demon.co.uk <br/><br/>
     *
     * The available types are: <br/><br/>
     *
     * 0: *!user@host.domain <br/>
     * 1: *!*user@host.domain <br/>
     * 2: *!*@host.domain <br/>
     * 3: *!*user@*.domain <br/>
     * 4: *!*@*.domain <br/>
     * 5: nick!user@host.domain <br/>
     * 6: nick!*user@host.domain <br/>
     * 7: nick!*@host.domain <br/>
     * 8: nick!*user@*.domain <br/>
     * 9: nick!*@*.domain <br/>
     * 10:*!*user@* <br/>
     *
     * @param address the address you want to mask in the form nick!user@host
     * @param maskType an integer representing the mask type to use
     * @return the masked address
     */
    public static String maskAddress(String address, int maskType) {
        Validate.address(address);
        int firstExclamation = address.indexOf('!');
        int firstAt = address.indexOf('@');
        String nickname = address.substring(0, firstExclamation);
        String username = address.substring(firstExclamation + 1, firstAt);
        String hostname = address.substring(firstAt + 1);

        return switch (maskType) {
            case 0 -> String.format("*!%s@%s", username, hostname);
            case 1 -> String.format("*!*%s@%s", username, hostname);
            case 2 -> String.format("*!*@%s", hostname);
            case 3 -> String.format("*!*%s@%s", trimUsername(username), maskHostname(hostname));
            case 4 -> String.format("*!*@%s", maskHostname(hostname));
            case 5 -> String.format("%s!%s@%s", nickname, username, hostname);
            case 6 -> String.format("%s!*%s@%s", nickname, trimUsername(username), hostname);
            case 7 -> String.format("%s!*@%s", nickname, hostname);
            case 8 -> String.format("%s!*%s@%s", nickname, trimUsername(username), maskHostname(hostname));
            case 9 -> String.format("%s!*@%s", nickname, maskHostname(hostname));
            case 10 -> String.format("*!*%s@*", username);
            default -> "";
        };
    }

    /**
     * Mask a hostname using the same logic as the EPIC $mask() function. <br/><br/>
     *
     * Examples: <br/>
     *  "this.is.a.long.vhost.domain.com" -> "*.domain.com" <br/>
     *  "101.102.103.104" -> "101.102.103.*" <br/>
     *  "some.foreign.domain.co.uk" -> "*.domain.co.uk" <br/>
     *  "2603:300a:1d10:c000:de4a:3eff:fe88:cc5f" -> "2603:300a:*" <br/>
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
        } else if (dots.size() >= 2) {
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
