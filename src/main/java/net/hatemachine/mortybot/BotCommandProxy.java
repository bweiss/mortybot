package net.hatemachine.mortybot;

import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BotCommandProxy implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(BotCommandProxy.class);

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
        User user = event.getUser();

        if (!enabled.contains(command.getClass().getSimpleName())) {
            String msg = "command is not enabled";
            log.warn(msg);
            throw new IllegalAccessException(msg);
        } else if (adminOnly.contains(command.getClass().getSimpleName()) && !bot.authorizeRick(user)) {
            String msg = "not authorized to run this command";
            log.warn(msg);
            throw new IllegalAccessException(msg);
        } else {
            result = m.invoke(command, args);
        }
        return result;
    }
}
