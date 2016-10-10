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
package gps.optimization.algorithm.incrementalSat;

import java.util.Optional;

import gps.ResultEnum;
import gps.bytecode.backends.IBytecodeBackend.SatisfactionProblemSolution;
import gps.bytecode.expressions.Constant;
import gps.bytecode.interfaces.BytecodeOptimizationHelper;
import gps.optimization.OptimizationReturn;
import gps.optimization.algorithm.AbstractOptimizer;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.wrapper.Optimizable;

/**
 * Implementation of incremental SAT. 
 * It is tested whether the return of the function that is to be optimized can exceed 
 * a value ({@link #currentValue} that is slowly increased by the {@link #incrementalStepWidth}. 
 * If the value cannot be exceeded anymore, the optimal solution is found.<br> 
 * 
 * Important: After the optimization process, to retrieve the solution the 
 * {@link #retrieveResult()} is to be called. 
 * 
 * @author mburri
 *
 * @param <T>
 * 			the type of the original problem class
 */
public class IncrementalSat<T> extends AbstractOptimizer<T> {

    /**
     * The constant by which the value 
     * that is to be tested is increased after every step. 
     */
    private double incrementalStepWidth;

    /**
     * The current value that is to be evaluated tested.
     */
    private double currentValue;

    /**
     * The result of the current optimization step
     */
    private SatisfactionProblemSolution result = null;

    /**
     * Initializes a new instance of the Incremental SAT solver for the
     * given {@link gps.optimization.wrapper.Optimizable}.
     * 
     * @param pToOpt
     *            the Optimizable the algorithm is to be applied on
     * @param pInitSolution
     * 			  the initial solution of the optimization process
     * @param pIncrementalStepWidth
     * 			  the step width
     */
    public IncrementalSat(Optimizable<T> pToOpt, FlatObjects pInitSolution,
            final double pIncrementalStepWidth) {
        super(pToOpt, pInitSolution);
        incrementalStepWidth = pIncrementalStepWidth;
    }

    /**
     * Performs one step according to this implementation of the 
     * incremental SAT procedure. Tests if the function that is to be 
     * optimized can exceed the currentBestEval. If that is the case, 
     * the value is increased by the {{@link #incrementalStepWidth}. 
     * If not, the optimization process is terminated. 
     */
    @Override
    protected void optimizeOneStep() {
        SatisfactionProblemSolution tempResult = BytecodeOptimizationHelper
                .canBeGreaterThan(toOpt, currentValue);
        if (!tempResult.satisfiable) {
            currentBestEval = currentValue;
            if (currentSteps == 1) {
                // If this is the first step of the optimization process
                // result is set according to the created tempResult.
                // This solves a bug that occurs if the initial solution also 
                // is the best solution.
                // If the initial value is not satisfiable and there is no 
                // satisfiable value at all, result is therefore set to null.
                // Since we return an Optional, that is not a problem
                result = tempResult;
            }
            terminateOptimizationProcess();
        } else if (maximize == -1 && currentValue > 0) {
            terminateOptimizationProcess();
        } else {
            result = tempResult;
            currentBestEval = currentValue;
            currentValue += incrementalStepWidth;
        }
    }

    /**
     * Terminates the optimization process prematurely by setting the 
     * maximal amount of steps to 0, if the solver is step constrained. 
     * If the solver is time constrained, the time limit is set to 0 ms.
     */
    public void terminateOptimizationProcess() {
        if (timeConstrained) {
            this.setTimeLimit(0);
        } else {
            this.setMaxSteps(0);
        }
    }

    /**
     * Retrieves the result of the optimization process as a 
     * {@link gps.optimization.OptimizationReturn}. This method 
     * is to be called after the optimization process. 
     * 
     * @return
     * 		the result of the optimization process
     */
    public Optional<OptimizationReturn> retrieveResult() {
        if (result == null) {
            return Optional.empty();
        }
        Object[] ret = new Object[result.variableValues.size()];
        Object[] resultConstants = result.variableValues.values().toArray();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = ((Constant) resultConstants[i]).getValue();
        }
        return Optional
                .of(new OptimizationReturn(ret, maximize * currentBestEval));
    }

    @Override
    protected void init() {
        solutionObject.setValues(currentSolution);
        currentBestEval = eval();
        currentValue = currentBestEval;
    }

    @Override
    public String getName() {
        return "Incremental SAT";
    }

    @Override
    public boolean isApplicable(ResultEnum type) {
        return type.equals(ResultEnum.MAXIMIZED)
                || type.equals(ResultEnum.MINIMIZED);
    }

}
