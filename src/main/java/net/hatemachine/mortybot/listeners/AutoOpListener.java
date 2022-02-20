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
package net.hatemachine.mortybot.listeners;

import net.hatemachine.mortybot.BotUser;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotState;
import org.pircbotx.Channel;
import org.pircbotx.UserHostmask;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class AutoOpListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(AutoOpListener.class);

    private final Map<String, Queue<String>> pending;

    public AutoOpListener() {
        this.pending = new HashMap<>();
    }

    @Override
    public void onJoin(final JoinEvent event) {
        log.debug("onJoin event: {}", event);
        boolean enabled = BotState.getBotState().getBooleanProperty("auto.op", BotDefaults.AUTO_OP);
        if (enabled) {
            handleJoin(event);
        }
    }

    @Override
    public void onNickChange(final NickChangeEvent event) {
        log.debug("NickChangeEvent: {} -> {}", event.getOldNick(), event.getNewNick());
        handleNickChange(event);
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
        final List<BotUser> matchedUsers = bot.getBotUserDao().getAll(hostmask.getHostmask(), BotUser.Flag.AOP);

        if (!matchedUsers.isEmpty()) {
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
                        int delay = BotState.getBotState()
                                .getIntProperty("auto.op.delay", BotDefaults.AUTO_OP_DELAY);
                        Thread.sleep(delay);
                        processQueue(event, channel);
                    } catch (InterruptedException e) {
                        log.warn("Thread interrupted", e);
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        }
    }

    /**
     * Handle a nick change event and update any queues containing that user's nick.
     *
     * @param event the nick change event
     */
    private synchronized void handleNickChange(final NickChangeEvent event) {
        pending.forEach((chan, queue) -> {
            if (queue.contains(event.getOldNick())) {
                queue.remove(event.getOldNick());
                queue.add(event.getNewNick());
            }
        });
    }

    /**
     * Process the op queue for a particular channel.
     *
     * @param event the join event that triggered the auto-op action
     * @param channel the channel it occurred on
     */
    private synchronized void processQueue(final JoinEvent event, final Channel channel) {
        MortyBot bot = event.getBot();
        Map<String, String> serverSupport = bot.getServerSupportMap();
        BotState state = BotState.getBotState();
        int maxModes = state.getIntProperty("auto.op.max.modes", BotDefaults.AUTO_OP_MAX_MODES);

        try {
            maxModes = Integer.parseInt(serverSupport.get("MODES"));
        } catch (NumberFormatException e) {
            log.warn("Couldn't determine server's maximum modes per command. Falling back to default...");
        }

        if (pending.containsKey(channel.getName())) {
            Queue<String> queue = pending.get(channel.getName());

            log.info("Attempting to op {} users on {}", queue.size(), channel.getName());

            while (!queue.isEmpty()) {
                StringBuilder modes = new StringBuilder();
                List<String> targets = new ArrayList<>();

                while (targets.size() < maxModes && !queue.isEmpty()) {
                    String nick = queue.remove();
                    if (channel.isOp(bot.getUserChannelDao().getUser(nick))) {
                        log.debug("{} already has operator status on {}", nick, channel.getName());
                    } else {
                        modes.append("o");
                        targets.add(nick);
                    }
                }

                if (!channel.isOp(bot.getUserBot())) {
                    log.debug("Bot is not an operator on {}", channel.getName());
                } else if (targets.isEmpty()) {
                    log.debug("No targets to op!");
                } else {
                    bot.sendIRC().mode(channel.getName(), "+" + modes + " " + String.join(" ", targets));
                }
            }

            pending.remove(channel.getName());
        }
    }
}
