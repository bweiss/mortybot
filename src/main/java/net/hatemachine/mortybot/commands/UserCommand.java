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
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.repositories.BotUserRepository;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implements the USER command, allowing users to view and manipulate bot users.
 */
@BotCommand(name = "USER", restricted = true, help = {
        "View and manipulate bot users",
        "Usage: USER ADD <name> <hostmask>",
        "Usage: USER LIST",
        "Usage: USER RM <name> [...]",
        "Usage: USER SET <name> <attribute> [new_val]",
        "Usage: USER SHOW <name> [...]",
        "Attributes: ADMIN, AOP, DCC, HOSTMASK, IGNORE, LOCATION"
})
public class UserCommand implements Command {

    private static final Logger log = LoggerFactory.getLogger(UserCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;
    private final BotUserRepository botUserRepository;

    public UserCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
        this.botUserRepository = new BotUserRepository();
    }

    @Override
    public void execute() {
        Validate.arguments(args, 1);

        String subCommand = args.get(0).toUpperCase();
        List<String> newArgs = args.subList(1, args.size());

        log.debug("subCommand: {}, args: {}", subCommand, newArgs);

        switch (subCommand) {
            case "ADD" -> addCommand(newArgs);
            case "LIST" -> listCommand(newArgs);
            case "RM" -> rmCommand(newArgs);
            case "SET" -> setCommand(newArgs);
            case "SHOW" -> showCommand(newArgs);
            default -> event.respondWith("Invalid subcommand: " + subCommand);
        }
    }

    private void addCommand(List<String> args) {
        Validate.arguments(args, 2);

        String name = Validate.botUserName(args.get(0));
        String hostmask = Validate.hostmask(args.get(1));

        if (botUserRepository.existsByName(name)) {
            event.respondWith("User already exists");
        } else {
            var botUser = new BotUser(name, hostmask);
            botUserRepository.save(botUser);

            event.respondWith("Added bot user " + name + " with hostmask " + hostmask);
        }
    }

    private void listCommand(List<String> args) {
        event.respondWith("Bot users: " + botUserRepository.findAll().stream()
                .map(BotUser::getName)
                .collect(Collectors.joining(", ")));
    }

    private void rmCommand(List<String> args) {
        List<BotUser> botUsers = botUserRepository.findAllByName(args);

        if (botUsers.isEmpty()) {
            event.respondWith("Nothing to remove");
        } else {
            botUserRepository.deleteAll(botUsers);

            event.respondWith("Removed bot users: " + botUsers.stream()
                    .map(BotUser::getName)
                    .collect(Collectors.joining(", ")));
        }
    }

    private void setCommand(List<String> args) {
        Validate.arguments(args, 2);

        Optional<BotUser> optionalBotUser = botUserRepository.findByName(args.get(0));
        String attr = args.get(1);
        String newVal = args.size() > 2 ? args.get(2) : null;

        if (optionalBotUser.isEmpty()) {
            event.respondWith("Unknown user");
        } else {
            BotUser botUser = optionalBotUser.get();

            switch (attr.toUpperCase()) {
                case "ADMIN":
                    if (newVal == null) {
                        botUser.setAdminFlag(!botUser.hasAdminFlag());
                    } else {
                        botUser.setAdminFlag(newVal.equalsIgnoreCase("true") || newVal.equalsIgnoreCase("1"));
                    }

                    event.respondWith("adminFlag set to " + botUser.hasAdminFlag());
                    break;

                case "AOP":
                    if (newVal == null || newVal.isBlank()) {
                        throw new IllegalArgumentException("Not enough arguments");
                    } else if (newVal.startsWith("-")) {
                        var channelName = newVal.substring(1);
                        botUser.getAutoOpChannels().remove(channelName);
                    } else {
                        botUser.getAutoOpChannels().add(newVal);
                    }

                    event.respondWith("Auto-op channels for " + botUser.getName() + " set to " + botUser.getAutoOpChannels());
                    break;

                case "DCC":
                    if (newVal == null) {
                        botUser.setDccFlag(!botUser.hasDccFlag());
                    } else {
                        botUser.setDccFlag(newVal.equalsIgnoreCase("true") || newVal.equalsIgnoreCase("1"));
                    }

                    event.respondWith("dccFlag set to " + botUser.hasDccFlag());
                    break;

                case "HOST", "HOSTMASK":
                    if (newVal == null || newVal.isBlank()) {
                        throw new IllegalArgumentException("Not enough arguments");
                    } else if (newVal.startsWith("-")) {
                        var hmask = newVal.substring(1);
                        botUser.getHostmasks().remove(hmask);
                    } else {
                        botUser.getHostmasks().add(newVal);
                    }

                    event.respondWith("Hostmasks for " + botUser.getName() + " set to " + botUser.getHostmasks());
                    break;

                case "IG", "IGNORE":
                    if (newVal == null) {
                        botUser.setIgnoreFlag(!botUser.hasIgnoreFlag());
                    } else {
                        botUser.setIgnoreFlag(newVal.equalsIgnoreCase("true") || newVal.equalsIgnoreCase("1"));
                    }

                    event.respondWith("ignoreFlag set to " + botUser.hasIgnoreFlag());
                    break;

                case "LOC", "LOCATION":
                    botUser.setLocation(newVal);
                    event.respondWith("Location set to " + botUser.getLocation());
                    break;

                default:
                    event.respondWith("Unknown attribute: " + attr);
            }

            botUserRepository.save(botUser);
        }
    }

    private void showCommand(List<String> args) {
        var botUsers = botUserRepository.findAllByName(args);
        botUsers.forEach(bu -> event.respondWith(bu.toString()));
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
