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
package net.hatemachine.mortybot;

import net.hatemachine.mortybot.exception.BotUserException;
import net.hatemachine.mortybot.model.BotUser;

import java.util.List;
import java.util.Optional;

public interface BotUserDao {

    /**
     * Get a bot user by their id.
     *
     * @param id the id of the bot user you want to retrieve
     * @return an {@link Optional} containing a {@link BotUser} if one exists with that id
     */
    Optional<BotUser> get(Integer id);

    /**
     * Get a bot user by their username.
     *
     * @param uname the username of the bot user you want to retrieve
     * @return an {@link Optional} containing a {@link BotUser} if one exists with that username
     */
    Optional<BotUser> getByUsername(String uname);

    /**
     * @param botUser the bot user to be added
     * @throws BotUserException if any error occurs
     */
    int add(BotUser botUser) throws BotUserException;

    /**
     * @param botUser the bot user to be updated
     * @throws BotUserException if any error occurs
     */
    int update(BotUser botUser) throws BotUserException;

    /**
     * @param botUser the bot user to be deleted
     * @throws BotUserException if any error occurs
     */
    int delete(BotUser botUser) throws BotUserException;

    /**
     * @return list of all bot users.
     */
    List<BotUser> getAll();

    /**
     * Get all the bot users that match a particular hostmask.
     *
     * @param hostmask the user's hostmask
     * @return list of bot users with matching hostmasks
     */
    List<BotUser> getAll(String hostmask);

    /**
     * Get a total count of all bot users.
     *
     * @return number of total bot users
     */
    long count();
}
