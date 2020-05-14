package net.hatemachine.mortybot;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MortyBot extends PircBotX {

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
            System.out.println("Invalid integer value encountered in properties file.");
            e.printStackTrace();
        }

        if (config == null) {
            System.out.println("Bot config not found! Exiting...");
            return;
        }

        // Start the bot and connect to a server
        try {
            MortyBot bot = new MortyBot(config);
            // todo - need a better way to handle admins
            bot.addBotAdmin("fudd");
            bot.startBot();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addBotAdmin(String nick) {
        if (!_admins.contains(nick)) {
            _admins.add(nick);
        }
    }

    public boolean isAdmin(String nick) {
        return _admins.contains(nick);
    }

    private List<String> _admins = new ArrayList<>();
}
