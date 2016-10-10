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
package optimization.analysis;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

import gps.GPS;
import gps.optimization.OptimizationReturn;
import gps.optimization.analysis.utility.ObjectiveFunctionAnalyzer;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.flattening.Flattener;
import gps.optimization.wrapper.Optimizable;
import optimization.clustering.ClusteringProblem;
import optimization.clustering.Point;
import optimization.pp.PlaygroundProblem;
import optimization.tsp.City;
import optimization.tsp.TSP;

/**
 * Tests the {@link gps.optimization.analysis.Analyzer} class 
 * and also the used utility classes in the gps.optimization.analysis.utilty package.
 * 
 * @author mburri
 *
 */
public class AnalyzerTest {

    /**
     * Tests the {@link gps.GPS#maximize()} function behavior.
     * There is probably a better place for this test
     */
    @Test
    public void testGPSMethodMaximize() {
        Optional<OptimizationReturn> result = new GPS<>(new PlaygroundProblem())
                .maximize();
        assertTrue(result.isPresent());
        OptimizationReturn ret = result.get();
        assertTrue(ret.getBestSolutionEval() > 0);
        assertTrue(ret.getBestSolution().length == 2);
        assertTrue(ret.getBestSolution()[0].getClass().equals(Integer.class)
                || ret.getBestSolution()[0].getClass().equals(Long.class));
        assertTrue(ret.getBestSolution()[1].getClass().equals(Integer.class)
                || ret.getBestSolution()[0].getClass().equals(Long.class));
    }

    /**
     * Tests the {@link gps.GPS#minimize()} function behavior.
     * There is probably a better place for this test.
     */
    @Test
    public void testGPSMethodMinimize() {
        Optional<OptimizationReturn> result = new GPS<>(new PlaygroundProblem())
                .minimize();
        assertTrue(result.isPresent());
        OptimizationReturn ret = result.get();
        assertTrue(ret.getBestSolutionEval() < 72);
        assertTrue(ret.getBestSolution().length == 2);
        assertTrue(ret.getBestSolution()[0].getClass().equals(Integer.class));
        assertTrue(ret.getBestSolution()[1].getClass().equals(Integer.class));
    }

    /**
     * Tests the {@link gps.GPS#maximize()} function behavior with 
     * a class containing an {@link gps.annotations.Neighbor} annotated method.
     * There is probably a better place for this test
     */
    @Test
    public void testGPSMethodMaximizeNeighbor() {
        List<City> cities = new ArrayList<City>();
        cities.add(new City(10, 10));
        cities.add(new City(20, 20));

        Optional<OptimizationReturn> result = new GPS<>(new TSP(cities))
                .maximize();
        assertTrue(result.isPresent());
        OptimizationReturn ret = result.get();
        assertTrue(ret.getBestSolutionEval() > 0);
        assertTrue(ret.getBestSolution().length == 1);
        assertTrue(ret.getBestSolution()[0].getClass().equals(ArrayList.class));
    }

    /**
     * Tests the {@link gps.GPS#minimize()} function behavior with 
     * a class containing an {@link gps.annotations.Neighbor} annotated method.
     * There is probably a better place for this test
     */
    @Test
    public void testGPSMethodMinimizeNeighbor() {
        List<City> cities = new ArrayList<City>();
        cities.add(new City(10, 10));
        cities.add(new City(20, 20));

        Optional<OptimizationReturn> result = new GPS<>(new TSP(cities))
                .minimize();
        assertTrue(result.isPresent());
        OptimizationReturn ret = result.get();
        assertTrue(ret.getBestSolutionEval() > 0);
        assertTrue(ret.getBestSolution().length == 1);
        assertTrue(ret.getBestSolution()[0].getClass().equals(ArrayList.class));
    }

    /**
     * Tests the 
     * {@link gps.optimization.analysis.utility.ObjectiveFunctionAnalyzer#getOptimalStepWidth()}
     * function
     */
    @Test
    public void voidTestObjFunctionAnalyzerOptStepWidth() {
        final ArrayList<Point> points = new ArrayList<>();
        points.add(new Point(10, 10));
        Optimizable<ClusteringProblem> opt = new Optimizable<>(
                GPS.wrap(new ClusteringProblem(points)));
        opt.setMaximize((byte) -1);
        ObjectiveFunctionAnalyzer oA = new ObjectiveFunctionAnalyzer(opt,
                (byte) -1,
                new Flattener().flattenAndWrap(opt.getDefaultParams()), 200);
        double stepWidth = oA.getOptimalStepWidth();
        assertTrue(stepWidth > 0 && stepWidth <= 15);
    }

    /**
     * Tests the 
     * {@link gps.optimization.analysis.utility.ObjectiveFunctionAnalyzer#getObjectiveFunctionScale(int)}
     * function
     */
    @Test
    public void TestObjFunctionAnalyzerObjFunctionScaleWParam() {
        final ArrayList<Point> points = new ArrayList<>();
        points.add(new Point(10, 10));
        Optimizable<ClusteringProblem> opt = new Optimizable<>(
                GPS.wrap(new ClusteringProblem(points)));
        opt.setMaximize((byte) -1);
        ObjectiveFunctionAnalyzer oA = new ObjectiveFunctionAnalyzer(opt,
                (byte) -1,
                new Flattener().flattenAndWrap(opt.getDefaultParams()), 200);
        assertTrue(oA.getObjectiveFunctionScale(1) > 0);
    }

    /**
     * Tests the 
     * {@link gps.optimization.analysis.utility.ObjectiveFunctionAnalyzer#getObjectiveFunctionScale()}
     * function
     */
    @Test
    public void TestObjFunctionAnalyzerObjFunctionScale() {
        final ArrayList<City> cities = new ArrayList<>();
        cities.add(new City(10, 10));
        cities.add(new City(20, 20));
        Optimizable<TSP> opt = new Optimizable<>(GPS.wrap(new TSP(cities)));
        opt.setMaximize((byte) -1);
        Object[] defParams = opt.getDefaultParams();
        Set<Class<?>> defParamTypes = new HashSet<>();
        for (Object o : defParams) {
            defParamTypes.add(o.getClass());
        }
        FlatObjects fO = new Flattener().flattenAndWrap(defParams,
                defParamTypes);
        ObjectiveFunctionAnalyzer oA = new ObjectiveFunctionAnalyzer(opt,
                (byte) -1, fO, 200);
        assertTrue(oA.getObjectiveFunctionScale() > 0);
    }

}
