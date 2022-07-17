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
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.dcc.DccManager;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CHAT command
 * Instructs the bot to initiate a DCC CHAT session with the calling user.
 */
public class ChatCommand implements BotCommand {

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
                List<BotUser> botUsers = bot.getBotUserDao().getAll(user.getHostmask());
                boolean dccFlag = botUsers.stream().anyMatch(u -> u.hasFlag("DCC"));

                if (dccFlag) {
                    BotUser botUser = botUsers.get(0);
                    log.info("Sending DCC CHAT request to {} (bot_user: {})", user.getHostmask(), botUser);
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
