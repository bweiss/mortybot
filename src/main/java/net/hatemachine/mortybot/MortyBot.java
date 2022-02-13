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

import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotState;
import net.hatemachine.mortybot.listeners.AutoOpListener;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.listeners.CoreHooksListener;
import net.hatemachine.mortybot.listeners.LinkListener;
import net.hatemachine.mortybot.listeners.RejoinListener;
import net.hatemachine.mortybot.listeners.ServerSupportListener;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.delay.StaticDelay;
import org.pircbotx.exception.IrcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MortyBot extends PircBotX {

    public static final String VERSION = "0.4.1";

    private static final Logger log = LoggerFactory.getLogger(MortyBot.class);

    private final BotUserDao botUserDao;
    private final Map<String, String> serverSupportMap;

    MortyBot(Configuration config) {
        super(config);
        this.botUserDao = new BotUserDaoImpl();
        this.serverSupportMap = new HashMap<>();
    }

    /**
     * Main entry point for the bot. Responsible for initial configuration and starting the bot.
     *
     * @param args command line arguments for the bot
     */
    public static void main(String[] args) {
        BotState state = BotState.getBotState();

        // Build our configuration
        Configuration.Builder config = new Configuration.Builder()
                .setName(state.getStringProperty("bot.name", BotDefaults.BOT_NAME))
                .setLogin(state.getStringProperty("bot.login", BotDefaults.BOT_LOGIN))
                .setRealName(state.getStringProperty("bot.realname", BotDefaults.BOT_REALNAME))
                .addServer(state.getStringProperty("irc.server", BotDefaults.IRC_SERVER),
                        state.getIntProperty("irc.port", BotDefaults.IRC_PORT))
                .setSocketFactory(new UtilSSLSocketFactory().trustAllCertificates())
                .setAutoReconnect(state.getBooleanProperty("auto.reconnect", BotDefaults.AUTO_RECONNECT))
                .setAutoReconnectDelay(new StaticDelay(state.getIntProperty("auto.reconnect.delay", BotDefaults.AUTO_RECONNECT_DELAY)))
                .setAutoReconnectAttempts(state.getIntProperty("auto.reconnect.attempts", BotDefaults.AUTO_RECONNECT_ATTEMPTS))
                .setAutoNickChange(state.getBooleanProperty("auto.nick.change", BotDefaults.AUTO_NICK_CHANGE))
                .addListener(new AutoOpListener())
                .addListener(new CommandListener(state.getStringProperty("bot.command.prefix", BotDefaults.BOT_COMMAND_PREFIX)))
                .addListener(new LinkListener())
                .addListener(new RejoinListener())
                .addListener(new ServerSupportListener());

        // Add our auto join channels if specified in the properties
        String channels = state.getStringProperty("auto.join.channels");
        if (channels != null && !channels.trim().isEmpty()) {
            config.addAutoJoinChannels(Arrays.asList(state.getStringProperty("auto.join.channels").split(" ")));
        }

        // Replace the CoreHooks listener with our own implementation
        config.replaceCoreHooksListener(new CoreHooksListener());

        // Start the bot
        try (MortyBot bot = new MortyBot(config.buildConfiguration())) {
            log.info("Starting bot with nick: {}", bot.getNick());
            bot.startBot();
        } catch (IrcException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error("Exception encountered during startup", e);
        }
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
     * Set the value for a server support parameter (005 numeric).
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MortyBot mortyBot = (MortyBot) o;
        return botUserDao.equals(mortyBot.botUserDao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), botUserDao);
    }
}
