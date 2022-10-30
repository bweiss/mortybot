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
package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.Command;
import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.dcc.ChatSession;
import net.hatemachine.mortybot.dcc.DccManager;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.User;
import org.pircbotx.dcc.Chat;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static net.hatemachine.mortybot.listeners.CommandListener.CommandSource.DCC;

@BotCommand(name="WHO", clazz= WhoCommand.class, help={
        "Displays all users connected to the party line (DCC chat only)",
        "Usage: WHO"
})
public class WhoCommand implements Command {

    private static final Logger log = LoggerFactory.getLogger(WhoCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public WhoCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        if (source != DCC) {
            event.respondWith("This command is only enabled over DCC chat");
        } else {
            DccManager dccManager = DccManager.getManager();
            List<User> partyLineUsers = dccManager.getActiveChatSessions()
                    .stream()
                    .map(ChatSession::getChat)
                    .map(Chat::getUser)
                    .toList();

            if (partyLineUsers.isEmpty()) {
                String errMessage = "WHO command with no party line users! This should never happen. D'oh!";
                log.error(errMessage);
                throw new RuntimeException(errMessage);
            } else {
                event.respondWith(String.format("There %s %d user%s on the party line: %s",
                        partyLineUsers.size() > 1 ? "are" : "is",
                        partyLineUsers.size(),
                        partyLineUsers.size() > 1 ? "s" : "",
                        partyLineUsers.stream().map(User::getNick).collect(Collectors.joining(", "))));
            }
        }
    }

    @Override
    public GenericMessageEvent getEvent() {
        return event;
    }

    @Override
    public CommandListener.CommandSource getSource() {
        return source;
    }

    @Override
    public List<String> getArgs() {
        return args;
    }
}
