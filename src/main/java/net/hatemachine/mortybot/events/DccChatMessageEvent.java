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
package net.hatemachine.mortybot.events;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.NotImplementedException;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.UserHostmask;
import org.pircbotx.dcc.Chat;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * Used whenever a message is sent to a DCC CHAT session.
 */
public class DccChatMessageEvent extends Event implements GenericMessageEvent {

    private static final Logger log = LoggerFactory.getLogger(DccChatMessageEvent.class);

    protected final Chat chat;
    protected final User user;
    protected final String message;

    public DccChatMessageEvent(PircBotX bot, Chat chat, User user, String message) {
        super(bot);
        this.chat = chat;
        this.user = user;
        this.message = message;
    }

    /**
     * Respond with a message over DCC in <code>user: message</code> format.
     *
     * @param response the response to send
     */
    @Override
    public void respond(String response) {
        respondWith(getUser().getNick() + ": " + response);
    }

    /**
     * Respond with a message over DCC.
     *
     * @param fullLine the line to send in response
     */
    @Override
    public void respondWith(String fullLine) {
        try {
            chat.sendLine(fullLine);
        } catch (IOException e) {
            log.error("Exception encountered responding to chat message", e);
        }
    }

    /**
     * Respond to the user directly over private message.
     *
     * @param response the response to send
     */
    @Override
    public void respondPrivateMessage(String response) {
        getUser().send().message(response);
    }

    public ImmutableMap<String, String> getV3Tags() {
        throw new NotImplementedException("getV3Tags not implemented in this class");
    }

    public Chat getChat() {
        return chat;
    }

    @Override
    public UserHostmask getUserHostmask() {
        throw new NotImplementedException("getUserHostmask() not implemented in this class");
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DccChatMessageEvent that = (DccChatMessageEvent) o;
        return chat.equals(that.chat) && user.equals(that.user) && message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), chat, user, message);
    }
}
