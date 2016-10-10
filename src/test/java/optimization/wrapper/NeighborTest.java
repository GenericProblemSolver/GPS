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
package optimization.wrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import gps.GPS;
import gps.IWrappedProblem;
import gps.annotations.Neighbor;
import gps.annotations.Optimize;
import gps.annotations.Variable;
import gps.optimization.algorithm.AbstractOptimizer;
import gps.optimization.algorithm.SearchOperatorProvider;
import gps.optimization.algorithm.thresholdAccepting.ThresholdAccepting;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.flattening.Flattener;
import gps.optimization.wrapper.Optimizable;
import optimization.tsp.City;
import optimization.tsp.TSP;

/**
 * Tests the {@link gps.annotations.Neighbor} annotation wrapping, the 
 * {@link gps.preprocessing.implementer.Neighbor} implementer.
 * 
 * @author mburri
 *
 */
public class NeighborTest {

    /**
     * Random generator used to generate random test numbers
     */
    private final Random random = new Random();

    /**
     * The instance of the {@link gps.optimization.algorithm.SearchOperatorProvider} 
     * used for this test
     */
    private SearchOperatorProvider sop;

    /**
     * The instance of the {@link gps.optimization.flattening.Flattener} used for 
     * this test.
     */
    private Flattener f = new Flattener();

    class NeighborA {

        @Variable
        public int init = 0;

        @Optimize
        public int opt(int i) {
            return i;
        }

        @Neighbor
        public List<Integer> neigh(int i) {
            List<Integer> ret = new ArrayList<Integer>();
            ret.add((i + 1) * 3);
            return ret;
        }

    }

    class NeighborB {

        @Variable
        public int init = 0;

        @Optimize
        public int opt(int i) {
            return i;
        }

        @Neighbor
        public int neigh(int i) {
            return (i + 1) * 3;
        }

    }

    class NeighborC {

        @Variable
        public int init = 0;

        @Variable
        public double initD = 0.0;

        @Optimize
        public int opt(int i, double d) {
            return i;
        }

        @Neighbor
        public List<Object[]> neigh(int i, double d) {
            List<Object[]> ret = new ArrayList<Object[]>();
            ret.add(new Object[] { (i + 1) * 3, (d - 2) * 0.5 });
            return ret;
        }

    }

    class NeighborD {

        @Variable
        public int init = 0;

        @Variable
        public double initD = 0.0;

        @Optimize
        public int opt(int i, double d) {
            return i;
        }

        @Neighbor
        public Object[] neigh(int i, double d) {
            return new Object[] { (i + 1) * 3, (d - 2) * 0.5 };
        }

    }

    class NeighborE {

        @Variable
        public int init = 0;

        @Variable
        public double initD = 0.0;

        @Optimize
        public int opt(int i, double d) {
            return i;
        }

        @Neighbor
        public Object[] neigh(int i, double d) {
            return new Object[] { "test", 0.5 };
        }

    }

    class NeighborF {

        @Variable
        public int init = 0;

        @Optimize
        public int opt(int i) {
            return i;
        }

        @Neighbor
        public List<Integer> neigh(int i) {
            return new ArrayList<Integer>();
        }

    }

    class NeighborG {

        public NeighborG(City[] pCities) {
            cities = pCities;
        }

        @Variable
        public City[] cities;

        @Optimize
        public int opt(City[] cities) {
            return 0;
        }

        @Neighbor
        public List<City[]> neigh(City[] cities) {
            City[] ret = new City[cities.length];
            for (int i = 0; i < cities.length; i++) {
                ret[i] = new City(0, 0);
            }
            List<City[]> ret1 = new ArrayList<City[]>();
            ret1.add(ret);
            return ret1;
        }

    }

