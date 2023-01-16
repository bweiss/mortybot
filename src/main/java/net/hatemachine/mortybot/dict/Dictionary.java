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
package net.hatemachine.mortybot.dict;

import java.util.List;

/**
 * Interface for performing dictionary lookups.
 */
public interface Dictionary {

    /**
     * Performs a dictionary lookup and returns any definitions found.
     *
     * @param term the term to lookup
     * @return a list of dictionary entries containing any definitions for the given term
     * @see DictionaryEntry
     */
    List<DictionaryEntry> lookup(String term);
}
