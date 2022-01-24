/*
 * MortyBot - An IRC bot built on the PircBotX framework.
 * Copyright © 2022 Brian Weiss (brianmweiss@gmail.com)
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

import net.hatemachine.mortybot.exception.BotCommandException;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.hatemachine.mortybot.exception.BotCommandException.Reason.COMMAND_NOT_ENABLED;
import static net.hatemachine.mortybot.exception.BotCommandException.Reason.USER_UNAUTHORIZED;

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
        List<String> enabled = new ArrayList<>(Arrays.asList(MortyBot.getStringProperty("CommandListener.enabled.commands").split(",")));
        List<String> adminOnly = new ArrayList<>(Arrays.asList(MortyBot.getStringProperty("CommandListener.admin.commands").split(",")));
        GenericMessageEvent event = command.getEvent();
        MortyBot bot = event.getBot();
        var user = event.getUser();

        if (!enabled.contains(command.getName())) {
            throw new BotCommandException(COMMAND_NOT_ENABLED, command.getName());
        } else if (adminOnly.contains(command.getName()) && !bot.getBotUserDao().isAdmin(user)) {
            throw new BotCommandException(USER_UNAUTHORIZED, command.getName() + " " + user);
        } else {
            result = m.invoke(command, args);
        }
        return result;
    }
}
