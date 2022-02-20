/*
 * MortyBot - An IRC bot built on the PircBotX framework.
 * Copyright © 2022 Brian Weiss (brianmweiss@gmail.com)
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
package net.hatemachine.mortybot.commands;

import net.hatemachine.mortybot.BotCommand;
import net.hatemachine.mortybot.listeners.CommandListener;
import net.hatemachine.mortybot.listeners.WordleGameListener;
import net.hatemachine.mortybot.wordle.WordleGame;
import net.hatemachine.mortybot.wordle.WordleHelper;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;

import static net.hatemachine.mortybot.listeners.CommandListener.CommandSource.PRIVATE;
import static net.hatemachine.mortybot.listeners.CommandListener.CommandSource.PUBLIC;

public class WordleCommand implements BotCommand {

    private final GenericMessageEvent event;
    private final CommandListener.CommandSource source;
    private final List<String> args;

    public WordleCommand(GenericMessageEvent event, CommandListener.CommandSource source, List<String> args) {
        this.event = event;
        this.source = source;
        this.args = args;
    }

    @Override
    public void execute() {
        if (source == PRIVATE) {
            event.respondWith("Sorry, not supported over private message yet");
        } else if (source == PUBLIC) {
            MessageEvent myEvent = (MessageEvent)event;
            WordleHelper helper = new WordleHelper(event.getBot());
            WordleGameListener matchingListener = null;

            // see if we have any listeners for this user and channel
            for (WordleGameListener listener : helper.getListeners()) {
                WordleGame game = listener.getGame();
                if (event.getUser() == game.getPlayer() && myEvent.getChannel() == game.getChannel()) {
                    matchingListener = listener;
                }
            }

            if (matchingListener != null) {
                WordleGame game = matchingListener.getGame();
                if (game.isActive()) {
                    myEvent.respondWith("You already have an active game. Say a word in the channel to make a guess.");
                    game.printScoreBoard();
                    game.printKeyboard();
                    return;
                } else {
                    // remove old listener
                    myEvent.getBot().getConfiguration().getListenerManager().removeListener(matchingListener);
                }
            }

            // start a new game
            WordleGameListener listener = new WordleGameListener(myEvent.getBot(), myEvent.getUser(), myEvent.getChannel());
            myEvent.getBot().getConfiguration().getListenerManager().addListener(listener);
        }
    }

    @Override
    public GenericMessageEvent getEvent() {
        return event;
    }

    @Override
    public CommandListener.CommandSource getSource() {
        return source;
    }

    @Override
    public List<String> getArgs() {
        return args;
    }
}
