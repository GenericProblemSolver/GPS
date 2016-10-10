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
package bytecode.examples.superHardMathProblems;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.runner.RunWith;

import bytecode.AssertUnsatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

/**
 * The problem of finding an odd perfect number: https://en.wikipedia.org/wiki/Perfect_number
 * 
 * @author caspar
 *
 */
@RunWith(ConstraintProblemRunner.class)
@AssertUnsatisfiable //This is unknown as of May 13 2016
@Ignore
public class OddPerfection {

    @Constraint
    boolean isOddPerfectNumber(BigInteger n) {
        List<BigInteger> divisors = divisors(n);
        BigInteger sumOfDivisors = BigInteger.ZERO;
        for (BigInteger i : divisors) {
            sumOfDivisors.add(i);
        }
        return n.equals(sumOfDivisors);
    }

    public static List<BigInteger> divisors(BigInteger n) {
        ArrayList<BigInteger> divisors = new ArrayList<>();
        for (BigInteger i = BigInteger.ONE; i.compareTo(n) < 0; i
                .add(BigInteger.ONE)) {
            if (n.mod(i).equals(BigInteger.ZERO)) {
                divisors.add(i);
            }
        }
        return divisors;
    }
}
