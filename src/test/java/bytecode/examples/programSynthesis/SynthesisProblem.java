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

import gps.annotations.Variable;

/**
 * The problem of synthesizing a program with certain properties.
 * 
 * @author caspar
 *
 */
public abstract class SynthesisProblem {

    @Variable
    String code;

    public SynthesisProblem() {
        code = "                        ";
    }

    public SynthesisProblem(String pCode) {
        code = pCode;
    }

    public SynthesisProblem(int maxLength) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxLength; i++) {
            sb.append(" ");
        }
        code = sb.toString();
    }

    /**
     * Should return positive fitness values, since these are used to determine probabilities in the GA.
     * 
     * @param p
     * @param numbOfTests
     * @return
     */
    public abstract boolean test();
}
