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
import net.hatemachine.mortybot.dao.BotUserDao;
import net.hatemachine.mortybot.dao.ManagedChannelUserDao;
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.model.ManagedChannel;
import net.hatemachine.mortybot.model.ManagedChannelUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

import static net.hatemachine.mortybot.util.StringUtils.wildcardToRegex;

public class BotUserHelper {

    private static final Logger log = LoggerFactory.getLogger(BotUserHelper.class);

    public BotUserHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Finds bot users by hostmask. Supports wildcards.
     *
     * @param hostmask a string representing the hostmask, or IRC address, to match against (e.g. nick!user@domain.tld)
     * @return a list of bot users with matching hostmasks
     */
    public static List<BotUser> findByHostmask(String hostmask) {
        var botUserDao = new BotUserDao();
        return botUserDao.getAll()
                .stream()
                .filter(u -> {
                    for (String s : u.getBotUserHostmasks()) {
                        if (Pattern.matches(wildcardToRegex(s), hostmask)) {
                            return true;
                        }
                    }
                    return false;
                }).toList();
    }

    /**
     * Gets a map of managed channel user flags for a bot user.
     *
     * @param botUser the bot user
     * @return a map of managed channel user flags with the channel name as the key
     */
    public static Map<ManagedChannel, List<ManagedChannelUserFlag>> getChannelFlagMap(BotUser botUser) {
        Map<ManagedChannel, List<ManagedChannelUserFlag>> channelFlagMap = new HashMap<>();
        ManagedChannelUserDao mcuDao = new ManagedChannelUserDao();

        for (ManagedChannelUser mcu : mcuDao.getMultipleWithBotUserId(botUser.getId())) {
            channelFlagMap.put(mcu.getManagedChannel(), mcu.getManagedChannelUserFlags());
        }

        return channelFlagMap;
    }

    /**
     * Parses a comma-delimited list of bot user flags into a list, removing any duplicate or invalid flags.
     *
     * @param flagStr a comma-delimited string representing one or more flags
     * @return a list of bot user flags
     */
    public static List<BotUserFlag> parseFlags(String flagStr) {
        List<BotUserFlag> flags = new ArrayList<>();

        for (String s : flagStr.split(",")) {
            try {
                BotUserFlag flag = Enum.valueOf(BotUserFlag.class, s.toUpperCase());
                if (!flags.contains(flag)) {
                    flags.add(flag);
                }
            } catch (IllegalArgumentException ex) {
                log.debug("Invalid flag: {}", s.toUpperCase());
            }
        }

        return flags;
    }
}
