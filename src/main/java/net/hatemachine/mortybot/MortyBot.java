package net.hatemachine.mortybot;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MortyBot extends PircBotX {

    private static final Logger log = LoggerFactory.getLogger(MortyBot.class);

    private static final List<String> _admins = new ArrayList<>();

    public MortyBot(Configuration configuration) {
        super(configuration);
    }

    public static void main(String[] args) {

        // Load our bot properties
        Properties botProperties = new Properties();
        try (InputStream inputStream = MortyBot.class.getClassLoader().getResourceAsStream("bot.properties")) {
            if (inputStream != null) {
                botProperties.load(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Build the bot configuration
        Configuration config = null;
        try {
            config = new Configuration.Builder()
                    .setName(botProperties.getProperty("botName", "botx"))
                    .setLogin(botProperties.getProperty("botLogin", "botx"))
                    .setRealName(botProperties.getProperty("botRealName", "Someone should change me!"))
                    .addServer(botProperties.getProperty("ircServer", "irc.hatemachine.net"),
                            Integer.parseInt(botProperties.getProperty("ircPort", "6667")))
                    .setSocketFactory(new UtilSSLSocketFactory().trustAllCertificates())
                    .setAutoReconnect(botProperties.getProperty("autoReconnect").equalsIgnoreCase("true"))
                    .setAutoReconnectDelay(Integer.parseInt(botProperties.getProperty("autoReconnectDelay", "60000")))
                    .setAutoReconnectAttempts(Integer.parseInt(botProperties.getProperty("autoReconnectAttempts", "5")))
                    .setAutoNickChange(botProperties.getProperty("autoNickChange").equalsIgnoreCase("true"))
                    .addAutoJoinChannels(Arrays.asList(botProperties.getProperty("channels").split(" ")))
                    .addListener(new MessageListener(botProperties.getProperty("commandPrefix", "!")))
                    .buildConfiguration();
        } catch (NumberFormatException e) {
            log.error("Invalid integer value encountered in properties file.");
            e.printStackTrace();
        }

        if (config == null) {
            log.error("Bot config not found! Exiting...");
            return;
        }

        // Start the bot and connect to a server
        try {
            MortyBot bot = new MortyBot(config);
            // todo - need a better way to handle admins
            if (bot.addBotAdmin(botProperties.getProperty("botAdmin"))) {
                log.info("Bot admin: " + botProperties.getProperty("botAdmin"));
            } else {
                log.warn("No default bot admin specified!");
            }
            log.info("Starting up bot with nick: " + bot.getNick());
            bot.startBot();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean addBotAdmin(String nick) {
        if (nick != null && !_admins.contains(nick)) {
            _admins.add(nick);
            return true;
        } else {
            return false;
        }
    }

    public static boolean isAdmin(String nick) {
        return _admins.contains(nick);
    }
}
