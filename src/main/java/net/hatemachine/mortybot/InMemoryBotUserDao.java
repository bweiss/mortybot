package net.hatemachine.mortybot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryBotUserDao implements BotUserDao {

    private final Map<String, BotUser> nameToBotUser = new HashMap<>();

    @Override
    public BotUser getByName(final String name) {
        return nameToBotUser.get(name);
    }

    @Override
    public boolean add(final BotUser botUser) {
        nameToBotUser.put(botUser.getName(), botUser);
        return true;
    }

    @Override
    public boolean update(final BotUser botUser) {
        nameToBotUser.remove(botUser.getName());
        nameToBotUser.put(botUser.getName(), botUser);
        return true;
    }

    @Override
    public boolean delete(final BotUser botUser) {
        return nameToBotUser.remove(botUser.getName()) != null;
    }

    @Override
    public List<BotUser> getAllBotUsers() {
        return new ArrayList<>(nameToBotUser.values());
    }
}
