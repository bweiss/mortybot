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
import net.hatemachine.mortybot.util.PasswordEncoderFactory;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

/**
 * Implements the IDENTIFY command that allows users to identify with the bot using their password.
 */
@BotCommand(name = "IDENT", help = {
        "Identify yourself with the bot and add your current hostmask (restricted to private messages and DCC chat)",
        "Usage: IDENT <username> <password>"
})
@BotCommand(name = "IDENTIFY", help = {
        "Identify yourself with the bot and add your current hostmask (restricted to private messages and DCC chat)",
        "Usage: IDENTIFY <username> <password>"
})
public class IdentifyCommand implements Command {

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public IdentifyCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        if (source == CommandListener.CommandSource.PUBLIC) {
            event.respondWith("Aww jeez, " + event.getUser().getNick() + ". Shouldn't that be in a private message?");
            return;
        }

        Validate.arguments(args, 2);

        BotUserRepository repo = new BotUserRepository();
        Optional<BotUser> matchingUser = repo.findByHostmask(event.getUser().getHostmask());

        if (matchingUser.isPresent()) {
            event.respondWith("You are already identified!");
            return;
        }

        String username = args.getFirst();
        String password = args.get(1);
        Optional<BotUser> optionalBotUser = repo.findByName(username);

        if (optionalBotUser.isEmpty()) {
            event.respondWith("Invalid username");
        } else {
            BotUser botUser = optionalBotUser.get();
            PasswordEncoder encoder = PasswordEncoderFactory.getEncoder();

            if (botUser.getPassword() == null) {
                event.respondWith("Password not set");
            } else if (encoder.matches(password, botUser.getPassword())) {
                BotProperties props = BotProperties.getBotProperties();
                int maskType = props.getIntProperty("identify.mask.type", BotDefaults.IDENTIFY_MASK_TYPE);
                String newHostmask = IrcUtils.maskAddress(event.getUser().getHostmask(), maskType);
                botUser.getHostmasks().add(newHostmask);
                repo.save(botUser);
                event.respondWith("Added hostmask " + newHostmask);
            } else {
                event.respondWith("Invalid password");
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
