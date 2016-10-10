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
package bytecode.symbolicExec;

import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class SomeMoreTests {
    @Constraint
    boolean testOdd(int x) {
        if (x % 2 == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Constraint
    boolean testMax(int x) {
        return x * x - 7 * x - 5 >= 5;
    }

    @Constraint
    boolean test2(int x, int y) {
        if (x * y > 10) {
            return x + y > 10;
        } else {
            return y - x < 0;
        }
    }

    @Constraint
    boolean test3(int x) {
        if (x > 0) {
            return x == 17;
        }
        int a = x + 17;
        if (a < 17) {
            a -= 5;
            return a == 7;
        } else {
            return x > 6;
        }
    }
}
