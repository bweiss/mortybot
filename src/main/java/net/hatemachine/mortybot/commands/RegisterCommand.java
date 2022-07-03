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
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.model.BotUser;
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
import java.util.stream.Collectors;

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
        BotProperties props = BotProperties.getBotProperties();
        String userName = args.isEmpty() ? Validate.botUserName(user.getNick()) : Validate.botUserName(args.get(0));
        String maskedAddress = IrcUtils.maskAddress(user.getHostmask(), props.getIntProperty("register.mask.type", BotDefaults.REGISTER_MASK_TYPE));
        String normalFlags = props.getStringProperty("register.normal.flags", BotDefaults.REGISTER_NORMAL_FLAGS);
        String ownerFlags = props.getStringProperty("register.owner.flags", BotDefaults.REGISTER_OWNER_FLAGS);
        String userFlags = dao.count() > 0 ? normalFlags : ownerFlags;
        List<BotUser> matchingBotUsers = dao.getAll(user.getHostmask());

        // first check to see if the user matches a hostmask for an existing bot user
        if (!matchingBotUsers.isEmpty()) {
            String usernames = matchingBotUsers.stream()
                    .map(BotUser::getUsername)
                    .collect(Collectors.joining(", "));

            log.warn("Hostmask {} matches existing bot user(s): {}", user.getHostmask(), usernames);
            event.respondWith("You are already registered!");
            return;
        }

        // if no matches found, attempt to register them
        try {
            dao.add(new BotUser(Validate.botUserName(userName), Validate.hostmask(maskedAddress), Validate.botUserFlags(userFlags)));
            event.respondWith(String.format("Registered %s with hostmask %s and flags [%s]",
                    userName, maskedAddress, userFlags));

        } catch (BotUserException e) {
            log.error("Failed to add bot user: {} {} {} {}",
                    userName, maskedAddress, e.getReason(), e.getMessage());

            if (e.getReason() == BotUserException.Reason.USER_EXISTS) {
                event.respondWith("A user with that name already exists, try another");
            } else {
                event.respondWith("Error registering user");
            }

        } catch (IllegalArgumentException e) {
            log.warn("Invalid argument: {}", e.getMessage());
            event.respondWith(e.getMessage());
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
