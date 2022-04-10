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
package net.hatemachine.mortybot.dcc;

import net.hatemachine.mortybot.BotUser;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.events.DccChatMessageEvent;
import org.pircbotx.User;
import org.pircbotx.Utils;
import org.pircbotx.dcc.Chat;
import org.pircbotx.hooks.events.IncomingChatRequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DccManager {

    private static final Logger log = LoggerFactory.getLogger(DccManager.class);

    private static DccManager manager;

    private final Map<User, ChatSession> userChatMap;

    private DccManager() {
        this.userChatMap = new HashMap<>();
    }

    public static DccManager getManager() {
        if (manager == null) {
            manager = new DccManager();
        }
        return manager;
    }

    public Optional<ChatSession> getChatSession(User user) {
        Optional<ChatSession> chatSession = Optional.empty();
        if (userChatMap.containsKey(user)) {
            chatSession = Optional.of(userChatMap.get(user));
        }
        return chatSession;
    }

    public synchronized void addChatSession(User user, ChatSession chatSession) {
        userChatMap.put(user, chatSession);
    }

    public synchronized void removeChatSession(User user) {
        userChatMap.remove(user);
    }

    public synchronized List<ChatSession> getChatSessions() {
        return new ArrayList<>(userChatMap.values());
    }

    public synchronized List<ChatSession> getActiveChatSessions() {
        return getChatSessions().stream()
                .filter(ChatSession::isActive)
                .toList();
    }

    public void startDccChat(User user) {
        ChatSession chatSession = new ChatSession(ChatSession.SessionType.SEND, user);
        addChatSession(user, chatSession);
        chatSession.start();
    }

    public void acceptDccChat(final IncomingChatRequestEvent event) {
        ChatSession chatSession = new ChatSession(ChatSession.SessionType.RECEIVE, event);
        addChatSession(event.getUser(), chatSession);
        chatSession.start();
    }

    public void handleChatMessage(ChatSession chatSession, String line) {
        Chat chat = chatSession.getChat();
        User user = chat.getUser();
        MortyBot bot = user.getBot();
        Utils.dispatchEvent(bot, new DccChatMessageEvent(bot, chat, user, line));
    }

    public void dispatchMessage(String message) {
        dispatchMessage(message, false);
    }

    public void dispatchMessage(String message, boolean adminOnly) {
        log.debug("Dispatching message to all party line members: \"{}\"", message);

        for (ChatSession cs : manager.getActiveChatSessions()) {
            Chat chat = cs.getChat();
            User user = chat.getUser();

            if (adminOnly) {
                MortyBot bot = user.getBot();
                List<BotUser> matchedBotUsers = bot.getBotUserDao().getAll(user.getHostmask(), BotUser.Flag.ADMIN);
                if (matchedBotUsers.isEmpty()) {
                    continue;
                }
            }

            try {
                chat.sendLine(message);
            } catch (IOException e) {
                log.error("Exception encountered dispatching message to party line", e);
            }
        }
    }

    public Map<User, ChatSession> getUserChatMap() {
        return userChatMap;
    }
}
