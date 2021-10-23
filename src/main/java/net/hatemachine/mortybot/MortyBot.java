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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static net.hatemachine.mortybot.util.IrcUtils.validateHostmask;
import static net.hatemachine.mortybot.util.StringUtils.*;

public class MortyBot extends PircBotX {

    // our main properties file
    private static final String  PROPERTIES_FILE = "bot.properties";

    // file containing users to add to the bot
    private static final String  BOT_USERS_FILE = "users.conf";

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
        loadBotUsersFromFile(System.getenv("MORTYBOT_HOME") + "/conf/" + BOT_USERS_FILE);
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
     * Get all the bot users that have a specific user flag.
     *
     * @param flag the user flag you are interested in
     * @return list of bot users that have the flag
     */
    public List<BotUser> getBotUsers(BotUser.Flag flag) {
        return botUserDao.getAll()
                .stream()
                .filter(u -> u.getFlags().contains(flag))
                .collect(toList());
    }

    /**
     * Get all the bot users that have a matching hostmask and flag.
     *
     * @param hostmask the user's hostmask
     * @param flag the user flag you are interested in
     * @return list of bot users matching both the hostmask and flag
     */
    public List<BotUser> getBotUsers(String hostmask, BotUser.Flag flag) {
        return botUserDao.getAll()
                .stream()
                .filter(u -> u.getFlags().contains(flag))
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
     * @throws BotUserException if there is an issue adding the user
     */
    public void addBotUser(String name, String hostmask) throws BotUserException {
        BotUser user = new BotUser(validateBotUsername(name), validateHostmask(hostmask));
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
            log.info("Removed hostmask {} from {}", hostmask, user.getName());
        } else {
            log.error("Failed to remove hostmask {} from {}", hostmask, user.getName());
        }
    }

    /**
     * Add a flag to a bot user.
     *
     * @param name the name of the bot user
     * @param flag the flag that you want to add
     * @throws BotUserException if there is an issue adding the flag
     */
    public void addBotUserFlag(String name, BotUser.Flag flag) throws BotUserException {
        BotUser user = botUserDao.getByName(validateString(name));
        if (user.getFlags().contains(flag)) {
            throw new BotUserException(BotUserException.Reason.FLAG_EXISTS, flag.toString());
        }
        if (user.addFlag(flag)) {
            botUserDao.update(user);
            log.info("Added {} flag to {}", flag, user.getName());
        } else {
            log.error("Failed to add {} flag to {}", flag, user.getName());
        }
    }

    /**
     * Remove a flag from a bot user.
     *
     * @param name the name of the bot user
     * @param flag the flag that you want to remove
     * @throws BotUserException if there is an issue removing the flag
     */
    public void removeBotUserFlag(String name, BotUser.Flag flag) throws BotUserException {
        BotUser user = botUserDao.getByName(validateString(name));
        if (!user.getFlags().contains(flag)) {
            throw new BotUserException(BotUserException.Reason.FLAG_NOT_FOUND, flag.toString());
        }
        if (user.removeFlag(flag)) {
            botUserDao.update(user);
            log.info("Removed {} flag from {}", flag, user.getName());
        } else {
            log.error("Failed to remove {} flag from {}", flag, user.getName());
        }
    }

    /**
     * Find out if a user is an admin.
     *
     * @param user that you want to verify
     * @return true if user is an admin
     */
    public boolean isAdmin(User user) {
        List<BotUser> admins = this.getBotUsers(user.getHostmask(), BotUser.Flag.ADMIN);
        return !admins.isEmpty();
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
     * Load some bot users from a file.
     *
     * @param filename the name of the file to load users from
     */
    private static void loadBotUsersFromFile(String filename) {
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(Path.of(filename));
        } catch (IOException e) {
            log.error("Error reading file: {}", filename);
            e.printStackTrace();
        }

        for (String line : lines) {
            if (line.startsWith("//")) {
                // comment, ignore...
            } else if (isValidString(line)) {
                List<String> tokens = Arrays.asList(line.split(" "));
                if (tokens.size() >= 2) {
                    String name = validateBotUsername(tokens.get(0));
                    String[] hostmasks = tokens.get(1).split(",");
                    Set<BotUser.Flag> flags = new HashSet<>();
                    if (tokens.size() == 3) {
                        flags = parseUserFlags(tokens.get(2));
                    }
                    BotUser user = new BotUser(name, validateHostmask(hostmasks[0]), flags);
                    if (hostmasks.length > 1) {
                        for (int i = 1; i < hostmasks.length; i++) {
                            user.addHostmask(validateHostmask(hostmasks[i]));
                        }
                    }
                    log.debug("Adding user: {}", user);
                    botUserDao.add(user);
                }
            }
        }
    }

    /**
     * Parse a comma-delimited string of user flags into a set of enums representing those flags.
     *
     * @param flagList the comma-delimited list of flags
     * @return a set of user flags
     */
    private static Set<BotUser.Flag> parseUserFlags(String flagList) {
        Set<BotUser.Flag> flags = new HashSet<>();
        for (String flagStr : flagList.split(",")) {
            BotUser.Flag flag;
            try {
                flag = Enum.valueOf(BotUser.Flag.class, flagStr.toUpperCase(Locale.ROOT));
                flags.add(flag);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid user flag: {}", flagStr.toUpperCase(Locale.ROOT));
            }
        }
        return flags;
    }
}
