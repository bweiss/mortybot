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
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.custom.entity.BotUserFlag;
import net.hatemachine.mortybot.dao.BotUserDao;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.helpers.BotUserHelper;
import net.hatemachine.mortybot.util.IrcUtils;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * Implements the REGISTER command, allowing users to register themselves with the bot.
 * If there are no existing bot users this will grant admin rights.
 * If no hostmask is specified it will generate one for you via the <code>IrcUtils.maskAddress()</code> method.<br/>
 * Note: There are times when the bot cannot adequately determine a user's hostname.
 */
@BotCommand(name = "REGISTER", help = {
        "Registers yourself with the bot using your current hostname",
        "Usage: REGISTER [name]"
})
public class RegisterCommand implements Command {

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
        String flagStr = botUserDao.count() > 0 ? normalFlags : ownerFlags;
        BotUserHelper botUserHelper = new BotUserHelper();
        List<BotUserFlag> flagList = botUserHelper.parseFlags(flagStr);
        List<BotUser> matchingBotUsers = botUserHelper.findByHostmask(user.getHostmask());

        if (!matchingBotUsers.isEmpty()) {
            String userNames = matchingBotUsers.stream()
                    .map(BotUser::getName)
                    .collect(Collectors.joining(", "));

            log.info("Hostmask {} matches existing bot user(s): {}", user.getHostmask(), userNames);
            event.respondWith("You are already registered!");
        } else {
            BotUser botUser = new BotUser();
            botUser.setName(userName);
            botUser.setBotUserHostmasks(List.of(maskedAddress));
            botUser.setBotUserFlags(flagList);
            botUser = botUserDao.create(botUser);

            String msg = String.format("Registered %s with hostmask [%s] and flags [%s]",
                    botUser.getName(),
                    botUser.getBotUserHostmasks() == null ? "" : String.join(", ", botUser.getBotUserHostmasks()),
                    botUser.getBotUserFlags() == null ? "" : botUser.getBotUserFlags().stream().map(Enum::name).collect(joining(", ")));

            log.info(msg);
            event.respondWith(msg);
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
