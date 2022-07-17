package net.hatemachine.mortybot.listeners;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ExceptionEvent;
import org.pircbotx.hooks.events.ListenerExceptionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener responsible for logging exception events from PircBotX.
 */
public class ExceptionListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ExceptionListener.class);

    @Override
    public void onException(ExceptionEvent event) {
        log.error("Exception encountered: {}", event.getMessage(), event.getException());
    }

    @Override
    public void onListenerException(ListenerExceptionEvent event) {
        log.error("ListenerException encountered: {}", event.getMessage(), event.getException());
    }
}
