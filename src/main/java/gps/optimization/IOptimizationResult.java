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

import java.util.Optional;

/**
 * The result types you can request for a given problem class.
 * 
 * @author mburri
 *
 * @param <T>   
 *          the problem class type
 */
public interface IOptimizationResult<T> {

    /**
     * Retrieves a solution that maximizes the function that is to be optimized.
     * The solution is represented as an Object[]-Array containing the 
     * best parameters for the function. Additionally, the value of the 
     * solution is returned.
     * 
     * @return
     *          an {@link Optional} containing an {@link OptimizationReturn} containing the 
     *          best found solution as well as the value of the found solution
     */
    public Optional<OptimizationReturn> maximize();

    /**
     * Retrieves a solution that minimizes the function that is to be optimized.
     * The solution is represented as an Object[]-Array containing the 
     * best found parameters for the function. Additionally, the value of the 
     * solution is returned.
     * 
     * @return
     *          an {@link Optional} containing an {@link OptimizationReturn} containing the 
     *          best found solution as well as the value of the found solution
     */
    public Optional<OptimizationReturn> minimize();

}
