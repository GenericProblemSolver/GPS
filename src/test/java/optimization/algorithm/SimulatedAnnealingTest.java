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

import org.junit.Before;
import org.junit.Test;

import gps.GPS;
import gps.optimization.algorithm.simulatedAnnealing.SimulatedAnnealing;
import gps.optimization.algorithm.simulatedAnnealing.schedule.ExponentialSchedule;
import gps.optimization.algorithm.simulatedAnnealing.schedule.ICoolingSchedule;
import gps.optimization.algorithm.simulatedAnnealing.schedule.LinearSchedule;
import gps.optimization.algorithm.simulatedAnnealing.schedule.QuadraticSchedule;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.flattening.Flattener;
import gps.optimization.wrapper.Optimizable;
import optimization.clustering.ClusteringProblem;
import optimization.clustering.Point;

/**
 * Tests the {@link gps.optimization.algorithm.simulatedAnnealing.SimulatedAnnealing}
 * algorithm.
 * 
 * @author mburri
 *
 */
public class SimulatedAnnealingTest {

    /**
     * the instance of the solver 
     */
    private SimulatedAnnealing<ClusteringProblem> sA;

    /**
     * the wrapped problem that will be used for the test
     */
    private Optimizable<ClusteringProblem> opt;

    /**
     * Initializes the Simulated Annealing Solver and 
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
        ICoolingSchedule sched = new ExponentialSchedule(0.9);
        sA = new SimulatedAnnealing<ClusteringProblem>(opt, fO, 10, sched);

    }

    /**
     * Tests the {@link gps.optimization.algorithm.simulatedAnnealing.schedule.LinearSchedule}
     */
    @Test
    public void testLinearSchedule() {
        ICoolingSchedule sched = new LinearSchedule(10);
        assertTrue(sched.decreaseTemperature(1000) == 990);
        assertTrue(sched.decreaseTemperature(990) == 980);
        sched = new LinearSchedule(0.0);
        assertTrue(sched.decreaseTemperature(1000) == 1000);
    }

    /**
     * Tests the {@link gps.optimization.algorithm.simulatedAnnealing.schedule.ExponentialSchedule}
     */
    @Test(expected = IllegalArgumentException.class)
    public void testExponentialSchedule() {
        ICoolingSchedule sched = new ExponentialSchedule(0.9);
        assertTrue(sched.decreaseTemperature(1000) == (0.9 * 1000));
        assertTrue(sched.decreaseTemperature(990) == (0.9 * 990));
        sched = new ExponentialSchedule(0.0);
    }

    /**
     * Tests the {@link gps.optimization.algorithm.simulatedAnnealing.schedule.QuadraticSchedule}
     */
    @Test
    public void testQuadraticSchedule() {
        ICoolingSchedule sched = new QuadraticSchedule(5, 1000);
        double temp1 = 1000 / (1 + 5 * Math.pow(1, 2));
        assertTrue(sched.decreaseTemperature(1000) == temp1);
        assertTrue(sched.decreaseTemperature(temp1) == 1000
                / (1 + 5 * Math.pow(2, 2)));
    }

    /**
     * Tests the {@link gps.optimization.algorithm.simulatedAnnealing.SimulatedAnnealing#optimizeOneStep()}-
     * function if the problem is to be minimized. The random element in the 
     * {@link gps.optimization.algorithm.SearchOperatorProvider#randomVariation(FlatObjects, int)}
     * -function, that the Solver uses, makes it impossible to reliably test whether the 
     * problem was actually minimized. <br>
     * Asserts that the temperature is decreased.
     */
    @Test
    public void minimizeOneStep() {
        sA.setMaxSteps(1);
        double initialTemperature = sA.getTemperature();
        sA.minimize();

        // Assert that the temperature was decreased
        assertTrue(sA.getTemperature() < initialTemperature);
    }

    /**
     * Tests the {@link gps.optimization.algorithm.simulatedAnnealing.SimulatedAnnealing#optimizeOneStep()}-
     * function if the problem is to be maximized. The random element in the 
     * {@link gps.optimization.algorithm.SearchOperatorProvider#randomVariation(FlatObjects, int)}
     * -function, that the s1olver uses, makes it impossible to reliably test whether the 
     * problem was actually minimized. <br>
     * Asserts that the temperature is decreased.
     */
    @Test
    public void maximizeOneStep() {
        sA.setMaxSteps(1);
        double initialTemperature = sA.getTemperature();
        sA.minimize();

        // Assert that the temperature was decreased
        assertTrue(sA.getTemperature() < initialTemperature);
    }

}
