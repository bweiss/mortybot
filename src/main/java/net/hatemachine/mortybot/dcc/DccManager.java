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
package net.hatemachine.mortybot.dcc;

import net.hatemachine.mortybot.custom.entity.BotUserFlag;
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.events.DccChatMessageEvent;
import net.hatemachine.mortybot.util.BotUserHelper;
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

/**
 * DCC manager class that keeps track of sessions and handles dispatching messages to the party line.
 */
public class DccManager {

    private static final Logger log = LoggerFactory.getLogger(DccManager.class);

    private static DccManager manager;

    private final Map<User, ChatSession> userChatMap;

    private DccManager() {
        this.userChatMap = new HashMap<>();
    }

    /**
     * Retrieves a singleton instance of our DccManager object.
     *
     * @return a dcc manager object
     */
    public static DccManager getManager() {
        if (manager == null) {
            manager = new DccManager();
        }
        return manager;
    }

    /**
     * Retrieves a chat session for a user if one exists.
     *
     * @param user the user to retrieve a session for
     * @return an optional containing a chat session if one exists for that user
     */
    public Optional<ChatSession> getChatSession(User user) {
        Optional<ChatSession> chatSession = Optional.empty();
        if (userChatMap.containsKey(user)) {
            chatSession = Optional.of(userChatMap.get(user));
        }
        return chatSession;
    }

    /**
     * Add a chat session for a particular user to our manager.
     *
     * @param user the user that the chat session is with
     * @param chatSession the chat session object for this user
     */
    public synchronized void addChatSession(User user, ChatSession chatSession) {
        userChatMap.put(user, chatSession);
    }

    /**
     * Removes a chat session from our manager.
     *
     * @param user the user that owns the chat session
     */
    public synchronized void removeChatSession(User user) {
        userChatMap.remove(user);
    }

    /**
     * Retrieves a list of all our chat sessions.
     *
     * @return a list containing all of our chat session objects
     */
    public synchronized List<ChatSession> getChatSessions() {
        return new ArrayList<>(userChatMap.values());
    }

    /**
     * Retrieves a list of only our active chat sessions.
     *
     * @return a list containing chat session objects of our active sessions
     */
    public synchronized List<ChatSession> getActiveChatSessions() {
        return getChatSessions().stream()
                .filter(ChatSession::isActive)
                .toList();
    }

    /**
     * Starts a new chat session with a user.
     *
     * @param user the user to start the chat with
     */
    public void startDccChat(User user) {
        ChatSession chatSession = new ChatSession(ChatSession.SessionType.SEND, user);
        addChatSession(user, chatSession);
        chatSession.start();
    }

    /**
     * Accepts a new chat session from a user.
     *
     * @param event the {incoming chat request event
     */
    public void acceptDccChat(final IncomingChatRequestEvent event) {
        ChatSession chatSession = new ChatSession(ChatSession.SessionType.RECEIVE, event);
        addChatSession(event.getUser(), chatSession);
        chatSession.start();
    }

    /**
     * Handles a message from a chat session and dispatch it as an event.
     *
     * @param chatSession the chat session object that's initiating the event
     * @param line the message text
     */
    public void handleChatMessage(ChatSession chatSession, String line) {
        Chat chat = chatSession.getChat();
        User user = chat.getUser();
        MortyBot bot = user.getBot();
        Utils.dispatchEvent(bot, new DccChatMessageEvent(bot, chat, user, line));
    }

    /**
     * Dispatches a message to users on the party line.
     *
     * @param message the message text
     */
    public void dispatchMessage(String message) {
        dispatchMessage(message, false);
    }

    /**
     * Dispatches a message to users on the party line.
     *
     * @param message the message text
     * @param adminOnly if true, message will only be dispatched to admin users
     */
    public void dispatchMessage(String message, boolean adminOnly) {
        log.debug("Dispatching message to all party line members: \"{}\"", message);

        for (ChatSession cs : manager.getActiveChatSessions()) {
            Chat chat = cs.getChat();
            User user = chat.getUser();

            // if adminOnly flag is set, skip over users that do not have the ADMIN flag
            if (adminOnly) {
                boolean adminFlag = false;
                List<BotUser> botUsers = BotUserHelper.findByHostmask(user.getHostmask());

                if (!botUsers.isEmpty()) {
                    adminFlag = botUsers.stream().anyMatch(u -> u.getBotUserFlags().contains(BotUserFlag.ADMIN));
                }

                if (!adminFlag) {
                    continue;
                }
            }

            try {
                chat.sendLine(message);
            } catch (IOException e) {
                log.error("Failed to dispatch message to chat with {}", user.getNick(), e);
            }
        }
    }
}
