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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gps.GPS;
import gps.optimization.OptimizationReturn;
import gps.optimization.algorithm.AbstractOptimizer;
import gps.optimization.analysis.Analyzer;
import gps.optimization.analysis.utility.SolverInstantiation;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.flattening.Flattener;
import gps.optimization.wrapper.Optimizable;
import optimization.benchmarks.gui.OptBenchmarkVisualization;
import optimization.benchmarks.problems.ClusteringBenchmark;
import optimization.benchmarks.problems.LayoutingBenchmark;
import optimization.benchmarks.problems.PPBenchmark;
import optimization.benchmarks.problems.TSPBenchmark;

/**
 * Main class used to run all available benchmarks.
 * Also initiates the visualization of the results.
 * 
 * @author mburri
 *
 */
public class OptBenchmarksMain {

    /**
     * Runs all benchmarks and creates the Window showing the 
     * results. 
     * 
     * @param args
     */
    public static void main(String[] args) {
        List<BenchmarkResult<?>> benchmarks = new ArrayList<BenchmarkResult<?>>();
        benchmarks.addAll(
                runBenchmarksForClass(new TSPBenchmark(), (byte) -1, false));
        benchmarks.addAll(runBenchmarksForClass(new ClusteringBenchmark(),
                (byte) -1, false));
        benchmarks.addAll(
                runBenchmarksForClass(new PPBenchmark(), (byte) 1, false));
        benchmarks.addAll(runBenchmarksForClass(new LayoutingBenchmark(),
                (byte) -1, true));
        new OptBenchmarkVisualization(benchmarks).setVisible(true);
    }

    /**
     * Runs the benchmarks of the given {@link optimization.benchmarks.IOptBenchmark}. 
     * 
     * @param benchmark
     * 			the IOptBenchmark containing the benchmarks to be run
     * @param inefficientObjectiveFunction
     * 			indicates whether the objective function is inefficient or not. 
     * 			Avoids very large benchmark run times.
     * 			{@code true} if it is inefficient, {@code false} otherwise
     * @param maximize
     * 			indicates whether the problem is to be maximized or minimized
     * 			-1 if it is to be minimized, 1 otherwise
     * @return
     * 			the BenchmarkResults created by running the given benchmarks.
     */
    public static <T> List<BenchmarkResult<?>> runBenchmarksForClass(
            final IOptBenchmark<T> benchmark, final byte maximize,
            final boolean inefficientObjectiveFunction) {
        List<BenchmarkResult<?>> ret = new ArrayList<BenchmarkResult<?>>();
        for (Map.Entry<T, AnalyzerBenchmark> bm : benchmark
                .getProblemInstancesWithParameters().entrySet()) {
            T currentInstance = bm.getKey();
            AnalyzerBenchmark aB = bm.getValue();
            Optimizable<T> opt = new Optimizable<>(GPS.wrap(currentInstance));
            opt.setMaximize(maximize);

            // Add analyzer benchmark
            ret.add(getAnalyzerBenchmark(opt, currentInstance, aB, maximize));

            SolverInstantiation<T> sI = new SolverInstantiation<>(opt,
                    getFlatObjectsForProblem(opt));

            // Add solver benchmarks
            ret.addAll(getSolverBenchmarks(sI, opt, currentInstance, aB,
                    maximize, inefficientObjectiveFunction));
        }
        return ret;
    }

    /**
     * Returns a BenchmarkResult for the given problem and the given 
     * AnalyzerBenchmark containing a set of expected parameters.
     * 
     * @param opt
     * 			the Optimizable wrapping the problem
     * @param currentInstance
     * 			the problem instance
     * @param aB
     * 			the corresponding AnalyzerBenchmark 
     * @param maximize
     * 			-1 if the problem is to be minimized, 1 otherwise
     * @return
     * 			the BenchmarkResult
     */
    public static <T> BenchmarkResult<T> getAnalyzerBenchmark(
            final Optimizable<T> opt, final T currentInstance,
            final AnalyzerBenchmark aB, final byte maximize) {
        long cT = System.currentTimeMillis();
        Analyzer<T> a = new Analyzer<T>(opt, maximize, true, 20000);
        OptimizationReturn solution = a.solve().get();
        return new BenchmarkResult<T>(currentInstance, solution,
                System.currentTimeMillis() - cT, a, aB);
    }

