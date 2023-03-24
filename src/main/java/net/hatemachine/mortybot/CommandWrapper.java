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
    private final boolean restricted;
    private final String[] help;
    private Command instance;

    public CommandWrapper(String name, Class<?> cmdClass, boolean restricted, String[] help) {
        this.name = name;
        this.cmdClass = cmdClass;
        this.restricted = restricted;
        this.help = help;
        this.instance = null;
    }

    public String getName() {
        return name;
    }

    public Class<?> getCmdClass() {
        return cmdClass;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public String[] getHelp() {
        return help;
    }

    public Command getInstance() {
        return instance;
    }

    public void setInstance(Command instance) {
        this.instance = instance;
    }
}
