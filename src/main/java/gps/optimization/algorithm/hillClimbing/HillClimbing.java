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
package gps.optimization.algorithm.hillClimbing;

import gps.ResultEnum;
import gps.optimization.algorithm.AbstractOptimizer;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.wrapper.Optimizable;

/**
 * Implements the hill climbing algorithm. This heuristic optimization algorithm 
 * searches the neighboring solutions of the current solution and only accepts 
 * a solution as the new current solution, if it is better than the current solution. 
 * If, however, no better neighboring solution is found, the algorithm is terminated. 
 * 
 * @author mburri
 *
 * @param <T>
 *            the type of the original problem class
 */
public class HillClimbing<T> extends AbstractOptimizer<T> {

    /**
     * Initializes a new instance of the hill climbing algorithm for the
     * given {@link gps.optimization.wrapper.Optimizable}.
     * 
     * @param pToOpt
     *            the Optimizable the algorithm is to be applied on
     * @param pInitSolution
     *            the initial solution for the algorithm
     */
    public HillClimbing(Optimizable<T> pToOpt, FlatObjects pInitSolution) {
        super(pToOpt, pInitSolution);
    }

    /**
     * Performs one step according to the hill climbing algorithm. 
     * Evaluates all neighboring solutions of the current solution 
     * returned by the {@link gps.optimization.algorithm.SearchOperatorProvider#neighbors}-
     * function and updates the current solution with the best found solution. 
     * If, however, no better solution is found, the optimization is terminated.
     */
    @Override
    protected void optimizeOneStep() {
        final Object[][] neighbors = sop.neighbors(solutionObject, 1);
        double nextEval = -Double.MAX_VALUE;
        Object[] nextSolution = null;
        for (Object[] neighbor : neighbors) {
            solutionObject.setValues(neighbor);
            double res = eval();
            if (res > nextEval) {
                nextSolution = neighbor;
                nextEval = res;
            }
        }
        if (nextEval > currentBestEval) {
            solutionObject.setValues(nextSolution);
            currentSolution = nextSolution;
            currentBestEval = nextEval;
        } else {
            if (timeConstrained) {
                setTimeLimit(0);
            } else {
                setMaxSteps(0);
            }
        }
    }

    @Override
    protected void init() {
        solutionObject.setValues(currentSolution);
        currentBestEval = eval();
    }

    @Override
    public String getName() {
        return "Hill Climbing";
    }

    @Override
    public boolean isApplicable(ResultEnum type) {
        return type.equals(ResultEnum.MAXIMIZED)
                || type.equals(ResultEnum.MINIMIZED);
    }

    @Override
    public String toString() {
        return "<html>Step width: " + super.stepWidth
                + "<br>(0 if @Neighbor is used)</html>";
    }

}
