package net.hatemachine.mortybot.listeners;

import net.hatemachine.mortybot.MortyBot;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.VersionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionListener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionListener.class);

    @Override
    public void onVersion(final VersionEvent event) {
        LOGGER.debug("VersionEvent: {}", event);
        event.respond("VERSION MortyBot " + MortyBot.VERSION);
    }
}
