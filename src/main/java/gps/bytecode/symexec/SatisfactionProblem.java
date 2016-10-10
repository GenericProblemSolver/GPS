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
package gps.bytecode.symexec;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gps.bytecode.backends.IBytecodeBackend.SatisfactionProblemSolution;
import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Variable;
import gps.bytecode.symexec.HeapContext.InsertPoint;

/**
 *
 * This class is the main entry point to the symbolic execution backend.
 * 
 * TODO: Implement! NOTE: On our whiteboard diagram we specified that this class
 * should also have a member that saves the object this symbolic execution
 * started on. I do not see how this could be helpful (or even sensible). If you
 * know, please add with comments.
 * 
 * @author jowlo@uni-bremen.de
 * 
 */
public class SatisfactionProblem {

    /**
     * All constraints as functional expressions that were formulated from the
     * original object.
     */
    public List<SEFunction> constraints;

    /**
     * A solution that can be assigned by a solver
     */
    public SatisfactionProblemSolution solution;

    // Maps the free Variables in this problem to their origins
    public final Map<Variable, InsertPoint> problemVariables;

    // Constructor is Package-private since it is only sensible to get an
    // instance from
    // SymbolicExec
    SatisfactionProblem(final List<SEFunction> constraints,
            Map<Variable, InsertPoint> variables) {
        this.constraints = constraints;
        this.problemVariables = variables;
    }

    /**
     * Assigns a Solution from a Backend to this Problem
     * 
     * Since the Solution can be constructed just by the Backends, noone else
     * can use this method
     * 
     * @param sol
     */
    public void assignSolution(SatisfactionProblemSolution sol) {
        this.solution = sol;

        //Insert the values of the model into the problem object
        for (Entry<Variable, Constant> var : sol.variableValues.entrySet()) {
            problemVariables.get(var.getKey()).put(var.getValue());
        }
    }

    /**
     * Returns whether all Constraints can be satisfied with the same Values
     * 
     * @return
     */
    public boolean isSatisfiable() {
        if (solution != null) {
            return solution.satisfiable;
        } else {
            return true;
        }
    }

    /**
     * Returns a String representation of the solution of the satisfaction
     * problem
     * 
     * Unstable, do not rely on it being in a specific format
     * 
     * @return
     */
    public String getSolution() {
        if (solution != null) {
            return solution.toString();
        } else {
            return "";
        }
    }
}
