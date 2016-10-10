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
package butt.tool.benchmark.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import butt.tool.benchmark.IBenchmarkRunner;
import butt.tool.benchmark.IRunnableBenchmark;
import gps.games.wrapper.Game;
import gps.util.reflections.ReflectionsHelper;

/**
 * Class that provides utility functions for benchmarks provided in this tool.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class Benchmarks {

    /**
     * The logger for this class
     */
    private static final Logger LOGGER = Logger
            .getLogger(Benchmarks.class.getCanonicalName());

    static {
        LOGGER.setLevel(Level.FINE);
    }

    /**
     * Instantiate a benchmark runner. Benchmark Runner classes must provide a
     * public constructor that accepts no arguments.
     * 
     * @param pClass
     *            The class to instantiate.
     * @return A newly created instance.
     */
    private static IBenchmarkRunner<?> instantiateBenchmarkRunner(
            @SuppressWarnings("rawtypes") Class<? extends IBenchmarkRunner> pClass) {
        try {
            return pClass.newInstance();
        } catch (InstantiationException | IllegalAccessException exception) {
            // throw exception since benchmarks are identified by
            // their
            // index. So we cannot continue without creating a mess.
            throw new RuntimeException("Cannot instantiate benchmark class",
                    exception);
        }
    }

    /**
     * Construct all classes that implement the IBechmarkRunner interface and
     * collect all runnable benchmarks.
     */
    public static List<IRunnableBenchmark<?>> constructAllBenchmarks() {
        @SuppressWarnings("rawtypes")
        final Set<Class<? extends IBenchmarkRunner>> subTypes = ReflectionsHelper
                .getSubTypesOfCached("benchmarks.*", IBenchmarkRunner.class);
        List<IRunnableBenchmark<?>> benchmarks = new ArrayList<>();
        LOGGER.logp(Level.FINE, Benchmarks.class.getCanonicalName(),
                "constructAllBenchmarks", "Constructing all benchmarks");
        subTypes.stream().sorted(
                (x, y) -> x.getCanonicalName().compareTo(y.getCanonicalName()))

                .forEach(e -> benchmarks
                        .addAll(instantiateBenchmarkRunner(e).getRunners()));

        LOGGER.logp(Level.FINE, Benchmarks.class.getCanonicalName(),
                "constructAllBenchmarks",
                ".. done total of " + benchmarks.size() + " benchmarks");
        return benchmarks;
    }

    /**
     * Construct all classes that implement the IBechmarkRunner interface and
     * collect all runnable benchmarks.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Stream<Game<?>> benchmarksGamesStream() {
        final Set<Class<? extends IBenchmarkRunner>> subTypes = ReflectionsHelper
                .getSubTypesOfCached("benchmarks.*", IBenchmarkRunner.class);

        return (Stream) subTypes.stream()
                .map(m -> instantiateBenchmarkRunner(m)
                        .getProblemInstancesStream())
                .reduce((a, b) -> Stream.concat(a, b)).orElse(Stream.empty());
    }

    /**
     * Singleton class does not provide a constructor
     */
    private Benchmarks() {

    }
}
