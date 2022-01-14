package net.hatemachine.mortybot;

import net.hatemachine.mortybot.listeners.AutoOpListener;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.listeners.LinkListener;
import net.hatemachine.mortybot.listeners.RejoinListener;
import net.hatemachine.mortybot.listeners.ServerSupportListener;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.exception.IrcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MortyBot extends PircBotX {

    public static final String   VERSION = "1.0-SNAPSHOT";

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

    private static final Properties properties = new Properties();

    private final String botHome;
    private final BotUserDao botUserDao;
    private final Map<String, String> serverSupportMap;

    MortyBot(Configuration config) {
        super(config);
        this.botHome = System.getenv("MORTYBOT_HOME");
        this.botUserDao = new BotUserDaoImpl(this);
        this.serverSupportMap = new HashMap<>();
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

        String propertiesFile = System.getenv("MORTYBOT_HOME") + "/conf/" + PROPERTIES_FILE;
        try (var reader = new FileReader(propertiesFile)) {
            properties.load(reader);
        } catch (FileNotFoundException e) {
            log.error("Properties file not found");
        } catch (IOException e) {
            log.error("Unable to read properties file");
        }

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
                .addAutoJoinChannels(Arrays.asList(getStringProperty("autoJoinChannels", AUTO_JOIN_CHANNELS_DEFAULT).split(" ")))
                .addListener(new ServerSupportListener())
                .addListener(new AutoOpListener())
                .addListener(new CommandListener(getStringProperty("commandPrefix", COMMAND_PREFIX_DEFAULT)))
                .addListener(new LinkListener())
                .addListener(new RejoinListener())
                .buildConfiguration();

        // Start the bot and connect to a server
        try (MortyBot bot = new MortyBot(config)) {
            log.info("Starting bot with nick: {}", bot.getNick());
            bot.startBot();
        } catch (IrcException e) {
            log.error("IrcException: ", e);
        } catch (IOException e ) {
            log.error("IOException: ", e);
        } catch (Exception e) {
            log.error("Unhandled Exception: ", e);
        }
    }

    public String getBotHome() {
        return botHome;
    }

    public BotUserDao getBotUserDao() {
        return botUserDao;
    }

    /**
     * Get a map of the server support (005 numeric).
     *
     * @return map of the server support parameters and their values
     */
    public Map<String, String> getServerSupportMap() {
        return serverSupportMap;
    }

    /**
     * Set the value for a server support (005 numeric) parameter.
     *
     * @param key the key you want to set
     * @param value the value to assign to that key
     */
    public void setServerSupportKey(String key, String value) {
        serverSupportMap.put(key, value);
    }

    /**
     * Clear the server support map. Used for events like server disconnects.
     */
    public void clearServerSupport() {
        serverSupportMap.clear();
    }

    public static String getStringProperty(String name, String defaultValue) {
        var prop = getStringProperty(name);
        return prop == null ? defaultValue : prop;
    }

    public static boolean getBooleanProperty(String name, boolean defaultValue) {
        var prop = getStringProperty(name);
        return prop == null ? defaultValue : prop.equalsIgnoreCase("true");
    }

    public static int getIntProperty(String name, int defaultValue) {
        var prop = getStringProperty(name);
        return prop == null ? defaultValue : Integer.parseInt(prop);
    }

    public static String getStringProperty(String name) {
        var prop = System.getProperty(name);
        return prop == null ? properties.getProperty(name) : prop;
    }
}
