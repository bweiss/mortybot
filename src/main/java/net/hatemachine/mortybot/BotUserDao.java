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
package net.hatemachine.mortybot;

import net.hatemachine.mortybot.exception.BotUserException;
import org.pircbotx.User;

import java.util.List;

public interface BotUserDao {

    /**
     * @param name unique identifier for the bot user
     * @return a bot user if one exists with unique identifier <code>name</code>
     * @throws BotUserException if any error occurs
     */
    BotUser getByName(String name) throws BotUserException;

    /**
     * @param botUser the bot user to be added
     * @throws BotUserException if any error occurs
     */
    void add(BotUser botUser) throws BotUserException;

    /**
     * @param botUser the bot user to be updated
     * @throws BotUserException if any error occurs
     */
    void update(BotUser botUser) throws BotUserException;

    /**
     * @param botUser the bot user to be deleted
     * @throws BotUserException if any error occurs
     */
    void delete(BotUser botUser) throws BotUserException;

    /**
     * @return list of all bot users.
     */
    List<BotUser> getAll();

    /**
     * @param hostmask the hostmask to search for
     * @return all bot users that match <code>hostmask</code>
     */
    List<BotUser> getAll(String hostmask);

    /**
     * @param flag the flag to search for
     * @return all bot users that have <code>flag</code>
     */
    List<BotUser> getAll(BotUser.Flag flag);

    /**
     * @param hostmask the hostmask to search for
     * @param flag the flag to search for
     * @return all bot users that match <code>hostmask</code> and have <code>flag</code>
     */
    List<BotUser> getAll(String hostmask, BotUser.Flag flag);

    /**
     * @param user the user to check
     * @return true if the user is a bot admin
     */
    boolean isAdmin(User user);
}
