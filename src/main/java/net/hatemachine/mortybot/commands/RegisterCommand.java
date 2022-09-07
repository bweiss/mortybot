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
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.custom.entity.BotUserFlag;
import net.hatemachine.mortybot.dao.BotUserDao;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.util.BotUserHelper;
import net.hatemachine.mortybot.util.IrcUtils;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Registers yourself with the bot.
 * If there are no existing bot users this will grant admin rights.
 * If no hostmask is specified it will generate one for you via the <code>IrcUtils.maskAddress()</code> method.
 *
 * @see IrcUtils
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
        User user = event.getUser();
        BotUserDao botUserDao = new BotUserDao();
        BotProperties props = BotProperties.getBotProperties();
        String userName = args.isEmpty() ? Validate.botUserName(user.getNick()) : Validate.botUserName(args.get(0));
        String maskedAddress = IrcUtils.maskAddress(Validate.hostmask(user.getHostmask()),
                props.getIntProperty("register.mask.type", BotDefaults.REGISTER_MASK_TYPE));
        String normalFlags = props.getStringProperty("register.normal.flags", BotDefaults.REGISTER_NORMAL_FLAGS);
        String ownerFlags = props.getStringProperty("register.owner.flags", BotDefaults.REGISTER_OWNER_FLAGS);
        String userFlags = botUserDao.count() > 0 ? normalFlags : ownerFlags;
        List<BotUserFlag> flagList = new ArrayList<>();
        List<BotUser> matchingBotUsers = BotUserHelper.findByHostmask(user.getHostmask());

        for (String s : userFlags.split(",")) {
            flagList.add(Enum.valueOf(BotUserFlag.class, s));
        }

        // first check to see if the user matches a hostmask for an existing bot user
        if (!matchingBotUsers.isEmpty()) {
            String userNames = matchingBotUsers.stream()
                    .map(BotUser::getName)
                    .collect(Collectors.joining(", "));

            log.warn("Hostmask {} matches existing bot user(s): {}", user.getHostmask(), userNames);
            event.respondWith("You are already registered!");
            return;
        } else {
            // if no matches found, attempt to register them
            BotUser botUser = new BotUser();
            botUser.setName(userName);
            botUser.setBotUserHostmasks(List.of(maskedAddress));
            botUser.setBotUserFlags(flagList);
            botUserDao.create(botUser);

            event.respondWith(String.format("Registered %s with hostmask %s and flags [%s]",
                    userName, maskedAddress, flagList.stream().map(BotUserFlag::name).collect(Collectors.joining(","))));
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
