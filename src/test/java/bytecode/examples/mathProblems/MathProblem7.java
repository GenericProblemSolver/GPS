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
import org.junit.Ignore;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

/**
 * ch. 6 problem 97 from Arthur Engel: Problem Solving Strategies, p. 134
 * Find the last n digits of b^(k^k);
 * This is annoying to calculate directly, but there are fairy simple tricks to solve it without doing so.
 * 
 * @author caspar
 *
 */
@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
@Ignore
public class MathProblem7 {

    int k;

    int n;

    int b;

    public MathProblem7() {
        k = 99999999;
        n = 10;
        b = 17;
    }

    @Constraint
    public boolean constraint(int x) {
        int correctVal = 1;
        for (BigInteger i = BigInteger.ZERO; i.compareTo(
                BigInteger.valueOf(k).pow(k)) < 0; i.add(BigInteger.ZERO)) {
            correctVal *= b;
            correctVal %= (int) Math.pow(n, 10);
        }
        return x == correctVal;
    }

}
