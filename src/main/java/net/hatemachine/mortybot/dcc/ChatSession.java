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

import org.pircbotx.User;
import org.pircbotx.dcc.Chat;
import org.pircbotx.exception.DccException;
import org.pircbotx.hooks.events.IncomingChatRequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static net.hatemachine.mortybot.dcc.ChatSession.SessionState.*;
import static net.hatemachine.mortybot.dcc.ChatSession.SessionType.RECEIVE;
import static net.hatemachine.mortybot.dcc.ChatSession.SessionType.SEND;

/**
 * Wraps our Chat objects and ensures that each chat session runs in its own thread.
 */
public class ChatSession extends Thread {

    private static final Logger log = LoggerFactory.getLogger(ChatSession.class);

    private Chat chat;
    private SessionState sessionState;
    private final SessionType sessionType;
    private final Object targetObj;

    public enum SessionState {
        WAITING,
        CONNECTED,
        CLOSED
    }

    public enum SessionType {
        SEND,
        RECEIVE
    }

    public ChatSession(SessionType sessionType, Object targetObj) {
        this.sessionType = sessionType;
        this.targetObj = targetObj;
        this.sessionState = WAITING;
    }

    @Override
    public void run() {
        DccManager dccManager = DccManager.getManager();

        try {
            if (sessionType == SEND) {
                User user = (User) targetObj;
                chat = user.send().dccChat(); // blocking
            } else if (sessionType == RECEIVE) {
                IncomingChatRequestEvent event = (IncomingChatRequestEvent) targetObj;
                chat = event.accept(); // blocking
            }

            sessionState = CONNECTED;
            log.info("DCC CHAT session established with {}", chat.getUser().getHostmask());
            dccManager.dispatchMessage(String.format("*** %s has joined the party line", chat.getUser().getNick()));
            chat.sendLine("Welcome to the party line! (type '.exit' to leave)");

            String line;
            while (!chat.isFinished() && (line = chat.readLine()) != null) {
                if (line.startsWith(".exit")) {
                    chat.sendLine("Bye!");
                    chat.close();
                    log.info("DCC CHAT with {} closed", chat.getUser().getNick());
                    dccManager.removeChatSession(chat.getUser());
                    dccManager.dispatchMessage(String.format("*** %s has left the party line", chat.getUser().getNick()));
                } else {
                    dccManager.handleChatMessage(this, line);
                }
            }

        } catch (SocketTimeoutException e) {
            log.info("Connection timed out");
        } catch (IOException e) {
            log.error("DCC CHAT failed", e);
        } catch (InterruptedException e) {
            log.warn("Thread interrupted!", e);
            Thread.currentThread().interrupt();
        } catch (DccException e) {
            log.error("DCC exception encountered", e);
        }

        sessionState = CLOSED;
    }

    public Chat getChat() {
        return chat;
    }

    public SessionState getSessionState() {
        return sessionState;
    }

    public boolean isActive() {
        return sessionState == CONNECTED;
    }
}
