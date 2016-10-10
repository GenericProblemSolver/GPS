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
package butt.tool.benchmark;

import java.util.Optional;

import butt.tool.benchmark.common.CSV;
import gps.ISolverModule;
import gps.ResultEnum;
import gps.common.IBenchmark;
import gps.common.AbstractAlgorithm;

/**
 * Represents a single runnable benchmark.
 * 
 * @author haker@uni-bremen.de
 *
 */
public interface IRunnableBenchmark<T> {
    /**
     * Run the benchmark and append the gathered data to the csv object.
     * 
     * @param out
     *            The csv object to where the gathered data is appended to.
     */
    public void run(Optional<CSV> out);

    /**
     * Predicate for filtering benchmarks.
     * 
     * @param pRegExpAlgorithm
     *            The algorithm class must match this regular expression
     * @param pRegExpResult
     *            The result type must match this regular expression
     * @param pRegExpProblem
     *            The problem name that must match this regular expression. The
     *            problem name is the name that is printed in the csv file.
     * @return {@code true} if all regular expressions match the benchmark.
     *         {@code false} otherwise.
     */
    public boolean isApplicable(String pRegExpAlgorithm, String pRegExpResult,
            String pRegExpProblem);

    /**
     * Get the algorithm of the runnable benchmark.
     * 
     * @return The Algorithm that has been benchmarked.
     */
    public AbstractAlgorithm getAlgorithm();

    /**
     * Get the result type of the runnable benchmark.
     * 
     * @return The result type that has been benchmarked.
     */
    public ResultEnum getResultType();

    /**
     * Get the problem that has been benchmarked.
     * 
     * @return The name of the problem that has been benchmarked.
     */
    public String getBenchmarkName();

    /**
     * Return a copy of the original problem object.
     * 
     * @return The problem.
     */
    public T getProblem();

    /**
     * Get the benchmark fields the has been provided by the algorithm after
     * stopping the benchmark.
     * 
     * @return The interface to the benchmark fields.
     */
    public IBenchmark getBenchmark();

    /**
     * Get the solver module for this problem.
     * 
     * @return The solver module.
     */
    public ISolverModule<T> getModule();
}
