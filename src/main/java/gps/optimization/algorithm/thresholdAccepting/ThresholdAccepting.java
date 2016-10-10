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
package gps.optimization.algorithm.thresholdAccepting;

import gps.optimization.wrapper.Optimizable;
import gps.ResultEnum;
import gps.optimization.algorithm.AbstractOptimizer;
import gps.optimization.flattening.FlatObjects;

/**
 * Implements the threshold accepting algorithm. This heuristic optimization
 * algorithm accepts a solution as the new current solution, provided that the
 * deterioration is not greater than a given threshold. The threshold is
 * decreased gradually with a given factor.
 * 
 * @author mburri
 * 
 * @param <T>
 *            the type of the original problem class
 * 
 * @see <a href="https://de.wikipedia.org/wiki/Schwellenakzeptanz"> https://de.
 *      wikipedia.org/wiki/Schwellenakzeptanz</a>
 */
public class ThresholdAccepting<T> extends AbstractOptimizer<T> {

    /**
     * Factor by which the threshold is decreased after every step
     */
    private final double decreaseFactor;

    /**
     * Current threshold for allowing solutions as new current solutions
     */
    private double currentThreshold;

    /**
     * Initial threshold. Stored only for output purposes.
     */
    private double initialThreshold;

    /**
     * Initializes a new instance of the TrehsholdAccepting algorithm for the
     * given {@link gps.optimization.wrapper.Optimizable}.
     * 
     * @param pToOpt
     *            the Optimizable the algorithm is to be applied on
     * @param pInitSolution
     *            the initial solution for the algorithm          
     * @param initialThreshold
     *            the initial threshold
     * @param pDecreaseFactor
     *            the factor by which the threshold is gradually decreased
     */
    public ThresholdAccepting(final Optimizable<T> pToOpt,
            final FlatObjects pInitSolution, final double initialThreshold,
            final double pDecreaseFactor) {
        super(pToOpt, pInitSolution);
        this.initialThreshold = initialThreshold;
        decreaseFactor = pDecreaseFactor;
        currentThreshold = initialThreshold;
    }

    /**
     * Gets the current threshold.
     * 
     * @return
     * 			the current threshold
     */
    public double getCurrentThreshold() {
        return currentThreshold;
    }

    /**
     * Sets the current threshold.
     * 
     * @param pThresh
     * 			the new current threshold
     */
    public void setCurrentThreshold(final double pThresh) {
        currentThreshold = pThresh;
    }

    /**
     * Performs one step according to the ThresholdAccepting algorithm. Creates
     * a random variation of the current solution and accepts it as the new current
     * solution if it is better or if the deterioration is not greater than a
     * given threshold. Additionally, the threshold is decreased by the decrease
     * factor.
     */
    @Override
    protected void optimizeOneStep() {
        solutionObject.setValues(currentSolution);
        sop.randomVariation(solutionObject, 1);

        final double res = eval();

        final double relaxedValue = currentBestEval - currentThreshold;

        if ((res - relaxedValue) > 0) {
            currentSolution = solutionObject.getObjects();
            currentBestEval = res;
        }

        currentThreshold *= decreaseFactor;
    }

    @Override
    protected void init() {
        solutionObject.setValues(currentSolution);
        currentBestEval = eval();
    }

    @Override
    public String getName() {
        return "Threshold Accepting";
    }

    @Override
    public boolean isApplicable(ResultEnum type) {
        return type.equals(ResultEnum.MAXIMIZED)
                || type.equals(ResultEnum.MINIMIZED);
    }

    @Override
    public String toString() {
        return "<html>Initial threshold: " + initialThreshold
                + "<br>decrease factor: " + decreaseFactor + "</html>";
    }

}
