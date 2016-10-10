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
public class CompareTest {

    // This test is so trivial because the online Z3 solver times out really
    // fast and I am too lazy to install Z3 on this PC right now.
    @Constraint
    boolean test1(double x) {
        if (x > 10) {
            return x == 37;
        } else {
            return x - 1 == 0;
        }
    }

    @Constraint
    boolean test2(long l) {
        if (l * l > 10) {
            return l + l == 13;
        } else {
            return l - 3 == 0;
        }
    }

    @Constraint
    boolean test3(double c) {
        return c > 4;
    }
}
