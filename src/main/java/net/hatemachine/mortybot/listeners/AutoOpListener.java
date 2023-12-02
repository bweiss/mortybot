/*
 * MortyBot - An IRC bot built on the PircBotX framework.
 * Copyright © 2022 Brian Weiss (brian@hatemachine.net)
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

import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.repositories.BotUserRepository;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Listener that handles automatically granting channel operator status to bot users.
 */
public class AutoOpListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(AutoOpListener.class);

    private final Map<String, BlockingQueue<String>> pending = new HashMap<>();

    public AutoOpListener() {
        // left empty
    }

    @Override
    public void onJoin(final JoinEvent event) {
        log.debug("onJoin event: {}", event);
        boolean enabled = BotProperties.getBotProperties().getBooleanProperty("aop.enabled", BotDefaults.AUTO_OP);
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
     * Handles a join event to a channel that the bot is on. This will check to see if the user's hostmask
     * matches that of a bot user with this channel in their auto-op list. If a match is found it will add
     * that user to the pending op queue for the channel and spin up a separate thread to process any modes
     * after a short delay. This allows us to op multiple users in a single command to the server and eliminates
     * redundant modes when a user has already received operator status, greatly reducing the noisiness of the bot,
     * particularly after netsplits.
     *
     * @param event the join event
     */
    private void handleJoin(final JoinEvent event) {
        MortyBot bot = event.getBot();
        Channel channel = event.getChannel();
        String channelName = channel.getName().toLowerCase();
        User user = (User) Validate.notNull(event.getUser());

        var botUserRepository = new BotUserRepository();
        Optional<BotUser> matchingBotUser = botUserRepository.findByHostmask(user.getHostmask());

        if (matchingBotUser.isPresent() && !user.getNick().equals(bot.getNick())) {
            var botUser = matchingBotUser.get();

            if (botUser.getAutoOpChannels().stream().anyMatch(channelName::equalsIgnoreCase)) {
                log.info("Adding {} to auto-op queue for {}", user.getNick(), channelName);

                BlockingQueue<String> queue = pending.containsKey(channelName) ? pending.get(channelName) : new LinkedBlockingQueue<>();

                if (!queue.contains(user.getNick())) {
                    queue.add(user.getNick());
                }

                pending.put(channelName, queue);

                log.debug("Creating new thread to process queue for {}", channelName);

                Thread.ofVirtual().start(() -> {
                    try {
                        int delay = BotProperties.getBotProperties().getIntProperty("aop.delay", BotDefaults.AUTO_OP_DELAY);
                        log.debug("Thread {} sleeping for {}", Thread.currentThread().getName(), delay);
                        Thread.sleep(delay);
                        processQueue(channel, event);
                    } catch (InterruptedException e) {
                        log.warn("Thread interrupted: {}", Thread.currentThread().getName());
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }
    }

    /**
     * Handles a nick change event and updates any queues containing that user's nick.
     *
     * @param event the nick change event
     */
    private synchronized void handleNickChange(final NickChangeEvent event) {
        log.debug("Nick change detected: {} -> {}", event.getOldNick(), event.getNewNick());

        pending.forEach((chan, queue) -> {
            if (queue.contains(event.getOldNick())) {
                log.debug("Updating queue for {}", chan);
                queue.remove(event.getOldNick());
                queue.add(event.getNewNick());
            }
        });
    }

    /**
     * Processes the op queue for a particular channel.
     *
     * @param channel the channel it occurred on
     * @param event the join event that triggered the auto-op action
     */
    private synchronized void processQueue(final Channel channel, final JoinEvent event) {
        MortyBot bot = event.getBot();
        BotProperties props = BotProperties.getBotProperties();
        int maxModes = props.getIntProperty("aop.max.modes", -1);
        if (maxModes == -1) {
            int sInfoMaxModes = bot.getServerInfo().getMaxModes();
            maxModes = sInfoMaxModes == -1 ? BotDefaults.AUTO_OP_MAX_MODES : sInfoMaxModes;
        }

        String channelName = channel.getName().toLowerCase();

        if (pending.containsKey(channelName)) {
            Queue<String> queue = pending.get(channelName);

            log.info("Attempting to op {} users on {}", queue.size(), channelName);

            while (!queue.isEmpty()) {
                StringBuilder modes = new StringBuilder();
                List<String> targets = new ArrayList<>();

                while (targets.size() < maxModes && !queue.isEmpty()) {
                    String nick = queue.remove();
                    if (channel.isOp(bot.getUserChannelDao().getUser(nick))) {
                        log.debug("{} already has operator status on {}", nick, channelName);
                    } else {
                        modes.append("o");
                        targets.add(nick);
                    }
                }

                if (!channel.isOp(bot.getUserBot())) {
                    log.debug("Bot is not an operator on {}", channelName);
                } else if (targets.isEmpty()) {
                    log.debug("No targets to op!");
                } else {
                    bot.sendIRC().mode(channelName, "+" + modes + " " + String.join(" ", targets));
                }
            }

            pending.remove(channelName);
        }
    }
}
