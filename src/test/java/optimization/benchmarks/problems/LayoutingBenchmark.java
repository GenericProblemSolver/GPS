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
package optimization.benchmarks.problems;

import java.util.HashMap;
import java.util.Map;

import optimization.benchmarks.AnalyzerBenchmark;
import optimization.benchmarks.IOptBenchmark;
import optimization.layouting.Layouting;

/**
 * Creates instances of the {@link optimization.layouting.Layouting} problem that 
 * are to be used for benchmarks. 
 * 
 * @author mburri
 *
 */
public class LayoutingBenchmark implements IOptBenchmark<Layouting> {

    @Override
    public Map<Layouting, AnalyzerBenchmark> getProblemInstancesWithParameters() {
        Map<Layouting, AnalyzerBenchmark> ret = new HashMap<Layouting, AnalyzerBenchmark>();

        ret.put(new Layouting(), new AnalyzerBenchmark(1, 1, 150000, 250000));

        return ret;
    }

}
