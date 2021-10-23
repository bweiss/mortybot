package net.hatemachine.mortybot;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import static net.hatemachine.mortybot.util.StringUtils.wildcardToRegex;

public class BotUser {

    private String name;
    private final Set<String> hostmasks = new HashSet<>();
    private boolean adminFlag;

    public BotUser(final String name, final String hostmask) {
        this(name, hostmask, false);
    }

    public BotUser(final String name, final String hostmask, final boolean adminFlag) {
        this.name = name;
        this.hostmasks.add(hostmask);
        this.adminFlag = adminFlag;
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

    public boolean getAdminFlag() {
        return adminFlag;
    }

    public void setAdminFlag(boolean adminFlag) {
        this.adminFlag = adminFlag;
    }

    @Override
    public String toString() {
        return "BotUser{" +
                ", name='" + name + '\'' +
                ", hostmasks=" + hostmasks +
                ", adminFlag=" + adminFlag +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotUser botUser = (BotUser) o;
        return name.equals(botUser.name) && hostmasks.equals(botUser.hostmasks) && adminFlag == botUser.adminFlag;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, hostmasks, adminFlag);
    }
}
