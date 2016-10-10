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
package gps.common;

import java.util.List;

import gps.ResultEnum;

/**
 * Abstract Algorithm class that serves as shared superclass for all algorithms
 * that can solve specific problems for the GPS.
 * 
 * All constructors that are implemented in classes that extend this class must
 * use wrapper classes (eg. Integer instead of int) in order to get found by the
 * invoker.
 * 
 * @author haker@uni-bremen.de
 *
 */
public abstract class AbstractAlgorithm {

    /**
     * Construct an algorithm. Algorithms can solve specific problems for the
     * the GPS.
     * 
     * @param usedFields
     *            the fields that this algorithm generated during execution. May
     *            be empty if no data is gathered at all.
     */
    public AbstractAlgorithm(final BenchmarkField... usedFields) {
        this.benchmark = new Benchmark(usedFields);
    }

    /**
     * Return the name of the algorithm that is used to distinguish between
     * algorithms. May not be {@code null} or empty.
     */
    public abstract String getName();

    /**
     * The benchmark data for this algorithm.
     */
    protected final Benchmark benchmark;

    /**
     * Return the benchmark data for this algorithm.
     * 
     * @return The Benchmark.
     */
    public final IBenchmark getBenchmark() {
        return benchmark;
    }

    /**
     * Checks whether the algorithm can solve the desired result type of the
     * given problem that has been passed to the constructor.
     *
     * @param type
     *            The desired result type.
     *
     * @return
     */
    public abstract boolean isApplicable(final ResultEnum type);

    /**
     * Retrieves additional Optional that can be used to instantiate the
     * Algorithm.
     * 
     * The Algorithm is never executed without options.
     * 
     * <p>
     * The algorithm must provide an additional constructor that accepts the
     * arguments represented in the array. Eg. if the additional constructor
     * accepts two additional arguments then the returned array must be of
     * length 2.
     * </p>
     * 
     * <p>
     * All Options are applied as Cartesian product. Meaning that every
     * combination is instantiated. The instance of the algorithm that is used
     * for calling getOptions is not executed if the getOptions array is empty.
     * </p>
     * 
     * <p>
     * Options are also applied if the {@link #isApplicable(ResultEnum)} returns
     * false. Thus giving the algorithm a chance to become applicable with
     * specific options.
     * </p>
     * 
     * Defaults to empty array.
     * 
     * @return A Collection of additional Options. For each option an additional
     *         instance of the implementing algorithm is created. May not be
     *         {@code null}. Each element in the array may not be {@code null}
     *         or empty.
     */
    public List<?>[] getOptions() {
        return new List<?>[] {};
    }

}
