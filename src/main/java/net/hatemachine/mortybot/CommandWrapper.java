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

/**
 * Wraps our command objects so that we can inject the command's class and pull in fields from the BotCommand annotations.
 */
public class CommandWrapper {

    private final String name;
    private final Class<?> cmdClass;
    private final String[] help;

    public CommandWrapper(String name, Class<?> cmdClass, String[] help) {
        this.name = name;
        this.cmdClass = cmdClass;
        this.help = help;
    }

    public String getName() {
        return name;
    }

    public Class<?> getCmdClass() {
        return cmdClass;
    }

    public String[] getHelp() {
        return help;
    }
}
