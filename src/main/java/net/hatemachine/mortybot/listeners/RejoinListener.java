package net.hatemachine.mortybot.listeners;

import net.hatemachine.mortybot.MortyBot;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.ServerPingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.hatemachine.mortybot.MortyBot.getStringProperty;

public class RejoinListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(RejoinListener.class);

    /**
     * Rejoin a channel if the bot is kicked.
     *
     * @param event the kick event
     */
    @Override
    public void onKick(final KickEvent event) {
        MortyBot bot = event.getBot();
        User recipient = event.getRecipient();
        if (recipient != null && recipient.getNick().equals(bot.getNick())) {
            log.info("Bot was kicked from {}, re-joining...", event.getChannel().getName());
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    bot.sendIRC().joinChannel(event.getChannel().getName());
                } catch (InterruptedException e) {
                    log.warn("Thread interrupted!");
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    /**
     * Periodically checks to make sure we're joined to our auto-join channels.
     *
     * @param event the server ping event
     */
    @Override
    public void onServerPing(final ServerPingEvent event) {
        MortyBot bot = event.getBot();
        String[] channels = getStringProperty("autoJoinChannels").split(" ");
        for (String chan : channels) {
            if (!bot.getUserChannelDao().containsChannel(chan)) {
                log.info("Auto-joining channel {}", chan);
                bot.sendIRC().joinChannel(chan);
            }
        }
    }
}
