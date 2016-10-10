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
import org.junit.runner.RunWith;

import bytecode.AssertUnsatisfiable;
import org.junit.Ignore;
import bytecode.ConstraintProblemRunner;
import bytecode.examples.superHardMathProblems.Goldbach;
import gps.annotations.Constraint;

/**
 * A class of factorization problems inspired by
 * E1 from Arthur Engel: Problem Solving Strategies, p. 121
 * n^4+4^n is never a prime for n>1.
 * 
 * @author caspar
 *
 */
@RunWith(ConstraintProblemRunner.class)
@AssertUnsatisfiable
@Ignore
public class SophieGermainPrimeProblem {

    @Constraint
    boolean isDivisor(int n) {
        if (n <= 1) {
            return false;
        }
        return Goldbach.isPrime(
                BigInteger.valueOf(n).pow(4).add(BigInteger.valueOf(4).pow(n)));
    }

}
