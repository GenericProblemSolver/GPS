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
package gps.optimization.analysis;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import gps.bytecode.backends.IBytecodeBackend.SatisfactionProblemSolution;
import gps.bytecode.interfaces.BytecodeOptimizationHelper;
import gps.optimization.OptimizationReturn;
import gps.optimization.algorithm.AbstractOptimizer;
import gps.optimization.algorithm.incrementalSat.IncrementalSat;
import gps.optimization.analysis.utility.ObjectiveFunctionAnalyzer;
import gps.optimization.analysis.utility.SolverComparison;
import gps.optimization.analysis.utility.SolverInstantiation;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.flattening.Flattener;
import gps.optimization.wrapper.Optimizable;

/**
 * An analyzer that analyzes the given problem, calculates fitting parameters for 
 * the solvers, chooses the best solver to use on the given problem and finally 
 * returns the best found solution. 
 * 
 * @author mburri
 * 
 * @param <T>
 *            the type of the original problem class
 *
 */
public class Analyzer<T> {

    /**
     * The maximal amount of steps that the {@link gps.optimization.algorithm.incrementalSat.IncrementalSat} 
     * solver is allowed to take.
     */
    private final static int MAX_INCREMENTAL_SAT_STEPS = 100;

    /**
     * The estimated number of solver instances used in the optimization process.
     */
    private final static int ESTIMATED_NUMBER_OF_SOLVER_INSTANCES = 17;

    /**
     * The default maximal amount of milliseconds that the solvers are allowed 
     * to run. 
     */
    private final long maxRuntime;

    /**
     * The {@link gps.optimization.wrapper.Optimizable} that is to be 
     * optimized.
     */
    private Optimizable<T> opt;

    /**
     * Determines whether the problem is to be maximized or minimized. 
     * 1, if it is to be maximized, -1 otherwise
     */
    private final byte maximize;

    /**
     * The object wrapping the flattened optimization parameters
     */
    protected FlatObjects solutionObject;

    /**
     * The optimal step width that the Analyzer calculates and uses
     */
    private int optStepWidth;

    /**
     * The objective function scale that the Analyzer calculates and usess
     */
    private double objectiveFunctionScale;

    /**
     * The incremental step width that the 
     * {@link gps.optimization.algorithm.incrementalSat.IncrementalSat} 
     * solver is to use.
     */
    private double incrementalSatStepWidth;

    /**
     * Determines whether or not the the incrementalSat-solver is 
     * started after the heuristic solvers are finished. 
     */
    private boolean exactSolutionDemanded;

    /**
     * Initializes a new instance of the analyzer for the given Optimizable.
     * 
     * @param pOpt
     * 			the Optimizable
     * @param pShouldBeMaximized
     * 			determines whether the problem is to be maximized or minimized. 
     * 			-1 if it is to be minimized, 1 otherwise
     * @param exact
     * 			determines whether the incrementalSat-solver is to be used
     * @param pMaxruntime
     * 			the maximal amount of milliseconds that are allowed to be 
     * 			used during the solver comparison
     */
    public Analyzer(final Optimizable<T> pOpt, final byte pShouldBeMaximized,
            final boolean exact, final long pMaxRuntime) {
        if (pOpt == null) {
            throw new IllegalArgumentException(
                    "The given optimizable must not be null");
        }
        opt = pOpt;
        maximize = pShouldBeMaximized;
        opt.setMaximize(maximize);
        if (opt.hasNeighborFunction()) {
            Object[] defParams = opt.getDefaultParams();
            Set<Class<?>> defParamTypes = new HashSet<>();
            for (Object o : defParams) {
                defParamTypes.add(o.getClass());
            }
            solutionObject = new Flattener().flattenAndWrap(defParams,
                    defParamTypes);
        } else {
            solutionObject = new Flattener()
                    .flattenAndWrap(opt.getDefaultParams());
        }
        exactSolutionDemanded = exact;
        maxRuntime = pMaxRuntime;
    }

