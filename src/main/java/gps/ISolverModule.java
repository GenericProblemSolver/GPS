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
package gps;

import java.util.List;

import gps.common.AbstractAlgorithm;

/**
 * All classes that extend this class are considered solver modules for the GPS.
 * 
 * <b>All subclasses classes must provide a constructor, that accept as argument
 * an IWrappedProblem type.</b>
 * 
 * @author haker@uni-bremen.de
 *
 * @param <T>
 *            The type of the problem class
 */
public interface ISolverModule<T> {

    /**
     * Checks if the SolverModule can solve the problem
     * 
     * @param The
     *            result type that is requested from the given solver
     * @return true if solvable, false otherwise
     */
    public abstract boolean canSolve(ResultEnum type);

    /**
     * Return new instances of all applicable algorithms for this solver module.
     * 
     * @param pResultEnum
     *            The Result the algorithm should solve for.
     * 
     * @return The Collection.
     */
    public abstract List<? extends AbstractAlgorithm> getApplicableAlgorithms(
            ResultEnum pResultEnum);
}
