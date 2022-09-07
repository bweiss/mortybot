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

public class V002_CreateBotUser implements MigrationScript {
    public BigDecimal getId() {
        return BigDecimal.valueOf(2L);
    }

    public String getDescription() {
        return "Create bot_user table";
    }

    public String getUpScript() {
        return """
                create table bot_user
                (
                    id        integer not null
                        constraint bot_user_pk
                            primary key autoincrement,
                    name      text    not null
                        constraint bot_user_pk
                            unique,
                    bot_user_hostmasks text default null,
                    bot_user_flags     text default null,
                    location           text default null
                );
                """;
    }

    public String getDownScript() {
        return "drop table bot_users;";
    }
}
