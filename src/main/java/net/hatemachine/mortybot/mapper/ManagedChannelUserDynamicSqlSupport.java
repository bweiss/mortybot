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

public final class ManagedChannelUserDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248175-04:00", comments="Source Table: managed_channel_users")
    public static final ManagedChannelUser managedChannelUser = new ManagedChannelUser();

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248233-04:00", comments="Source field: managed_channel_users.managed_channel_user_id")
    public static final SqlColumn<Integer> managedChannelUserId = managedChannelUser.managedChannelUserId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248258-04:00", comments="Source field: managed_channel_users.managed_channel_id")
    public static final SqlColumn<Integer> managedChannelId = managedChannelUser.managedChannelId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248282-04:00", comments="Source field: managed_channel_users.bot_user_id")
    public static final SqlColumn<Integer> botUserId = managedChannelUser.botUserId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248304-04:00", comments="Source field: managed_channel_users.auto_op_flag")
    public static final SqlColumn<Integer> autoOpFlag = managedChannelUser.autoOpFlag;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248323-04:00", comments="Source field: managed_channel_users.auto_voice_flag")
    public static final SqlColumn<Integer> autoVoiceFlag = managedChannelUser.autoVoiceFlag;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-08-11T21:21:29.248202-04:00", comments="Source Table: managed_channel_users")
    public static final class ManagedChannelUser extends AliasableSqlTable<ManagedChannelUser> {
        public final SqlColumn<Integer> managedChannelUserId = column("managed_channel_user_id", JDBCType.INTEGER);

        public final SqlColumn<Integer> managedChannelId = column("managed_channel_id", JDBCType.INTEGER);

        public final SqlColumn<Integer> botUserId = column("bot_user_id", JDBCType.INTEGER);

        public final SqlColumn<Integer> autoOpFlag = column("auto_op_flag", JDBCType.INTEGER);

        public final SqlColumn<Integer> autoVoiceFlag = column("auto_voice_flag", JDBCType.INTEGER);

        public ManagedChannelUser() {
            super("managed_channel_users", ManagedChannelUser::new);
        }
    }
}