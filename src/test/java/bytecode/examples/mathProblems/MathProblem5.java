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

import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import org.junit.Ignore;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

/**
 * ch. 6 problem 103 from Arthur Engel: Problem Solving Strategies, p. 134
 * Solve x^3-y^3 =xy + 61 in positive integers.
 * 
 * @author caspar
 *
 */
@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
@Ignore
public class MathProblem5 {

    @Constraint
    public static boolean constraint(int x, int y) {
        if (x < 1 || y < 1) {
            return false;
        }
        return (int) Math.pow(x, 3) - (int) Math.pow(y, 3) == x * y + 61;
    }

}
