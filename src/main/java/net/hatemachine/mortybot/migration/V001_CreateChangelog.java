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
import org.apache.ibatis.migration.MigrationScript;

public class V001_CreateChangelog implements MigrationScript {
    public BigDecimal getId() {
        return BigDecimal.valueOf(1L);
    }

    public String getDescription() {
        return "Create changelog table";
    }

    public String getUpScript() {
        return """
                create table changelog
                (
                    id          integer not null
                        constraint changelog_pk
                            primary key,
                    applied_at  text    not null,
                    description text    not null
                );
                                
                create unique index changelog_id_uindex
                    on changelog (id);
                                
                """;
    }

    public String getDownScript() {
        return "drop table changelog;";
    }
}