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
package net.hatemachine.mortybot.migration;

import org.apache.ibatis.migration.MigrationScript;

import java.math.BigDecimal;

public class V004_CreateManagedChannelUser implements MigrationScript {

    public BigDecimal getId() {
        return BigDecimal.valueOf(4L);
    }

    public String getDescription() {
        return "Create managed_channel_user table";
    }

    public String getUpScript() {
        return """
                create table managed_channel_user
                (
                    id                 integer not null
                        constraint managed_channel_user_pk
                            primary key autoincrement,
                    managed_channel_id         integer not null,
                    bot_user_id                integer not null,
                    managed_channel_user_flags text default null
                );
                
                create index managed_channel_user_bot_user_id_index
                    on managed_channel_user (bot_user_id);
                
                create index managed_channel_user_managed_channel_id_index
                    on managed_channel_user (managed_channel_id);
                """;
    }

    public String getDownScript() {
        return """
               drop table managed_channel_user;
               """;
    }
}
