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
import net.hatemachine.mortybot.MortyBot;
import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.util.IrcUtils;
import net.hatemachine.mortybot.util.Validate;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.UserChannelDao;
import org.pircbotx.exception.DaoException;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.pircbotx.exception.DaoException.Reason.*;

/**
 * Implements the BAN, BANKICK, KICK, and KICKBAN bot commands.
 */
@BotCommand(name="BAN", clazz=BanKickCommand.class, help={
        "Bans a user from a channel",
        "Usage: BAN <nick|hostmask>",
        "Usage: BAN <nick|hostmask> <channel>",
        "You must specify the channel if command is not from a public source"
})
@BotCommand(name="BANKICK", clazz=BanKickCommand.class, help={
        "Bans and kicks a user from a channel",
        "Usage: BANKICK <nick> [reason]",
        "Usage: BANKICK <nick> <channel> [reason]",
        "You must specify the channel if command is not from a public source"
})
@BotCommand(name="KICK", clazz=BanKickCommand.class, help={
        "Kicks a user from a channel",
        "Usage: KICK <user> [reason]",
        "Usage: KICK <user> <channel> [reason]",
        "You must specify the channel if command is not from a public source"
})
@BotCommand(name="KICKBAN", clazz=BanKickCommand.class, help={
        "Kicks and bans a user from a channel",
        "Usage: KICKBAN <user> [reason]",
        "Usage: KICKBAN <user> <channel> [reason]",
        "You must specify the channel if command is not from a public source"
})
public class BanKickCommand implements Command {

    private static final Logger log = LoggerFactory.getLogger(BanKickCommand.class);

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public BanKickCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    /**
     * Main execution point for all kick/ban related commands. Responsible for basic argument checking
     * and determining which specific command is being executed. We have to do a little extra work,
     * but it allows us to implement multiple commands in a single class.
     */
    @Override
    public void execute() {
        BotProperties props = BotProperties.getBotProperties();
        String cmdPrefix = props.getStringProperty("bot.command.prefix", BotDefaults.BOT_COMMAND_PREFIX);
        String message = event.getMessage();
        MortyBot bot = event.getBot();
        UserChannelDao<User, Channel> dao = bot.getUserChannelDao();
        Channel channel;
        String target;
        List<String> newArgs;

        if (args.isEmpty()) {
            event.respondWith("Not enough arguments. Consult the HELP command.");
            return;
        }

        // if from a public message, attempt to determine the channel
        if (source == CommandListener.CommandSource.PUBLIC) {
            channel = ((MessageEvent) event).getChannel();
            target = args.get(0);
            newArgs = args.subList(1, args.size());

        // otherwise get it from the user
        } else if (args.size() > 1) {
            try {
                channel = dao.getChannel(args.get(1));
                target = args.get(0);
                newArgs = args.subList(2, args.size());
            } catch (DaoException ex) {
                handleDaoException(ex, "execute", "could not locate channel: " + args.get(1));
                return;
            }
        } else {
            log.warn("Unable to determine channel");
            event.respondWith("Please specify a channel");
            return;
        }

        if (channel != null) {
            // make sure we have ops
            if (!channel.isOp(bot.getUserBot())) {
                log.warn("Bot does not have operator status on {}", channel.getName());
                event.respondWith(String.format("Sorry, I don't have operator status on %s", channel.getName()));
                return;
            }

            // this should always be true, we're just being careful
            if (message.startsWith(cmdPrefix)) {
                String cmd = message.split(" ")[0]
                        .substring(cmdPrefix.length())
                        .toUpperCase();

                switch (cmd) {
                    case "BAN" -> banCommand(channel, target);
                    case "BANKICK" -> banKickCommand(channel, target, newArgs);
                    case "KICK" -> kickCommand(channel, target, newArgs);
                    case "KICKBAN" -> kickBanCommand(channel, target, newArgs);
                    default -> log.warn("Unknown command: {}", cmd);
                }
            }
        }
    }

