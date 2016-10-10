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
package optimization.algorithm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import gps.GPS;
import gps.optimization.OptimizationReturn;
import gps.optimization.algorithm.particleSwarmOpt.ParticleSwarm;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.flattening.Flattener;
import gps.optimization.wrapper.Optimizable;
import optimization.clustering.ClusteringProblem;
import optimization.clustering.Point;

/**
 * Tests the {@link gps.optimization.algorithm.particleSwarmOpt.ParticleSwarm}
 * algorithm.
 * 
 * @author mburri
 *
 */
public class ParticleSwarmTest {

    /**
     * the instance of the solver 
     */
    private ParticleSwarm<ClusteringProblem> pS;

    /**
     * the wrapped problem that will be used for the test
     */
    private Optimizable<ClusteringProblem> opt;

    /**
     * Initializes the ParticleSwarm and 
     * the ClusteringProblem that is to be optimized 
     * and used for the test
     */
    @Before
    public void init() {
        // Define the data points for which the centroids are wanted
        List<Point> data = new ArrayList<Point>();
        data.add(new Point(1, 1));
        ClusteringProblem cl = new ClusteringProblem(data);

        Flattener f = new Flattener();

        opt = new Optimizable<>(GPS.wrap(cl));
        FlatObjects fO = f.flattenAndWrap(opt.getDefaultParams());
        pS = new ParticleSwarm<ClusteringProblem>(opt, fO, 5);

    }

    /**
     * Test minimize with 20 steps, makes sure that the initial points are 
     * not just returned but optimized, at least better than the initial points
     * (with the given set of data points, the initial solution is far from minimal).
     * Number of particles in the swarm is set to 5.
     */
    @Ignore
    @Test
    public void minimizeClustering100Steps() {
        pS.setMaxSteps(20);
        Optional<OptimizationReturn> result = pS.minimize();
        Point c1 = (Point) result.get().getBestSolution()[0];
        Point c2 = (Point) result.get().getBestSolution()[1];
        // Assert that the initial centroids were changed
        assertTrue(c1.getX() != 0.0 && c1.getY() != 0.0);
        assertTrue(c2.getX() != 0.0 && c2.getY() != 0.0);
        // Assert that the found centroids are better than the initial ones
        assertTrue(opt.objectiveFunction(
                new Object[] { new Point(0.0, 0.0), new Point(0.0, 0.0) }) > opt
                        .objectiveFunction(result.get().getBestSolution()));

    }

    /**
     * Test maximize with 20 steps, makes sure that the initial points are 
     * not just returned but 'optimized', at least 'better' than the initial points
     * (with the given set of data points, the initial solution is far from maximal)
     * Number of particles in the swarm is set to 5.
     */
    @Ignore
    @Test
    public void maximizeClustering100Steps() {
        pS.setMaxSteps(20);
        Optional<OptimizationReturn> result = pS.maximize();
        Point c1 = (Point) result.get().getBestSolution()[0];
        Point c2 = (Point) result.get().getBestSolution()[1];
        // Assert that the initial centroids were changed
        assertTrue(c1.getX() != 0.0 && c1.getY() != 0.0);
        assertTrue(c2.getX() != 0.0 && c2.getY() != 0.0);
        // Assert that the found centroids are 'worse' than the initial ones
        assertTrue(opt.objectiveFunction(
                new Object[] { new Point(0.0, 0.0), new Point(0.0, 0.0) }) < opt
                        .objectiveFunction(result.get().getBestSolution()));
    }

    /**
     * Tests the {@link gps.optimization.algorithm.particleSwarmOpt.ParticleSwarm#onlyNumbers}-
     * function.
     */
    @SuppressWarnings("static-access")
    @Test
    public void testOnlyNumbers() {
        Flattener f = new Flattener();

        // Test flattened user defined class
        FlatObjects f1 = f.flattenAndWrap(
                new Object[] { new Point(0, 0), new Point(1, 32) });
        assertTrue(pS.onlyNumbers(f1));

        // Test array of different number types (arbitrarily chosen)
        FlatObjects f2 = f.flattenAndWrap(new Object[] { new Double(10),
                (double) 1, new Integer(12), (byte) 3 });
        assertTrue(pS.onlyNumbers(f2));

        // Test array containing something not instance of Number
        FlatObjects f3 = f.flattenAndWrap(new Object[] { 12, 1.2, "test" });
        assertFalse(pS.onlyNumbers(f3));
    }

}
