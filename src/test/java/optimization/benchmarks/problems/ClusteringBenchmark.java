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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import optimization.benchmarks.AnalyzerBenchmark;
import optimization.benchmarks.IOptBenchmark;
import optimization.clustering.ClusteringProblem;
import optimization.clustering.Point;

/**
 * Creates instances of the {@link optimization.clustering.ClusteringProblem} that 
 * are to be used for benchmarks. 
 * 
 * @author mburri
 *
 */
public class ClusteringBenchmark implements IOptBenchmark<ClusteringProblem> {

    public List<ClusteringProblem> getProblemInstances() {
        final List<ClusteringProblem> ret = new ArrayList<ClusteringProblem>();

        // add our instance: 7 points
        final List<Point> points = new ArrayList<>();
        points.add(new Point(10, 10));
        points.add(new Point(20, 15));
        points.add(new Point(9, 13));
        points.add(new Point(17, 17));
        points.add(new Point(3, 11));
        points.add(new Point(8, 9));
        points.add(new Point(30, 18));
        final ClusteringProblem cp = new ClusteringProblem(points);
        ret.add(cp);

        ret.add(new ClusteringProblem(100));

        ret.add(new ClusteringProblem(1000));

        return ret;
    }

    @Override
    public Map<ClusteringProblem, AnalyzerBenchmark> getProblemInstancesWithParameters() {
        Map<ClusteringProblem, AnalyzerBenchmark> ret = new HashMap<ClusteringProblem, AnalyzerBenchmark>();
        ret.put(getProblemInstances().get(0),
                new AnalyzerBenchmark(1, 5, 100, 200));
        ret.put(getProblemInstances().get(1),
                new AnalyzerBenchmark(1, 15, 5000, 10000));
        ret.put(getProblemInstances().get(2),
                new AnalyzerBenchmark(1, 15, 500000, 1000000));
        return ret;
    }

}
