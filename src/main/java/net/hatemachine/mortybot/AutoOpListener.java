package net.hatemachine.mortybot;

import org.pircbotx.Channel;
import org.pircbotx.UserHostmask;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AutoOpListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(AutoOpListener.class);

    @Override
    public void onJoin(final JoinEvent event) throws InterruptedException {
        log.debug("onJoin event: {}", event);
        boolean enabled = MortyBot.getBooleanProperty("AutoOpListener.enabled", false);
        if (enabled) {
            handleJoin(event);
        }
    }

    /**
     * Handle a join event to a channel that the bot is on. This will check to see if the user's hostmask
     * matches that of a bot user that has the AOP flag. If a match is found it will set mode +o for that user.
     *
     * @param event the JoinEvent
     */
    private void handleJoin(final JoinEvent event) throws InterruptedException {
        MortyBot bot = event.getBot();
        Channel channel = event.getChannel();
        UserHostmask uh = event.getUserHostmask();
        List<BotUser> autoOpUsers = bot.getBotUsers(uh.getHostmask(), BotUser.Flag.AOP);

        if (bot.hasOps(channel) && !autoOpUsers.isEmpty()) {
            Thread.sleep(4000);
            log.info("Automatically granting channel operator status to {} on {}", uh.getNick(), channel.getName());
            bot.sendIRC().mode(channel.getName(), "+o " + uh.getNick());
        }
    }
}
