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
import net.hatemachine.mortybot.custom.entity.ManagedChannelFlag;
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

import static net.hatemachine.mortybot.util.ManagedChannelHelper.getAutoJoinChannels;

public class ManagedChannelListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ManagedChannelListener.class);

    /**
     * Automatically join any channels with the auto-join flag on connect.
     *
     * @param event the connection event
     */
    @Override
    public void onConnect(ConnectEvent event) {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                joinAllAutoJoinChannels(event.getBot());
            } catch (InterruptedException e) {
                log.warn("Thread interrupted", e);
                Thread.currentThread().interrupt();
            }
        }).start();
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
        ManagedChannelDao managedChannelDao = new ManagedChannelDao();
        List<ManagedChannel> managedChannels = managedChannelDao.getWithName(event.getChannel().getName());

        if (!managedChannels.isEmpty() && recipient != null && recipient.getNick().equals(bot.getNick())) {
            ManagedChannel managedChannel = managedChannelDao.get(0);

            if (managedChannel.getManagedChannelFlags().contains(ManagedChannelFlag.AUTO_JOIN)) {
                log.info("Bot was kicked from {}, re-joining...", event.getChannel().getName());

                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        bot.sendIRC().joinChannel(managedChannel.getName());
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
        joinAllAutoJoinChannels(event.getBot());
    }

    /**
     * Join all of our auto-join channels.
     *
     * @param bot the bot object
     */
    private void joinAllAutoJoinChannels(MortyBot bot) {
        for (ManagedChannel chan : getAutoJoinChannels()) {
            if (!bot.getUserChannelDao().containsChannel(chan.getName())) {
                log.info("Auto-joining channel {}", chan.getName());
                bot.sendIRC().joinChannel(chan.getName());
            }
        }
    }
}
