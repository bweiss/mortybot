package net.hatemachine.mortybot;

import java.util.List;

public interface BotUserDao {

    /**
     * @param name unique identifier for the bot user.
     * @return a bot user if one exists with unique identifier <code>name</code>
     * @throws Exception if any error occurs.
     */
    BotUser getByName(String name) throws Exception;

    /**
     * @param botUser the bot user to be added.
     * @return true if bot user is successfully added, false if bot user already exists.
     * @throws Exception if any error occurs.
     */
    boolean add(BotUser botUser) throws Exception;

    /**
     * @param botUser the bot user to be updated.
     * @return true if bot user exists and is successfully updated, false otherwise.
     * @throws Exception if any error occurs.
     */
    boolean update(BotUser botUser) throws Exception;

    /**
     * @param botUser the bot user to be deleted.
     * @return true if bot user exists and is successfully deleted, false otherwise.
     * @throws Exception if any error occurs.
     */
    boolean delete(BotUser botUser) throws Exception;

    /**
     * @return list of all bot users.
     */
    List<BotUser> getAllBotUsers();
}
