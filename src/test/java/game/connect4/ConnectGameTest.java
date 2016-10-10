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
package game.connect4;

import gps.GPS;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Various tests for the connect four game.
 *
 * @author haker@uni-bremen.de
 */
public class ConnectGameTest {

    /**
     * Test the standard connect 4 game.
     */
    @Test
    public void testStandardConnect4() {
        final ConnectGame p = ConnectGame.createConnect4();
        new GPS<>(p);
    }

    /**
     * Test a connect 6 variant.
     */
    @Test
    public void testConnect6() {
        final ConnectGame p = ConnectGame.createConnect6();
        new GPS<>(p);
    }

    /**
     * Test a multiple player variant of connect 4. This test features 4 players
     * in a standard connect 4 game.
     */
    @Test
    public void testConnect4Multi() {
        final ConnectGame p = new ConnectGame(4, 7, 6, 4);
        new GPS<>(p);
    }

    @Test
    public void testHeuristic() {
        ConnectGame cgame = ConnectGame.createConnect4();
        cgame.move(3);
        cgame.move(2);
        cgame.move(2);
        cgame.move(1);
        cgame.move(1);
        cgame.move(0);
        cgame.move(1);
        assertEquals(400, cgame.heuristic(0));
        cgame.move(0);
        cgame.move(0);
        assertEquals(600, cgame.heuristic(0));
        cgame.move(4);
        cgame.move(6);
        cgame.move(3);
        cgame.move(6);
        cgame.move(2);
        assertEquals(3500, cgame.heuristic(0));
    }

}