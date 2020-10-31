package net.hatemachine.mortybot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryBotUserDao implements BotUserDao {

    private final Map<Integer, BotUser> idToBotUser = new HashMap<>();

    @Override
    public BotUser getById(final int id) {
        return idToBotUser.get(id);
    }

    @Override
    public boolean add(final BotUser botUser) {
        idToBotUser.put(botUser.getId(), botUser);
        return true;
    }

    @Override
    public boolean update(final BotUser botUser) {
        idToBotUser.remove(botUser.getId());
        idToBotUser.put(botUser.getId(), botUser);
        return true;
    }

    @Override
    public boolean delete(final BotUser botUser) {
        return idToBotUser.remove(botUser.getId()) != null;
    }

    @Override
    public List<BotUser> getAllBotUsers() {
        return new ArrayList<>(idToBotUser.values());
    }
}
