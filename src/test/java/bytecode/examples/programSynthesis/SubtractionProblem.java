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
package bytecode.examples.programSynthesis;

import java.math.BigInteger;

import gps.annotations.Constraint;

/**
 * The problem of writing a BF program that substracts one number from another.
 * 
 * @author caspar
 *
 */
public class SubtractionProblem extends SynthesisProblem {

    private int allowedTime;

    public SubtractionProblem() {
        super();
        allowedTime = 10000;
    }

    public SubtractionProblem(String pCode) {
        super(pCode);
        allowedTime = 10000;
    }

    @Constraint
    @Override
    public boolean test() {
        char[] input = new char[2];
        char[] expOutput = new char[1];
        for (int minuend = 0; minuend < 254; minuend++) {
            for (int subtrahend = 0; subtrahend <= minuend; subtrahend++) {

                input[0] = (char) minuend;
                input[1] = (char) subtrahend;
                //System.out.println("input: "+((int) input[0])+","+((int) input[1]));
                expOutput[0] = (char) (input[0] - input[1]);
                String result = BFInterpreter
                        .execute(new String(input),
                                BigInteger.valueOf(allowedTime), code)
                        .getResult();
                if (!result.equals(new String(expOutput))) {
                    return false;
                }
            }
        }

        return true;
    }

}
