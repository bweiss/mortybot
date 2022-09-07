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
package net.hatemachine.mortybot.model;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.hatemachine.mortybot.custom.entity.BotUserFlag;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BotUser implements Serializable {
    private Integer id;

    private String name;

    private List<String> botUserHostmasks;

    private List<BotUserFlag> botUserFlags;

    private String location;

    private List<ManagedChannelUser> managedChannelUsers;

    private static final long serialVersionUID = 1L;
}