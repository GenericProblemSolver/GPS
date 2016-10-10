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
package game.minesweeper;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class MinesweeperTest {

    Minesweeper test;

    @Before
    public void setup() {
        test = new Minesweeper(10, 10, 60, true, 1);
    }

    @Test
    public void test() {
        // An empty board has 100 covered fields
        // And the first action can only be a click
        assertEquals(test.actions().size(), 100);
        test.move(0 | (0 << 6));

        // This seed opens at [0][0] 4 fields (with populate2)
        // 96 fields are covered and every one of them
        // can be clicked or flagged
        System.out.println(test.actions().size());
        assertEquals(test.actions().size(), 192);
    }
}
