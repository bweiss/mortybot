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

import net.hatemachine.mortybot.config.BotProperties;
import net.hatemachine.mortybot.listeners.CommandListener;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Interface for classes that want to implement a bot command. Any class implementing this is expected to have
 * the following fields defined: <code>event</code>, <code>source</code>, and <code>args</code>.
 * The <code>execute()</code> method should be overridden and contain the code to be executed for the command.<br/><br/>
 *
 * For a command to be made available to end users, at least one BotCommand annotation needs to also be present.
 *
 * @see BotCommand
 * @see CommandListener
 * @see CommandProxy
 */
public interface Command {
    /**
     * Executes whenever the command is triggered by an event.
     */
    void execute();

    /**
     * Gets the event that triggered the command.
     *
     * @return the event from the pircbotx event handler
     */
    GenericMessageEvent getEvent();

    /**
     * Gets the source of the command. This can be PUBLIC, PRIVATE, or DCC.
     *
     * @return the source of the command
     */
    CommandListener.CommandSource getSource();

    /**
     * Gets any arguments passed to the command.
     *
     * @return a list of arguments
     */
    List<String> getArgs();

    /**
     * Checks if a command is enabled in the bot's properties.
     *
     * @return true if the command is enabled
     * @see BotProperties
     */
    default boolean isEnabled() {
        String prop = BotProperties.getBotProperties().getStringProperty("commands.enabled");
        List<String> enabled = Arrays.asList(prop.split(","));
        return (enabled.contains(this.getClass().getSimpleName()));
    }
}
