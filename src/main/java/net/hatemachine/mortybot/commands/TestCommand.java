/*
 * MortyBot - An IRC bot built on the PircBotX framework.
 * Copyright © 2022 Brian Weiss (brian@hatemachine.net)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.custom.entity.ManagedChannelFlag;
import net.hatemachine.mortybot.dao.BotUserDao;
import net.hatemachine.mortybot.dao.ManagedChannelDao;
import net.hatemachine.mortybot.dao.ManagedChannelUserDao;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.model.ManagedChannel;
import net.hatemachine.mortybot.model.ManagedChannelUser;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;

import static net.hatemachine.mortybot.custom.entity.ManagedChannelUserFlag.AUTO_OP;
import static net.hatemachine.mortybot.custom.entity.ManagedChannelUserFlag.AUTO_VOICE;

public class TestCommand implements BotCommand {

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public TestCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        var botUserDao = new BotUserDao();
        var managedChannelDao = new ManagedChannelDao();
        var managedChannelUserDao = new ManagedChannelUserDao();

        var managedChannel = new ManagedChannel();
        managedChannel.setName("#drunkards");
        managedChannel.setManagedChannelFlags(List.of(ManagedChannelFlag.AUTO_JOIN, ManagedChannelFlag.SHORTEN_LINKS));
        managedChannelDao.create(managedChannel);

        var managedChannelUser = new ManagedChannelUser();
        managedChannelUser.setBotUserId(botUserDao.getWithName("brian").getId());
        managedChannelUser.setManagedChannelId(managedChannelDao.getWithName("#drunkards").get(0).getId());
        managedChannelUser.setManagedChannelUserFlags(List.of(AUTO_OP, AUTO_VOICE));
        managedChannelUserDao.create(managedChannelUser);

        event.respondWith("Test completed");
    }

    @Override
    public GenericMessageEvent getEvent() {
        return event;
    }

    @Override
    public CommandListener.CommandSource getSource() {
        return source;
    }

    @Override
    public List<String> getArgs() {
        return args;
    }
}
