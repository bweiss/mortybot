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

import com.darwinsys.io.FileSaver;
import net.hatemachine.mortybot.exception.BotUserException;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.hatemachine.mortybot.exception.BotUserException.Reason.UNKNOWN_USER;
import static net.hatemachine.mortybot.exception.BotUserException.Reason.USER_EXISTS;

public class BotUserDaoImpl implements BotUserDao {
    
    private static final String USER_FILE = "/conf/users.conf";

    private static final Logger log = LoggerFactory.getLogger(BotUserDaoImpl.class);

    private final MortyBot bot;
    private final Object accessLock = new Object();
    private final Map<String, BotUser> botUserMap = new HashMap<>();

    public BotUserDaoImpl(MortyBot bot) {
        this.bot = bot;
        init();
    }

    /**
     * Get a bot user by name.
     *
     * @param name unique identifier for the bot user
     * @return a bot user if one exists with unique identifier <code>name</code>
     * @throws BotUserException if any error occurs
     */
    @Override
    public BotUser getByName(final String name) throws BotUserException {
        synchronized (this.accessLock) {
            if (!botUserMap.containsKey(name))
                throw new BotUserException(UNKNOWN_USER, name);

            return botUserMap.get(name);
        }
    }

    /**
     * Add a new user to the bot.
     *
     * @param botUser the bot user to be added
     * @throws BotUserException if any error occurs
     */
    @Override
    public void add(final BotUser botUser) throws BotUserException {
        synchronized (this.accessLock) {
            if (botUserMap.containsKey(botUser.getName()))
                throw new BotUserException(USER_EXISTS, botUser.getName());

            botUserMap.put(botUser.getName(), botUser);
            save();
        }
    }

    /**
     * Update an existing bot user.
     *
     * @param botUser the bot user to be updated
     * @throws BotUserException if any error occurs
     */
    @Override
    public void update(final BotUser botUser) throws BotUserException {
        synchronized (this.accessLock) {
            if (!botUserMap.containsKey(botUser.getName()))
                throw new BotUserException(UNKNOWN_USER, botUser.getName());

            botUserMap.remove(botUser.getName());
            botUserMap.put(botUser.getName(), botUser);
            save();
        }
    }

    /**
     * Delete a bot user.
     *
     * @param botUser the bot user to be deleted
     * @throws BotUserException if any error occurs
     */
    @Override
    public void delete(final BotUser botUser) throws BotUserException {
        synchronized (this.accessLock) {
            if (!botUserMap.containsKey(botUser.getName()))
                throw new BotUserException(UNKNOWN_USER, botUser.getName());

            botUserMap.remove(botUser.getName());
            save();
        }
    }

    /**
     * Get all bot users.
     *
     * @return a list of bot users
     */
    @Override
    public List<BotUser> getAll() {
        synchronized (this.accessLock) {
            return new ArrayList<>(botUserMap.values());
        }
    }

    /**
     * Get all the bot users that match a particular hostmask.
     *
     * @param hostmask the user's hostmask
     * @return list of bot users with matching hostmasks
     */
    public List<BotUser> getAll(String hostmask) {
        synchronized (this.accessLock) {
            return getAll().stream()
                    .filter(u -> u.hasMatchingHostmask(hostmask))
                    .toList();
        }
    }

    /**
     * Get all the bot users that have a specific user flag.
     *
     * @param flag the user flag you are interested in
     * @return list of bot users that have the flag
     */
    public List<BotUser> getAll(BotUser.Flag flag) {
        synchronized (this.accessLock) {
            return getAll().stream()
                    .filter(u -> u.getFlags().contains(flag))
                    .toList();
        }
    }

    /**
     * Get all the bot users that have a matching hostmask and flag.
     *
     * @param hostmask the user's hostmask
     * @param flag the user flag you are interested in
     * @return list of bot users matching both the hostmask and flag
     */
    public List<BotUser> getAll(String hostmask, BotUser.Flag flag) {
        synchronized (this.accessLock) {
            return getAll().stream()
                    .filter(u -> u.getFlags().contains(flag))
                    .filter(u -> u.hasMatchingHostmask(hostmask))
                    .toList();
        }
    }

    /**
     * Find out if a user is an admin.
     *
     * @param user that you want to verify
     * @return true if user is an admin
     */
    public boolean isAdmin(User user) {
        List<BotUser> admins = this.getAll(user.getHostmask(), BotUser.Flag.ADMIN);
        return !admins.isEmpty();
    }

    /**
     * Load our initial bot users from disk.
     */
    private void init() {
        synchronized (this.accessLock) {
            String file = bot.getBotHome() + USER_FILE;
            List<String> lines;

            try {
                lines = Files.readAllLines(Path.of(file));
                for (String line : lines) {
                    if (line != null && !line.trim().isEmpty() && !line.startsWith("#")) {
                        Optional<BotUser> user = parseLine(line);
                        user.ifPresent(botUser -> botUserMap.put(botUser.getName(), botUser));
                    }
                }
            } catch (IOException e) {
                log.error("Error reading bot user file: {}", e.getMessage());
            }
        }
    }

    /**
     * Save all bot users to disk.
     */
    private void save() {
        synchronized (this.accessLock) {
            Path file = Paths.get(bot.getBotHome() + USER_FILE);

            try {
                FileSaver saver = new FileSaver(file);
                Writer writer = saver.getWriter();
                PrintWriter out = new PrintWriter(writer);

                for (BotUser user : getAll()) {
                    String line = user.getName() +
                            " " +
                            String.join(",", user.getHostmasks()) +
                            " " +
                            user.getFlags().stream().map(BotUser.Flag::toString).collect(Collectors.joining(","));
                    out.println(line);
                }

                out.close();
                saver.finish();

            } catch (IOException e) {
                log.error("Failed to write user file: {}", e.getMessage());
            }
        }
    }

    /**
     * Parses a line of text representing a bot user. This should be of the following format.
     *
     *  name hostmask1,hostmask2 flag1,flag2
     *
     *  e.g.
     *  brian *!brian@hatemachine.net,*!brian@hugmachine.net ADMIN,AOP
     *
     * @param line the line of text to be parsed
     * @return an optional containing a bot user if one is created successfully
     */
    private static Optional<BotUser> parseLine(String line) {
        Optional<BotUser> botUser = Optional.empty();
        List<String> tokens = Arrays.asList(line.split(" "));

        if (tokens.size() >= 2) {
            String name = tokens.get(0);
            String[] hostmasks = tokens.get(1).split(",");
            Set<BotUser.Flag> flags = new HashSet<>();

            // parse any user flags if present
            if (tokens.size() == 3) {
                flags = parseFlags(tokens.get(2));
            }

            BotUser user = new BotUser(Validate.botUserName(name), Validate.hostmask(hostmasks[0]), flags);

            // check for additional hostmasks and add them to the user
            if (hostmasks.length > 1) {
                for (int i = 1; i < hostmasks.length; i++) {
                    user.addHostmask(Validate.hostmask(hostmasks[i]));
                }
            }

            botUser = Optional.of(user);
        }

        return botUser;
    }

    /**
     * Parse a comma-delimited string of user flags into a set of enums representing those flags.
     *
     * @param flagList the comma-delimited list of flags
     * @return a set of user flags
     */
    private static Set<BotUser.Flag> parseFlags(String flagList) {
        Set<BotUser.Flag> flags = new HashSet<>();
        for (String flagStr : flagList.split(",")) {
            try {
                flags.add(Enum.valueOf(BotUser.Flag.class, flagStr.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                log.error("Invalid bot user flag: {}", e.getMessage());
            }
        }
        return flags;
    }
}
