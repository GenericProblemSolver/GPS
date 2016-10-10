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
package gps.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import gps.ISolverModule;
import gps.IWrappedProblem;
import gps.ResultEnum;
import gps.common.AbstractAlgorithm;
import gps.optimization.analysis.Analyzer;
import gps.optimization.wrapper.Optimizable;

public class OptimizationModule<T>
        implements ISolverModule<T>, IOptimizationResult<T> {

    /**
     * The problem that has been passed to the module.
     */
    private IWrappedProblem<T> problem;

    /**
     * The {@link gps.optimization.wrapper.Optimizable} wrapping the 
     * problem class. 
     */
    private Optimizable<T> opt;

    /**
     * Determines whether or not the global optimum is demanded or a 
     * heuristically found solution is sufficient.
     * Per default, the exact solution is demanded.
     */
    private static boolean EXACT_SOLUTION_DEMANDED = true;

    /**
     * The maximal amount of milliseconds that are allowed to be used 
     * during the actual optimization process. 
     */
    private static long MAX_RUNTIME_FOR_OPTIMIZERS = 10000;

    /**
     * Creates a new instance of the OptimizationModule for 
     * the given problem
     * 
     * @param pProblem
     * 			the problem that is to be optimized
     */
    public OptimizationModule(IWrappedProblem<T> pProblem) {
        problem = pProblem;
        opt = new Optimizable<T>(problem);
    }

    @Override
    public boolean canSolve(ResultEnum type) {
        return problem.hasObjectiveFunction()
                && (type.equals(ResultEnum.MAXIMIZED)
                        || type.equals(ResultEnum.MINIMIZED));
    }

    @Override
    public Optional<OptimizationReturn> maximize() {
        return new Analyzer<T>(opt, (byte) 1, EXACT_SOLUTION_DEMANDED,
                MAX_RUNTIME_FOR_OPTIMIZERS).solve();
    }

    @Override
    public Optional<OptimizationReturn> minimize() {
        return new Analyzer<T>(opt, (byte) -1, EXACT_SOLUTION_DEMANDED,
                MAX_RUNTIME_FOR_OPTIMIZERS).solve();
    }

    /**
     * Sets if the global optimum is demanded.
     * 
     * @param exact
     * 			{@code true} if the optimal solution is demanded,
     * 			{@code false} otherwise
     */
    public void setExact(final boolean exact) {
        EXACT_SOLUTION_DEMANDED = exact;
    }

    /**
     * Sets the maximal amount of milliseconds that are allowed 
     * during optimizer comparison which takes up the main part of the 
     * time passed during the whole optimization process.
     * 
     * @param ms
     * 			the maximal amount of milliseconds
     */
    public void setMaxRuntimeForOptimizers(final long ms) {
        MAX_RUNTIME_FOR_OPTIMIZERS = ms;
    }

    @Override
    public List<? extends AbstractAlgorithm> getApplicableAlgorithms(
            ResultEnum pResultEnum) {
        return new ArrayList<>();
    }

}
