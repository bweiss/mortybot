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

public class V002_CreateBotUsers implements MigrationScript {
    public BigDecimal getId() {
        return BigDecimal.valueOf(2L);
    }

    public String getDescription() {
        return "Create bot_users table";
    }

    public String getUpScript() {
        return """
                create table bot_users
                (
                    bot_user_id integer not null
                        constraint bot_users_pk
                            primary key autoincrement,
                    username    text    not null,
                    hostmasks   text,
                    flags       text,
                    location    text
                );
                                
                create unique index bot_users_bot_user_id_uindex
                    on bot_users (bot_user_id);
                                
                create unique index bot_users_username_uindex
                    on bot_users (username);
                """;
    }

    public String getDownScript() {
        return "drop table bot_users;";
    }
}
