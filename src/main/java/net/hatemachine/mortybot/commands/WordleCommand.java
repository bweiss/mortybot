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
import net.hatemachine.mortybot.wordle.Game;
import net.hatemachine.mortybot.wordle.GameManager;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;
import java.util.Optional;

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
        GameManager gm = GameManager.getGameManager();
        Optional<Game> optGame = gm.getGame(event.getUser());
        if (optGame.isPresent()) {
            Game game = optGame.get();
            if (game.isActive()) {
                game.showAllTried(event);
                game.showLetters(event);
            } else {
                gm.removeGame(game);
                startGame();
            }
        } else {
            startGame();
        }
    }

    private void startGame() {
        var gm = GameManager.getGameManager();
        var game = new Game(event.getUser());
        gm.addGame(game);
        event.respond(String.format("Starting a game of Wordle. You have %d attempts.", game.getMaxAttempts()));
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
