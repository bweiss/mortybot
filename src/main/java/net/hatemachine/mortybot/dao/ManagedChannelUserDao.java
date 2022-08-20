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
package net.hatemachine.mortybot.dao;

import net.hatemachine.mortybot.exception.BotUserException;
import net.hatemachine.mortybot.exception.ManagedChannelException;
import net.hatemachine.mortybot.exception.ManagedChannelUserException;
import net.hatemachine.mortybot.model.ManagedChannelUser;

import java.util.List;
import java.util.Optional;

/**
 * <p>Data access object interface for {@link ManagedChannelUser} objects.</p>
 * <br />
 * <p>It is recommended that all implementations of this interface's methods be synchronized.</p>
 */
public interface ManagedChannelUserDao {

    /**
     * Get a managed channel user by its id.
     *
     * @param id the id of the managed channel user you want to retrieve
     * @return an {@link Optional} containing a {@link ManagedChannelUser} if one exists with that id
     */
    Optional<ManagedChannelUser> get(Integer id);

    /**
     * Add a new managed channel user.
     *
     * @param managedChannelUser the managed channel user to be added
     * @throws BotUserException if bot user does not exist
     * @throws ManagedChannelException if managed channel does not exist
     * @throws ManagedChannelUserException if the user already has an entry for this channel
     */
    int add(ManagedChannelUser managedChannelUser) throws BotUserException, ManagedChannelException, ManagedChannelUserException;

    /**
     * Update an existing managed channel user.
     *
     * @param managedChannelUser the managed channel user to be updated
     * @throws BotUserException if bot user does not exist
     * @throws ManagedChannelException if managed channel does not exist
     * @throws ManagedChannelUserException if managed channel user does not exist
     */
    int update(ManagedChannelUser managedChannelUser) throws BotUserException, ManagedChannelException, ManagedChannelUserException;

    /**
     * Delete a managed channel user.
     *
     * @param managedChannelUser the managed channel user to be deleted
     * @throws ManagedChannelUserException if managed channel user does not exist
     */
    int delete(ManagedChannelUser managedChannelUser) throws ManagedChannelUserException;

    /**
     * @return list of all managed channel users.
     */
    List<ManagedChannelUser> getAll();

    /**
     * Get a total count of all managed channel users.
     *
     * @return number of total managed channel users
     */
    long count();
}
