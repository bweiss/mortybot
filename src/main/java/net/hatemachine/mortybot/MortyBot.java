package net.hatemachine.mortybot;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.exception.IrcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static java.util.stream.Collectors.toList;

public class MortyBot extends PircBotX {

    private static final Logger log = LoggerFactory.getLogger(MortyBot.class);
    private static final Properties props = new Properties();
    private static final BotUserDao botUserDao = new InMemoryBotUserDao();

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
                    .addListener(new CommandListener(props.getProperty("commandPrefix", "!")))
                    .buildConfiguration();
        } catch (NumberFormatException e) {
            log.error("Invalid server port", e);
        }

        // extra sanity check
        if (config == null) {
            log.error("Bot config not found, exiting...");
            return;
        }

        // Add some users for testing
        try {
            addBotUsers(botUserDao);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<BotUser> botUsers = botUserDao.getAllBotUsers();
        log.info("Number of bot users: {}", botUsers.size());
        botUsers.forEach(u -> log.info("User: {}", u));

        // Start the bot and connect to a server
        try (MortyBot bot = new MortyBot(config)) {
            log.info("Starting bot with nick: {}", bot.getNick());
            bot.startBot();
        } catch (IOException | IrcException e) {
            log.error("Failed to start bot, exiting...", e);
        }
    }

    /**
     * Returns a list of bot users that have hostmasks matching a specified userhost.
     *
     * @param userhost in the form of nick!ident@hostname
     * @return List of BotUser objects that matched
     */
    public final List<BotUser> getBotUsersByUserhost(String userhost) {
        return botUserDao.getAllBotUsers().stream().filter(u -> u.hasMatchingHostmask(userhost)).collect(toList());
    }

    /**
     * @return list of all bot users
     */
    public List<BotUser> getBotUsers() {
        return botUserDao.getAllBotUsers();
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
        botUsers.add(new BotUser(1, "brian", "*!brian@hatemachine.net", BotUser.Type.ADMIN));
        botUsers.add(new BotUser(3, "megan", "*!megan@hugmachine.net", BotUser.Type.USER));
        botUsers.add(new BotUser(2, "drgonzo", "*!gonzo@*.beerandloathing.org", BotUser.Type.GUEST));
        return botUsers;
    }
}
