package net.hatemachine.mortybot;

import net.hatemachine.mortybot.exception.BotUserException;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.exception.IrcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static java.util.stream.Collectors.toList;
import static net.hatemachine.mortybot.util.IrcUtils.validateHostmask;
import static net.hatemachine.mortybot.util.StringUtils.validateBotUsername;
import static net.hatemachine.mortybot.util.StringUtils.validateString;

public class MortyBot extends PircBotX {

    // our main properties file
    private static final String  PROPERTIES_FILE = "bot.properties";

    // setup some defaults
    private static final String  BOT_NAME_DEFAULT = "morty";
    private static final String  BOT_LOGIN_DEFAULT = "morty";
    private static final String  BOT_REAL_NAME_DEFAULT = "Aww jeez, Rick!";
    private static final String  IRC_SERVER_DEFAULT = "irc.hatemachine.net";
    private static final int     IRC_PORT_DEFAULT = 6697;
    private static final boolean AUTO_RECONNECT_DEFAULT = false;
    private static final int     AUTO_RECONNECT_DELAY_DEFAULT = 30000;
    private static final int     AUTO_RECONNECT_ATTEMPTS_DEFAULT = 3;
    private static final boolean AUTO_NICK_CHANGE_DEFAULT = true;
    private static final String  AUTO_JOIN_CHANNELS_DEFAULT = "#drunkards";
    private static final String  COMMAND_PREFIX_DEFAULT = ".";

    private static final Logger log = LoggerFactory.getLogger(MortyBot.class);

    private static final BotUserDao botUserDao = new InMemoryBotUserDao();
    private static final Properties properties = new Properties();

    MortyBot(Configuration config) {
        super(config);
    }

    /**
     * Main entry point for the bot. Responsible for initial configuration and setting up the bot users.
     *
     * @param args command line arguments for the bot
     */
    public static void main(String[] args) {
        if (System.getenv("MORTYBOT_HOME") == null) {
            log.error("MORTYBOT_HOME not set, exiting...");
            return;
        }

        // try to load some bot properties
        String propertiesFile = System.getenv("MORTYBOT_HOME") + "/conf/" + PROPERTIES_FILE;
        log.debug("Attempting to load properties from {}", propertiesFile);
        try (var reader = new FileReader(propertiesFile)) {
            properties.load(reader);
            log.debug("Loaded {} properties", properties.size());
        } catch (FileNotFoundException e) {
            String msg = "file not found: " + propertiesFile;
            log.error(msg, e.getMessage());
        } catch (IOException e) {
            String msg = "unable to read file: " + propertiesFile;
            log.error(msg, e.getMessage());
        }

        if (properties.isEmpty()) {
            log.warn("Unable to load bot properties (defaults will be used)");
        }

        // build the bot configuration
        var config = new Configuration.Builder()
                .setName(getStringProperty("botName", BOT_NAME_DEFAULT))
                .setLogin(getStringProperty("botLogin", BOT_LOGIN_DEFAULT))
                .setRealName(getStringProperty("botRealName", BOT_REAL_NAME_DEFAULT))
                .addServer(getStringProperty("ircServer", IRC_SERVER_DEFAULT),
                        getIntProperty("ircPort", IRC_PORT_DEFAULT))
                .setSocketFactory(new UtilSSLSocketFactory().trustAllCertificates())
                .setAutoReconnect(getBooleanProperty("autoReconnect", AUTO_RECONNECT_DEFAULT))
                .setAutoReconnectDelay(getIntProperty("autoReconnectDelay", AUTO_RECONNECT_DELAY_DEFAULT))
                .setAutoReconnectAttempts(getIntProperty("autoReconnectAttempts", AUTO_RECONNECT_ATTEMPTS_DEFAULT))
                .setAutoNickChange(getBooleanProperty("autoNickChange", AUTO_NICK_CHANGE_DEFAULT))
                .addAutoJoinChannels(Arrays.asList(getStringProperty("autoJoinChannels", AUTO_JOIN_CHANNELS_DEFAULT).split(" ")))
                .addListener(new CommandListener(getStringProperty("commandPrefix", COMMAND_PREFIX_DEFAULT)))
                .addListener(new LinkListener())
                .buildConfiguration();

        // extra sanity check
        if (config == null) {
            log.error("Bot config not found, exiting...");
            return;
        }

        // Add some users
        addBotUsers(botUserDao);

        log.debug("Bot users:");
        botUserDao.getAll().forEach(u -> log.debug(u.toString()));

        // Start the bot and connect to a server
        try (var bot = new MortyBot(config)) {
            log.info("Starting bot with nick: {}", bot.getNick());
            bot.startBot();
        } catch (IOException | IrcException e) {
            log.error("Failed to start bot, exiting...", e);
        }
    }

    /**
     * Get all the bot users for this bot.
     *
     * @return list of all bot users
     */
    public List<BotUser> getBotUsers() {
        return botUserDao.getAll();
    }

    /**
     * Get all the bot users of a particular type.
     *
     * @param type the type of user
     * @return list of bot users matching the type
     */
    public List<BotUser> getBotUsers(BotUserType type) {
        return botUserDao.getAll()
                .stream()
                .filter(u -> u.getType().equals(type))
                .collect(toList());
    }

    /**
     * Get all the bot users that match a particular hostmask.
     *
     * @param hostmask the user's hostmask
     * @return list of bot users with matching hostmasks
     */
    public List<BotUser> getBotUsers(String hostmask) {
        return botUserDao.getAll()
                .stream()
                .filter(u -> u.hasMatchingHostmask(hostmask))
                .collect(toList());
    }

