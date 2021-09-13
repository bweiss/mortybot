package net.hatemachine.mortybot;

import net.hatemachine.mortybot.exception.BotUserException;

import java.util.List;

public interface BotUserDao {

    /**
     * @param name unique identifier for the bot user.
     * @return a bot user if one exists with unique identifier <code>name</code>
     * @throws BotUserException if any error occurs.
     */
    BotUser getByName(String name) throws BotUserException;

    /**
     * @param botUser the bot user to be added.
     * @throws BotUserException if any error occurs.
     */
    void add(BotUser botUser) throws BotUserException;

    /**
     * @param botUser the bot user to be updated.
     * @throws BotUserException if any error occurs.
     */
    void update(BotUser botUser) throws BotUserException;

    /**
     * @param botUser the bot user to be deleted.
     * @return true if bot user exists and is successfully deleted, false otherwise.
     * @throws BotUserException if any error occurs.
     */
    boolean delete(BotUser botUser) throws BotUserException;

    /**
     * @return list of all bot users.
     */
    List<BotUser> getAll();
}
