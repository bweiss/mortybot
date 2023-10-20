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
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.repositories.BotUserRepository;
import net.hatemachine.mortybot.util.IrcUtils;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Implements the REGISTER command, allowing users to register themselves with the bot.
 * The first user to register with the bot will be given the admin flag.
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
        var botUserRepository = new BotUserRepository();
        var props = BotProperties.getBotProperties();
        var maskType = props.getIntProperty("register.mask.type", BotDefaults.REGISTER_MASK_TYPE);

        User user = event.getUser();
        String desiredName = args.isEmpty() ? user.getNick() : args.get(0);
        Validate.botUserName(desiredName);
        String maskedAddress = IrcUtils.maskAddress(user.getHostmask(), maskType);

        Optional<BotUser> optionalBotUser = botUserRepository.findByHostmask(user.getHostmask());

        if (optionalBotUser.isPresent()) {
            log.info("Hostmask {} matches existing bot user(s): {}", user.getHostmask(), optionalBotUser.get().getName());
            event.respondWith("Your hostmask already matches an existing bot user");
        } else {
            BotUser botUser = new BotUser(desiredName, maskedAddress);

            // If this is the first user to register with the bot then give them the admin flag.
            if (botUserRepository.count() == 0) {
                botUser.setAdminFlag(true);
            }

            botUser = botUserRepository.save(botUser);

            String msg = String.format("Registered %s with hostmask %s", botUser.getName(), botUser.getHostmasks());
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
