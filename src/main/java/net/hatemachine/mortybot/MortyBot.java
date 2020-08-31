package net.hatemachine.mortybot;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.exception.IrcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

public class MortyBot extends PircBotX {

    private static final Logger log = LoggerFactory.getLogger(MortyBot.class);
    private static final Properties props = new Properties();

    public MortyBot(Configuration configuration) {
        super(configuration);
    }

    public static void main(String[] args) {

        // Load our bot properties
        try (InputStream inputStream = MortyBot.class.getClassLoader().getResourceAsStream("bot.properties")) {
            if (inputStream != null) {
                props.load(inputStream);
            }
        } catch (IOException e) {
            log.warn("Unable to read bot properties file (defaults will be used)", e);
        }

        // Build the bot configuration
        Configuration config = null;
        try {
            config = new Configuration.Builder()
                    .setName(props.getProperty("botName", "morty"))
                    .setLogin(props.getProperty("botLogin", "morty"))
                    .setRealName(props.getProperty("botRealName", "Someone should change me!"))
                    .addServer(props.getProperty("ircServer", "irc.hatemachine.net"),
                            Integer.parseInt(props.getProperty("ircPort", "6667")))
                    .setSocketFactory(new UtilSSLSocketFactory().trustAllCertificates())
                    .setAutoReconnect(props.getProperty("autoReconnect").equalsIgnoreCase("true"))
                    .setAutoReconnectDelay(Integer.parseInt(props.getProperty("autoReconnectDelay", "60000")))
                    .setAutoReconnectAttempts(Integer.parseInt(props.getProperty("autoReconnectAttempts", "5")))
                    .setAutoNickChange(props.getProperty("autoNickChange").equalsIgnoreCase("true"))
                    .addAutoJoinChannels(Arrays.asList(props.getProperty("channels").split(" ")))
                    .addListener(new MessageListener(props.getProperty("commandPrefix", "!")))
                    .buildConfiguration();
        } catch (NumberFormatException e) {
            log.error("Invalid server port", e);
        }

        // extra sanity check
        if (config == null) {
            log.error("Bot config not found, exiting...");
            return;
        }

        // Start the bot and connect to a server
        try (MortyBot bot = new MortyBot(config)) {
            log.info("Starting bot with nick: {}", bot.getNick());
            bot.startBot();
        } catch (IOException | IrcException e) {
            log.error("Failed to start bot, exiting...", e);
        }
    }
}
