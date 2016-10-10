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
import optimization.pp.PlaygroundProblem;

/**
 * Creates instances of the {@link optimization.pp.PlaygroundProblem} that 
 * are to be used for benchmarks. 
 * 
 * @author mburri
 *
 */
public class PPBenchmark implements IOptBenchmark<PlaygroundProblem> {

    @Override
    public Map<PlaygroundProblem, AnalyzerBenchmark> getProblemInstancesWithParameters() {
        Map<PlaygroundProblem, AnalyzerBenchmark> ret = new HashMap<PlaygroundProblem, AnalyzerBenchmark>();

        ret.put(new PlaygroundProblem(24), new AnalyzerBenchmark(1, 5, 20, 80));

        ret.put(new PlaygroundProblem(100),
                new AnalyzerBenchmark(1, 10, 500, 1500));

        return ret;
    }

}
