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

import bytecode.AssertUnsatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

/**
 * E12 from Arthur Engel: Problem Solving Strategies, p. 124
 * The equation x^2 + y^2 + z^2 = 2xyz has no integral solutions except (0,0,0)
 * 
 * @author caspar
 *
 */
@RunWith(ConstraintProblemRunner.class)
@AssertUnsatisfiable
@Ignore
public class MathProblem3 {

    @Constraint
    public static boolean constraint(int a, int b, int c) {
        if (a == 0 && b == 0 && c == 0) {
            return false;
        }
        return a * a + b * b + c * c == 2 * a * b * c;
    }

}
