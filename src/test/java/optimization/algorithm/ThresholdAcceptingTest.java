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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import gps.GPS;
import gps.optimization.OptimizationReturn;
import gps.optimization.algorithm.thresholdAccepting.ThresholdAccepting;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.flattening.Flattener;
import gps.optimization.wrapper.Optimizable;
import optimization.clustering.ClusteringProblem;
import optimization.clustering.Point;

/**
 * Tests the {@link gps.optimization.algorithm.thresholdAccepting.ThresholdAccepting}
 * algorithm.
 * 
 * @author mburri
 *
 */
public class ThresholdAcceptingTest {

    /**
     * the instance of the solver 
     */
    private ThresholdAccepting<ClusteringProblem> tA;

    /**
     * the wrapped problem that will be used for the test
     */
    private Optimizable<ClusteringProblem> opt;

    /**
     * Initializes the Threshold Accepting Solver and 
     * the ClusteringProblem that is to be optimized 
     * and used for the test
     */
    @Before
    public void init() {
        // Define the data points for which the centroids are wanted
        List<Point> data = new ArrayList<Point>();
        data.add(new Point(0, 0));
        ClusteringProblem cl = new ClusteringProblem(data);

        Flattener f = new Flattener();

        opt = new Optimizable<>(GPS.wrap(cl));
        FlatObjects fO = f.flattenAndWrap(opt.getDefaultParams());
        tA = new ThresholdAccepting<ClusteringProblem>(opt, fO, 100, 0.5);

    }

    /**
     * Tests the {@link gps.optimization.algorithm.thresholdAccepting.ThresholdAccepting#opimizeOneStep}
     * -function if the problem is to be minimized.
     * Since the initial threshold is -100, in the first step every possible
     * solution that can be created by the solver will be accepted as the new current solution. 
     * Also asserts that the threshold is decreased.
     */
    @Test
    public void minimizeOneStep() {
        tA.setMaxSteps(1);
        double initThreshold = tA.getCurrentThreshold();
        Optional<OptimizationReturn> result = tA.minimize();
        Point c1 = (Point) result.get().getBestSolution()[0];
        Point c2 = (Point) result.get().getBestSolution()[1];
        // Assert that the initial centroids were changed
        assertTrue(c1.getX() != 0.0 && c1.getY() != 0.0);
        assertTrue(c2.getX() != 0.0 && c2.getY() != 0.0);

        // Assert that the threshold was decreased
        assertTrue(initThreshold > tA.getCurrentThreshold());
    }

    /**
     * Tests the {@link gps.optimization.algorithm.thresholdAccepting.ThresholdAccepting#opimizeOneStep}
     * -function if the problem is to be maximized.
     * Since the initial threshold is -100, in the first step every possible
     * solution that can be created by the solver will be accepted as the new current solution. 
     * Also asserts that the threshold is decreased.
     */
    @Test
    public void maximizeOneStep() {
        tA.setMaxSteps(1);
        double initThreshold = tA.getCurrentThreshold();
        Optional<OptimizationReturn> result = tA.maximize();
        Point c1 = (Point) result.get().getBestSolution()[0];
        Point c2 = (Point) result.get().getBestSolution()[1];
        // Assert that the initial centroids were changed
        assertTrue(c1.getX() != 0.0 && c1.getY() != 0.0);
        assertTrue(c2.getX() != 0.0 && c2.getY() != 0.0);

        // Assert that the threshold was decreased
        assertTrue(initThreshold > tA.getCurrentThreshold());
    }

}
