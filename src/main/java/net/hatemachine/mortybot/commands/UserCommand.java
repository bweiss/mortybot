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
import net.hatemachine.mortybot.exception.CommandException;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.repositories.BotUserRepository;
import net.hatemachine.mortybot.util.Validate;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;
import net.sourceforge.argparse4j.internal.UnrecognizedCommandException;
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
        "Attributes supported with SET: ADMIN, DCC, HMASK, IGNORE, LOCATION"
})
public class UserCommand implements Command {

    private enum SubCommand {
        ADD,
        LIST,
        RM,
        SET,
        SHOW
    }

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
        Validate.commandArguments(args, 1);

        // As far as I can tell, argparse4j doesn't have a way to do case-insensitive subcommands, so we uppercase the first arg.
        args.set(0, args.get(0).toUpperCase());

        // Set up our command parsers
        ArgumentParser parser = ArgumentParsers.newFor("USER").build();
        Subparsers subparsers = parser.addSubparsers();

        // For the time being all of our subcommands are the same and consume all remaining args, but we could do other things.
        for (SubCommand scmd : SubCommand.values()) {
            subparsers.addParser(scmd.toString()).setDefault("command", scmd).addArgument("args").nargs("*");
        }

        // Parse the args and dispatch to the appropriate method for handling
        try {
            Namespace ns = parser.parseArgs(args.toArray(new String[0]));

            SubCommand subCommand = ns.get("command");
            List<String> newArgs = ns.get("args");

            log.debug("subCommand: {}, args: {}", subCommand, newArgs);

            switch (subCommand) {
                case ADD -> addCommand(newArgs);
                case LIST -> listCommand(newArgs);
                case RM -> rmCommand(newArgs);
                case SET -> setCommand(newArgs);
                case SHOW -> showCommand(newArgs);
                default -> log.warn("This default case should never happen");
            }
        } catch (UnrecognizedCommandException e) {
            event.respondWith(e.getMessage());
        } catch (ArgumentParserException e) {
            log.error("Failed to parse subcommand arguments");
            event.respondWith("Invalid arguments");
        }
    }

    private void addCommand(List<String> args) {
        Validate.commandArguments(args, 2);

        String botUserName = args.get(0);
        String botUserHostmask = args.get(1);

        var botUser = new BotUser(botUserName, botUserHostmask);
        botUserRepository.save(botUser);

        event.respondWith("Added bot user " + botUserName + " with hostmask " + botUserHostmask);
    }

    private void listCommand(List<String> args) {
        event.respondWith("Bot users: " + botUserRepository.findAll().stream()
                .map(BotUser::getName)
                .collect(Collectors.joining(", ")));
    }

    private void rmCommand(List<String> args) {
        List<BotUser> botUsers = botUserRepository.findAllByName(args);

        botUserRepository.deleteAllById(botUsers.stream()
                .map(BotUser::getId)
                .toList());

        event.respondWith("Removed bot users: " + botUsers.stream()
                .map(BotUser::getName)
                .collect(Collectors.joining(", ")));
    }

    private void setCommand(List<String> args) {
        Validate.commandArguments(args, 2);

        Optional<BotUser> optionalBotUser = botUserRepository.findByName(args.get(0));
        String attr = args.get(1);
        String newVal = args.size() > 2 ? args.get(2) : null;

        if (optionalBotUser.isPresent()) {
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

                case "DCC":
                    if (newVal == null) {
                        botUser.setDccFlag(!botUser.hasDccFlag());
                    } else {
                        botUser.setDccFlag(newVal.equalsIgnoreCase("true") || newVal.equalsIgnoreCase("1"));
                    }

                    event.respondWith("dccFlag set to " + botUser.hasDccFlag());
                    break;

                case "HMASK":
                    // FIXME: this doesn't work with argparse4j as implemented. using a hyphen in the args will throw an exception.
                    String hmask = newVal;
                    String action = "add";

                    if (newVal == null || newVal.isBlank()) {
                        throw new CommandException(CommandException.Reason.INVALID_ARGS, "Not enough arguments");
                    } else if (newVal.startsWith("+") || newVal.startsWith("-")) {
                        hmask = newVal.substring(1);
                        action = newVal.startsWith("-") ? "remove" : "add";
                    }

                    if (action.equals("add")) {
                        botUser.getHostmasks().add(hmask);
                        botUserRepository.save(botUser);
                        event.respondWith("Added hostmask: " + hmask);
                    } else {
                        botUser.getHostmasks().remove(hmask);
                        botUserRepository.save(botUser);
                        event.respondWith("Removed hostmask: " + hmask);
                    }

                    break;

                case "IGNORE":
                    if (newVal == null) {
                        botUser.setIgnoreFlag(!botUser.hasIgnoreFlag());
                    } else {
                        botUser.setIgnoreFlag(newVal.equalsIgnoreCase("true") || newVal.equalsIgnoreCase("1"));
                    }

                    event.respondWith("ignoreFlag set to " + botUser.hasIgnoreFlag());
                    break;

                case "LOCATION":
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
