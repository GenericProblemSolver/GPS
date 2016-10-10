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
package bytecode.examples.mathProblems;

import org.junit.Ignore;
import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

/**
 * ch. 6 problem 59 from Arthur Engel: Problem Solving Strategies, p. 132
 * Find the five-digit number abcde such that 4*abcde = edcba.
 * 
 * @author caspar
 *
 */
@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
@Ignore
public class MathProblem4 {

    @Constraint
    public static boolean constraint(int a, int b, int c, int d, int e) {
        if (a <= 0 || b < 0 || c < 0 || d < 0 || c < 0 || d < 0 || e <= 0) {
            return false;
        }
        if (a > 9 || b > 9 || c > 9 || d > 9 || e > 9) {
            return false;
        }
        return 4 * (a * 10000 + b * 1000 + c * 100 + d * 10 + e) == e * 10000
                + d * 1000 + c * 100 + b * 10 + a;
    }

}
