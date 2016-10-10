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
package optimization.benchmarks;

import gps.optimization.OptimizationReturn;
import gps.optimization.algorithm.AbstractOptimizer;
import gps.optimization.analysis.Analyzer;

/**
 * Represents a benchmark result for a problem that is to be optimized.
 * 
 * @author mburri
 *
 * @param <T>
 * 		the type of the problem class
 */
public class BenchmarkResult<T> {

    /**
     * the problem
     */
    private final T problem;

    /**
     * the found solution
     */
    private final OptimizationReturn solution;

    /**
     * the time used for the optimization process
     */
    private final long usedTime;

    /**
     * the number of used steps during the process
     */
    private final int usedSteps;

    /**
     * the instance of the Analyzer ({@code null} if this is 
     * not an analyzer benchmark)
     */
    private final Analyzer<T> analyzerInstance;

    /**
     * the AnalyzerBenchmark containing the expected parameters
     * ({@code null} if this is not an analyzer benchmark}
     */
    private final AnalyzerBenchmark analyzerB;

    /**
     * the solver that was used for the benchmark 
     * ({@code null} if this is an analyzer benchmark}
     */
    private final AbstractOptimizer<T> optimizer;

    /**
     * Creates a new BenchmarkResult for the given parameters. 
     * This constructor is to be used if the benchmark is an 
     * analyzer benchmark. 
     * 
     * @param pProblem
     * @param pSolution
     * @param pUsedTime
     * @param pAnalyzerInstance
     * @param aB
     */
    public BenchmarkResult(final T pProblem, final OptimizationReturn pSolution,
            final long pUsedTime, final Analyzer<T> pAnalyzerInstance,
            final AnalyzerBenchmark aB) {
        problem = pProblem;
        solution = pSolution;
        usedTime = pUsedTime;
        analyzerInstance = pAnalyzerInstance;
        usedSteps = -1;
        analyzerB = aB;
        optimizer = null;
    }

    /**
     * Creates a new BenchmarkResult for the given parameters. 
     * This constructor is to be used if the benchmark is not an 
     * analyzer benchmark.
     * 
     * @param pProblem
     * @param pSolution
     * @param pUsedTime
     * @param pOptimizer
     * @param pUsedSteps
     */
    public BenchmarkResult(final T pProblem, final OptimizationReturn pSolution,
            final long pUsedTime, final AbstractOptimizer<T> pOptimizer,
            final int pUsedSteps) {
        problem = pProblem;
        solution = pSolution;
        usedTime = pUsedTime;
        analyzerInstance = null;
        analyzerB = null;
        usedSteps = pUsedSteps;
        optimizer = pOptimizer;
    }

    public T getProblem() {
        return problem;
    }

    public OptimizationReturn getSolution() {
        return solution;
    }

    public long getUsedTime() {
        return usedTime;
    }

    public int getUsedSteps() {
        return usedSteps;
    }

    public Analyzer<T> getAnalyzerInstance() {
        return analyzerInstance;
    }

    public AbstractOptimizer<T> getOptimizer() {
        return optimizer;
    }

    public AnalyzerBenchmark getAnalyzerB() {
        return analyzerB;
    }

    /**
     * Returns whether this BenchmarkResult was created for an 
     * analyzer benchmark or not. 
     * 
     * @return	{@code true} if it was created for an analyzer benchmark,
     * 			{@code false} otherwise
     */
    public boolean hasAnalyzerBenchmark() {
        return analyzerInstance != null;
    }

}
