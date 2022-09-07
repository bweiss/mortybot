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

import net.hatemachine.mortybot.custom.entity.BotUserFlag;
import net.hatemachine.mortybot.dao.BotUserDao;
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.dcc.DccManager;
import net.hatemachine.mortybot.util.BotUserHelper;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.IncomingChatRequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Listens for DCC CHAT requests from users.
 */
public class DccRequestListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(DccRequestListener.class);

    /**
     * Handle incoming DCC CHAT requests from users. The dcc.chat.enabled property must be TRUE and the user must
     * be a registered bot user with the DCC flag or the request will be rejected.
     *
     * @param event the {@link IncomingChatRequestEvent} object containing our event data
     */
    @Override
    public void onIncomingChatRequest(IncomingChatRequestEvent event) {
        MortyBot bot = event.getBot();
        BotProperties botProperties = BotProperties.getBotProperties();
        boolean dccEnabled = botProperties.getBooleanProperty("dcc.chat.enabled", BotDefaults.DCC_CHAT_ENABLED);
        User user = (User) Validate.notNull(event.getUser());
        List<BotUser> matchedBotUsers = BotUserHelper.findByHostmask(user.getHostmask());
        boolean dccFlag = matchedBotUsers.stream().anyMatch(u -> u.getBotUserFlags().contains(BotUserFlag.DCC));

        if (dccEnabled && dccFlag) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.warn("Thread interrupted", e);
                Thread.currentThread().interrupt();
            }

            log.info("Accepting DCC CHAT request from {}", user.getHostmask());
            DccManager.getManager().acceptDccChat(event);
        } else {
            log.info("Rejecting DCC CHAT request from {}", user.getHostmask());
            user.send().ctcpResponse("DCC REJECT CHAT chat");
        }
    }
}
