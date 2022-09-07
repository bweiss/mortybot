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
import net.hatemachine.mortybot.dao.BotUserDao;
import net.hatemachine.mortybot.model.BotUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static net.hatemachine.mortybot.util.StringUtils.wildcardToRegex;

public class BotUserHelper {

    private static final Logger log = LoggerFactory.getLogger(BotUserHelper.class);

    public BotUserHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Build a list of bot user flags from a comma-delimited string.
     *
     * @param flags a comma-delimited string representing one or more flags
     * @return a list of bot user flags
     * @see BotUserFlag
     */
    public static List<BotUserFlag> buildFlagList(String flags) {
        return buildFlagList(new ArrayList<>(), flags);
    }

    /**
     * Build a list of bot user flags from a comma-delimited string.
     *
     * @param flagList a list of bot user flags that will be used as a starting list
     * @param flags a comma-delimited string representing one or more flags
     * @return a list of bot user flags
     * @see BotUserFlag
     */
    public static List<BotUserFlag> buildFlagList(List<BotUserFlag> flagList, String flags) {
        for (String s : flags.split(",")) {
            String flagStr = s.trim().toUpperCase();

            try {
                BotUserFlag flag = Enum.valueOf(BotUserFlag.class, flagStr);

                if (!flagList.contains(flag)) {
                    flagList.add(flag);
                }

            } catch (IllegalArgumentException ex) {
                log.debug("Invalid flag: {}", flagStr);
            }
        }

        return flagList;
    }

    /**
     * Finds bot users by hostmask. Supports wildcards.
     *
     * @param hostmask a string representing the hostmask, or IRC address, to match against (e.g. nick!user@domain.tld)
     * @return a list of bot users with matching hostmasks
     */
    public static List<BotUser> findByHostmask(String hostmask) {
        var botUserDao = new BotUserDao();

        return botUserDao.getAll().stream()
                .filter(u -> {
                    for (String s : u.getBotUserHostmasks()) {
                        if (Pattern.matches(wildcardToRegex(s), hostmask)) {
                            return true;
                        }
                    }
                    return false;
                }).toList();
    }
}
