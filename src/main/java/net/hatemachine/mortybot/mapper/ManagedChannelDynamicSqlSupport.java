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
package net.hatemachine.mortybot.mapper;

import java.sql.JDBCType;
import java.util.List;
import net.hatemachine.mortybot.custom.entity.ManagedChannelFlag;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class ManagedChannelDynamicSqlSupport {
    public static final ManagedChannel managedChannel = new ManagedChannel();

    public static final SqlColumn<Integer> id = managedChannel.id;

    public static final SqlColumn<String> name = managedChannel.name;

    public static final SqlColumn<List<ManagedChannelFlag>> managedChannelFlags = managedChannel.managedChannelFlags;

    public static final class ManagedChannel extends SqlTable {
        public final SqlColumn<Integer> id = column("id", JDBCType.INTEGER);

        public final SqlColumn<String> name = column("\"name\"", JDBCType.VARCHAR);

        public final SqlColumn<List<ManagedChannelFlag>> managedChannelFlags = column("managed_channel_flags", JDBCType.VARCHAR, "net.hatemachine.mortybot.custom.handler.ManagedChannelFlagListHandler");

        public ManagedChannel() {
            super("managed_channel");
        }
    }
}