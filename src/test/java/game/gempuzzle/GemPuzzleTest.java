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
package game.gempuzzle;

import gps.GPS;
import gps.games.MemorySavingMode;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests a GemPuzzle problem.
 *
 * @author haker@uni-bremen.de
 */
public class GemPuzzleTest {

    private Game<?> game;

    @Before
    public void setUpGameInterface()
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, ClassNotFoundException {
        game = new Game<>(GPS.wrap(new GemPuzzle(3, 0)));
    }

    /**
     * Tests whether {@link gps.games.wrapper.Game#clone()} really provides
     * another instance.
     */
    @Test
    public void testClone() {
        assertEquals(game, game.copy());
        assertNotSame(game, game.copy());
    }

    /**
     * Tests whether all the methods return the expected results for the
     * GemPuzzle.
     */
    @Test
    public void testGameInterface() {
        assertTrue(game.hasActionMethod());
        //assertFalse(game.hasHeuristicMethod());
        assertFalse(game.hasPlayerMethod());
        assertTrue(game.hasTerminalMethod());
        assertFalse(game.hasUtilityMethod());
        assertTrue(game.hasSuccessorsMethod());

        List<Action> action = game.getActions();

        assertEquals(action.size(), 4);
        assertEquals(game.asRoot().getSuccessors(MemorySavingMode.NONE).size(),
                4);

        game.applyAction(action.get(1));
        assertEquals(game, game.asRoot().getSuccessors(MemorySavingMode.NONE)
                .get(1).getGame());
        assertNotSame(game, game.asRoot().getSuccessors(MemorySavingMode.NONE)
                .get(1).getGame());
    }

    /**
     * Test the solver for radix 3
     */
    @Test
    public void testGemPuzzleRdx3() {
        GemPuzzle m = new GemPuzzle(3, 0);
        new GPS<>(m);//.moves();
    }

    /**
     * Test the solver for radix 10
     */
    @Ignore
    @Test
    public void testGemPuzzleRdx10() {
        GemPuzzle m = new GemPuzzle(10, 0);
        new GPS<>(m).moves();
    }

    /**
     * Test the solver for radix 100
     */
    @Ignore
    @Test
    public void testGemPuzzleRdx100() {
        GemPuzzle m = new GemPuzzle(100, 0);
        new GPS<>(m).moves();
    }
}
