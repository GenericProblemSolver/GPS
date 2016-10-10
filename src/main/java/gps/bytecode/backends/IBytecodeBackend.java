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
package gps.bytecode.backends;

import java.util.*;
import java.util.Map.Entry;

import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Variable;
import gps.bytecode.symexec.SatisfactionProblem;

/**
 * Interface for the implementation of "backends", things that are able to solve
 * our functional bytecode representation
 * 
 * @author mfunk@tzi.de
 *
 */
public interface IBytecodeBackend {

    /**
     * Attempt to find a model for a SatisfactionProblem
     *
     * Model is assigned to the SatisfactionProblem as a SatisfactionproblemSolution
     * 
     * Returns SOLVED if problem is satifiable or unsatisfiable
     * Returns ERROR if an error occured
     * Returns UNABLE_TO_SOLVE if the backend is unable to solve this specific problem
     * 
     * @param problem
     * @return
     */
    public BackendResult solve(SatisfactionProblem problem);

    /**
     * Checks if the native/external components of this backend are available
     * @return
     */
    boolean isAvailable();

    public static enum BackendResult {
        SOLVED, ERROR, UNABLE_TO_SOLVE
    }

    /**
     * Represents a solution to a satisfaction problem. Representation of the
     * solution needs to be improved as time goes on
     *
     * Constructor is protected to restrict construction to actual implementers
     * of BytecodeBackend
     * 
     * @author mfunk
     */
    public static class SatisfactionProblemSolution {
        public final boolean satisfiable;
        public final String solution;

        public Map<Variable, Constant> variableValues = new HashMap<>();

        protected SatisfactionProblemSolution(boolean solvable, String desc) {
            satisfiable = solvable;
            solution = desc;
        }

        @Override
        public String toString() {
            StringBuilder sa = new StringBuilder();
            for (Entry<Variable, Constant> var : variableValues.entrySet()) {
                sa.append(var.getKey() + " = " + var.getValue() + "\n");
            }
            return sa.toString();
        }
    }

}
