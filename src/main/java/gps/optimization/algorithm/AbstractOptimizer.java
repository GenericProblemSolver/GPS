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
package gps.optimization.algorithm;

import java.util.Arrays;
import java.util.Optional;

import gps.common.AbstractAlgorithm;
import gps.optimization.IOptimizationResult;
import gps.optimization.OptimizationReturn;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.wrapper.Optimizable;

/**
 * An optimization algorithm as applied to a given problem.
 * Some fields have to be accessible by the subclasses (solvers) and are therefore 
 * not private but protected. An alternative would be getter- and setter methods.
 * 
 * @author mburri, oesterca
 * 
 * @param <T> 
 *          the type of the original problem class
 *          
 */
public abstract class AbstractOptimizer<T> extends AbstractAlgorithm
        implements IOptimizationResult<T> {

    /**
     * Default value for the constraint mode.
     * true for default = time-constrained-constrained
     * false for default = number-of-steps-constrained
     */
    private final static boolean DEFAULT_CONSTRAINT_MODE = true;

    /**
     * Default value for the maximal number of milliseconds
     */
    private final static int DEFAULT_MAX_TIME = 10000;

    /**
     * The annotated object containing the function that is to be optimized
     * wrapped as an Optimizable object
     */
    protected final Optimizable<T> toOpt;

    /**
     * The step width that the {@link gps.optimization.algorithm.SearchOperatorProvider} 
     * is to use to search through the solution space. 
     */
    protected int stepWidth;

    /**
     * The SearchOperatorProvider providing functions used to search 
     * the space of solutions
     */
    protected SearchOperatorProvider sop;

    /**
     * The object wrapping the flattened optimization parameters
     */
    protected FlatObjects solutionObject;

    /**
     * The best set of parameters that has been found by this Optimizer.
     */
    protected Object[] currentSolution;

    /**
     * The best evaluation that has been found, yet.
     */
    protected double currentBestEval;

    /**
     * Number of steps that this Optimizer is allowed to make.
     */
    private int maxSteps;

    /**
     * Number of steps in the optimization process that have already been conducted.
     */
    protected int currentSteps;

    /**
     * true, if the algorithm is to be time-constrained
     * false, if the algorithm is to be number-of-steps-constrained
     */
    protected boolean timeConstrained;

    /**
     * Maximum number of milliseconds that are allowed to be used for optimization.
     */
    private long maxTime;

    /**
     * Time at which the optimization process was started.
     */
    private long startTime;

    /**
     * true, if the function is to be maximized, 
     * false if it is to be minimized
     */
    protected byte maximize;

    /**
     * Initializes a new instance of the solver for the given {@link Optimizable}.
     * Sets the constraint mode and maxTime to the default values.
     * 
     * @param pToOpt    the Optimizable the algorithm is to be applied on
     */
    public AbstractOptimizer(final Optimizable<T> pToOpt,
            final FlatObjects pInitSolution) {
        toOpt = pToOpt;
        sop = new SearchOperatorProvider(toOpt);
        solutionObject = pInitSolution;
        currentSolution = Arrays.copyOf(solutionObject.getObjects(),
                solutionObject.getObjects().length);
        timeConstrained = DEFAULT_CONSTRAINT_MODE;
        maxTime = DEFAULT_MAX_TIME;
    }

    /**
     * Sets the {@link #stepWidth}
     * 
     * @param pStepWidth
     * 			the new step width
     */
    public void setStepWidth(final int pStepWidth) {
        stepWidth = pStepWidth;
    }

    /**
     * Sets the constraint mode to time-constrained and sets the 
     * maximal number of milliseconds allowed to be used by the algorithm to 
     * the given value.
     * 
     * @param pMaxTime  number of milliseconds allowed to be used
     */
    public void setTimeLimit(final long pMaxTime) {
        timeConstrained = true;
        maxTime = pMaxTime;
    }

    /**
     * Gets the current time limit.
     * 
     * @return
     * 		the current time limit
     */
    public long getTimeLimit() {
        return maxTime;
    }

    /**
     * Sets the constraint mode to number-of-steps-constrained and sets the 
     * maximal number of steps allowed to be used by the algorithm to 
     * the given value.
     * 
     * @param pMaxSteps number of steps allowed to be used
     */
    public void setMaxSteps(final int pMaxSteps) {
        timeConstrained = false;
        maxSteps = pMaxSteps;
    }

    /**
     * Gets the maximal number of steps that this optimizer is allowed to perform.
     * 
     * @return
     * 		the maximal number of steps
     */
    public int getMaxSteps() {
        return maxSteps;
    }

    /**
     * Performs one step in the optimization process of the target function.
     */
    abstract protected void optimizeOneStep();

    /**
     * Optimizes until the maximal time or the maximal number of steps is reached.
     * Can be called repeatedly.
     * 
     * @return an OptimizationReturn containing the best found Object[] with its evaluation
     */
    public OptimizationReturn optimize() {
        init();
        startTime = System.currentTimeMillis();
        currentSteps = 0;
        while (check()) {
            optimizeOneStep();
        }
        solutionObject.setValues(currentSolution);
        return new OptimizationReturn(solutionObject.getRootObjects(),
                (double) (currentBestEval * maximize));
    }

    @Override
    public Optional<OptimizationReturn> maximize() {
        maximize = 1;
        toOpt.setMaximize((byte) 1);
        return Optional.of(this.optimize());
    }

    @Override
    public Optional<OptimizationReturn> minimize() {
        maximize = -1;
        toOpt.setMaximize((byte) -1);
        return Optional.of(this.optimize());
    }

    /**
     * Evaluates a given solution.
     * Normalizes the objective function so the solvers have to maximize regardless of 
     * the function is actually to be minimized or not.<br>
     * Throws a {@link RuntimeException} if the given {@link gps.optimization.flattening.FlatObjects} 
     * does not wrap a set of parameters matching the parameters of the {@link gps.annotations.Optimize}-
     * annotated method, which is possible if the user defined {@link gps.annotations.Neighbor}-annotated 
     * method does not return a set of matching parameters, e.g. <br>
     * Optimize: double opt(int i) <br>
     * Neighbor: ({@link java.util.List} of {@link java.lang.Double}s) neigh(int i)
     * 
     * @return  the value of the given solution
     */
    protected double eval() {
        try {
            return toOpt.objectiveFunction(solutionObject.getRootObjects());
        } catch (Exception e) {
            throw new RuntimeException(
                    "Bad implementation of @Neighbor-annotated function. "
                            + "Check the documentation of gps.annotations.Neighbor for more information.");
        }
    }

    /**
     * Returns true if the optimizer is allowed to continue. 
     * Returns false if the maximal time or the allowed number of steps is reached.
     * 
     * @return true if the optimizer is allowed to continue, false otherwise
     */
    private boolean check() {
        if (timeConstrained) {
            return System.currentTimeMillis() - startTime > maxTime;
        } else {
            currentSteps++;
            return maxSteps >= currentSteps;
        }
    }

    /**
     * Initializes the optimizer 
     * (Stuff like initial solution, initial value of solution, etc.).
     * Has to be called after either {@link #minimize} or {@link #maximize} have been 
     * called.
     */
    protected abstract void init();

}
