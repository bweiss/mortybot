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

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreHooksListener.class);

    @Override
    public void onVersion(final VersionEvent event) {
        LOGGER.debug("VersionEvent: {}", event);
        event.respond("VERSION MortyBot " + MortyBot.VERSION);
    }
}
