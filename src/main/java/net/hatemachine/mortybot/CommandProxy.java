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

import net.hatemachine.mortybot.custom.entity.BotUserFlag;
import net.hatemachine.mortybot.exception.CommandException;
import net.hatemachine.mortybot.helpers.BotUserHelper;
import net.hatemachine.mortybot.model.BotUser;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static net.hatemachine.mortybot.exception.CommandException.Reason.*;

/**
 * This class is used by the CommandListener to perform some checks on received commands prior to execution.<br/>
 * <br/>
 * It is responsible for checking if:<br/>
 * - the command is restricted and, if so, whether the user is authorized to execute it.<br/>
 * - the user is being ignored by the bot.<br/>
 *
 * @see Command
 * @see net.hatemachine.mortybot.listeners.CommandListener
 */
public class CommandProxy implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(CommandProxy.class);

    private final CommandWrapper commandWrapper;

    private CommandProxy(CommandWrapper cmdWrapper) {
        this.commandWrapper = cmdWrapper;
    }

    public static Command newInstance(CommandWrapper cmdWrapper) {
        return (Command) java.lang.reflect.Proxy.newProxyInstance(
                cmdWrapper.getCmdClass().getClassLoader(),
                cmdWrapper.getCmdClass().getInterfaces(),
                new CommandProxy(cmdWrapper)
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
     * @throws CommandException
     */
    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws CommandException {
        Object result = new Object();
        GenericMessageEvent event = commandWrapper.getInstance().getEvent();
        User user = event.getUser();
        BotUserHelper botUserHelper = new BotUserHelper();
        List<BotUser> matchingBotUsers = botUserHelper.findByHostmask(user.getHostmask());
        boolean userIsIgnored = matchingBotUsers.stream().anyMatch(u -> u.getBotUserFlags().contains(BotUserFlag.IGNORE));
        boolean userIsAdmin = matchingBotUsers.stream().anyMatch(u -> u.getBotUserFlags().contains(BotUserFlag.ADMIN));

        if (userIsIgnored) {
            throw new CommandException(IGNORED_USER, user.toString());

        } else if (commandWrapper.isRestricted() && !userIsAdmin) {
            throw new CommandException(UNAUTHORIZED_USER, user.toString());

        } else {
            try {
                result = m.invoke(commandWrapper.getInstance(), args);
            } catch (InvocationTargetException e) {
                var targetException = e.getTargetException();

                if (targetException instanceof CommandException ex) {
                    throw new CommandException(ex.getReason(), ex.getMessage());
                } else if (targetException instanceof IllegalArgumentException ex) {
                    throw new IllegalArgumentException(ex.getMessage());
                } else {
                    log.error("Exception encountered during command invocation ({})", commandWrapper.getName(), e);
                }
            } catch (IllegalAccessException e) {
                log.error("Access exception: {}", e.getMessage(), e);
            }
        }

        return result;
    }
}
