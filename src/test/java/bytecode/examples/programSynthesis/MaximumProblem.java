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

/**
 * The problem of writing a BF-Program for computing the maximum of two numbers.
 * 
 * @author caspar
 *
 */
public class MaximumProblem extends SynthesisProblem {
    private int allowedTime;

    public MaximumProblem() {
        super();
        allowedTime = 10000;
    }

    public MaximumProblem(String pCode) {
        super(pCode);
        allowedTime = 10000;
    }

    @Override
    public boolean test() {
        char[] input = new char[2];
        char[] expOutput = new char[1];
        for (int first = 0; first <= 255; first++) {
            for (int second = 0; second <= 255; second++) {
                input[0] = (char) first;
                input[1] = (char) second;
                expOutput[0] = (char) (Math.max(((int) input[0]),
                        ((int) input[1])));
                //System.out.println(" first: "+(int) input[0] +"\n second: "+(int) input[1]+"\n expOut: "+(int) expOutput[0]);
                ComputationResult res = BFInterpreter.execute(new String(input),
                        BigInteger.valueOf(allowedTime), this.code);
                if (!res.hasHalted()) {
                    return false;
                }
                if (!res.getResult().equals(new String(expOutput))) {
                    return false;
                }
            }
        }
        return true;
    }

}
