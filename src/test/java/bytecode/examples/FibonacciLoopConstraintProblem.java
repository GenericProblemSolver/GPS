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
package bytecode.examples;

import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

/**
 * 
 * @author caspar
 *
 */
@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class FibonacciLoopConstraintProblem {

    //Find an n such that the nth Fibonacci number is divisible by 17
    @Constraint
    boolean constraintWithRecursion(int n) {
        int value1 = 1;
        int value2 = 1;
        int newVal = 1;
        for (int i = 0; i < n; i++) {
            newVal = value1 + value2;
            value1 = value2;
            value2 = newVal;
        }
        return newVal % 17 == 0;
    }
}
