package net.hatemachine.mortybot.util;

import org.pircbotx.Channel;

public class IrcUtils {

    private IrcUtils() {}

    /**
     * Find out if a user has operator status on a channel.
     *
     * @param targetUser the user to check for operator status
     * @param channel the channel you want to check
     * @return true if user is oped on channel
     */
    public static boolean userHasOps(String targetUser, Channel channel) {
        return channel.getUsers()
                .stream()
                .anyMatch(u -> u.getNick().equalsIgnoreCase(targetUser));
    }
}
