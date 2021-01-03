package net.hatemachine.mortybot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BotConfiguration {

    private static final String CONFIGURATION_FILE = "bot.properties";

    private static final String BOT_NAME_DEFAULT = "morty";
    private static final String BOT_LOGIN_DEFAULT = "morty";
    private static final String BOT_REAL_NAME_DEFAULT = "Aww jeez, Rick!";
    private static final String IRC_SERVER_DEFAULT = "irc.efnet.org";
    private static final int IRC_PORT_DEFAULT = 6667;
    private static final boolean AUTO_RECONNECT_DEFAULT = false;
    private static final int AUTO_RECONNECT_DELAY_DEFAULT = 30000;
    private static final int AUTO_RECONNECT_ATTEMPTS_DEFAULT = 3;
    private static final boolean AUTO_NICK_CHANGE_DEFAULT = true;
    private static final String CHANNELS_DEFAULT = "";
    private static final String COMMAND_PREFIX_DEFAULT = ".";

    private String botName;
    private String botLogin;
    private String botRealName;
    private String ircServer;
    private int ircPort;
    private boolean autoReconnect;
    private int autoReconnectDelay;
    private int autoReconnectAttempts;
    private boolean autoNickChange;
    private String channels;
    private String commandPrefix;

    private final Properties properties;

    private static final Logger log = LoggerFactory.getLogger(BotConfiguration.class);

    public BotConfiguration() {
        this.botName = null;
        this.botLogin = null;
        this.botRealName = null;
        this.ircServer = null;
        this.ircPort = 0;
        this.autoReconnect = false;
        this.autoReconnectDelay = 0;
        this.autoReconnectAttempts = 0;
        this.autoNickChange = false;
        this.channels = null;
        this.commandPrefix = null;
        this.properties = new Properties();
    }

    void init() {
        this.loadProperties();
        this.botName = this.getStringProperty("botName", BOT_NAME_DEFAULT);
        this.botLogin = this.getStringProperty("botLogin", BOT_LOGIN_DEFAULT);
        this.botRealName = this.getStringProperty("botRealName", BOT_REAL_NAME_DEFAULT);
        this.ircServer = this.getStringProperty("ircServer", IRC_SERVER_DEFAULT);
        this.ircPort = this.getIntProperty("ircPort", IRC_PORT_DEFAULT);
        this.autoReconnect = this.getBooleanProperty("autoReconnect", AUTO_RECONNECT_DEFAULT);
        this.autoReconnectDelay = this.getIntProperty("autoReconnectDelay", AUTO_RECONNECT_DELAY_DEFAULT);
        this.autoReconnectAttempts = this.getIntProperty("autoReconnectAttempts", AUTO_RECONNECT_ATTEMPTS_DEFAULT);
        this.autoNickChange = this.getBooleanProperty("autoNickChange", AUTO_NICK_CHANGE_DEFAULT);
        this.channels = this.getStringProperty("channels", CHANNELS_DEFAULT);
        this.commandPrefix = this.getStringProperty("commandPrefix", COMMAND_PREFIX_DEFAULT);
    }

    private void loadProperties() {
        try (InputStream inputStream = BotConfiguration.class.getClassLoader().getResourceAsStream(CONFIGURATION_FILE)) {
            if (inputStream != null) {
                this.properties.load(inputStream);
            }
        } catch (IOException e) {
            log.warn("Unable to read properties file {} (defaults will be used)", CONFIGURATION_FILE, e);
        }
    }

    String getStringProperty(String name, String defaultValue) {
        String prop = this.getStringProperty(name);
        return prop == null ? defaultValue : prop;
    }

    boolean getBooleanProperty(String name, boolean defaultValue) {
        String prop = this.getStringProperty(name);
        return prop == null ? defaultValue : "true".equalsIgnoreCase(prop);
    }

    int getIntProperty(String name, int defaultValue) {
        String prop = this.getStringProperty(name);
        return prop == null ? defaultValue : Integer.parseInt(prop);
    }

    String getStringProperty(String name) {
        String prop = System.getProperty(name);
        return prop == null ? this.properties.getProperty(name) : prop;
    }

    public String getBotName() {
        return botName;
    }

    public String getBotLogin() {
        return botLogin;
    }

    public String getBotRealName() {
        return botRealName;
    }

    public String getIrcServer() {
        return ircServer;
    }

    public int getIrcPort() {
        return ircPort;
    }

    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    public int getAutoReconnectDelay() {
        return autoReconnectDelay;
    }

    public int getAutoReconnectAttempts() {
        return autoReconnectAttempts;
    }

    public boolean isAutoNickChange() {
        return autoNickChange;
    }

    public String getChannels() {
        return channels;
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }
}
