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

import java.util.List;

import gps.optimization.algorithm.SearchOperatorProvider;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.wrapper.Optimizable;

/**
 * Utility class providing several methods to analyze the objective function of 
 * a given Optimizable.
 * 
 * @author mburri
 *
 */
public class ObjectiveFunctionAnalyzer {

    /**
     * The maximal step width that the {@link gps.optimization.algorithm.SearchOperatorProvider}
     * is allowed to use to search through the solution space
     */
    private final static int MAX_STEP_WIDTH = 15;

    /**
     * The maximal amount of milliseconds the analyzer module is allowed to use to 
     * calculate each of the necessary parameters for the objective function.
     */
    private final long maxRuntime;

    /**
     * The {@link gps.optimization.algorithm.SearchOperatorProvider} providing 
     * the methods to search through the solution space.
     */
    private final SearchOperatorProvider sop = new SearchOperatorProvider();

    /**
     * The start FlatObjects that this instance is to use to test solution
     */
    private final FlatObjects startParams;

    /**
     * The initial parameters/content of the given FlatObject
     */
    private final Object[] initialParams;

    /**
     * The {@link gps.optimization.wrapper.Optimizable} that contains the objective 
     * function that is to be analyzed.
     */
    private final Optimizable<?> opt;

    /**
     * Indicates whether the objective function is to be minimized or maximized.
     * -1, if it is to be minimized, 1 otherwise.
     */
    private final byte maximize;

    /**
     * The amount of evaluations made during the calculation of the objective function scale.
     * Used to determine the steps per iteration per solver. 
     */
    private int numberOfPerformedEvals;

    /**
     * Creates a new instance of the ObjectiveFunctionAnalyzer 
     * for the given {@link gps.optimization.wrapper.Optimizable}.
     * 
     * @param toOpt
     * 			the Optimizable
     * @param pMaximize
     * 			indicator whether the problem is to be minimized or maximized
     * @param pStartParams
     * 			the FlatObjects for the given Optimizable
     * @param pMaxRunTime
     * 			the amount of milliseconds this Analyzer module is allowed to use 
     * 			to calculate the necessary parameters
     */
    public ObjectiveFunctionAnalyzer(final Optimizable<?> toOpt,
            final byte pMaximize, final FlatObjects pStartParams,
            final long pMaxRunTime) {
        opt = toOpt;
        maximize = pMaximize;
        startParams = pStartParams;
        initialParams = startParams.getObjects();
        numberOfPerformedEvals = 0;
        maxRuntime = pMaxRunTime;
    }

    /**
     * Returns the optimal step width that the SearchOperatorProvider is to use 
     * to search through the solution space. There is no need to call this method,  
     * if a {@link gps.annotations.Neighbor}-annotated method is present, since we 
     * don't need a step width to search through the solution space. Instead, we can 
     * use the method. 
     * 
     * @return
     * 			the optimal step width 
     */
    public int getOptimalStepWidth() {
        startParams.setValues(initialParams);
        int optStepWidth = 1;
        long cT = System.currentTimeMillis();
        double initialValue = opt
                .objectiveFunction(startParams.getRootObjects());
        while (optStepWidth <= MAX_STEP_WIDTH - 1
                && (System.currentTimeMillis() - cT) < maxRuntime) {
            double difference = 0;
            Object[][] n1 = sop.neighbors(startParams, optStepWidth);
            for (Object[] o : n1) {
                startParams.setValues(o);
                difference += Math
                        .abs(opt.objectiveFunction(startParams.getRootObjects())
                                - initialValue);
            }
            if (difference < (double) optStepWidth * (n1.length / 2)) {
                optStepWidth++;
            } else {
                break;
            }
        }
        startParams.setValues(initialParams);
        return optStepWidth;
    }

