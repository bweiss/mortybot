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

import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.custom.entity.BotUserFlag;
import net.hatemachine.mortybot.exception.BotCommandException;
import net.hatemachine.mortybot.model.BotUser;
import net.hatemachine.mortybot.util.BotUserHelper;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static net.hatemachine.mortybot.exception.BotCommandException.Reason.*;

/**
 * This class is used by the CommandListener to perform some checks on received commands prior to execution.<br/>
 * <br/>
 * It is responsible for checking if:<br/>
 * - the command is enabled.<br/>
 * - the command is restricted and, if so, whether the user is authorized to execute it.<br/>
 * - the user is being ignored by the bot.<br/>
 *
 * @see Command
 * @see net.hatemachine.mortybot.listeners.CommandListener
 */
public class CommandProxy implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(CommandProxy.class);

    private final Command command;

    private CommandProxy(Command command) {
        this.command = command;
    }

    public static Command newInstance(Command cmd) {
        return (Command) java.lang.reflect.Proxy.newProxyInstance(
                cmd.getClass().getClassLoader(),
                cmd.getClass().getInterfaces(),
                new CommandProxy(cmd)
        );
    }

    /**
     * Executes a bot command after verification of the user and command is performed.
     *
     * @param proxy the proxy instance that the method was invoked on
     *
     * @param m the {@code Method} instance corresponding to the interface method
     *          invoked on the proxy instance.  The declaring class of the {@code Method}
     *          object will be the interface that the method was declared in, which
     *          may be a superinterface of the proxy interface that the proxy class
     *          inherits the method through.
     *
     * @param args an array of objects containing the values of the
     *             arguments passed in the method invocation on the proxy instance
     *             or {@code null} if interface method takes no arguments.
     *             Arguments of primitive types are wrapped in instances of the
     *             appropriate primitive wrapper class, such as
     *             {@code java.lang.Integer} or {@code java.lang.Boolean}.
     *
     * @return the result of dispatching the method
     * @throws BotCommandException
     */
    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws BotCommandException {
        Object result = new Object();
        BotProperties props = BotProperties.getBotProperties();
        List<String> enabled = Arrays.asList(props.getStringProperty("commands.enabled").split(","));
        List<String> adminOnly = Arrays.asList(props.getStringProperty("commands.restricted").split(","));
        GenericMessageEvent event = command.getEvent();
        User user = event.getUser();
        List<BotUser> botUsers = BotUserHelper.findByHostmask(user.getHostmask());
        boolean ignoredFlag = botUsers.stream().anyMatch(u -> u.getBotUserFlags().contains(BotUserFlag.IGNORE));
        boolean adminFlag = botUsers.stream().anyMatch(u -> u.getBotUserFlags().contains(BotUserFlag.ADMIN));
        String className = command.getClass().getSimpleName();

        if (ignoredFlag) {
            throw new BotCommandException(USER_IGNORED, user.toString());
        } else if (!enabled.contains(className)) {
            throw new BotCommandException(COMMAND_NOT_ENABLED, className);
        } else if (adminOnly.contains(className) && !adminFlag) {
            throw new BotCommandException(USER_UNAUTHORIZED, className + " " + user);
        } else {
            try {
                result = m.invoke(command, args);
            } catch (InvocationTargetException | IllegalAccessException e) {
                log.error("Exception encountered during command invocation ({})", className, e);
            }
        }

        return result;
    }
}
