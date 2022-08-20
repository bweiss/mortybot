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

import net.hatemachine.mortybot.exception.ManagedChannelException;
import net.hatemachine.mortybot.model.ManagedChannel;

import java.util.List;
import java.util.Optional;

/**
 * <p>Data access object interface for {@link ManagedChannel} objects.</p>
 * <br />
 * <p>It is recommended that all implementations of this interface's methods be synchronized.</p>
 */
public interface ManagedChannelDao {

    /**
     * Get a managed channel by its id.
     *
     * @param id the id of the managed channel you want to retrieve
     * @return an {@link Optional} containing a {@link ManagedChannel} if one exists with that id
     */
    Optional<ManagedChannel> get(Integer id);

    /**
     * Get a managed channel by its name.
     *
     * @param channelName the name of the managed channel you want to retrieve
     * @return an {@link Optional} containing a {@link ManagedChannel} if one exists with that name
     */
    Optional<ManagedChannel> getByName(String channelName);

    /**
     * Add a new managed channel.
     *
     * @param managedChannel the managed channel to be added
     * @throws ManagedChannelException if managed channel already exists
     */
    int add(ManagedChannel managedChannel) throws ManagedChannelException;

    /**
     * Update an existing managed channel.
     *
     * @param managedChannel the managed channel to be updated
     * @throws ManagedChannelException if managed channel does not exist
     */
    int update(ManagedChannel managedChannel) throws ManagedChannelException;

    /**
     * Delete a managed channel.
     *
     * @param managedChannel the managed channel to be deleted
     * @throws ManagedChannelException if managed channel does not exist
     */
    int delete(ManagedChannel managedChannel) throws ManagedChannelException;

    /**
     * Retrieve all managed channels.
     *
     * @return list of all managed channels.
     */
    List<ManagedChannel> getAll();

    /**
     * Get a total count of all managed channels.
     *
     * @return number of total managed channels
     */
    long count();
}