    /**
     * Tries to optimize the given problem and returns the best found solution 
     * as an {@link gps.optimization.OptimizationReturn}. 
     * 
     * @return
     * 		the best found solution
     */
    public Optional<OptimizationReturn> solve() {
        long maxRunTimeForCalculations = maxRuntime / 5;
        ObjectiveFunctionAnalyzer ofa = new ObjectiveFunctionAnalyzer(opt,
                maximize, solutionObject, maxRunTimeForCalculations);
        SolverInstantiation<T> si = new SolverInstantiation<T>(opt,
                solutionObject);
        if (opt.hasNeighborFunction()) {
            objectiveFunctionScale = ofa.getObjectiveFunctionScale();
            optStepWidth = 1;
        } else {
            optStepWidth = ofa.getOptimalStepWidth();
            objectiveFunctionScale = ofa
                    .getObjectiveFunctionScale(optStepWidth);
        }
        incrementalSatStepWidth = ofa
                .getIncrementalSatStepWidth(objectiveFunctionScale);

        // get the estimated amount of steps that every solver is allowed
        // to use during the whole optimization process
        int estimatedAmountOfStepsForEverySolver = ofa
                .getNumberOfStepsPerIteration(
                        ESTIMATED_NUMBER_OF_SOLVER_INSTANCES)
                * ESTIMATED_NUMBER_OF_SOLVER_INSTANCES;

        List<AbstractOptimizer<T>> solvers = si.getSolversWithParameters(
                optStepWidth, objectiveFunctionScale,
                estimatedAmountOfStepsForEverySolver, maximize);

        // get the best solution found by the heuristic solvers
        OptimizationReturn ret = new SolverComparison<T>().getBestSolverResult(
                solvers,
                ofa.getNumberOfStepsPerIteration(
                        ESTIMATED_NUMBER_OF_SOLVER_INSTANCES),
                maximize, maxRuntime);
        System.out.println(
                "Best found solution by solver: " + ret.getBestSolutionEval());

        // check if the found solution is optimal (if the optimal solution is demanded)
        ret = exactSolutionDemanded ? tryIterativeSat(ret) : ret;
        System.out.println("Best found solution: " + ret.getBestSolutionEval());

        return Optional.of(ret);
    }

    /**
     * Tries to improve the given solution by using the 
     * {@link gps.optimization.algorithm.iterativeSat.IterativeSat} solver.
     * 
     * If, for some reason, the problem is not fit to be transformed into a 
     * SatisfactionProblem, or if the given solution is the optimal solution, 
     * the given solution is returned. 
     * 
     * @param ret
     * 			the solution that is to be improved
     * @param objectiveFunctionScale
     * 			the scale of the objective function
     * @return
     * 			the optimal solution for the problem
     */
    private OptimizationReturn tryIterativeSat(final OptimizationReturn ret) {
        // check if the found solution is maximal
        SatisfactionProblemSolution sat;
        try {
            sat = BytecodeOptimizationHelper.canBeGreaterThan(opt,
                    maximize * ret.getBestSolutionEval());
        } catch (Exception e) {
            // SATProblem could not be constructed for our 
            // problem, so the given result is returned
            return ret;
        }
        if (sat != null && sat.satisfiable) {
            // best solution has not been found
            // we could start IncSat with the given start value
            FlatObjects bestSolutionFlat = new Flattener()
                    .flattenAndWrap(ret.getBestSolution());
            IncrementalSat<T> incSat = new IncrementalSat<T>(opt,
                    bestSolutionFlat, incrementalSatStepWidth);
            incSat.setMaxSteps(MAX_INCREMENTAL_SAT_STEPS);
            try {
                if (maximize == 1) {
                    incSat.maximize();
                } else {
                    incSat.minimize();
                }
                if (incSat.retrieveResult().isPresent()) {
                    return incSat.retrieveResult().get();
                }
            } catch (Exception e) {
                // ERROR while using the BytecodeInterface
                // given result is returned
                return ret;
            }
        }
        // best solution was given and could not be improved so we can just return it
        return ret;
    }

    /**
     * Gets the optimal step width that the Analyzer calculated
     * 
     * @return
     * 		the optimal step width calculated
     */
    public int getOptimalStepWidth() {
        return optStepWidth;
    }

    /**
     * Gets the objective function scale width that the Analyzer calculated
     * 
     * @return
     * 		the objective function scale width calculated
     */
    public double getObjectiveFunctionScale() {
        return objectiveFunctionScale;
    }

}
