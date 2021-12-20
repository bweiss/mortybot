package net.hatemachine.mortybot.listeners;

import net.hatemachine.mortybot.BotUser;
import net.hatemachine.mortybot.MortyBot;
import org.pircbotx.Channel;
import org.pircbotx.UserHostmask;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static net.hatemachine.mortybot.util.IrcUtils.userHasOps;

public class AutoOpListener extends ListenerAdapter {

    private static final int MODES_PER_COMMAND_DEFAULT = 4;
    private static final int DELAY_IN_SECONDS_DEFAULT = 10;

    private static final Logger log = LoggerFactory.getLogger(AutoOpListener.class);

    private final Map<String, Queue<String>> pending;

    public AutoOpListener() {
        this.pending = new HashMap<>();
    }

    @Override
    public void onJoin(final JoinEvent event) {
        log.debug("onJoin event: {}", event);
        boolean enabled = MortyBot.getBooleanProperty("AutoOpListener.enabled", false);
        if (enabled) {
            handleJoin(event);
        }
    }

    /**
     * Handle a join event to a channel that the bot is on. This will check to see if the user's hostmask
     * matches that of a bot user that has the AOP flag. If a match is found it will add that user to the
     * pending op queue for that channel and spin up a separate thread to process any modes for that channel.
     *
     * @param event the join event
     */
    private synchronized void handleJoin(final JoinEvent event) {
        final MortyBot bot = event.getBot();
        final Channel channel = event.getChannel();
        final UserHostmask hostmask = event.getUserHostmask();
        final String nick = hostmask.getNick();
        final List<BotUser> matchedUsers = bot.getBotUsers(hostmask.getHostmask(), BotUser.Flag.AOP);

        if (bot.hasOps(channel) && !matchedUsers.isEmpty()) {
            log.debug("Adding {} to auto-op queue for {}", nick, channel.getName());

            if (pending.containsKey(channel.getName())) {
                Queue<String> queue = pending.get(channel.getName());
                if (!queue.contains(nick)) {
                    queue.add(nick);
                }
            } else {
                Queue<String> queue = new LinkedList<>();
                queue.add(nick);
                pending.put(channel.getName(), queue);

                new Thread(() -> {
                    try {
                        long delay = MortyBot.getIntProperty("AutoOpListener.delay_in_seconds", DELAY_IN_SECONDS_DEFAULT);
                        Thread.sleep(delay * 1000);
                        processQueue(event, channel);
                    } catch (InterruptedException e) {
                        log.warn("thread interrupted!");
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        }
    }

    /**
     * Process the op queue for a particular channel.
     *
     * @param event the join event that triggered the auto-op action
     * @param channel the channel it occurred on
     */
    private synchronized void processQueue(final JoinEvent event, final Channel channel) {
        MortyBot bot = event.getBot();
        Map<String, String> serverSupport = bot.getServerSupport();
        int modesPerCommand = MODES_PER_COMMAND_DEFAULT;
        try {
            modesPerCommand = Integer.parseInt(serverSupport.get("MODES"));
        } catch (NumberFormatException e) {
            log.warn("Invalid value for server support parameter MODES. Falling back to default...");
        }

        if (pending.containsKey(channel.getName())) {
            Queue<String> queue = pending.get(channel.getName());

            log.info("Attempting to op {} users on {}", queue.size(), channel.getName());

            while (!queue.isEmpty()) {
                StringBuilder modes = new StringBuilder();
                List<String> targets = new ArrayList<>();

                while (targets.size() < modesPerCommand && !queue.isEmpty()) {
                    String userNick = queue.remove();
                    if (!userHasOps(userNick, channel)) {
                        modes.append("o");
                        targets.add(userNick);
                    }
                }

                bot.sendIRC().mode(channel.getName(), "+" + modes + " " + String.join(" ", targets));
            }

            pending.remove(channel.getName());
        }
    }
}
