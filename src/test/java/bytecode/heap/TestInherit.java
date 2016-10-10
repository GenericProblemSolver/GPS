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
public class TestInherit {

    static class P {
        int i1;
        int f1;
    }

    static class Q extends P {
        int i2;
        int i3;
    }

    static class R extends Q {
        int i1;
    }

    Q q1 = new Q();

    @Constraint
    boolean test(int n) {
        Q q = new Q();
        q.i1 = 17;
        q.i2 = n;
        return q.i1 == q.i2;
    }

    @Constraint
    boolean test2(int n) {
        Q q = new Q();
        P p = q;
        p.i1 = n;
        return q.i1 == p.i1;
    }

    @Constraint
    boolean test3(int n) {
        Q q = new R();
        P p = q;
        p.i1 = n;
        return q.i1 == p.i1;
    }

    @Constraint
    boolean test4() {
        R r = new R();
        P p = r;
        p.i1 = 18;
        return r.i1 != 18;
    }

    @Constraint
    boolean testRealWorld(int n) {
        return q1.i1 == n;
    }
}
