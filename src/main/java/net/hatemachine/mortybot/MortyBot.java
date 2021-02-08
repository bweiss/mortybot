package net.hatemachine.mortybot;

import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.exception.IrcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class MortyBot extends PircBotX {

    private static final String  PROPERTIES_FILE = "bot.properties";

    private static final String  BOT_NAME_DEFAULT = "morty";
    private static final String  BOT_LOGIN_DEFAULT = "morty";
    private static final String  BOT_REAL_NAME_DEFAULT = "Aww jeez, Rick!";
    private static final String  IRC_SERVER_DEFAULT = "irc.efnet.org";
    private static final int     IRC_PORT_DEFAULT = 6667;
    private static final boolean AUTO_RECONNECT_DEFAULT = false;
    private static final int     AUTO_RECONNECT_DELAY_DEFAULT = 30000;
    private static final int     AUTO_RECONNECT_ATTEMPTS_DEFAULT = 3;
    private static final boolean AUTO_NICK_CHANGE_DEFAULT = true;
    private static final String  AUTO_JOIN_CHANNELS_DEFAULT = "";
    private static final String  COMMAND_PREFIX_DEFAULT = ".";

    private static final Logger log = LoggerFactory.getLogger(MortyBot.class);

    private static final BotUserDao botUserDao = new InMemoryBotUserDao();
    private static final Properties properties = new Properties();

    {
        // This is needed to use JsonPath with Jackson
        com.jayway.jsonpath.Configuration.setDefaults(new com.jayway.jsonpath.Configuration.Defaults() {
            private final JsonProvider jsonProvider = new JacksonJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }

    MortyBot(Configuration config) {
        super(config);
    }

    public static void main(String[] args) {
        try (InputStream inputStream = MortyBot.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            log.warn("Unable to read properties file {} (defaults will be used)", PROPERTIES_FILE, e);
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
                .addAutoJoinChannels(Collections.singletonList(getStringProperty("autoJoinChannels", AUTO_JOIN_CHANNELS_DEFAULT)))
                .addListener(new CommandListener(getStringProperty("commandPrefix", COMMAND_PREFIX_DEFAULT)))
                .addListener(new LinkListener())
                .buildConfiguration();

        // extra sanity check
        if (config == null) {
            log.error("Bot config not found, exiting...");
            return;
        }

        // Add some users
        try {
            addBotUsers(botUserDao);
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.debug("Bot users:");
        botUserDao.getAllBotUsers().forEach(u -> log.debug(u.toString()));

        // Start the bot and connect to a server
        try (MortyBot bot = new MortyBot(config)) {
            log.info("Starting bot with nick: {}", bot.getNick());
            bot.startBot();
        } catch (IOException | IrcException e) {
            log.error("Failed to start bot, exiting...", e);
        }
    }

    /**
     * @return list of all bot users
     */
    public List<BotUser> getBotUsers() {
        return botUserDao.getAllBotUsers();
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

    static String getStringProperty(String name, String defaultValue) {
        String prop = getStringProperty(name);
        return prop == null ? defaultValue : prop;
    }

    static boolean getBooleanProperty(String name, boolean defaultValue) {
        String prop = getStringProperty(name);
        return prop == null ? defaultValue : "true".equalsIgnoreCase(prop);
    }

    static int getIntProperty(String name, int defaultValue) {
        String prop = getStringProperty(name);
        return prop == null ? defaultValue : Integer.parseInt(prop);
    }

    static String getStringProperty(String name) {
        String prop = System.getProperty(name);
        return prop == null ? properties.getProperty(name) : prop;
    }
}