    /**
     * Test user defined neighbor function with one parameter 
     * and return type = List<parameter type>.
     * Also test whether the searchOperatorProvider calls the 
     * user defined function.
     */
    @Test
    public void testWrappingA() {
        NeighborA p = new NeighborA();
        IWrappedProblem<NeighborA> wrapped = GPS.wrap(p);
        Optimizable<?> opt = new Optimizable<>(wrapped);
        sop = new SearchOperatorProvider(opt);

        int testNumber = random.nextInt(12);
        FlatObjects fO = f.flattenAndWrap(new Object[] { testNumber });
        sop.randomVariation(fO);
        assertEquals(p.neigh(testNumber).get(0), fO.getRootObjects()[0]);

        // test neighbors of SearchOperatorProvider
        fO = f.flattenAndWrap(new Object[] { testNumber });
        Object[][] ret = sop.neighbors(fO, 1);
        assertTrue(ret.length == 1);
        assertEquals(ret[0][0], (1 + testNumber) * 3);
    }

    /**
     * Test user defined neighbor function with one parameter 
     * and return type = parameter type.
     * Also test whether the searchOperatorProvider calls the 
     * user defined function.
     */
    @Test
    public void testWrappingB() {
        NeighborB p = new NeighborB();
        IWrappedProblem<NeighborB> wrapped = GPS.wrap(p);
        Optimizable<?> opt = new Optimizable<>(wrapped);
        sop = new SearchOperatorProvider(opt);

        int testNumber = random.nextInt(12);
        FlatObjects fO = f.flattenAndWrap(new Object[] { testNumber });
        sop.randomVariation(fO);
        assertEquals(p.neigh(testNumber), fO.getRootObjects()[0]);

        // test neighbors of SearchOperatorProvider
        fO = f.flattenAndWrap(new Object[] { testNumber });
        Object[][] ret = sop.neighbors(fO, 1);
        assertTrue(ret.length == 1);
        assertEquals(ret[0][0], (1 + testNumber) * 3);
    }

    /**
     * Test user defined neighbor function with multiple parameters 
     * and return type = List<Object[]>.
     * Also test whether the searchOperatorProvider calls the 
     * user defined function.
     */
    @Test
    public void testWrappingC() {
        NeighborC p = new NeighborC();
        IWrappedProblem<NeighborC> wrapped = GPS.wrap(p);
        Optimizable<?> opt = new Optimizable<>(wrapped);
        sop = new SearchOperatorProvider(opt);

        int testNumber1 = random.nextInt(12);
        double testNumber2 = random.nextDouble() + testNumber1;
        FlatObjects fO = f
                .flattenAndWrap(new Object[] { testNumber1, testNumber2 });
        sop.randomVariation(fO);
        assertEquals(p.neigh(testNumber1, testNumber2).get(0)[0],
                fO.getRootObjects()[0]);
        assertEquals(p.neigh(testNumber1, testNumber2).get(0)[1],
                fO.getRootObjects()[1]);

        // test neighbors of SearchOperatorProvider
        fO = f.flattenAndWrap(new Object[] { testNumber1, testNumber2 });
        Object[][] ret = sop.neighbors(fO, 1);
        assertTrue(ret.length == 1);
        assertEquals(ret[0][0], (1 + testNumber1) * 3);
        assertEquals(ret[0][1], (testNumber2 - 2) * 0.5);
    }

    /**
     * Test user defined neighbor function with multiple parameters 
     * and return type = Object[].
     * Also test whether the searchOperatorProvider calls the 
     * user defined function.
     */
    @Test
    public void testWrappingD() {
        NeighborD p = new NeighborD();
        IWrappedProblem<NeighborD> wrapped = GPS.wrap(p);
        Optimizable<?> opt = new Optimizable<>(wrapped);
        sop = new SearchOperatorProvider(opt);

        int testNumber1 = random.nextInt(12);
        double testNumber2 = random.nextDouble() + testNumber1;
        FlatObjects fO = f
                .flattenAndWrap(new Object[] { testNumber1, testNumber2 });
        sop.randomVariation(fO);
        assertEquals(p.neigh(testNumber1, testNumber2)[0],
                fO.getRootObjects()[0]);
        assertEquals(p.neigh(testNumber1, testNumber2)[1],
                fO.getRootObjects()[1]);

        // test neighbors of SearchOperatorProvider
        fO = f.flattenAndWrap(new Object[] { testNumber1, testNumber2 });
        Object[][] ret = sop.neighbors(fO, 1);
        assertTrue(ret.length == 1);
        assertEquals(ret[0][0], (1 + testNumber1) * 3);
        assertEquals(ret[0][1], (testNumber2 - 2) * 0.5);
    }

