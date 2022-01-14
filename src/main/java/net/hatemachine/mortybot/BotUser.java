package net.hatemachine.mortybot;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import static net.hatemachine.mortybot.util.StringUtils.wildcardToRegex;

public class BotUser {

    private String name;
    private final Set<String> hostmasks = new HashSet<>();
    private Set<Flag> flags = new HashSet<>();

    public enum Flag {
        ADMIN,
        AOP
    }

    public BotUser(final String name, final String hostmask) {
        this(name, hostmask, new HashSet<>());
    }

    public BotUser(final String name, final String hostmask, final Set<Flag> flags) {
        this.name = name;
        this.hostmasks.add(hostmask);
        this.flags = flags;
    }

    public BotUser(final String name, final String hostmask, final String flags) {
        this.name = name;
        this.hostmasks.add(hostmask);
        for (String flagStr : flags.split(",")) {
            try {
                this.flags.add(Enum.valueOf(BotUser.Flag.class, flagStr.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                // discard unknown flags
            }
        }
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

    public boolean addFlag(Flag flag) {
        return flags.add(flag);
    }

    public boolean removeFlag(Flag flag) {
        return flags.remove(flag);
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

    public Set<Flag> getFlags() {
        return flags;
    }

    @Override
    public String toString() {
        return "BotUser{" +
                "name='" + name + '\'' +
                ", hostmasks=" + hostmasks +
                ", flags=" + flags +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotUser botUser = (BotUser) o;
        return name.equals(botUser.name) && hostmasks.equals(botUser.hostmasks) && flags == botUser.flags;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, hostmasks, flags);
    }
}
