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
public class MathProblems {

    int a = 2;
    int b = 3;
    int c = 4;
    int d = 7;
    int greaterThan = 1000;
    int smallerThan = 50;
    int equal = 500;

    @Constraint
    public boolean polynomialGreater(int x) {
        return a * x * x * x + b * x * x + c * x + d > greaterThan;
    }

    @Constraint
    public boolean polynomialSmaller(int x) {
        return a * x * x * x + b * x * x + c * x + d < smallerThan;
    }

    //not considered so that a model will be produced
    public boolean polynomialEqual(int x) {
        return a * x * x * x + b * x * x + c * x + d == equal;
    }

    // Requires invokes to work
    public boolean exponentialGreater(double x) {
        return Math.pow(a, x) > greaterThan;
    }

    // Requires invokes to work
    public boolean exponentialSmaller(double x) {
        return Math.pow(a, x) < smallerThan;
    }

    // Requires invokes to work
    public boolean exponentialEqual(double x) {
        return Math.pow(a, x) == equal;
    }

    @Constraint
    public boolean linearGreater(int x) {
        return a * x + b > greaterThan;
    }

    @Constraint
    public boolean linearSmaller(int x) {
        return a * x + b < smallerThan;
    }

    //not considered so that a model will be produced
    public boolean linearEqual(int x) {
        return a * x + b == equal;
    }

}
