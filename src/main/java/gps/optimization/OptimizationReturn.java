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

/**
 * Represents the output of an optimization performed 
 * by a subclass of the {@link gps.optimization.algorithm.AbstractOptimizer}
 * 
 * @author mburri, oesterca
 * 
 */
public class OptimizationReturn {

    /**
     * The best found solution/parameter(s) for the function that is to be optimized
     */
    private Object[] bestFoundSolution;

    /**
     * Value of the best found solution
     */
    private double bestFoundSolutionEval;

    /**
     * Creates a pair of an object found to be the best solution and its value
     * 
     * @param pBest
     *          optimal object(s) found
     *          
     * @param pBestValue
     *          value of the object
     */
    public OptimizationReturn(Object[] pBest, double pBestValue) {
        bestFoundSolution = pBest;
        bestFoundSolutionEval = pBestValue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Best found solution: \n");
        for (int i = 0; i < bestFoundSolution.length; i++) {
            sb.append(bestFoundSolution[i].toString());
            if (i != bestFoundSolution.length - 1) {
                sb.append(" | ");
            }
        }
        sb.append("\nValue of the found solution: \n" + bestFoundSolutionEval);
        return sb.toString();
    }

    /**
     * Returns the best found solution 
     * 
     * @return
     *      the best found solution
     */
    public Object[] getBestSolution() {
        return bestFoundSolution;
    }

    /**
     * Returns the value of the best found solution
     * 
     * @return
     *      the value of the best found solution
     */
    public double getBestSolutionEval() {
        return bestFoundSolutionEval;
    }

}