    /**
     * Returns a List of BenchmarkResults for the given SolverInstantiation 
     * and problem. 
     * 
     * @param sI
     * 			the SolverInstantion that is to be used to create solver instances
     * @param opt
     * 			the Optimizable wrapping the problem
     * @param currentInstance
     * 			the problem instance
     * @param aB
     * 			the AnalyzerBenchmark (used to calculate solver parameters)
     * @param maximize
     * 			-1 if the problem is to be minimized, 1 otherwise
     * @param inefficientObjectiveFunction
     * 			indicates how many solver instances are to be used for the benchmarks
     * 			{@code false} if solvers with up to 2000 steps are to be used, 
     * 			{@code true} otherwise
     * @return
     * 			a list of BenchmarkResults
     */
    public static <T> List<BenchmarkResult<T>> getSolverBenchmarks(
            final SolverInstantiation<T> sI, final Optimizable<T> opt,
            final T currentInstance, final AnalyzerBenchmark aB,
            final byte maximize, final boolean inefficientObjectiveFunction) {
        List<BenchmarkResult<T>> ret = new ArrayList<BenchmarkResult<T>>();

        // Add solver benchmarks - 100 Steps
        ret.addAll(getSolverBenchmarks(sI, opt, currentInstance, 100, aB,
                maximize));
        // Add solver benchmarks - 1000 Steps
        ret.addAll(getSolverBenchmarks(sI, opt, currentInstance, 1000, aB,
                maximize));

        if (!inefficientObjectiveFunction) {
            // Add solver benchmarks - 8000 Steps
            ret.addAll(getSolverBenchmarks(sI, opt, currentInstance, 6000, aB,
                    maximize));
        }

        return ret;
    }

    /**
     * Returns a List of BenchmarkResults for the given SolverInstantiation 
     * and problem. 
     * 
     * @param sI
     * 			the SolverInstantion that is to be used to create solver instances
     * @param opt
     * 			the Optimizable wrapping the problem
     * @param currentInstance
     * 			the problem instance
     * @param steps
     * 			the amount of steps the solvers are allowed to take
     * @param aB
     * 			the AnalyzerBenchmark (used to calculate solver parameters)
     * @param maximize
     * 			-1 if the problem is to be minimized, 1 otherwise
     * @return
     * 			a list of BenchmarkResults
     */
    public static <T> List<BenchmarkResult<T>> getSolverBenchmarks(
            final SolverInstantiation<T> sI, final Optimizable<T> opt,
            final T currentInstance, final int steps,
            final AnalyzerBenchmark aB, final byte maximize) {
        List<BenchmarkResult<T>> ret = new ArrayList<BenchmarkResult<T>>();
        List<AbstractOptimizer<T>> solvers = sI.getSolversWithParameters(
                Math.abs((aB.getExpectedStepWidthMax()
                        - aB.getExpectedStepWidthMin()) / 2),
                Math.abs((aB.getExpectedObjectiveFunctionScaleMin()
                        - aB.getExpectedObjectiveFunctionScaleMax()) / 2),
                steps, maximize);
        for (AbstractOptimizer<T> s : solvers) {
            s.setMaxSteps(steps);
            long cT = System.currentTimeMillis();
            OptimizationReturn temp = maximize == -1 ? s.minimize().get()
                    : s.maximize().get();
            BenchmarkResult<T> res1 = new BenchmarkResult<T>(currentInstance,
                    temp, System.currentTimeMillis() - cT, s, steps);
            ret.add(res1);
        }
        return ret;
    }

    /**
     * Utility method that gets the FlatObjects for the given Optimizable. 
     * 
     * @param opt
     * 			the Optimizable that the FlatObjects-Object is to be created for
     * @return
     * 			the FlatObjects-Object
     */
    public static FlatObjects getFlatObjectsForProblem(
            final Optimizable<?> opt) {
        if (opt.hasNeighborFunction()) {
            Object[] defParams = opt.getDefaultParams();
            Set<Class<?>> defParamTypes = new HashSet<>();
            for (Object o : defParams) {
                defParamTypes.add(o.getClass());
            }
            return new Flattener().flattenAndWrap(defParams, defParamTypes);
        } else {
            return new Flattener().flattenAndWrap(opt.getDefaultParams());
        }
    }

}
