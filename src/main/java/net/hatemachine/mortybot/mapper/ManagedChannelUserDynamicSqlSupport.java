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
import net.hatemachine.mortybot.custom.entity.ManagedChannelUserFlag;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class ManagedChannelUserDynamicSqlSupport {
    public static final ManagedChannelUser managedChannelUser = new ManagedChannelUser();

    public static final SqlColumn<Integer> id = managedChannelUser.id;

    public static final SqlColumn<Integer> managedChannelId = managedChannelUser.managedChannelId;

    public static final SqlColumn<Integer> botUserId = managedChannelUser.botUserId;

    public static final SqlColumn<List<ManagedChannelUserFlag>> managedChannelUserFlags = managedChannelUser.managedChannelUserFlags;

    public static final class ManagedChannelUser extends SqlTable {
        public final SqlColumn<Integer> id = column("id", JDBCType.INTEGER);

        public final SqlColumn<Integer> managedChannelId = column("managed_channel_id", JDBCType.INTEGER);

        public final SqlColumn<Integer> botUserId = column("bot_user_id", JDBCType.INTEGER);

        public final SqlColumn<List<ManagedChannelUserFlag>> managedChannelUserFlags = column("managed_channel_user_flags", JDBCType.VARCHAR, "net.hatemachine.mortybot.custom.handler.ManagedChannelUserFlagListHandler");

        public ManagedChannelUser() {
            super("managed_channel_user");
        }
    }
}