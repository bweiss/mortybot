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

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;

import java.util.Objects;

/**
 * Core bot object class. This extends the {@link PircBotX} class and is how you interact with a bot instance.
 * To start a bot you must create an instance of this class and call the startBot() method.
 *
 * @see Main
 */
public class MortyBot extends PircBotX {

    public static final String VERSION = "0.9.0"; // automatically generated; do not change

    MortyBot(Configuration config) {
        super(config);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MortyBot bot = (MortyBot) o;
        return botId == bot.getBotId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), botId);
    }
}
