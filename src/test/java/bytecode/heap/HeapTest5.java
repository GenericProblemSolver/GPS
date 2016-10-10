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
import gps.annotations.Variable;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class HeapTest5 {
    @Variable
    int i;

    @Variable
    float f;

    @Constraint
    boolean test1() {
        return i < 4;
    }

    @Constraint
    boolean test2() {
        return i > 2;
    }

    @Constraint
    boolean test3() {
        return f < 17;
    }

    @Constraint
    boolean test4() {
        return f > 8;
    }

    @Override
    public String toString() {
        return "i: " + i + "\nf: " + f;
    }

    @Constraint
    boolean test5(int n) {
        float[] floats = { 5.2f, 6.7f };
        int[] ints = new int[2];
        for (int i = 0; i < ints.length; ++i) {
            ints[i] = (int) (n * floats[i]);
        }
        return ints[0] + ints[1] > 5;
    }

    @Constraint
    boolean test6(int n) {
        short[] shorts = { 17, 26 };
        int[] ints = new int[2];
        for (int i = 0; i < ints.length; ++i) {
            ints[i] = (int) (n * shorts[i]);
        }
        return ints[0] + ints[1] > 5;
    }

    static class P {
        int i;
        float f;
        boolean b;
        short s;
        long l;
        double d;
        P r;
    }

    @Constraint
    boolean test7() {
        P[] p = new P[2];
        p[0] = new P();
        p[1] = new P();
        if (p[0].i != p[1].i) {
            return false;
        }
        if (p[0].f != p[1].f) {
            return false;
        }
        if (p[0].b != p[1].b) {
            return false;
        }
        if (p[0].s != p[1].s) {
            return false;
        }
        if (p[0].l != p[1].l) {
            return false;
        }
        if (p[0].d != p[1].d) {
            return false;
        }
        if (p[0].r != p[1].r) {
            return false;
        }
        return true;
    }
}
