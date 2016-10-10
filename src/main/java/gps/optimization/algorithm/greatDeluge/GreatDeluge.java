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
package gps.optimization.algorithm.greatDeluge;

import gps.optimization.wrapper.Optimizable;
import gps.ResultEnum;
import gps.optimization.algorithm.AbstractOptimizer;
import gps.optimization.flattening.FlatObjects;

/**
 * Implements the great deluge algorithm. This heuristic optimization
 * algorithm accepts a solution as the new current solution, provided that the
 * value of the solution is above a threshold, called water level. 
 * The threshold is normally initialized with a value >= 0  and increased gradually 
 * by a given value >= 0, called rain. <br>
 * If the function is to be minimized, the rain should be negative.
 * 
 * @author mburri
 * 
 * @param <T>
 *            the type of the original problem class
 */
public class GreatDeluge<T> extends AbstractOptimizer<T> {

    /**
     * Factor by which the threshold is decreased after every step
     */
    private double rain;

    /**
     * Current threshold for allowing solutions as new current solutions
     */
    private double currentWaterlevel;

    /**
     * The initial water level. Stored only for output purposes
     */
    private double initialWaterlevel;

    /**
     * Initializes a new instance of the Great Deluge algorithm for the
     * given {@link gps.optimization.wrapper.Optimizable}.
     * 
     * @param pToOpt
     *            the Optimizable the algorithm is to be applied on
     * @param pInitSolution
     * 			  the initial solution of the optimization process
     * @param initialWaterlevel
     *            the initial water level
     * @param pRain
     *            the value used to increase the water level
     */
    public GreatDeluge(final Optimizable<T> pToOpt,
            final FlatObjects pInitSolution, final double initialWaterlevel,
            final double pRain) {
        super(pToOpt, pInitSolution);
        this.initialWaterlevel = initialWaterlevel;
        currentWaterlevel = initialWaterlevel;
        rain = pRain;
    }

    /**
     * Gets the rain that is added to the water level after every step
     * 
     * @return
     * 		the rain
     */
    public double getRain() {
        return rain;
    }

    /**
     * Sets the rain that is added to the water level after every step
     * 
     * @param pRain
     * 		the new rain
     */
    public void setRain(double pRain) {
        rain = pRain;
    }

    /**
     * Gets the current water level
     * 
     * @return
     * 		the current water level
     */
    public double getCurrentWaterlevel() {
        return currentWaterlevel;
    }

    /**
     * Sets the current water level
     * 
     * @param pCurrentWaterlevel
     * 		the new water level
     */
    public void setCurrentWaterlevel(double pCurrentWaterlevel) {
        currentWaterlevel = pCurrentWaterlevel;
    }

    /**
     * Performs one step according to the Great Deluge algorithm. Creates
     * a random variation of the best solution and accepts it as the new best
     * solution if it is better than the current water level/threshold. 
     * Additionally, the water level is increased by the value of {@link #rain}.
     */
    @Override
    protected void optimizeOneStep() {
        solutionObject.setValues(currentSolution);
        sop.randomVariation(solutionObject, 1);

        final double res = eval();
        if (res > currentBestEval || res > currentWaterlevel) {
            currentSolution = solutionObject.getObjects();
            currentBestEval = res;
        }

        currentWaterlevel += rain;
    }

    @Override
    protected void init() {
        solutionObject.setValues(currentSolution);
        currentBestEval = eval();
    }

    @Override
    public String getName() {
        return "Great Deluge";
    }

    @Override
    public boolean isApplicable(ResultEnum type) {
        return type.equals(ResultEnum.MAXIMIZED)
                || type.equals(ResultEnum.MINIMIZED);
    }

    @Override
    public String toString() {
        return "<html>Initial water level: " + initialWaterlevel + "<br>Rain: "
                + rain + "</html>";
    }
}
