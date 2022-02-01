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

import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.config.BotState;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.ServerPingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        String[] channels = BotState.getBotState().getStringProperty("autoJoinChannels").split(" ");
        for (String chan : channels) {
            if (!bot.getUserChannelDao().containsChannel(chan)) {
                log.info("Auto-joining channel {}", chan);
                bot.sendIRC().joinChannel(chan);
            }
        }
    }
}
