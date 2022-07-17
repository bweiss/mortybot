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

import net.hatemachine.mortybot.model.BotUser;
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

/**
 * DCC manager class.
 * Right now this is just responsible for keeping track of DCC CHAT sessions and dispatching messages to the party line.
 */
public class DccManager {

    private static final Logger log = LoggerFactory.getLogger(DccManager.class);

    private static DccManager manager;

    private final Map<User, ChatSession> userChatMap;

    private DccManager() {
        this.userChatMap = new HashMap<>();
    }

    /**
     * Retrieve a singleton instance of our DccManager object.
     *
     * @return a {@link DccManager} object
     */
    public static DccManager getManager() {
        if (manager == null) {
            manager = new DccManager();
        }
        return manager;
    }

    /**
     * Retrieve a chat session for a user if one exists.
     *
     * @param user the {@link User} to retrieve a session for
     * @return an {@link Optional} containing a {@link ChatSession} if one exists for that user
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
     * @param user the {@link User} that the chat session is with
     * @param chatSession the {@link ChatSession} object for this user
     */
    public synchronized void addChatSession(User user, ChatSession chatSession) {
        userChatMap.put(user, chatSession);
    }

    /**
     * Remove a chat session from our manager.
     *
     * @param user the {@link User} that owns the chat session
     */
    public synchronized void removeChatSession(User user) {
        userChatMap.remove(user);
    }

    /**
     * Retrieve a list of all our chat sessions.
     *
     * @return a {@link List} containing all of our {@link ChatSession} objects
     */
    public synchronized List<ChatSession> getChatSessions() {
        return new ArrayList<>(userChatMap.values());
    }

    /**
     * Retrieve a list of only our active chat sessions.
     *
     * @return a {@link List} containing {@link ChatSession} objects of our active sessions
     */
    public synchronized List<ChatSession> getActiveChatSessions() {
        return getChatSessions().stream()
                .filter(ChatSession::isActive)
                .toList();
    }

    /**
     * Start a new chat session with a user.
     *
     * @param user the {@link User} to start the chat with
     */
    public void startDccChat(User user) {
        ChatSession chatSession = new ChatSession(ChatSession.SessionType.SEND, user);
        addChatSession(user, chatSession);
        chatSession.start();
    }

    /**
     * Accept a new chat session from a user.
     *
     * @param event the {@link IncomingChatRequestEvent} event representing the incoming chat request
     */
    public void acceptDccChat(final IncomingChatRequestEvent event) {
        ChatSession chatSession = new ChatSession(ChatSession.SessionType.RECEIVE, event);
        addChatSession(event.getUser(), chatSession);
        chatSession.start();
    }

    /**
     * Handle a message from a chat session and dispatch it as an event.
     *
     * @param chatSession the {@link ChatSession} object that's initiating the event
     * @param line a {@link String} containing the message text
     */
    public void handleChatMessage(ChatSession chatSession, String line) {
        Chat chat = chatSession.getChat();
        User user = chat.getUser();
        MortyBot bot = user.getBot();
        Utils.dispatchEvent(bot, new DccChatMessageEvent(bot, chat, user, line));
    }

    /**
     * Dispatch a message to users on the party line.
     *
     * @param message the message text
     */
    public void dispatchMessage(String message) {
        dispatchMessage(message, false);
    }

    /**
     * Dispatch a message to users on the party line.
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
                MortyBot bot = user.getBot();
                List<BotUser> botUsers = bot.getBotUserDao().getAll(user.getHostmask());
                boolean adminFlag = botUsers.stream().anyMatch(u -> u.hasFlag("ADMIN"));
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