    /**
     * Get all the bot users that match a particular type and hostmask.
     *
     * @param type the type of user
     * @param hostmask the user's hostmask
     * @return list of bot users matching both the type and hostmask
     */
    public List<BotUser> getBotUsers(BotUserType type, String hostmask) {
        return botUserDao.getAll()
                .stream()
                .filter(u -> u.getType().equals(type))
                .filter(u -> u.hasMatchingHostmask(hostmask))
                .collect(toList());
    }

    /**
     * Get a bot user by their name.
     *
     * @param name the name of the user you want to retrieve
     * @return the BotUser if found
     */
    public BotUser getBotUserByName(String name) {
        return botUserDao.getByName(name);
    }

    /**
     * Add a bot user.
     *
     * @param name the name of the bot user
     * @param hostmask the initial hostmask for this user
     * @param type the type of user
     * @throws BotUserException if there is an issue adding the user
     */
    public void addBotUser(String name, String hostmask, BotUserType type) throws BotUserException {
        BotUser user = new BotUser(validateBotUsername(name), validateHostmask(hostmask), type);
        botUserDao.add(user);
        log.info("Added bot user: {}", user.getName());
    }

    /**
     * Remove a bot user.
     *
     * @param name the name of the user to be removed
     * @throws BotUserException if there is an issue removing the user
     */
    public void removeBotUser(String name) throws BotUserException {
        BotUser user = botUserDao.getByName(validateString(name));
        botUserDao.delete(user);
        log.info("Removed bot user: {}", user.getName());
    }

    /**
     * Add a hostmask to a bot user.
     *
     * @param name the name of the user
     * @param hostmask the hostmask to add
     * @throws BotUserException if there is an issue adding the hostmask
     */
    public void addBotUserHostmask(String name, String hostmask) throws BotUserException {
        BotUser user = botUserDao.getByName(validateString(name));
        if (user.getHostmasks().contains(validateHostmask(hostmask))) {
            throw new BotUserException(BotUserException.Reason.HOSTMASK_EXISTS, hostmask);
        }
        if (user.addHostmask(hostmask)) {
            botUserDao.update(user);
            log.info("Added hostmask {} to user {}", hostmask, user.getName());
        } else {
            log.error("Failed to add hostmask {} to user {}", hostmask, user.getName());
        }
    }

    /**
     * Remove a hostmask from a bot user.
     *
     * @param name the name of the user
     * @param hostmask the hostmask to remove
     * @throws BotUserException if there is an issue removing the hostmask
     */
    public void removeBotUserHostmask(String name, String hostmask) throws BotUserException {
        BotUser user = botUserDao.getByName(validateString(name));
        if (!user.getHostmasks().contains(validateString(hostmask))) {
            throw new BotUserException(BotUserException.Reason.HOSTMASK_NOT_FOUND, hostmask);
        }
        if (user.removeHostmask(hostmask)) {
            botUserDao.update(user);
            log.info("Removed hostmask {} from user {}", hostmask, user.getName());
        } else {
            log.error("Failed to remove hostmask {} from {}", hostmask, user.getName());
        }
    }

    /**
     * Change the type of user.
     *
     * @param name the name of the user to update
     * @param type the type to set them to
     */
    public void setBotUserType(String name, BotUserType type) {
        BotUser user = botUserDao.getByName(validateString(name));
        user.setType(type);
        botUserDao.update(user);
        log.info("User type for {} changed to {}", user.getName(), type);
    }

    /**
     * Lets you know if a user is a Rick or just some Morty.
     *
     * @param user that you want to verify
     * @return true if user is a rick
     */
    public boolean isRick(User user) {
        List<BotUser> ricks = this.getBotUsers(BotUserType.RICK, user.getHostmask());
        return !ricks.isEmpty();
    }

    /**
     * Find out if the bot has ops in a channel.
     *
     * @param channel that we want to see if the bot has operator status in
     * @return true if the bot is oped in the channel
     */
    public boolean hasOps(Channel channel) {
        return channel.getOps().stream().anyMatch(u -> u.getNick().equalsIgnoreCase(this.getNick()));
    }

    public static String getStringProperty(String name, String defaultValue) {
        var prop = getStringProperty(name);
        return prop == null ? defaultValue : prop;
    }

    public static boolean getBooleanProperty(String name, boolean defaultValue) {
        var prop = getStringProperty(name);
        return prop == null ? defaultValue : "true".equalsIgnoreCase(prop);
    }

    public static int getIntProperty(String name, int defaultValue) {
        var prop = getStringProperty(name);
        return prop == null ? defaultValue : Integer.parseInt(prop);
    }

    public static String getStringProperty(String name) {
        var prop = System.getProperty(name);
        return prop == null ? properties.getProperty(name) : prop;
    }

    /**
     * Generate some bot users for testing.
     *
     * @return list of bot users
     */
    private static List<BotUser> generateBotUsers() {
        final List<BotUser> botUsers = new ArrayList<>();
        botUsers.add(new BotUser("brian", "*!brian@hatemachine.net", BotUserType.RICK));
        botUsers.add(new BotUser("megan", "*!megan@hugmachine.net", BotUserType.MORTY));
        botUsers.add(new BotUser("megan", "*!megan@hatemachine.net", BotUserType.JERRY));
        botUsers.add(new BotUser("drgonzo", "*!gonzo@*.beerandloathing.org", BotUserType.JERRY));
        return botUsers;
    }

    /**
     * Add some users to the bot.
     *
     * @param botUserDao the BotUserDao object we're using
     */
    private static void addBotUsers(BotUserDao botUserDao) {
        for (BotUser botUser : generateBotUsers()) {
            try {
                log.debug("Adding user: {}", botUser);
                botUserDao.add(botUser);
            }
            catch (BotUserException e) {
                log.debug(e.getMessage());
            }
        }
    }
}
