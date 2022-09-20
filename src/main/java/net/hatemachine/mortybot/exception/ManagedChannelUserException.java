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

public class ManagedChannelUserException extends Exception {

    protected final ManagedChannelUserException.Reason reason;

    public enum Reason {
        NO_SUCH_RECORD("No such record"),
        RECORD_EXISTS("Record exists");

        private final String description;

        Reason(String desc) {
            this.description = desc;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    public ManagedChannelUserException(ManagedChannelUserException.Reason reason, String message) {
        this(reason, message, null);
    }

    public ManagedChannelUserException(ManagedChannelUserException.Reason reason, String message, Exception e) {
        super(reason + ": " + message, e);
        this.reason = reason;
    }

    public ManagedChannelUserException.Reason getReason() {
        return this.reason;
    }
}
