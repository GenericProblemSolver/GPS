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
package game.dice;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DiceTest {

    private Dice d;

    @Before
    public void setup() {
        d = new Dice();
    }

    /**
     * Checks if the game is won after 10 rolls (which is the case for the seed
     * 1).
     */
    @Test
    public void winAfter10() {
        for (int i = 10; i > 0; i--) {
            d.roll(1);
            System.out.println("[" + d.rolls[0] + "][" + d.rolls[1] + "]["
                    + d.rolls[2] + "]");
        }
        assertTrue(d.winCheck());
    }

    /**
     * Checks if the game is won after 13 rolls (which is the case for the seed
     * 1 with bigger range).
     */
    @Test
    public void winAfter13() {
        for (int i = 13; i > 0; i--) {
            d.roll(2);
            System.out.println("[" + d.rolls[0] + "][" + d.rolls[1] + "]["
                    + d.rolls[2] + "]");
        }
        assertTrue(d.winCheck());
    }
}
