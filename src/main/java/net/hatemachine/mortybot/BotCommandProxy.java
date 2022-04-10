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
package net.hatemachine.mortybot;

import net.hatemachine.mortybot.config.BotState;
import net.hatemachine.mortybot.exception.BotCommandException;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static net.hatemachine.mortybot.exception.BotCommandException.Reason.*;

/**
 * This class is used by the CommandListener to perform some checks on received commands prior to execution.
 * It is responsible for checking whether the command is enabled and whether the user is authorized to use that command.
 */
public class BotCommandProxy implements InvocationHandler {

    private final BotCommand command;

    private BotCommandProxy(BotCommand command) {
        this.command = command;
    }

    public static BotCommand newInstance(BotCommand cmd) {
        return (BotCommand)java.lang.reflect.Proxy.newProxyInstance(cmd.getClass().getClassLoader(), cmd
                .getClass().getInterfaces(), new BotCommandProxy(cmd));
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws IllegalAccessException, InvocationTargetException {
        Object result;
        BotState bs = BotState.getBotState();
        List<String> enabled = Arrays.asList(bs.getStringProperty("commands.enabled").split(","));
        List<String> adminOnly = Arrays.asList(bs.getStringProperty("commands.restricted").split(","));
        GenericMessageEvent event = command.getEvent();
        MortyBot bot = event.getBot();
        User user = event.getUser();
        List<BotUser> ignored = bot.getBotUserDao().getAll(user.getHostmask(), BotUser.Flag.IGNORE);

        if (!ignored.isEmpty()) {
            BotUser ignoredBotUser = ignored.get(0);
            throw new BotCommandException(USER_IGNORED, user.getHostmask() + " [" + ignoredBotUser.getName() + "]");
        } else if (!enabled.contains(command.getName())) {
            throw new BotCommandException(COMMAND_NOT_ENABLED, command.getName());
        } else if (adminOnly.contains(command.getName()) && !bot.getBotUserDao().isAdmin(user)) {
            throw new BotCommandException(USER_UNAUTHORIZED, command.getName() + " " + user);
        } else {
            result = m.invoke(command, args);
        }

        return result;
    }
}
