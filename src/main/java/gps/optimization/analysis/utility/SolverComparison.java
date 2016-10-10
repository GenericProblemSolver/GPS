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
package gps.optimization.analysis.utility;

import java.util.ArrayList;
import java.util.List;

import gps.optimization.OptimizationReturn;
import gps.optimization.algorithm.AbstractOptimizer;
import gps.optimization.algorithm.geneticAlgorithm.GeneticAlgorithm;
import gps.optimization.algorithm.particleSwarmOpt.ParticleSwarm;

/**
 * Utility class that "benchmarks" a given list of 
 * {@link gps.optimization.algorithm.AbstractOptimizer}s.
 * 
 * @author mburri
 * 
 */
public class SolverComparison<T> {

    /**
     * The maximal amount of milliseconds the whole optimization process,
     * more specific: the solver "choosing" process, is allowed to take.
     */
    private long maxRuntime;

    /**
     * Compares the given list of solvers and returns the best found solution.
     * 
     * @param solvers
     * 		the solvers that are to be compared
     * @param maxStepsPerIteration
     * 		the amount of steps every solver is allowed to take during each iteration step
     * @param maximize
     * 		indicates if the problem is to be maximized or minimized
     * @param pMaxTime
     * 		the maximal amount of milliseconds the "benchmarking" process is allowed to take
     * @return
     * 		the best found solution
     */
    public OptimizationReturn getBestSolverResult(
            final List<AbstractOptimizer<T>> solvers,
            final int maxStepsPerIteration, final byte maximize,
            final long pMaxTime) {
        maxRuntime = pMaxTime;
        return getBestSolverResultMax(solvers, maxStepsPerIteration, maximize);
    }

    /**
     * "Benchmarks". As long as there is more than one solver left or the 
     * {@link #MAX_TIME} is not exceeded, every remaining solver performs 
     * the given amount of steps. The solver with the worst current 
     * result is removed from the list. Ultimately, the best found solution is returned.
     * 
     * @param solvers
     * 			the initial list of solvers that are to be compared
     * @param maxStepsPerIteration
     * 			the amount of steps every solver is allowed to take during each iteration step
     * @return
     * 			the best found solution
     */
    private OptimizationReturn getBestSolverResultMax(
            final List<AbstractOptimizer<T>> solvers,
            final int maxStepsPerIteration, final byte maximize) {
        boolean max = maximize == 1;
        List<OptimizationReturn> currentResults = new ArrayList<OptimizationReturn>();
        for (int i = 0; i < solvers.size(); i++) {
            currentResults.add(new OptimizationReturn(new Object[] {}, 0));
        }
        final long startTime = System.currentTimeMillis();
        while (solvers.size() > 1
                && (System.currentTimeMillis() - startTime) < maxRuntime) {
            // Optimization steps are completed for every remaining solver
            for (int i = 0; i < solvers.size(); i++) {
                AbstractOptimizer<T> solv = solvers.get(i);
                currentResults.remove(i);
                solv.setMaxSteps(solv.getClass().equals(ParticleSwarm.class)
                        || solv.getClass().equals(GeneticAlgorithm.class)
                                ? Math.max(1, (maxStepsPerIteration / 10))
                                : maxStepsPerIteration);
                // since we want to maximize, we have to call the
                // maximize method
                currentResults.add(i,
                        max ? solv.maximize().get() : solv.minimize().get());
            }
            // Solver with the worst current result is determined and removed
            double worstResult = currentResults.get(0).getBestSolutionEval()
                    * maximize;
            int worstResultIndex = 0;
            for (int i = 1; i < solvers.size(); i++) {
                if ((currentResults.get(i).getBestSolutionEval()
                        * maximize) < worstResult) {
                    worstResult = currentResults.get(i).getBestSolutionEval()
                            * maximize;
                    worstResultIndex = i;
                }
            }
            solvers.remove(worstResultIndex);
            currentResults.remove(worstResultIndex);
        }
        // if the time limit was exceeded and there is more than one solver 
        // left, the best result is determined and returned
        if (currentResults.size() > 1) {
            double bestResult = currentResults.get(0).getBestSolutionEval()
                    * maximize;
            int bestResultIndex = 0;
            for (int i = 1; i < solvers.size(); i++) {
                if ((currentResults.get(i).getBestSolutionEval()
                        * maximize) > bestResult) {
                    bestResult = currentResults.get(i).getBestSolutionEval()
                            * maximize;
                    bestResultIndex = i;
                }
            }
            // required so the solution objects contain the correct values
            // since we only need the solver to call its init-method
            // it does not matter whether we maximize or minimize 
            // since no steps are performed anyways
            solvers.get(bestResultIndex).setMaxSteps(0);
            solvers.get(bestResultIndex).maximize();
            return currentResults.get(bestResultIndex);
        }
        return currentResults.get(0);
    }

}
