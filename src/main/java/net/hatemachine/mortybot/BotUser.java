package net.hatemachine.mortybot;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static net.hatemachine.mortybot.StringUtils.validateString;
import static net.hatemachine.mortybot.StringUtils.wildcardToRegex;

public class BotUser {

    private String name;
    private final Set<String> hostmasks = new HashSet<>();
    private final Type type;

    public enum Type {
        ADMIN,
        USER,
        GUEST
    }

    public BotUser(String name, String hostmask, Type type) {
        this.name = validateString(name);
        this.hostmasks.add(validateString(hostmask));
        this.type = type;
    }

    public boolean hasMatchingHostmask(String userhost) {
        return hostmasks.stream().anyMatch(h -> Pattern.matches(wildcardToRegex(h), userhost));
    }

    public boolean addHostmask(String hostmask) {
        return hostmasks.add(validateString(hostmask));
    }

    public boolean removeHostmask(String hostmask) {
        return hostmasks.remove(hostmask);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getHostmasks() {
        return hostmasks;
    }

    public Type getType() {
        return type;
    }
}