    /**
     * Returns the general scale of the objective function.
     * If, for example, the objective function returns values between 0 and 1, 
     * a value between 0 and 1 should be returned. This method is to be called, if a 
     * {@link gps.annotations.Neighbor}-annotated method has been declared.
     * 
     * @return
     * 			the general scale of the objective function
     */
    public double getObjectiveFunctionScale() {
        startParams.setValues(initialParams);
        double scale = 0.0;
        numberOfPerformedEvals = 0;
        long cT = System.currentTimeMillis();
        List<Object[]> neighbors = opt.neighbor(startParams.getRootObjects());
        while ((System.currentTimeMillis() - cT) < maxRuntime) {
            double evalOfCurrent = opt
                    .objectiveFunction(startParams.getRootObjects());
            double highestDifference = -Double.MAX_VALUE;
            Object[] highestDifferenceNeighbor = null;
            for (Object[] o : neighbors) {
                startParams.setValues(o);
                double eval = opt
                        .objectiveFunction(startParams.getRootObjects());
                numberOfPerformedEvals++;
                if (Math.abs(eval - evalOfCurrent) > highestDifference) {
                    highestDifference = Math.abs(eval - evalOfCurrent);
                    highestDifferenceNeighbor = o;
                }
                scale += eval;
            }
            startParams.setValues(highestDifferenceNeighbor);
            neighbors = opt.neighbor(startParams.getRootObjects());
        }
        startParams.setValues(initialParams);
        return (double) maximize * (scale / numberOfPerformedEvals);
    }

    /**
     * Returns the general scale of the objective function.
     * If, for example, the objective function returns values between 0 and 1, 
     * a value between 0 and 1 should be returned. Is to be called, if no 
     * {@link gps.annotations.Neighbor}-annotated method has been declared.
     * 
     * @param stepWidth
     * 			the step width that the SearchOperatorProvider is to use 
     * 			to search through the solution space
     * 
     * @return
     * 			the general scale of the objective function
     */
    public double getObjectiveFunctionScale(final int stepWidth) {
        startParams.setValues(initialParams);
        double scale = 0.0;
        numberOfPerformedEvals = 0;
        long cT = System.currentTimeMillis();
        sop.randomVariation(startParams, stepWidth);
        Object[][] neighbors = sop.neighbors(startParams, stepWidth);
        while ((System.currentTimeMillis() - cT) < maxRuntime) {
            double evalOfCurrent = opt
                    .objectiveFunction(startParams.getRootObjects());
            double highestDifference = -Double.MAX_VALUE;
            Object[] highestDifferenceNeighbor = null;
            for (Object[] o : neighbors) {
                startParams.setValues(o);
                double eval = opt
                        .objectiveFunction(startParams.getRootObjects());
                numberOfPerformedEvals++;
                if (Math.abs(eval - evalOfCurrent) > highestDifference) {
                    highestDifference = Math.abs(eval - evalOfCurrent);
                    highestDifferenceNeighbor = o;
                }
                scale += eval;
            }
            startParams.setValues(highestDifferenceNeighbor);
            neighbors = sop.neighbors(startParams, stepWidth);
        }
        startParams.setValues(initialParams);
        return (double) maximize * (scale / numberOfPerformedEvals);
    }

    /**
     * Returns the incrementalSAT step width for the 
     * {@link gps.optimization.algorithm.incrementalSat.IncrementalSat} 
     * calculated for the given scale of the objective function. 
     * So far, this method chooses the step width completely arbitrarily.
     * 
     * @param objScale
     * 			the scale of the objective function
     * @return
     * 			the incrementalSAT step width
     */
    public double getIncrementalSatStepWidth(final double objScale) {
        if (objScale > 1) {
            return 1;
        } else {
            return 0.01;
        }
    }

    /**
     * Gets the number of steps every solver is allowed to make in an iteration step. 
     * Calculated using the {@link #numberOfPerformedEvals}.
     * 
     * @param	noOfSolverInstances
     * 			the estimated number of solver instances participating in the solver 
     * 			comparison
     * @return
     * 			the number of steps every solver is allowed to take
     */
    public int getNumberOfStepsPerIteration(final int noOfSolverInstances) {
        // calculate how many evaluations are possible the max amount of milliseconds
        int evalsPerformableInMaxSeconds = numberOfPerformedEvals * 5;

        // in every iteration, one solver is removed
        // at the start, we have noOfSolverInstances instances
        // which gives us noOfSolverInstances+noOfSolverInstances-1+...+1+0 
        // times any solver instance is called 
        int sum = 0;
        for (int i = noOfSolverInstances; i > 0; i--) {
            sum += i;
        }
        int ret = evalsPerformableInMaxSeconds / sum;

        // lower bound is 5
        ret = ret < 5 ? 5 : ret;
        // if the limit of 500 steps is exceeded, return 500 instead
        return ret > 500 ? 500 : ret;
    }

}
