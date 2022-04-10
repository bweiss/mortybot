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
import net.hatemachine.mortybot.listeners.DccChatListener;
import net.hatemachine.mortybot.listeners.DccRequestListener;
import net.hatemachine.mortybot.listeners.LinkListener;
import net.hatemachine.mortybot.listeners.RejoinListener;
import net.hatemachine.mortybot.listeners.WordleListener;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.delay.StaticDelay;
import org.pircbotx.exception.IrcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MortyBot extends PircBotX {

    public static final String VERSION = "0.7.0-SNAPSHOT";

    private static final Logger log = LoggerFactory.getLogger(MortyBot.class);

    private final BotUserDao botUserDao;

    MortyBot(Configuration config) {
        super(config);
        this.botUserDao = new BotUserDaoImpl();
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
                .setAutoReconnect(state.getBooleanProperty("auto.reconnect", BotDefaults.AUTO_RECONNECT))
                .setAutoReconnectDelay(new StaticDelay(state.getIntProperty("auto.reconnect.delay", BotDefaults.AUTO_RECONNECT_DELAY)))
                .setAutoReconnectAttempts(state.getIntProperty("auto.reconnect.attempts", BotDefaults.AUTO_RECONNECT_ATTEMPTS))
                .setAutoNickChange(state.getBooleanProperty("auto.nick.change", BotDefaults.AUTO_NICK_CHANGE))
                .addListener(new AutoOpListener())
                .addListener(new CommandListener(state.getStringProperty("bot.command.prefix", BotDefaults.BOT_COMMAND_PREFIX)))
                .addListener(new DccChatListener())
                .addListener(new DccRequestListener())
                .addListener(new LinkListener())
                .addListener(new RejoinListener())
                .addListener(new WordleListener());

        if (state.getBooleanProperty("irc.ssl", BotDefaults.IRC_SSL)) {
            config.setSocketFactory(new UtilSSLSocketFactory().trustAllCertificates());
        }

        // Add our auto join channels if specified in the properties
        String channels = state.getStringProperty("auto.join.channels");
        if (channels != null && !channels.trim().isEmpty()) {
            config.addAutoJoinChannels(Arrays.asList(state.getStringProperty("auto.join.channels").split(" ")));
        }

        // DCC settings
        // TODO: support ranges for dcc.ports
        String dccPortsStr = state.getStringProperty("dcc.ports");
        List<Integer> dccPorts = new ArrayList<>();
        if (dccPortsStr != null && !dccPortsStr.isBlank()) {
            dccPorts = Arrays.stream(state.getStringProperty("dcc.ports").split(","))
                    .map(Integer::parseInt)
                    .toList();
        }
        if (!dccPorts.isEmpty()) {
            config.setDccPorts(dccPorts);
        }

        String dccLocalAddress = state.getStringProperty("dcc.local.address");
        if (dccLocalAddress != null) {
            try {
                InetAddress localAddress = InetAddress.getByName(dccLocalAddress);
                config.setDccLocalAddress(localAddress);
            } catch (UnknownHostException e) {
                log.warn("Unknown host provided by dcc.local.address property ({}), falling back to default", dccLocalAddress);
            }
        }

        String dccPublicAddress = state.getStringProperty("dcc.public.address");
        if (dccPublicAddress != null) {
            try {
                InetAddress publicAddress = InetAddress.getByName(dccPublicAddress);
                config.setDccPublicAddress(publicAddress);
            } catch (UnknownHostException e) {
                log.warn("Unknown host provided by dcc.public.address property ({}), falling back to default", dccLocalAddress);
            }
        }

        int dccAcceptTimeout = state.getIntProperty("dcc.accept.timeout", -1);
        if (dccAcceptTimeout > -1) {
            config.setDccAcceptTimeout(dccAcceptTimeout);
        }

        int dccResumeAcceptTimeout = state.getIntProperty("dcc.resume.accept.timeout", -1);
        if (dccResumeAcceptTimeout > -1) {
            config.setDccResumeAcceptTimeout(dccResumeAcceptTimeout);
        }

        config.setDccFilenameQuotes(state.getBooleanProperty("dcc.filename.quotes", true));

        // Replace the CoreHooks listener with our own implementation
        config.replaceCoreHooksListener(new CoreHooksListener());

        // Start the bot
        try (MortyBot bot = new MortyBot(config.buildConfiguration())) {
            log.info("Starting bot with nick: {}", bot.getNick());
            bot.startBot();
        } catch (IrcException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error("Exception encountered in main()", e);
        }
    }

    public BotUserDao getBotUserDao() {
        return botUserDao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MortyBot bot = (MortyBot) o;
        return botId == bot.getBotId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), botId);
    }
}
