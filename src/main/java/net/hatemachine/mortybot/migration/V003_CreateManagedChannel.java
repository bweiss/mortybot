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

public class V003_CreateManagedChannel implements MigrationScript {

    public BigDecimal getId() {
        return BigDecimal.valueOf(3L);
    }

    public String getDescription() {
        return "Create managed_channel table";
    }

    public String getUpScript() {
        return """
                create table managed_channel
                (
                    id    integer not null
                        constraint managed_channel_pk
                            primary key autoincrement,
                    name  text    not null
                        constraint managed_channel_pk
                            unique,
                    managed_channel_flags text default null,
                    bans                  text default null,
                    modes                 text default null
                );
                """;
    }

    public String getDownScript() {
        return """
               drop table managed_channel;
               """;
    }
}
