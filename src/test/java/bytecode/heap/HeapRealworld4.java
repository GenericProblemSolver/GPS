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
public class HeapRealworld4 {
    boolean[] b = { false, false, true, false, true };

    char[] c = { 'a', 'b', 'c' };

    short[] s = { 5, 6, 7 };

    long[] l = { 5, 6, 7 };

    float[] f = { 5, 6, 7 };

    double[] d = { 5, 6, 7 };

    int[] i = { 5, 6, 7 };

    @Constraint
    boolean test(int n) {
        b[2] = false;
        return b[n];
    }

    @Constraint
    boolean test2(int n) {
        return c[n] == 'b';
    }

    @Constraint
    boolean test3(int n) {
        return s[n] == 6;
    }

    @Constraint
    boolean test4(int n) {
        return l[n] == 6;
    }

    @Constraint
    boolean test5(int n) {
        return f[n] == 6;
    }

    @Constraint
    boolean test6(int n) {
        return d[n] == 6;
    }

    @Constraint
    boolean test7(int n) {
        return i[n] == 6;
    }
}
