/*
 * Copyright 2016  Generic Problem Solver Project
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package game.algorithm.mtdf;

import game.connect4.ConnectGame;
import gps.GPS;
import gps.games.GamesModule;
import gps.games.algorithm.mtdf.MTDf;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;
import org.junit.Test;

import java.util.Optional;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

/**
 * @author alueck@uni-bremen.de
 */
public class MTDfTest {

    public static void main(String[] args) {
        ConnectGame cgame = ConnectGame.createConnect4();
        System.out.println(
                cgame + "\n current player: " + cgame.getCurrentPlayer());
        Game<ConnectGame> game = new Game<>(GPS.wrap(cgame));
        GamesModule<ConnectGame> mod = new GamesModule<>(game);
        mod.setDepthlimit(8);
        MTDf<ConnectGame> alg = new MTDf<>(mod);
        try (Scanner in = new Scanner(System.in)) {
            while (!game.isTerminal()) {
                Optional<Action> bestMove = alg.bestMove();
                if (bestMove.isPresent()) {
                    game.applyAction(bestMove.get());
                } else {
                    System.out.println("Algorithm did not find a best move.");
                    break;
                }
                System.out.println(game);
                if (game.isTerminal()) {
                    break;
                }
                System.out.println("Where do you want to place your disc?");
                String number = in.next();
                game.applyAction(new Action(Integer.valueOf(number)));
                System.out.println(game);
                mod = new GamesModule<>(game);
                mod.setDepthlimit(8);
                alg = new MTDf<>(mod);
            }
        }
    }

    @Test
    public void test() {
        ConnectGame cgame = ConnectGame.createConnect4();
        Game<ConnectGame> game = new Game<>(GPS.wrap(cgame));
        GamesModule<ConnectGame> mod = new GamesModule<>(game);
        mod.setDepthlimit(4);
        MTDf<ConnectGame> alg = new MTDf<>(mod);
        while (!game.isTerminal()) {
            game.applyAction(alg.bestMove().get());
            mod = new GamesModule<>(game);
            mod.setDepthlimit(4);
            alg = new MTDf<>(mod);
        }
        assertEquals(-1, game.getPlayer().get());
        System.out.println(game);
    }
}