    /**
     * Test user defined neighbor function that does not 
     * return an Object[] containing objects with types matching 
     * the types of the parameters of the Optimize-method.
     */
    @Test(expected = RuntimeException.class)
    public void testWrappingE() {
        NeighborE p = new NeighborE();
        IWrappedProblem<NeighborE> wrapped = GPS.wrap(p);
        Optimizable<?> opt = new Optimizable<>(wrapped);
        sop = new SearchOperatorProvider(opt);

        int testNumber1 = random.nextInt(12);
        double testNumber2 = random.nextDouble() + testNumber1;
        FlatObjects fO = f
                .flattenAndWrap(new Object[] { testNumber1, testNumber2 });
        sop.randomVariation(fO);
        AbstractOptimizer<NeighborF> ap = new ThresholdAccepting<NeighborF>(
                null, fO, 0.0, 0.0);
        // eval should be called
        ap.optimize();
    }

    /**
     * Test user defined neighbor function that does not 
     * return at least one set of parameters for the Optimize-method.
     */
    @Test(expected = RuntimeException.class)
    public void testWrappingF() {
        NeighborF p = new NeighborF();
        IWrappedProblem<NeighborF> wrapped = GPS.wrap(p);
        Optimizable<?> opt = new Optimizable<>(wrapped);
        sop = new SearchOperatorProvider(opt);

        int testNumber1 = random.nextInt(12);
        double testNumber2 = random.nextDouble() + testNumber1;
        FlatObjects fO = f
                .flattenAndWrap(new Object[] { testNumber1, testNumber2 });
        sop.randomVariation(fO);
    }

    /**
     * Test user defined neighbor function that has a non-primitive 
     * array as the only parameter. 
     */
    @Test
    public void testWrappingG() {
        City c1 = new City();
        NeighborG p = new NeighborG(new City[] { c1 });
        IWrappedProblem<NeighborG> wrapped = GPS.wrap(p);
        Optimizable<?> opt = new Optimizable<>(wrapped);
        sop = new SearchOperatorProvider(opt);

        // since there is a user defined neighbor function
        // there is no need for us to flatten the initial parameters
        // so we add them as primitive leaves
        Set<Class<?>> s = new HashSet<>();
        if (opt.hasNeighborFunction()) {
            for (Object o : opt.getDefaultParams()) {
                s.add(o.getClass());
            }
        }
        FlatObjects fO = f.flattenAndWrap(opt.getDefaultParams(), s);
        sop.randomVariation(fO);
        City[] ret = (City[]) fO.getRootObjects()[0];
        assertTrue(c1 != ret[0]);

        // test neighbors of SearchOperatorProvider
        Object[][] neighbors = sop.neighbors(fO, 1);
        assertTrue(c1 != neighbors[0][0]);
    }

    /**
     * Test user defined neighbor function that has an ArrayList of 
     * non-primitives as the only parameter. Uses the 
     * {@link optimization.tsp.TSP} class for testing purposes.
     */
    @Test
    public void testWrappingArrayListTSP() {
        TSP t = new TSP(3);
        IWrappedProblem<TSP> wrapped = GPS.wrap(t);
        Optimizable<?> opt = new Optimizable<>(wrapped);
        sop = new SearchOperatorProvider(opt);

        // since there is a user defined neighbor function
        // there is no need for us to flatten the initial parameters
        // so we add them as primitive leaves
        Set<Class<?>> s = new HashSet<>();
        if (opt.hasNeighborFunction()) {
            for (Object o : opt.getDefaultParams()) {
                s.add(o.getClass());
            }
        }
        FlatObjects fO = f.flattenAndWrap(opt.getDefaultParams(), s);
        String tourBefore = fO.getRootObjects()[0].toString();
        sop.randomVariation(fO);
        assertTrue(fO.getRootObjects()[0].toString() != tourBefore);

        // test neighbors of SearchOperatorProvider
        Object[][] ret = sop.neighbors(fO, 1);
        @SuppressWarnings("unchecked")
        TSP t1 = new TSP((List<City>) ret[0][0]);
        assertTrue(t1.toString() != t.toString());
    }

}
