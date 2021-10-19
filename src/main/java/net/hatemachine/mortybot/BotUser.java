package net.hatemachine.mortybot;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import static net.hatemachine.mortybot.BotUserType.*;
import static net.hatemachine.mortybot.util.StringUtils.wildcardToRegex;

public class BotUser {

    private String name;
    private final Set<String> hostmasks = new HashSet<>();
    private BotUserType type;

    public BotUser(final String name, final String hostmask) {
        this(name, hostmask, MORTY);
    }

    public BotUser(final String name, final String hostmask, final BotUserType type) {
        this.name = name;
        this.hostmasks.add(hostmask);
        this.type = type;
    }

    public boolean hasMatchingHostmask(String userhost) {
        return hostmasks.stream().anyMatch(h -> Pattern.matches(wildcardToRegex(h), userhost));
    }

    public boolean addHostmask(String hostmask) {
        return hostmasks.add(hostmask);
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

    public BotUserType getType() {
        return type;
    }

    public void setType(BotUserType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "BotUser{" +
                ", name='" + name + '\'' +
                ", hostmasks=" + hostmasks +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotUser botUser = (BotUser) o;
        return name.equals(botUser.name) && hostmasks.equals(botUser.hostmasks) && type == botUser.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, hostmasks, type);
    }
}
