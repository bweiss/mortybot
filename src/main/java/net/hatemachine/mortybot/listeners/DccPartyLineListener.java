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

import net.hatemachine.mortybot.ExtendedListenerAdapter;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.dcc.DccManager;
import net.hatemachine.mortybot.events.DccChatMessageEvent;
import org.pircbotx.User;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.managers.ListenerManager;

import java.util.Optional;

/**
 * Responsible for dispatching chat messages on the party line.
 * Also shows admin users all commands and private messages sent to the bot.
 */
public class DccPartyLineListener extends ExtendedListenerAdapter {

    private static final DccManager dccManager = DccManager.getManager();

    /**
     * Handles DCC CHAT events from users on the party line and dispatches them to other members.
     * Bot commands are ignored as they are handled by the {@link CommandListener} class
     *
     * @param event the dcc chat message event
     */
    @Override
    public void onDccChatMessage(final DccChatMessageEvent event) {
        MortyBot bot = event.getBot();
        ListenerManager listenerManager = bot.getConfiguration().getListenerManager();
        Optional<Listener> commandListener = listenerManager.getListeners()
                .stream()
                .filter(CommandListener.class::isInstance)
                .findFirst();

        if (commandListener.isPresent()) {
            String commandPrefix = ((CommandListener) commandListener.get()).getCommandPrefix();
            if (event.getMessage().startsWith(commandPrefix)) {
                // ignore commands
                return;
            }
        }

        dccManager.dispatchMessage(String.format("<%s> %s", event.getUser().getNick(), event.getMessage()));
    }

    /**
     * Notifies admin users of server connection events.
     *
     * @param event the connect event
     */
    @Override
    public void onConnect(ConnectEvent event) {
        MortyBot bot = event.getBot();
        String hostname = bot.getServerHostname();
        Integer port = bot.getServerPort();
        dccManager.dispatchMessage(String.format("*** Connected to %s:%d", hostname, port), true);
    }

    /**
     * Notifies admin users of server disconnection events.
     *
     * @param event the disconnect event
     */
    @Override
    public void onDisconnect(DisconnectEvent event) {
        dccManager.dispatchMessage(String.format("*** Disconnected from %s",
                event.getBot().getServerHostname()
        ), true);
    }

    /**
     * Notifies admin users if the bot gets kicked from a channel.
     *
     * @param event the kick event
     */
    @Override
    public void onKick(final KickEvent event) {
        MortyBot bot = event.getBot();
        User user = event.getUser();
        User recipient = event.getRecipient();

        if (user != null && recipient != null && recipient.getNick().equals(bot.getNick())) {
            dccManager.dispatchMessage(String.format("*** %s has been kicked from %s by %s (%s)",
                    bot.getNick(),
                    event.getChannel().getName(),
                    user.getNick(),
                    event.getReason()
            ), true);
        }
    }

    /**
     * Handles private messages sent to the bot and dispatches them to admin users that are on the party line.
     *
     * @param event the dcc chat message event
     */
    @Override
    public void onPrivateMessage(final PrivateMessageEvent event) {
        User user = event.getUser();

        if (user != null) {
            dccManager.dispatchMessage(String.format("*** PRIVMSG from %s!%s@%s: %s",
                    user.getNick(), user.getIdent(), user.getHostname(), event.getMessage()
            ), true);
        }
    }
}
