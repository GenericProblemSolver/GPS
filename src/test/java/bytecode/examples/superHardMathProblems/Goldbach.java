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

import org.junit.Ignore;
import org.junit.runner.RunWith;

import bytecode.AssertUnsatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

/**
 * The problem of refuting the Goldbach conjecture. https://en.wikipedia.org/wiki/Goldbach's_conjecture
 * 
 * @author caspar
 *
 */
@RunWith(ConstraintProblemRunner.class)
@AssertUnsatisfiable //This is unknown as of May 13 2016
@Ignore
public class Goldbach {

    @Constraint
    boolean violatesGoldbach(BigInteger n) {
        if (n.mod(BigInteger.ONE.add(BigInteger.ONE)).equals(BigInteger.ONE)) {
            return false;
        }
        for (BigInteger i = BigInteger.ONE; i.compareTo(n) < 0; i
                .add(BigInteger.ONE)) {
            if (isPrime(i)) {
                for (BigInteger j = BigInteger.ONE; j.compareTo(n) < 0; j
                        .add(BigInteger.ONE)) {
                    if (isPrime(j)) {
                        if (i.add(j).equals(n)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean isPrime(BigInteger p) {
        return OddPerfection.divisors(p).size() == 1;
    }

}
