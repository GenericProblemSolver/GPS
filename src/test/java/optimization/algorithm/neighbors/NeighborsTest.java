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
package optimization.algorithm.neighbors;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Array;

import org.junit.Before;
import org.junit.Test;

import gps.ResultEnum;
import gps.optimization.algorithm.SearchOperatorProvider;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.flattening.Flattener;
import optimization.clustering.Point;

/**
 * Tests the {@link gps.optimization.algorithm.SearchOperatorProvider#neighbors(FlatObjects)} 
 * function.
 * 
 * @author bargen
 *
 */
public class NeighborsTest {

    /**
     * The flattener used to create the {@link gps.optimization.flattening.FlatObjects} 
     * used to test the neighbors-function.
     */
    Flattener fl;

    /**
     * The instance of the SearchOperatorProvider that provides the neighbors-function.
     */
    SearchOperatorProvider so;

    /**
     * Initializes the Flattener and the SearchOperatorProvider 
     * used by this test.
     */
    @Before
    public void init() {
        fl = new Flattener();
        so = new SearchOperatorProvider();
    }

    /**
     * Tests if the neighbors-function can handle ENUMs.
     */
    @Test
    public void testEnum() {
        ResultEnum res = ResultEnum.MOVES;
        FlatObjects f = fl.flattenAndWrap(new Object[] { res });
        Object[][] a = so.neighbors(f, 1);
        assertTrue(a[0][0].getClass().isEnum());
        assertTrue(!a[0][0].equals(res));

    }

    /**
     * Tests if the neighbor function can handle arrays.
     */
    @Test
    public void testPrimitivArrays() {
        Object[] o = new Object[] { new int[] { 1, 200 } };
        FlatObjects f = fl.flattenAndWrap(o);
        Object[][] a = so.neighbors(f, 1);
        assertTrue(a[0][0].getClass().isArray());
        assertTrue(Array.get((a[0][0]), 0).equals(2));
        assertTrue(Array.get((a[0][0]), 1).equals(200));
        assertTrue(Array.get((a[1][0]), 0).equals(0));
        assertTrue(Array.get((a[1][0]), 1).equals(200));
        assertTrue(Array.get((a[2][0]), 0).equals(1));
        assertTrue(Array.get((a[2][0]), 0).equals(1));
        assertTrue(Array.get((a[2][0]), 1).equals(201));
        assertTrue(Array.get((a[3][0]), 1).equals(199));
        assertTrue(Array.get((a[3][0]), 0).equals(1));
    }

    /**
     * Tests if the neighbor function can handle primitive values.
     */
    @Test
    public void testPrimitivValues() {
        FlatObjects f = fl.flattenAndWrap(new Object[] { 1, 200 });
        Object[][] a = so.neighbors(f, 1);
        assertTrue(a[0][0].getClass().isAssignableFrom(Integer.class));
        assertTrue((a[0][1]).equals(200));
        assertTrue((a[1][0]).equals(0));
        assertTrue((a[1][1]).equals(200));
        assertTrue((a[2][0]).equals(1));
        assertTrue((a[2][1]).equals(201));
        assertTrue((a[3][0]).equals(1));
        assertTrue((a[3][1]).equals(199));

    }

    /**
     * Test a combination of different primitive values.
     */
    @Test
    public void testCombi() {
        FlatObjects f = fl.flattenAndWrap(
                new Object[] { (int) 1, (Integer) 1, false, (float) 2.1,
                        (double) 2.9, (short) 1, (long) 2, (byte) 5 });
        Object[][] a = so.neighbors(f, 1);
        assertTrue(a.length == 16);
        for (int i = 0; i < a.length; i++) {
            assertTrue(a[i][0].getClass().isAssignableFrom(Integer.class));
            assertTrue(a[i][1].getClass().isAssignableFrom(Integer.class));
            assertTrue(a[i][2].getClass().isAssignableFrom(Boolean.class));
            assertTrue(a[i][3].getClass().isAssignableFrom(Float.class));
            assertTrue(a[i][4].getClass().isAssignableFrom(Double.class));
            assertTrue(a[i][5].getClass().isAssignableFrom(Short.class));
            assertTrue(a[i][6].getClass().isAssignableFrom(Long.class));
            assertTrue(a[i][7].getClass().isAssignableFrom(Byte.class));
        }
        // check values with a few samples
        assertTrue((a[0][1]).equals(1));
        assertTrue((a[0][0]).equals(2));
        assertTrue(!(boolean) (a[2][2]));
        assertTrue((a[2][0]).equals(1));
    }

    /**
     * Uses the {@link optimization.clustering.Point} class to test 
     * of the neighbors-function can handle self defined classes.
     */
    @Test
    public void testSelfDefinedClass() {
        Object[] res = { new Point(1, 1) };
        FlatObjects f = fl.flattenAndWrap(res);
        Object[][] a = so.neighbors(f, 1);

        assertTrue((double) a[0][0] > 1.0);
        assertTrue(a[0][1].equals(1.0));

        assertTrue((double) a[1][0] < 1.0);
        assertTrue(a[1][1].equals(1.0));

        assertTrue(a[2][0].equals(1.0));
        assertTrue((double) a[2][1] > 1.0);

        assertTrue(a[3][0].equals(1.0));
        assertTrue((double) a[3][1] < 1.0);

    }

}
