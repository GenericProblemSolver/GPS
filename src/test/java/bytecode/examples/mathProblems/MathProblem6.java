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

import bytecode.AssertUnsatisfiable;
import org.junit.Ignore;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

/**
 * ch. 6 problem 76 from Arthur Engel: Problem Solving Strategies, p. 133
 * There is no m, m such that m^2+(m+1)^2=n^4+(n+1)^4.
 * 
 * @author caspar
 *
 */
@RunWith(ConstraintProblemRunner.class)
@AssertUnsatisfiable
@Ignore
public class MathProblem6 {

    @Constraint
    public static boolean constraint(int n, int m) {
        if (n < 1 || m < 1) {
            return false;
        }
        return (int) Math.pow(m, 2)
                + (int) Math.pow(m + 1, 2) == (int) Math.pow(n, 4)
                        + (int) Math.pow(n + 1, 4);
    }

}
