/*
 * MortyBot - An IRC bot built on the PircBotX framework.
 * Copyright © 2022 Brian Weiss (brianmweiss@gmail.com)
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
package net.hatemachine.mortybot.wordle;

import org.pircbotx.Colors;

import java.util.Locale;

import static net.hatemachine.mortybot.wordle.LetterState.*;

public record Letter(Character character, LetterState state) {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (state == UNUSED) {
            sb.append(Colors.set(character.toString().toUpperCase(Locale.ROOT), Colors.NORMAL));
        } else if (state == NO_MATCH) {
            sb.append(Colors.set(character.toString().toUpperCase(Locale.ROOT), Colors.DARK_GRAY));
        } else if (state == IMPRECISE_MATCH) {
            sb.append(Colors.set(character.toString().toUpperCase(Locale.ROOT), Colors.BLACK, Colors.YELLOW));
        } else if (state == EXACT_MATCH) {
            sb.append(Colors.set(character.toString().toUpperCase(Locale.ROOT), Colors.BLACK, Colors.GREEN));
        }

        sb.append(Colors.NORMAL);

        return sb.toString();
    }
}
