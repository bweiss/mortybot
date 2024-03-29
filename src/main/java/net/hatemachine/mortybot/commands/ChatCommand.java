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

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.Command;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.dcc.DccManager;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.repositories.BotUserRepository;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Implements the CHAT command. This instructs the bot to initiate a DCC CHAT session with a user.
 */
@BotCommand(name = "CHAT", help = {
        "Tells the bot to initiate a DCC chat request with you or [nick] if specified",
        "Usage: CHAT [nick]"
})
public class ChatCommand implements Command {

    private static final Logger log = LoggerFactory.getLogger(ChatCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public ChatCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        MortyBot bot = event.getBot();
        BotProperties botProperties = BotProperties.getBotProperties();
        boolean dccEnabled = botProperties.getBooleanProperty("dcc.chat.enabled", BotDefaults.DCC_CHAT_ENABLED);

        if (dccEnabled) {
            User user;
            if (args.isEmpty()) {
                user = event.getUser();
            } else {
                user = bot.getUserChannelDao().getUser(args.get(0));
            }

            if (user != null) {
                var botUserRepository = new BotUserRepository();
                Optional<BotUser> optionalBotUser = botUserRepository.findByHostmask(user.getHostmask());

                if (optionalBotUser.isPresent() && optionalBotUser.get().hasDccFlag()) {
                    log.info("Sending DCC CHAT request to {}", user.getHostmask());
                    DccManager mgr = DccManager.getManager();
                    mgr.startDccChat(user);
                }
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
