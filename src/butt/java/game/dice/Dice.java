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

import java.util.Random;

import gps.annotations.Move;
import gps.annotations.TerminalTest;
import gps.annotations.Utility;
import gps.util.IButtSampleProblem;

/**
 * A simple game of dice. The game is over when the player rolls the same 
 * number thrice.
 * 
 * @author wahler@uni-bremen.de
 */
public class Dice implements IButtSampleProblem {

    /**
     * the random number generator for this Dice
     */
    final private Random r;

    /**
     * the saved rolls
     */
    final /* private */ int[] rolls;

    /**
     * Creates a new game of Dice.
     */
    public Dice() {
        r = new Random(1); // 4 5 2 4 3 5 3 5 5 5
        rolls = new int[3];
    }

    /**
     * Rolls a Die. If the first roll was already taken and the second roll is
     * different, the second one gets deleted and the first one gets the value
     * of the 2nd. Same for the third.
     */
    @Move
    public void roll() {
        if (rolls[0] == 0) {
            rolls[0] = r.nextInt(6) + 1;
        } else if (rolls[1] == 0) {
            rolls[1] = r.nextInt(6) + 1;
            if (rolls[1] != rolls[0]) {
                rolls[0] = rolls[1];
                rolls[1] = 0;

            }
        } else if (rolls[2] == 0) {
            rolls[2] = r.nextInt(6) + 1;
            if (rolls[1] != rolls[2]) {
                rolls[0] = rolls[2];
                rolls[1] = rolls[2] = 0;
            }
        }
    }

    /**
     * Rolls a Die. If the first roll was already taken and the second roll is
     * different, the second one gets deleted and the first one gets the value
     * of the 2nd. Same for the third.
     * 
     * The player can choose one of two dice before each roll. The 2nd die has a
     * bigger range.
     * 
     * @param which
     *            chooses one of two dice
     */
    @Move
    public void roll(final int which) {
        int range;
        if (which != 1 && which != 2) {
            range = 500;
        } else if (which == 1) {
            range = 6;
        } else {
            range = 12;
        }

        if (rolls[0] == 0) {
            rolls[0] = r.nextInt(range) + 1;
        } else if (rolls[1] == 0) {
            rolls[1] = r.nextInt(range) + 1;
            if (rolls[1] != rolls[0]) {
                rolls[0] = rolls[1];
                rolls[1] = 0;

            }
        } else if (rolls[2] == 0) {
            rolls[2] = r.nextInt(range) + 1;
            if (rolls[1] != rolls[2]) {
                rolls[0] = rolls[2];
                rolls[1] = rolls[2] = 0;
            }
        }
    }

    /**
     * Checks if all three saved values are the same.
     * 
     * @return <code>true</code>, if all saved values are the same,
     *         <code>false</code> otherwise
     */
    @TerminalTest
    public boolean winCheck() {
        return rolls[0] != 0 && rolls[0] == rolls[1] && rolls[1] == rolls[2];
    }

    /**
     * Return 1 if winCheck is <code>true</code>.
     * 
     * @return 1 if winCheck is <code>true</code>, 0 otherwise
     */
    @Utility
    public int utility() {
        return winCheck() ? 1 : 0;
    }

    @Override
    public Object getIdentifier() {
        return "Dice";
    }
}
