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
public class HeapTestLoop {

    static class P {
        int i;
    }

    @Constraint
    boolean test() {
        P p = null;
        for (int i = 0; i < 4; ++i) {
            p = new P();
            p.i = i;
        }
        return p.i == 3;
    }

    @Constraint
    boolean test2(int n) {
        P p = null;
        for (int i = 0; i < 4; ++i) {
            p = new P();
            p.i = i;
        }
        return p.i == n;
    }

}
