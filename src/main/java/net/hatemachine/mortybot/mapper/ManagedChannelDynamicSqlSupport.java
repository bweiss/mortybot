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
import javax.annotation.Generated;
import org.mybatis.dynamic.sql.AliasableSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

public final class ManagedChannelDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246893-04:00", comments="Source Table: managed_channels")
    public static final ManagedChannel managedChannel = new ManagedChannel();

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246965-04:00", comments="Source field: managed_channels.managed_channel_id")
    public static final SqlColumn<Integer> managedChannelId = managedChannel.managedChannelId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246999-04:00", comments="Source field: managed_channels.name")
    public static final SqlColumn<String> name = managedChannel.name;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247024-04:00", comments="Source field: managed_channels.auto_join_flag")
    public static final SqlColumn<Integer> autoJoinFlag = managedChannel.autoJoinFlag;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247052-04:00", comments="Source field: managed_channels.modes")
    public static final SqlColumn<String> modes = managedChannel.modes;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.247073-04:00", comments="Source field: managed_channels.enforce_modes_flag")
    public static final SqlColumn<Integer> enforceModesFlag = managedChannel.enforceModesFlag;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.246928-04:00", comments="Source Table: managed_channels")
    public static final class ManagedChannel extends AliasableSqlTable<ManagedChannel> {
        public final SqlColumn<Integer> managedChannelId = column("managed_channel_id", JDBCType.INTEGER);

        public final SqlColumn<String> name = column("name", JDBCType.VARCHAR);

        public final SqlColumn<Integer> autoJoinFlag = column("auto_join_flag", JDBCType.INTEGER);

        public final SqlColumn<String> modes = column("modes", JDBCType.VARCHAR);

        public final SqlColumn<Integer> enforceModesFlag = column("enforce_modes_flag", JDBCType.INTEGER);

        public ManagedChannel() {
            super("managed_channels", ManagedChannel::new);
        }
    }
}