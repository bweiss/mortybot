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

import java.math.BigDecimal;

public class V003_CreateManagedChannels {

    public BigDecimal getId() {
        return BigDecimal.valueOf(3L);
    }

    public String getDescription() {
        return "Create managed_channels and managed_channel_users tables";
    }

    public String getUpScript() {
        return """
                create table managed_channels
                (
                    managed_channel_id INTEGER not null
                        constraint managed_channels_pk
                            primary key autoincrement,
                    name               TEXT    not null,
                    auto_join_flag     INTEGER,
                    modes              TEXT,
                    enforce_modes_flag INTEGER
                );
                                
                create unique index managed_channels_managed_channel_id_uindex
                    on managed_channels (managed_channel_id);
                                
                create unique index managed_channels_name_uindex
                    on managed_channels (name);
                    
                create table managed_channel_users
                (
                    managed_channel_user_id integer not null
                        constraint managed_channel_users_managed_channel_user_id_pk
                            primary key autoincrement,
                    managed_channel_id      integer not null
                        constraint managed_channel_users_managed_channel_user_id_fk
                            references managed_channels (managed_channel_id),
                    bot_user_id             integer not null
                        constraint managed_channel_users_bot_user_id_fk
                            references bot_users (bot_user_id),
                    auto_op_flag            integer,
                    auto_voice_flag         integer
                );
                                
                create index managed_channel_users_bot_user_id_index
                    on managed_channel_users (bot_user_id);
                                
                create index managed_channel_users_managed_channel_id_index
                    on managed_channel_users (managed_channel_id);
                                
                create unique index managed_channel_users_managed_channel_user_id_uindex
                    on managed_channel_users (managed_channel_user_id);
                """;
    }

    public String getDownScript() {
        return """
               drop table managed_channel_users;
               drop table managed_channels;
               """;
    }
}
