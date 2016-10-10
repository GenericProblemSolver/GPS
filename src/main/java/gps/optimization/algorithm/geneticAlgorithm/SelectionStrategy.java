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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Different selection strategies for the Genetic Algorithm. These determine
 * which solutions are used to create new solutions. TODO:add more
 * 
 * @author Steffen
 */
public enum SelectionStrategy {

    TRUNCATION {
        @Override
        public List<Object[]> select(List<SolutionFitnessTuple> pPopulation,
                final int pSolutionsWanted) {
            List<Object[]> result = new ArrayList<>();
            for (int x = 0; x < pSolutionsWanted; x++) {
                result.add(pPopulation.get(x).getSolution());
            }
            return result;
        }
    };
    /**
     * Selects the solutions used to create the new solution.
     * 
     * @param pPopulation
     *            List of Tuples representing solutions (by their String) name
     *            their fitness
     * @param pSolutionsWanted
     *            the amount of solutions to be returned
     * @return List of solutions used to create new solution, by the String
     *         representing them.
     */
    public abstract List<Object[]> select(
            final List<SolutionFitnessTuple> pPopulation,
            final int pSolutionsWanted);
}
