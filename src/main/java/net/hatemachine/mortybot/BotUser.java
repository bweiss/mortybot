package net.hatemachine.mortybot;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static net.hatemachine.mortybot.StringUtils.validateString;
import static net.hatemachine.mortybot.StringUtils.wildcardToRegex;

public class BotUser {

    private String name;
    private final Set<String> hostmasks = new HashSet<>();
    private boolean adminFlag;

    public BotUser(String name, String hostmask, boolean adminFlag) {
        this.name = validateString(name);
        this.hostmasks.add(validateString(hostmask));
        this.adminFlag = adminFlag;
    }

    public boolean hasMatchingHostmask(String userhost) {
        return hostmasks.stream()
                .anyMatch(h -> Pattern.matches(wildcardToRegex(h), userhost));
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

    public boolean isAdmin() {
        return this.adminFlag;
    }

    public void setAdminFlag(boolean adminFlag) {
        this.adminFlag = adminFlag;
    }
}
