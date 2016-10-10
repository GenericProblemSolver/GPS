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
 * Result of a BF computation.
 * 
 * @author caspar
 *
 */
public class ComputationResult {
    final private String output;

    final private BigInteger numbOfSteps;

    /**
     * False if the program was halted prematurely.
     */
    final private boolean halted;

    public ComputationResult(final String output, final BigInteger numbOfSteps,
            final boolean pHalted) {
        if (output == null || numbOfSteps == null
                || numbOfSteps.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException();
        }
        this.output = output;
        this.numbOfSteps = numbOfSteps;
        halted = pHalted;
    }

    public ComputationResult(final String output, final boolean pHalted) {
        if (output == null) {
            throw new IllegalArgumentException();
        }
        this.output = output;
        this.numbOfSteps = BigInteger.ONE.negate();
        halted = pHalted;
    }

    public String getResult() {
        return output;
    }

    public BigInteger getNumbOfSteps() {
        if (numbOfSteps.equals(BigInteger.ONE.negate())) {
            throw new IllegalStateException(
                    "This computation result does not contain a number of computation steps.");
        }
        return numbOfSteps;
    }

    public boolean hasHalted() {
        return halted;
    }
}
