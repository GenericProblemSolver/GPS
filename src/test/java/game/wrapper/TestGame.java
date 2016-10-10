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
package game.wrapper;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import game.vectorracer.VectorRacer;
import gps.GPS;
import gps.annotations.Move;
import gps.annotations.Player;
import gps.annotations.TerminalTest;
import gps.annotations.Utility;
import gps.games.MemorySavingMode;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;

/**
 * @author alueck@uni-bremen.de
 */
public class TestGame {

    @Test
    public void testGetNewGame() {
        Game<VectorRacer> game = new Game<>(
                GPS.wrap(new VectorRacer(2, "vectorracer/track.bmp")));
        Action a = game.getActions().get(0);

        Game<VectorRacer> newGame = game.getNewGame(a,
                MemorySavingMode.ATTRIBUTE_GRAPH);
        assertSame(game.getProblem().getTrack(),
                newGame.getProblem().getTrack());

        newGame = game.getNewGame(a, MemorySavingMode.PREPROCESSING);
        assertSame(game.getProblem().getTrack(),
                newGame.getProblem().getTrack());
    }

    @Test
    public void testGetNewGameArrays() {
        Game<TestClass> game = new Game<>(GPS.wrap(new TestClass()));

        Game<TestClass> newGame = game.getNewGame(game.getActions().get(0),
                MemorySavingMode.ATTRIBUTE_GRAPH);
        assertNotSame(game.getProblem().array, newGame.getProblem().array);

        newGame = game.getNewGame(game.getActions().get(1),
                MemorySavingMode.ATTRIBUTE_GRAPH);
        assertSame(game.getProblem().array, newGame.getProblem().array);

        newGame = game.getNewGame(game.getActions().get(0),
                MemorySavingMode.PREPROCESSING);
        assertNotSame(game.getProblem().array, newGame.getProblem().array);

        newGame = game.getNewGame(game.getActions().get(1),
                MemorySavingMode.PREPROCESSING);
        assertSame(game.getProblem().array, newGame.getProblem().array);
    }

    public class TestClass {

        private String string1 = "string1";

        private String string2 = "string2";

        public String[][] array = { { string1, string2 },
                { string1, string2 } };

        @TerminalTest
        public Boolean terminalTest = false;

        @Player
        public int getPlayer() {
            return 2;
        }

        @Utility
        public float utility(Integer b) {
            return b.floatValue();
        }

        @gps.annotations.Action
        public List<Integer> getAction() {
            return Arrays.asList(0, 1);
        }

        @Move
        public void move(Integer move) {
            if (move == 0) {
                array[1][1] = string1;
            }
        }

    }
}
