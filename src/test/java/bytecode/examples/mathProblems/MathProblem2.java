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

import java.math.BigInteger;

import org.junit.Ignore;
import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import bytecode.examples.superHardMathProblems.Goldbach;
import gps.annotations.Constraint;

/**
 * The problem of finding a counter-example to the alleged prime-number generating formula
 * 2n^2 + 29. (See ch.4 of T. Kemperman: Zahlentheoretische Kostproben)
 * 
 * @author caspar
 *
 */
@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
@Ignore
public class MathProblem2 {

    @Constraint
    boolean refusesAllegedPrimeNumberFormula(int n) {
        if (n < 0) {
            return false;
        }
        int z = 2 * n * n + 29;
        return !Goldbach.isPrime(BigInteger.valueOf(z));
    }

}
