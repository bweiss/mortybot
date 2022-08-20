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
import net.hatemachine.mortybot.dao.ManagedChannelDao;
import net.hatemachine.mortybot.model.ManagedChannel;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.ServerPingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class ManagedChannelListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ManagedChannelListener.class);

    /**
     * Automatically join any channels with the auto-join flag on connect.
     *
     * @param event the connection event
     */
    @Override
    public void onConnect(ConnectEvent event) {
        joinAllChannels(event.getBot());
    }

    /**
     * Rejoin a channel if the bot is kicked from a managed channel that has the auto-join flag.
     *
     * @param event the kick event
     */
    @Override
    public void onKick(final KickEvent event) {
        MortyBot bot = event.getBot();
        User recipient = event.getRecipient();
        ManagedChannelDao mcDao = bot.getManagedChannelDao();
        Optional<ManagedChannel> optManagedChannel = mcDao.getByName(event.getChannel().getName());

        if (optManagedChannel.isPresent() && recipient != null && recipient.getNick().equals(bot.getNick())) {
            ManagedChannel managedChannel = optManagedChannel.get();

            if (managedChannel.getAutoJoinFlag() == 1) {
                log.info("Bot was kicked from {}, re-joining...", event.getChannel().getName());

                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        bot.sendIRC().joinChannel(event.getChannel().getName());
                    } catch (InterruptedException e) {
                        log.warn("Thread interrupted", e);
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        }
    }

    /**
     * Periodically checks to make sure we're joined to our auto-join channels.
     *
     * @param event the server ping event
     */
    @Override
    public void onServerPing(final ServerPingEvent event) {
        joinAllChannels(event.getBot());
    }

    /**
     * Retrieve a list of channels that have the auto-join flag.
     *
     * @param bot the bot object
     * @return a {@link List} of channel names
     */
    private List<String> getAutoJoinChannels(MortyBot bot) {
        ManagedChannelDao mcDao = bot.getManagedChannelDao();
        return mcDao.getAll().stream()
                .filter(c -> c.getAutoJoinFlag() == 1)
                .map(ManagedChannel::getName)
                .toList();
    }

    /**
     * Join all of our auto-join channels.
     *
     * @param bot the bot object
     */
    private void joinAllChannels(MortyBot bot) {
        for (String chan : getAutoJoinChannels(bot)) {
            if (!bot.getUserChannelDao().containsChannel(chan)) {
                log.info("Auto-joining channel {}", chan);
                bot.sendIRC().joinChannel(chan);
            }
        }
    }
}
