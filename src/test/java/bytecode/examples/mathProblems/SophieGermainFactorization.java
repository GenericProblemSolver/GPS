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

import bytecode.AssertSatisfiable;
import org.junit.Ignore;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

/**
 * A class of factorization problems inspired by
 * E2 from Arthur Engel: Problem Solving Strategies, p. 121
 * Numbers of the scheme a^4 + 4*(b^n)^4 are never prime.
 * Easy to do without much calculation, if you know/utilize Sophie Germain's identity.
 * 
 * @author caspar
 *
 */
@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
@Ignore
public class SophieGermainFactorization {

    int a;

    int b;

    int n;

    public SophieGermainFactorization() {
        a = 421337;
        b = 42;
        n = 1337;
    }

    public SophieGermainFactorization(int pA, int pN, int pB) {
        a = pA;
        n = pN;
        b = pB;
    }

    @Constraint
    boolean isDivisor(BigInteger d) {
        BigInteger summand1 = BigInteger.valueOf(a).pow(4);
        BigInteger summand2 = BigInteger.valueOf(b).pow(n).pow(4)
                .multiply(BigInteger.valueOf(4));

        BigInteger toDivide = summand1.add(summand2);

        if (d.compareTo(BigInteger.ONE) <= 0) {
            return false;
        }
        return toDivide.mod(d).equals(BigInteger.ZERO);
    }

}
