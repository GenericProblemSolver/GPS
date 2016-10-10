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
package optimization.tsp;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import gps.GPS;
import gps.optimization.wrapper.Optimizable;

/**
 * Tests the {@link optimization.tsp.TSP}.
 * 
 * @author mburri
 *
 */
public class TSPTest {

    /**
     * The instance of the tsp that is used for this test
     */
    private TSP tsp;

    /**
     * The list of cities used for this test
     */
    private List<City> cities;

    /**
     * Initializes the instance of the tsp with 
     * a List of cities that were chosen arbitrarily.
     */
    @Before
    public void init() {
        cities = new ArrayList<City>();
        cities.add(new City(0, 0));
        cities.add(new City(100, 100));
        tsp = new TSP(cities);
    }

    /**
     * Tests whether {@link optimization.tsp.TSP#getCosts} actually returns 
     * the cost of the tour.
     */
    @Test
    public void testGetCosts() {
        System.out.println(tsp.getCosts(cities));
        assertTrue(tsp.getCosts(cities) == 282.842712474619);
    }

    /**
     * Tests whether {@link optimization.tsp.TSP#randomTwoOpt} actually changes the tour.
     */
    @Test
    public void testTwoOpt() {
        TSP t = new TSP(5);
        List<City> l1 = t.randomTwoOpt(t.cities);
        TSP t1 = new TSP(l1);
        assertTrue(t.getTourString() != t1.getTourString());
    }

    /**
     * Tests whether all functions work correctly with an empty set of cities.
     */
    @Test
    public void testEmptyTSP() {
        TSP t = new TSP(0);
        assertTrue(t.getCosts(t.cities) == 0);
        assertTrue(t.cities.isEmpty());
        assertTrue(t.randomTwoOpt(t.cities).isEmpty());
        t = new TSP(new ArrayList<City>());
        assertTrue(t.getCosts(t.cities) == 0);
        assertTrue(t.cities.isEmpty());
        assertTrue(t.randomTwoOpt(t.cities).isEmpty());
    }

    /**
     * Tests whether the wrapping works for the TSP.
     * Tests both the wrapping of the {@code @Optimize}-annotated {@link optimization.tsp.TSP#getCosts} 
     * and the {@code @Neighbor}-annotated {@link optimization.tsp.TSP#randomTwoOpt}.
     */
    @Test
    public void testWrapping() {
        Optimizable<TSP> opt = new Optimizable<>(GPS.wrap(tsp));
        opt.setMaximize((byte) -1);
        assertTrue(opt.hasObjectiveFunction());
        assertTrue(tsp.getCosts(tsp.cities) == (-1)
                * opt.objectiveFunction(new Object[] { cities }));

        assertTrue(opt.hasNeighborFunction());
        @SuppressWarnings("unchecked")
        TSP t1 = new TSP(
                (List<City>) opt.neighbor(new Object[] { cities }).get(0)[0]);
        assertTrue(tsp.getTourString() != t1.getTourString());
    }

}
