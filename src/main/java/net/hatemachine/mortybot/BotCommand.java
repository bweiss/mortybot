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

import java.lang.annotation.*;

/**
 * Runtime annotation for defining bot commands and their help text. Multiple annotations can be used to create
 * additional aliases for the command.
 *
 * @see BotCommands
 * @see Command
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(BotCommands.class)
public @interface BotCommand {
    /**
     * The name of the bot command. This is what determines the actual command available to end users.
     *
     * @return the command name
     */
    String name();

    /**
     * The class implementing the command.
     *
     * @return the command's implementation class
     */
    Class<?> clazz();

    /**
     * The help text for the command.
     *
     * @return an array of help text lines
     */
    String[] help();
}
