package net.hatemachine.mortybot;

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
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static java.util.stream.Collectors.toList;

public class MortyBot extends PircBotX {

    private static final String  PROPERTIES_FILE = "bot.properties";

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

    public static void main(String[] args) {

        // try to load some bot properties
        String propertiesFile = System.getenv("MORTYBOT_HOME") + "/conf/" + PROPERTIES_FILE;
        log.info("Attempting to load properties from {}", propertiesFile);
        try (FileReader reader = new FileReader(propertiesFile)) {
            properties.load(reader);
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
        Configuration config = new Configuration.Builder()
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
                .addAutoJoinChannels(Collections.singletonList(getStringProperty("autoJoinChannels", AUTO_JOIN_CHANNELS_DEFAULT)))
                .addListener(new CommandListener(getStringProperty("commandPrefix", COMMAND_PREFIX_DEFAULT)))
                .addListener(new LinkListener())
                .buildConfiguration();

        // extra sanity check
        if (config == null) {
            log.error("Bot config not found, exiting...");
            return;
        }

        // Add some users
        try {
            addBotUsers(botUserDao);
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.debug("Bot users:");
        botUserDao.getAllBotUsers().forEach(u -> log.debug(u.toString()));

        // Start the bot and connect to a server
        try (MortyBot bot = new MortyBot(config)) {
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
        return botUserDao.getAllBotUsers();
    }

    /**
     * Get all the bot users of a particular type.
     *
     * @param type the bot user type
     * @return list of bot users matching the type
     */
    public List<BotUser> getBotUsers(BotUserType type) {
        return botUserDao.getAllBotUsers()
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
        return botUserDao.getAllBotUsers()
                .stream()
                .filter(u -> u.hasMatchingHostmask(hostmask))
                .collect(toList());
    }

    /**
     * Get all the bot users that match a particular type and hostmask.
     *
     * @param type the bot user type
     * @param hostmask the user's hostmask
     * @return list of bot users matching both the type and hostmask
     */
    public List<BotUser> getBotUsers(BotUserType type, String hostmask) {
        return botUserDao.getAllBotUsers()
                .stream()
                .filter(u -> u.getType().equals(type))
                .filter(u -> u.hasMatchingHostmask(hostmask))
                .collect(toList());
    }

    /**
     * Validates the rickness of a user by their hostmask.
     *
     * @param user that you want to check if they are a rick or morty
     * @return true if user is a rick
     */
    public boolean authorizeRick(User user) {
        List<BotUser> ricks = this.getBotUsers(BotUserType.RICK, user.getHostmask());
        return !ricks.isEmpty();
    }

    /**
     * Add some users to the bot.
     *
     * @param botUserDao the BotUserDao object we're using
     * @throws Exception if anything goes wrong
     */
    private static void addBotUsers(BotUserDao botUserDao) throws Exception {
        for (BotUser botUser : generateBotUsers()) {
            botUserDao.add(botUser);
        }
    }

    /**
     * Generate some bot users for testing.
     *
     * @return list of bot users
     */
    private static List<BotUser> generateBotUsers() {
        final List<BotUser> botUsers = new ArrayList<>();
        botUsers.add(new BotUser(1, "brian", "*!brian@hatemachine.net", BotUserType.RICK));
        botUsers.add(new BotUser(3, "megan", "*!megan@hugmachine.net", BotUserType.MORTY));
        botUsers.add(new BotUser(2, "drgonzo", "*!gonzo@*.beerandloathing.org", BotUserType.JERRY));
        return botUsers;
    }

    static String getStringProperty(String name, String defaultValue) {
        String prop = getStringProperty(name);
        return prop == null ? defaultValue : prop;
    }

    static boolean getBooleanProperty(String name, boolean defaultValue) {
        String prop = getStringProperty(name);
        return prop == null ? defaultValue : "true".equalsIgnoreCase(prop);
    }

    static int getIntProperty(String name, int defaultValue) {
        String prop = getStringProperty(name);
        return prop == null ? defaultValue : Integer.parseInt(prop);
    }

    static String getStringProperty(String name) {
        String prop = System.getProperty(name);
        return prop == null ? properties.getProperty(name) : prop;
    }
}
