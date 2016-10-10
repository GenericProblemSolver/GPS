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
package gps.optimization.algorithm.geneticAlgorithm;

/**
 * Represents 2-Tuples made of a solution (represented by a String) and the fitness of the solution (represented by a double)
 * 
 * @author Steffen
 */
public class SolutionFitnessTuple implements Comparable<SolutionFitnessTuple> {

    /**
     * Object array representing the solution
     */
    private Object[] solution;

    /**
     * Fitness of the solution
     */
    private double fitness;

    /**
     * Creates a new SolutionFitnessTuple.
     * 
     * @param pSolution Object array representing the solution
     * @param pFitness fitness of the solution
     */
    public SolutionFitnessTuple(final Object[] pSolution,
            final double pFitness) {
        solution = pSolution;
        fitness = pFitness;
    }

    /**
     * Returns the Object array representing the solution
     * @return the Object array representing the solution
     */
    public Object[] getSolution() {
        return solution;
    }

    /**
     * Returns the fitness of the solution
     * @return the fitness of the solution
     */
    public double getFitness() {
        return fitness;
    }

    @Override
    public int compareTo(SolutionFitnessTuple o) {
        return o.getFitness() == fitness ? 0
                : o.getFitness() > fitness ? 1 : -1;
    }
}