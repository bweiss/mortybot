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

import net.hatemachine.mortybot.custom.entity.ManagedChannelFlag;
import net.hatemachine.mortybot.custom.entity.ManagedChannelUserFlag;
import net.hatemachine.mortybot.dao.ManagedChannelDao;
import net.hatemachine.mortybot.dao.ManagedChannelUserDao;
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.model.ManagedChannel;
import net.hatemachine.mortybot.model.ManagedChannelUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagedChannelHelper {

    private static final Logger log = LoggerFactory.getLogger(ManagedChannelHelper.class);

    public ManagedChannelHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Retrieves a list of channels that have the auto-join flag.
     *
     * @return a list of managed channels that have the AUTO_JOIN flag
     */
    public static List<ManagedChannel> getAutoJoinChannels() {
        ManagedChannelDao managedChannelDao = new ManagedChannelDao();
        return managedChannelDao.getAll().stream()
                .filter(c -> {
                    var flags = c.getManagedChannelFlags();
                    return (flags != null && flags.contains(ManagedChannelFlag.AUTO_JOIN));
                }).toList();
    }

    /**
     * Retrieves a map of bot users and their managed channel user flags.
     *
     * @param managedChannel the managed channel to retrieve users for
     * @return a map of bot users and their flags for the provided channel
     */
    public static Map<BotUser, List<ManagedChannelUserFlag>> getUserFlagMap(ManagedChannel managedChannel) {
        Map<BotUser, List<ManagedChannelUserFlag>> userFlagMap = new HashMap<>();
        ManagedChannelUserDao mcuDao = new ManagedChannelUserDao();

        for (ManagedChannelUser mcu : mcuDao.getMultipleWithManagedChannelId(managedChannel.getId())) {
            userFlagMap.put(mcu.getBotUser(), mcu.getManagedChannelUserFlags());
        }

        return userFlagMap;
    }

    /**
     * Parses a comma-delimited list of managed channel flags into a list, removing any duplicate or invalid flags.
     *
     * @param flagStr a comma-delimited string representing one or more flags
     * @return a list of managed channel flags
     */
    public static List<ManagedChannelFlag> parseFlags(String flagStr) {
        List<ManagedChannelFlag> flags = new ArrayList<>();

        for (String s : flagStr.split(",")) {
            try {
                ManagedChannelFlag flag = Enum.valueOf(ManagedChannelFlag.class, s.toUpperCase());
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
