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
package bytecode.heap;

import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class MultiRefArrayTest {

    public boolean multiATest(Integer i) {
        Integer[][] array = new Integer[5][5];
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                array[x][y] = x * y;
            }
        }
        return i == array[3][4];
    }
}
