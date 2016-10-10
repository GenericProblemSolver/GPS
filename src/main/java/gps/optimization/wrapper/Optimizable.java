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
package gps.optimization.wrapper;

import java.util.List;

import gps.IWrappedProblem;
import gps.annotations.Constraint;

/**
 * Wraps the Optimizable interface provided by the preprocessing process to this class.
 * All optimization-algorithms can work with this class.
 * 
 * @author mburri
 *
 * @param <T>
 *            The type of the problem class
 */
public class Optimizable<T> {

    /**
     * The interface to the problem object.
     */
    private final IWrappedProblem<T> problem;

    /**
     * Construct a optimizable object from a wrapped interface
     * 
     * @param pProblem
     *          the wrapped problem
     */
    public Optimizable(final IWrappedProblem<T> pProblem) {
        if (pProblem == null) {
            throw new IllegalArgumentException("pProblem must not be null");
        }
        problem = pProblem;
    }

    /**
     * Evaluates a solution represented as a an object array 
     * in which every element is one parameter
     * 
     * @param params
     *          the parameters that are to be evaluated
     * @return
     *          the value of the solution
     */
    public double objectiveFunction(final Object[] params) {
        return (double) problem.objectiveFunction(params);
    }

    /**
     * Checks whether the {@link #objectiveFunction} method can be called safely.
     * 
     * @return 
     * 			{@code true} if the method is available 
     * 			{@code false} if not
     */
    public boolean hasObjectiveFunction() {
        return problem.hasObjectiveFunction();
    }

    /**
     * Gets the user defined initial values of the parameters 
     * that are to be optimized. 
     * 
     * @return
     * 			the initial values of the parameters that are to be optimized
     */
    public Object[] getDefaultParams() {
        return problem.getDefaultParams();
    }

    /**
     * Gets neighboring solutions (or only one) created by modifying a given 
     * solution.  
     * 
     * @param params
     * 			the solution that the neighboring solutions are to be obtained for
     * @return
     * 			the neighboring solutions
     */
    public List<Object[]> neighbor(final Object[] params) {
        return problem.neighbor(params);
    }

    /**
     * Checks whether the {@link #neighbor} method can be called safely.
     * 
     * @return 
     * 			{@code true} if the method is available 
     * 			{@code false} if not
     */
    public boolean hasNeighborFunction() {
        return problem.hasNeighborFunction();
    }

    /**
     * Sets the threshold that the {@link #objectiveFunction(Object[])} is to 
     * exceed. Used by the {@link gps.optimization.algorithm.incrementalSat.IncrementalSat} solver
     * and the {@link gps.bytecode.interfaces.BytecodeOptimizationHelper#canBeGreaterThan(Optimizable, double)} 
     * function. 
     * 
     * @param pThresh
     * 			the new threshold
     */
    public void setThresholdForObjectiveFunction(final double pThresh) {
        problem.setThresholdForObjectiveFunction(pThresh);
    }

    /**
     * Sets the maximize attribute of the wrapped problem class depending on whether 
     * the problem is to be minimized or maximized.
     * 
     * @param pMax
     * 			the new value for the maximize attribute. -1 if the problem is to be minimized, 
     * 			1 otherwise
     */
    public void setMaximize(final byte pMax) {
        problem.setMaximize(pMax);
    }

    /**
     * Checks whether the {@link #objectiveFunction(Object[])} can exceed 
     * the current threshold.
     * 
     * @return
     * 			{@code true} if the threshold can be exceeded,
     * 			{@code false} otherwise
     */
    @Constraint
    public boolean canBeGreaterThanConstraint() {
        return problem.canBeGreaterThan();
    }
}
