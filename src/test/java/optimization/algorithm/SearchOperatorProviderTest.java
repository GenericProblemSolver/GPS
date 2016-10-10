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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import gps.optimization.algorithm.SearchOperatorProvider;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.flattening.Flattener;
import gps.optimization.flattening.ObjectArrayFlatWrapper;
import optimization.flattening.TestClasses;
import optimization.flattening.TestClasses.TestA;
import optimization.flattening.TestClasses.TestD;

/**
 * Tests the {@link gps.optimization.algorithm.SearchOperatorProvider} class.
 */
public class SearchOperatorProviderTest {
    /**
     * The instance of the SearchOperatorProvider used for the tests
     */
    SearchOperatorProvider s;

    /**
     * The instance of the Flattener used for the tests
     */
    Flattener f;

    /**
     * Used to instantiate the test classes used by this test
     */
    TestClasses tC = new TestClasses();

    @Before
    public void init() {
        s = new SearchOperatorProvider();
        f = new Flattener();
    }

    /**
     * Tests the randomVariation-method for FlatObjects
     */
    @Test
    public void testRandomVariationFlatObjects() {
        FlatObjects fO = f.flattenAndWrap(new Object[] { tC.new TestA() });
        s.randomVariation(fO);
        assertTrue(1 != (int) fO.getFlatWrappers().get(0).get());
        assertTrue(0.5 != (double) fO.getFlatWrappers().get(2).get());
    }

    /**
     * Tests the randomRandomVariation-method for primitives
     */
    @Ignore
    @Test
    public void testRandomRandomVariationPrimitives() {
        FlatObjects fO = f.flattenAndWrap(new Object[] { tC.new TestA() });
        for (int x = 0; x < 100; x++) {
            s.randomRandomVariation(fO, 5);
        }
        assertTrue(1 != (int) fO.getFlatWrappers().get(0).get());
        assertTrue(0.5 != (double) fO.getFlatWrappers().get(2).get());
    }

    /**
     * Tests the randomRandomVariation-method for primitive array
     */
    @Test
    public void testRandomRandomVariationPrimitiveArray() {
        FlatObjects fO = f.flattenAndWrap(new Object[] { tC.new TestC() });
        for (int x = 0; x < 10000; x++) {
            s.randomRandomVariation(fO, 5);
        }
        int[] array = (int[]) fO.getFlatWrappers().get(0).get();
        assertTrue(1 != array[0]);
        assertTrue(3 != array[1]);
        assertTrue(5 != array[2]);
        assertTrue(7 != array[3]);
    }

    /**
     * Tests the randomRandomVariation-method for non-primitive array
     */
    @Test
    public void testRandomRandomVariationNonPrimitiveArray() {
        Object[] oArray = new TestD[] { tC.new TestD(), tC.new TestD() };
        FlatObjects fO = f.flattenAndWrap(new Object[] { oArray });
        for (int x = 0; x < 50000; x++) {
            s.randomRandomVariation(fO, 5);
        }
        int[] array = (int[]) ((ObjectArrayFlatWrapper) fO.getFlatWrappers()
                .get(0)).get(0).getFlatWrappers().get(0).get();
        assertTrue(1 != array[0]);
        assertTrue(3 != array[1]);
        assertTrue(5 != array[2]);
        assertTrue(7 != array[3]);
        array = (int[]) ((ObjectArrayFlatWrapper) fO.getFlatWrappers().get(0))
                .get(1).getFlatWrappers().get(0).get();
        assertTrue(1 != array[0]);
        assertTrue(3 != array[1]);
        assertTrue(5 != array[2]);
        assertTrue(7 != array[3]);
    }
}
