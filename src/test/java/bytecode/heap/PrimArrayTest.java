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
import gps.annotations.Constraint;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class PrimArrayTest {

    @Constraint
    public boolean test1(int i) {
        int[] array = { 0, 1, 2, 3 };
        return i == array[3];
    }

    @Constraint
    public boolean test2(int i) {
        int[] array = new int[4];
        for (int x = 0; x < 4; x++) {
            array[x] = x;
        }
        return i == array[3];
    }

    @Constraint
    public boolean lengthTest(int i) {
        int[] array = new int[7];
        return i == array.length;
    }
}
