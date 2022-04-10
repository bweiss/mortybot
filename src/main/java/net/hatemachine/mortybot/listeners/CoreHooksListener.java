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
package net.hatemachine.mortybot.listeners;

import net.hatemachine.mortybot.MortyBot;
import org.pircbotx.hooks.CoreHooks;
import org.pircbotx.hooks.events.VersionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replacement class for PircBotX's CoreHooks listener.
 * This allows us to override the default PircBotX behavior for certain events (e.g. CTCP VERSION).
 */
public class CoreHooksListener extends CoreHooks {

    private static final Logger log = LoggerFactory.getLogger(CoreHooksListener.class);

    @Override
    public void onVersion(final VersionEvent event) {
        log.debug("VersionEvent: {}", event);
        event.respond("VERSION MortyBot " + MortyBot.VERSION);
    }
}
