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
public class TestClassFloatDoubleLong {

    @Constraint
    boolean floatTest() {
        float a = (float) 1.2;
        float b = (float) 2.3;
        float c = a + b;
        return c * 3 > 0;
    }

    @Constraint
    boolean doubleTest() {
        double a = 1.2;
        double b = 2.3;
        double c = a + b;
        return c * 3 > 0;
    }

    @Constraint
    boolean longTest() {
        long a = 2200000;
        long b = 2301832;
        long c = a * b;
        a = c * 10;
        return a > 0;
    }
}
