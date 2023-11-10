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
package net.hatemachine.mortybot.exception;

/**
 * CommandException is a runtime exception intended to be thrown by command implementations whenever they encounter
 * common, expected issues (e.g. not enough args) and should be handled by the command listener.
 */
public class CommandException extends RuntimeException {
    protected final CommandException.Reason reason;

    public enum Reason {
        IGNORED_USER,
        UNAUTHORIZED_USER
    }

    public CommandException(CommandException.Reason reason, String message) {
        this(reason, message, null);
    }

    public CommandException(CommandException.Reason reason, String message, Exception e) {
        super(message, e);
        this.reason = reason;
    }

    public CommandException.Reason getReason() {
        return this.reason;
    }
}
