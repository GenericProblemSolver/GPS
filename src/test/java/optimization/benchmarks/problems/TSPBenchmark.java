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
import optimization.tsp.City;
import optimization.tsp.TSP;

/**
 * Creates instances of the {@link optimization.tsp.TSP} that 
 * are to be used for benchmarks. 
 * 
 * @author mburri
 *
 */
public class TSPBenchmark implements IOptBenchmark<TSP> {

    public List<TSP> getProblemInstances() {
        List<TSP> ret = new ArrayList<TSP>();

        // instance with 20 nodes
        List<City> cities = new ArrayList<City>();
        cities.add(new City(60, 200));
        cities.add(new City(180, 200));
        cities.add(new City(80, 180));
        cities.add(new City(140, 180));
        cities.add(new City(20, 160));
        cities.add(new City(100, 160));
        cities.add(new City(200, 160));
        cities.add(new City(140, 140));
        cities.add(new City(40, 120));
        cities.add(new City(100, 120));
        cities.add(new City(180, 100));
        cities.add(new City(60, 80));
        cities.add(new City(120, 80));
        cities.add(new City(180, 60));
        cities.add(new City(20, 40));
        cities.add(new City(100, 40));
        cities.add(new City(200, 40));
        cities.add(new City(20, 20));
        cities.add(new City(60, 20));
        cities.add(new City(160, 20));
        TSP t = new TSP(cities);
        ret.add(t);

        // random instance with 1000 nodes
        TSP t2 = new TSP(1000);
        ret.add(t2);

        // random instance with 10000 nodes
        TSP t3 = new TSP(10000);
        ret.add(t3);

        // random instance with 20000 nodes
        TSP t4 = new TSP(20000);
        ret.add(t4);

        return ret;
    }

    @Override
    public Map<TSP, AnalyzerBenchmark> getProblemInstancesWithParameters() {
        Map<TSP, AnalyzerBenchmark> ret = new HashMap<TSP, AnalyzerBenchmark>();
        // first instance
        ret.put(getProblemInstances().get(0),
                new AnalyzerBenchmark(1, 1, 2000, 3000));
        ret.put(getProblemInstances().get(1),
                new AnalyzerBenchmark(1, 1, 500000, 600000));
        ret.put(getProblemInstances().get(2),
                new AnalyzerBenchmark(1, 1, 500000, 6000000));
        ret.put(getProblemInstances().get(3),
                new AnalyzerBenchmark(1, 1, 9000000, 11000000));
        return ret;
    }

}
