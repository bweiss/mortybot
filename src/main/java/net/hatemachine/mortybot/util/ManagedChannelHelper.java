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
import net.hatemachine.mortybot.dao.ManagedChannelDao;
import net.hatemachine.mortybot.model.ManagedChannel;

import java.util.List;

public class ManagedChannelHelper {

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
                .filter(c -> c.getManagedChannelFlags().contains(ManagedChannelFlag.AUTO_JOIN))
                .toList();
    }
}
