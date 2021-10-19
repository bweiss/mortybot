package net.hatemachine.mortybot;

import net.hatemachine.mortybot.exception.BotUserException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.hatemachine.mortybot.exception.BotUserException.Reason.*;

public class InMemoryBotUserDao implements BotUserDao {

    private final Map<String, BotUser> nameToBotUser = new HashMap<>();

    @Override
    public BotUser getByName(final String name) {
        if (!nameToBotUser.containsKey(name))
            throw new BotUserException(USER_NOT_FOUND, name);

        return nameToBotUser.get(name);
    }

    @Override
    public void add(final BotUser botUser) {
        if (nameToBotUser.containsKey(botUser.getName()))
            throw new BotUserException(USER_EXISTS, botUser.getName());

        nameToBotUser.put(botUser.getName(), botUser);
    }

    @Override
    public void update(final BotUser botUser) {
        if (!nameToBotUser.containsKey(botUser.getName()))
            throw new BotUserException(USER_NOT_FOUND, botUser.getName());

        nameToBotUser.remove(botUser.getName());
        nameToBotUser.put(botUser.getName(), botUser);
    }

    @Override
    public boolean delete(final BotUser botUser) {
        if (!nameToBotUser.containsKey(botUser.getName()))
            throw new BotUserException(USER_NOT_FOUND, botUser.getName());

        return nameToBotUser.remove(botUser.getName()) != null;
    }

    @Override
    public List<BotUser> getAll() {
        return new ArrayList<>(nameToBotUser.values());
    }
}
