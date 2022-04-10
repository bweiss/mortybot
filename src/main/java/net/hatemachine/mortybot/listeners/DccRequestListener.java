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
import net.hatemachine.mortybot.dcc.DccManager;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.IncomingChatRequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Listens for DCC CHAT requests from users.
 *
 * The dcc.chat.enabled property must be TRUE and the user must have the DCC BotUser flag or the CHAT request will be rejected.
 */
public class DccRequestListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(DccRequestListener.class);

    @Override
    public void onIncomingChatRequest(IncomingChatRequestEvent event) throws InterruptedException {
        MortyBot bot = event.getBot();
        BotState botState = BotState.getBotState();
        boolean dccEnabled = botState.getBooleanProperty("dcc.chat.enabled", BotDefaults.DCC_CHAT_ENABLED);
        User user = (User) Validate.notNull(event.getUser());
        List<BotUser> matchedBotUsers = bot.getBotUserDao().getAll(user.getHostmask(), BotUser.Flag.DCC);
        Optional<BotUser> botUser = matchedBotUsers.isEmpty() ? Optional.empty() : Optional.of(matchedBotUsers.get(0));

        if (dccEnabled && botUser.isPresent()) {
            Thread.sleep(1000);
            log.info("Accepting DCC CHAT request from {}", user.getHostmask());
            DccManager mgr = DccManager.getManager();
            mgr.acceptDccChat(event);
        } else {
            log.info("Rejecting DCC CHAT request from {}", user.getHostmask());
            user.send().ctcpResponse("DCC REJECT CHAT chat");
        }
    }
}
