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

import java.util.List;
import java.util.stream.Stream;

import gps.games.wrapper.Game;

/**
 * Benchmark runner. Provides Benchmarks. The class implementing this interface
 * can provide multiple benchmarks that must not be related to each other.
 * 
 * @author haker@uni-bremen.de
 *
 */
public interface IBenchmarkRunner<T> {

    /**
     * Get all benchmarks that this class provides.
     * 
     * @return A list of benchmarks.
     */
    public List<IRunnableBenchmark<T>> getRunners();

    /**
     * Return a stream of the problem instances.
     * 
     * @return The streeam.
     */
    public Stream<Game<T>> getProblemInstancesStream();
}
