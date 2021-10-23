package net.hatemachine.mortybot;

import net.hatemachine.mortybot.exception.BotUserException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.hatemachine.mortybot.exception.BotUserException.Reason.*;

public class InMemoryBotUserDao implements BotUserDao {

    private final Map<String, BotUser> botUsers = new HashMap<>();

    @Override
    public BotUser getByName(final String name) {
        if (!botUsers.containsKey(name))
            throw new BotUserException(USER_NOT_FOUND, name);

        return botUsers.get(name);
    }

    @Override
    public void add(final BotUser botUser) {
        if (botUsers.containsKey(botUser.getName()))
            throw new BotUserException(USER_EXISTS, botUser.getName());

        botUsers.put(botUser.getName(), botUser);
    }

    @Override
    public void update(final BotUser botUser) {
        if (!botUsers.containsKey(botUser.getName()))
            throw new BotUserException(USER_NOT_FOUND, botUser.getName());

        botUsers.remove(botUser.getName());
        botUsers.put(botUser.getName(), botUser);
    }

    @Override
    public void delete(final BotUser botUser) {
        if (!botUsers.containsKey(botUser.getName()))
            throw new BotUserException(USER_NOT_FOUND, botUser.getName());

        botUsers.remove(botUser.getName());
    }

    @Override
    public List<BotUser> getAll() {
        return new ArrayList<>(botUsers.values());
    }
}
