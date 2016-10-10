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
public class TestArrayRuntimeType {

    int[] a = new int[3];
    int[][] b = new int[3][3];

    @Constraint
    boolean test() {
        Integer[] a = new Integer[2];
        return a instanceof Integer[];
    }

    @Constraint
    boolean test2() {
        int[] a = new int[2];
        return a instanceof int[];
    }

    @Constraint
    boolean test3() {
        int[][] a = new int[1][1];
        return a instanceof int[][];
    }

    @Constraint
    boolean test4() {
        return b instanceof int[][];
    }

    @Constraint
    boolean test5() {
        return a instanceof int[];
    }
}
