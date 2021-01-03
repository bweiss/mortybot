package net.hatemachine.mortybot;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.exception.IrcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class MortyBot extends PircBotX {

    private static final Logger log = LoggerFactory.getLogger(MortyBot.class);

    private static final BotUserDao botUserDao = new InMemoryBotUserDao();
    private static BotConfiguration botConfig = null;

    MortyBot(Configuration configuration) {
        super(configuration);
    }

    public static void main(String[] args) {
        botConfig = new BotConfiguration();
        botConfig.init();
        Configuration config = null;
        config = new Configuration.Builder()
                .setName(botConfig.getBotName())
                .setLogin(botConfig.getBotLogin())
                .setRealName(botConfig.getBotRealName())
                .addServer(botConfig.getIrcServer(), botConfig.getIrcPort())
                .setSocketFactory(new UtilSSLSocketFactory().trustAllCertificates())
                .setAutoReconnect(botConfig.isAutoReconnect())
                .setAutoReconnectDelay(botConfig.getAutoReconnectDelay())
                .setAutoReconnectAttempts(botConfig.getAutoReconnectAttempts())
                .setAutoNickChange(botConfig.isAutoNickChange())
                .addAutoJoinChannels(Collections.singletonList(botConfig.getChannels()))
                .addListener(new CommandListener(botConfig.getCommandPrefix()))
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
     * Retrieve the bot's configuration
     * @return
     */
    public BotConfiguration getBotConfig() {
        return botConfig;
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
}
