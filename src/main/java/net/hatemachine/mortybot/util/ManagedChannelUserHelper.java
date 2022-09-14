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

import net.hatemachine.mortybot.custom.entity.BotUserFlag;
import net.hatemachine.mortybot.custom.entity.ManagedChannelUserFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ManagedChannelUserHelper {

    private static final Logger log = LoggerFactory.getLogger(ManagedChannelHelper.class);

    public ManagedChannelUserHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Parses a comma-delimited list of managed channel user flags into a list, removing any duplicate or invalid flags.
     *
     * @param flags a comma-delimited string representing one or more flags
     * @return a list of managed channel user flags
     * @see BotUserFlag
     */
    public static List<ManagedChannelUserFlag> parseFlags(String flags) {
        return parseFlags(new ArrayList<>(), flags);
    }

    /**
     * Parses a comma-delimited list of managed channel user flags into a list, removing any duplicate or invalid flags.
     *
     * @param flagList a list of managed channel user flags that will be used as a starting list
     * @param flags a comma-delimited string representing one or more flags
     * @return a list of managed channel user flags
     * @see BotUserFlag
     */
    public static List<ManagedChannelUserFlag> parseFlags(List<ManagedChannelUserFlag> flagList, String flags) {
        for (String s : flags.split(",")) {
            String flagStr = s.trim().toUpperCase();

            try {
                ManagedChannelUserFlag flag = Enum.valueOf(ManagedChannelUserFlag.class, flagStr);

                if (!flagList.contains(flag)) {
                    flagList.add(flag);
                }

            } catch (IllegalArgumentException ex) {
                log.debug("Invalid flag: {}", flagStr);
            }
        }

        return flagList;
    }
}
