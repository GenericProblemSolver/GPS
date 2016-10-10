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
package optimization.clustering;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import gps.GPS;
import gps.optimization.wrapper.Optimizable;

/**
 * Tests the {@link ClusteringProblem}
 * 
 * @author mburri
 *
 */
public class ClusteringTest {

    /**
     * The instance of the ClusteringProblem that is to be tested
     */
    ClusteringProblem cl;

    /**
     * The data points in the clustering
     */
    List<Point> data;

    /**
     * Initializes the instance of the clustering problem with 
     * a set of points that was chosen (kind of) arbitrarily
     */
    @Before
    public void init() {
        // Define the data points for which the centroids are wanted
        data = new ArrayList<Point>();
        data.add(new Point(0, 0));
        data.add(new Point(0, 2));
        data.add(new Point(2, 0));
        data.add(new Point(2, 2));
        data.add(new Point(6, 6));
        data.add(new Point(8, 6));
        data.add(new Point(6, 8));
        data.add(new Point(8, 8));
        cl = new ClusteringProblem(data);
    }

    /**
     * Tests whether the wrapping works for the clustering problem
     */
    @Test
    public void testWrapping() {
        Optimizable<ClusteringProblem> opt = new Optimizable<>(GPS.wrap(cl));
        opt.setMaximize((byte) -1);
        assertTrue(opt.hasObjectiveFunction());

        Point centroid1 = new Point(0.0, 2.1); // chosen arbitrarily
        Point centroid2 = new Point(2.5, 10.9); // chosen arbitrarily

        assertTrue(cl.evalTwoClusterCenters(centroid1, centroid2) == (-1)
                * opt.objectiveFunction(new Object[] { centroid1, centroid2 }));
    }

    /**
     * Tests a clustering problem with an empty data set
     */
    @Test
    public void testEmptyDataSet() {
        data.clear();
        cl = new ClusteringProblem(data);
        Point centroid1 = new Point(0.0, 2.1); // chosen arbitrarily
        Point centroid2 = new Point(2.5, 10.9); // chosen arbitrarily
        assertTrue(cl.evalTwoClusterCenters(centroid1, centroid2) == 0);
    }

    /**
     * Tests the problems method that is to be optimized with null-parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void testEvalNull() {
        cl.evalTwoClusterCenters(null, null);
    }

    /**
     * Tests the evaluation-Method of the problem
     */
    @Test
    public void testEval() {
        // the two optimal centroids for this instance of the problem
        Point opt1 = new Point(1, 1);
        Point opt2 = new Point(7, 7);
        assertTrue(11.313708498984763 == cl.evalTwoClusterCenters(opt1, opt2));

        // two compare centroids chosen arbitrarily
        Point comp1 = new Point(2.1, 9.3);
        Point comp2 = new Point(3.5, 7.6);
        assertTrue(cl.evalTwoClusterCenters(opt1, opt2) < cl
                .evalTwoClusterCenters(comp1, comp2));
    }

}
