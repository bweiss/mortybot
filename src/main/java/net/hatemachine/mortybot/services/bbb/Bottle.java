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
package net.hatemachine.mortybot.services.bbb;

public record Bottle(String name,
                     String url,
                     String type,
                     String bottled,
                     String age,
                     String proof,
                     String size,
                     String owner,
                     String producer,
                     String location) {

    @Override
    public String toString() {
        return String.format("[%s] %s (%s), %s (%s) [Bottled: %s, Age: %s, Proof: %s]",
                type,
                name,
                size,
                producer,
                location,
                bottled,
                age,
                proof);
    }
}

