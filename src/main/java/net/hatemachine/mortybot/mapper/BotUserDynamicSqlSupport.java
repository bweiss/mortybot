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

public final class BotUserDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6145285-04:00", comments="Source Table: bot_users")
    public static final BotUser botUser = new BotUser();

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6145285-04:00", comments="Source field: bot_users.bot_user_id")
    public static final SqlColumn<Integer> botUserId = botUser.botUserId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6155288-04:00", comments="Source field: bot_users.username")
    public static final SqlColumn<String> username = botUser.username;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6155288-04:00", comments="Source field: bot_users.hostmasks")
    public static final SqlColumn<String> hostmasks = botUser.hostmasks;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6155288-04:00", comments="Source field: bot_users.flags")
    public static final SqlColumn<String> flags = botUser.flags;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6155288-04:00", comments="Source field: bot_users.location")
    public static final SqlColumn<String> location = botUser.location;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2022-06-26T17:54:26.6145285-04:00", comments="Source Table: bot_users")
    public static final class BotUser extends AliasableSqlTable<BotUser> {
        public final SqlColumn<Integer> botUserId = column("bot_user_id", JDBCType.INTEGER);

        public final SqlColumn<String> username = column("username", JDBCType.VARCHAR);

        public final SqlColumn<String> hostmasks = column("hostmasks", JDBCType.VARCHAR);

        public final SqlColumn<String> flags = column("flags", JDBCType.VARCHAR);

        public final SqlColumn<String> location = column("location", JDBCType.VARCHAR);

        public BotUser() {
            super("bot_users", BotUser::new);
        }
    }
}