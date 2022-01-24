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

import com.google.common.collect.UnmodifiableIterator;
import net.hatemachine.mortybot.listeners.AutoOpListener;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.listeners.LinkListener;
import net.hatemachine.mortybot.listeners.RejoinListener;
import net.hatemachine.mortybot.listeners.ServerSupportListener;
import net.hatemachine.mortybot.listeners.CoreHooksListener;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.delay.StaticDelay;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.CoreHooks;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.managers.ListenerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class MortyBot extends PircBotX {

    public static final String VERSION = "0.1.0";

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

    private static final Properties PROPERTIES = new Properties();
    private static final String PROPERTIES_FILE = "bot.properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(MortyBot.class);

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
            LOGGER.error("MORTYBOT_HOME not set, exiting...");
            return;
        }

        String propertiesFile = System.getenv("MORTYBOT_HOME") + "/conf/" + PROPERTIES_FILE;
        try (var reader = new FileReader(propertiesFile)) {
            PROPERTIES.load(reader);
        } catch (FileNotFoundException e) {
            LOGGER.warn("Properties file not found");
        } catch (IOException e) {
            LOGGER.warn("Unable to read properties file");
        }

        Configuration config = new Configuration.Builder()
                .setName(getStringProperty("botName", BOT_NAME_DEFAULT))
                .setLogin(getStringProperty("botLogin", BOT_LOGIN_DEFAULT))
                .setRealName(getStringProperty("botRealName", BOT_REAL_NAME_DEFAULT))
                .addServer(getStringProperty("ircServer", IRC_SERVER_DEFAULT),
                        getIntProperty("ircPort", IRC_PORT_DEFAULT))
                .setSocketFactory(new UtilSSLSocketFactory().trustAllCertificates())
                .setAutoReconnect(getBooleanProperty("autoReconnect", AUTO_RECONNECT_DEFAULT))
                .setAutoReconnectDelay(new StaticDelay(getIntProperty("autoReconnectDelay", AUTO_RECONNECT_DELAY_DEFAULT)))
                .setAutoReconnectAttempts(getIntProperty("autoReconnectAttempts", AUTO_RECONNECT_ATTEMPTS_DEFAULT))
                .setAutoNickChange(getBooleanProperty("autoNickChange", AUTO_NICK_CHANGE_DEFAULT))
                .addAutoJoinChannels(Arrays.asList(getStringProperty("autoJoinChannels", AUTO_JOIN_CHANNELS_DEFAULT).split(" ")))
                .addListener(new AutoOpListener())
                .addListener(new CommandListener(getStringProperty("commandPrefix", COMMAND_PREFIX_DEFAULT)))
                .addListener(new LinkListener())
                .addListener(new RejoinListener())
                .addListener(new ServerSupportListener())
                .buildConfiguration();

        try (MortyBot bot = new MortyBot(config)) {
            LOGGER.info("Starting bot with nick: {}", bot.getNick());
            bot.replaceCoreHooksListener(new CoreHooksListener());
            bot.startBot();
        } catch (IrcException | IOException e) {
            LOGGER.error("Exception encountered during startup: ", e);
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
        return prop == null ? PROPERTIES.getProperty(name) : prop;
    }

    /**
     * Replace the CoreHooks listener class.
     *
     * This is basically the same as the method of the same name in the PircBotX Configuration class
     * and is here because I can't seem to figure out how to use the original correctly. ;P
     *
     * @param listener the listener to replace with
     */
    public void replaceCoreHooksListener(CoreHooks listener) {
        ListenerManager listenerManager = this.getConfiguration().getListenerManager();
        UnmodifiableIterator<Listener> i = listenerManager.getListeners().iterator();
        CoreHooks orig = null;

        while (i.hasNext()) {
            Listener cur = i.next();
            if (cur instanceof CoreHooks) {
                orig = (CoreHooks) cur;
            }
        }

        if (orig != null) {
            listenerManager.removeListener(orig);
        }

        listenerManager.addListener(listener);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MortyBot mortyBot = (MortyBot) o;
        return botHome.equals(mortyBot.botHome) && botUserDao.equals(mortyBot.botUserDao) && serverSupportMap.equals(mortyBot.serverSupportMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), botHome, botUserDao);
    }
}
