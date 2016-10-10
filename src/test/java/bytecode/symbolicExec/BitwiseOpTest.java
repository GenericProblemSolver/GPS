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
public class BitwiseOpTest {

    @Constraint
    public boolean test(int a) {
        // looking for a=2
        return ((6 & a) == 2); // && ((1 << 1 | a) == 2) && ((12 >> 1 ^ a)
        // == 4);
    }

    @Constraint
    public boolean test2(int a, int b) {
        return ((6 & a) + ((5 & b) - 2) == 2);
    }

    @Constraint
    public boolean test3(int a) {
        // a == 1
        return ((6 & a) + (5 & a) + (7 & a) == 2);
    }

    @Constraint
    public boolean testXor(int a) {
        // a = 3
        return (a ^ 2) == 1;
    }

}