    /**
     * BAN command.
     *
     * @param channel the {@link Channel} to perform the action in
     * @param target the target of the action
     */
    private void banCommand(Channel channel, String target) {
        MortyBot bot = event.getBot();
        UserChannelDao<User, Channel> dao = bot.getUserChannelDao();
        String hostmask = target;

        // If our target is not a hostmask already, attempt to retrieve the user and their hostmask.
        // If the user is not found, we fall back to using the original target as the nickname portion
        // of the hostmask (e.g. a target of "foo" will end up banning "foo!*@*")
        if (!Validate.isHostmask(target)) {
            try {
                int maskType = BotProperties.getBotProperties()
                        .getIntProperty("ban.mask.type", BotDefaults.BAN_MASK_TYPE);
                User user = dao.getUser(target);

                // sometimes we don't know ident@hostname
                if (user.getIdent() == null || user.getHostname() == null) {
                    hostmask = user.getNick() + "!*@*";
                } else {
                    String ircAddress = constructIrcAddress(user);
                    hostmask = IrcUtils.maskAddress(ircAddress, maskType);
                }
            } catch (DaoException ex) {
                handleDaoException(ex, "kickCommand", target);
                return;
            }
        }

        bot.sendIRC().mode(channel.getName(), "+b " + hostmask);
    }

    /**
     * BANKICK command.
     *
     * @param channel the {@link Channel} to perform the action in
     * @param target the target of the action
     */
    private void banKickCommand(Channel channel, String target, List<String> newArgs) {
        banCommand(channel, target);
        kickCommand(channel, target, newArgs);
    }

    /**
     * KICK command.
     *
     * @param channel the {@link Channel} to perform the action in
     * @param target the target of the action
     */
    private void kickCommand(Channel channel, String target, List<String> newArgs) {
        MortyBot bot = event.getBot();
        UserChannelDao<User, Channel> dao = bot.getUserChannelDao();
        String reason = newArgs.isEmpty()
                ? BotProperties.getBotProperties().getStringProperty("kick.reason", BotDefaults.KICK_REASON)
                : String.join(" ", newArgs);

        try {
            User user = dao.getUser(target);
            channel.send().kick(user, reason);
        } catch (DaoException ex) {
            handleDaoException(ex, "kickCommand", target);
        }
    }

    /**
     * KICKBAN command.
     *
     * @param channel the {@link Channel} to perform the action in
     * @param target the target of the action
     * @param newArgs any remaining arguments to the command
     */
    private void kickBanCommand(Channel channel, String target, List<String> newArgs) {
        MortyBot bot = event.getBot();
        UserChannelDao<User, Channel> dao = bot.getUserChannelDao();
        int maskType = BotProperties.getBotProperties().getIntProperty("ban.mask.type", BotDefaults.BAN_MASK_TYPE);
        String banString;

        try {
            User user = dao.getUser(target);

            // sometimes we don't know ident@hostname
            if (user.getIdent() == null || user.getHostname() == null) {
                banString = user.getNick() + "!*@*";
            } else {
                banString = IrcUtils.maskAddress(constructIrcAddress(user), maskType);
            }

            kickCommand(channel, target, newArgs);
            banCommand(channel, banString);
        } catch (DaoException ex) {
            handleDaoException(ex, "kickBanCommand", target);
        }
    }

    private void handleDaoException(DaoException ex, String method, String message) {
        String response;

        if (ex.getReason() == UNKNOWN_CHANNEL) {
            response = "Unknown channel";
        } else if (ex.getReason() == UNKNOWN_USER) {
            response = "Unknown user";
        } else if (ex.getReason() == UNKNOWN_USER_HOSTMASK) {
            response = "Unknown user hostmask";
        } else {
            response = "Unexpected error";
        }

        log.warn("{}: {}: {}", method, response, message);
        event.respondWith(response);
    }

    /**
     * Construct an appropriate hostmask string from a user object.
     * This will replace null values for the ident and hostname with asterisks.
     *
     * @param user the {@link User} we're getting our hostmask from
     * @return a {@link String} representing the user's hostmask with nulls replaced with an asterisk
     */
    private String constructIrcAddress(User user) {
        StringBuilder sb = new StringBuilder();

        if (user.getNick() == null) {
            throw new IllegalArgumentException("User nick cannot be null");
        }

        sb.append(user.getNick());
        sb.append("!");

        if (user.getIdent() != null) {
            sb.append(user.getIdent());
        } else {
            sb.append("*");
        }

        sb.append("@");

        if (user.getHostname() != null) {
            sb.append(user.getHostname());
        } else {
            sb.append("*");
        }

        return sb.toString();
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
