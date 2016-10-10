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

import org.junit.Ignore;
import org.junit.runner.RunWith;

import bytecode.AssertUnsatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

/**
 * The problem of finding a counterexample to https://en.wikipedia.org/wiki/Fermat's_Last_Theorem
 * @author caspar
 *
 */
@RunWith(ConstraintProblemRunner.class)
@AssertUnsatisfiable
@Ignore
public class Fermat {

    int exponent;

    public Fermat(int pExponent) {
        if (pExponent > 2) {
            exponent = pExponent;
        } else {
            exponent = 3;
        }
    }

    @Constraint
    boolean isFermatRefusingTriplet(int a, int b, int c) {
        if (a < 1 || b < 1 || c < 1) {
            return false;
        }
        return (int) Math.pow(a, exponent)
                + (int) Math.pow(b, exponent) == (int) Math.pow(c, exponent);
    }
}
