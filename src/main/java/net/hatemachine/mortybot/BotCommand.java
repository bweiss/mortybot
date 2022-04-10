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
package net.hatemachine.mortybot;

import net.hatemachine.mortybot.config.BotState;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.Arrays;
import java.util.List;

public interface BotCommand {
    void execute();
    GenericMessageEvent getEvent();
    CommandListener.CommandSource getSource();
    List<String> getArgs();

    default String getName() {
        return this.getClass().getSimpleName();
    }

    default boolean isEnabled() {
        String prop = BotState.getBotState().getStringProperty("commands.enabled");
        List<String> enabled = Arrays.asList(prop.split(","));
        return (enabled.contains(this.getClass().getSimpleName()));
    }
}
