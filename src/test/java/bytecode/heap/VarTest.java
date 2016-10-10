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
public class VarTest {
    @Variable
    int i;

    @Variable
    char c;

    @Variable
    double d;

    @Variable
    float f;

    @Variable
    byte b;

    @Variable
    short s;

    @Variable
    long l;

    @Constraint
    boolean test() {
        if (i != 0) {
            return false;
        }
        if (c != 0) {
            return false;
        }
        if (d != 0) {
            return false;
        }
        if (f != 0) {
            return false;
        }
        if (b != 0) {
            return false;
        }
        if (s != 0) {
            return false;
        }
        if (l != 0) {
            return false;
        }
        return true;
    }

}