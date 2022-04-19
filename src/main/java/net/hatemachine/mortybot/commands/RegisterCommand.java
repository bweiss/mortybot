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
import net.hatemachine.mortybot.BotUser;
import net.hatemachine.mortybot.BotUserDao;
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.exception.BotUserException;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.util.IrcUtils;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Registers yourself with the bot.
 * If there are no existing bot users this will grant admin rights.
 * If no hostmask is specified it will generate one for you via the <code>IrcUtils.maskAddress()</code> method.
 */
public class RegisterCommand implements BotCommand {

    private static final Logger log = LoggerFactory.getLogger(RegisterCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public RegisterCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        MortyBot bot = event.getBot();
        User user = event.getUser();
        BotUserDao dao = bot.getBotUserDao();
        String userName = "";
        String userHostmask = "";
        String userFlags = BotDefaults.REGISTER_BOT_USER_FLAGS;

        if (bot.getBotUserDao().getAll().isEmpty()) {
            try {
                if (args.isEmpty()) {
                    userName = Validate.botUserName(user.getNick());
                } else {
                    userName = Validate.botUserName(args.get(0));
                }

                if (args.size() < 2) {
                    userHostmask = IrcUtils.maskAddress(user.getHostmask(), BotDefaults.REGISTER_MASK_TYPE);
                } else {
                    userHostmask = Validate.hostmask(args.get(1));
                }

                dao.add(new BotUser(Validate.botUserName(userName), Validate.hostmask(userHostmask), userFlags));
                event.respondWith(String.format("Registered %s with hostmask %s and flags %s",
                        userName, userHostmask, userFlags));

            } catch (BotUserException e) {
                log.error("There was an error trying to add {} ({}) to the bot: {} {}",
                        userName, userHostmask, e.getReason(), e.getMessage());
                event.respondWith("Error registering user: " + e.getMessage());

            } catch (IllegalArgumentException e) {
                log.warn("Illegal argument: {}", e.getMessage());
                event.respondWith(e.getMessage());
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
