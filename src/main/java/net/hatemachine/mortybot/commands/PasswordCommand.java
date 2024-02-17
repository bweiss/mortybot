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
import net.hatemachine.mortybot.util.PasswordEncoderFactory;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

/**
 * Implements the PASS and CHPASS commands that allow a user to set or update their password with the bot.
 */
@BotCommand(name = "PASS", help = {
        "Sets your password for the first time (restricted to private messages and DCC chat)",
        "Usage: PASS <password>",
        "Use CHPASS to change your password"
})
@BotCommand(name = "CHPASS", help = {
        "Changes your password (restricted to private messages and DCC chat)",
        "Usage: CHPASS <current_password> <new_password>"
})
public class PasswordCommand implements Command {

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public PasswordCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
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

        BotProperties props = BotProperties.getBotProperties();
        String cmdPrefix = props.getStringProperty("bot.command.prefix", BotDefaults.BOT_COMMAND_PREFIX);
        String cmd = event.getMessage().split(" ")[0]
                .substring(cmdPrefix.length())
                .toUpperCase();

        switch (cmd) {
            case "PASS" -> passCommand();
            case "CHPASS" -> chPassCommand();
        }
    }

    private void passCommand() {
        Validate.arguments(args, 1);

        BotUserRepository repo = new BotUserRepository();
        User user = event.getUser();
        Optional<BotUser> optionalBotUser = repo.findByHostmask(user.getHostmask());

        if (optionalBotUser.isPresent()) {
            BotUser botUser = optionalBotUser.get();

            if (botUser.getPassword() == null) {
                PasswordEncoder encoder = PasswordEncoderFactory.getEncoder();
                String newPass = Validate.password(args.getFirst());
                botUser.setPassword(encoder.encode(newPass));
                repo.save(botUser);
                event.respondWith("Password set");
            } else {
                event.respondWith("You have already set a password. Use the CHPASS command to change it.");
            }
        } else {
            event.respondWith("You are not recognized. Use the REGISTER command to register.");
        }
    }

    private void chPassCommand() {
        Validate.arguments(args, 2);

        BotUserRepository repo = new BotUserRepository();
        User user = event.getUser();
        Optional<BotUser> optionalBotUser = repo.findByHostmask(user.getHostmask());

        if (optionalBotUser.isPresent()) {
            BotUser botUser = optionalBotUser.get();
            String currentPass = Validate.password(args.getFirst());
            String newPass = Validate.password(args.get(1));
            PasswordEncoder encoder = PasswordEncoderFactory.getEncoder();

            if (botUser.getPassword() == null) {
                event.respondWith("You do not have a password set. Use the PASS command to set one.");
            } else if (encoder.matches(currentPass, botUser.getPassword())) {
                botUser.setPassword(encoder.encode(newPass));
                repo.save(botUser);
                event.respondWith("Password changed");
            } else {
                event.respondWith("Current password does not match");
            }
        } else {
            event.respondWith("You are not recognized. User the REGISTER command to register or the IDENTIFY command to add your hostmask");
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
