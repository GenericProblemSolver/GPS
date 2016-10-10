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
package optimization.flattening;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.flattening.Flattener;
import gps.optimization.flattening.ObjectArrayFlatWrapper;
import optimization.flattening.TestClasses.TestC;
import optimization.flattening.TestClasses.TestD;

/**
 * Tests the {@link gps.optimization.flattening.FlatObjects} class.
 */
public class FlatObjectsTest {
    /**
     * The instance of the Flattener used for the tests
     */
    Flattener f;

    /**
     * The instance of the {@link gps.optimization.flattening.FlatObjects} used
     * to store the output of the Flattener.
     */
    FlatObjects fO;

    /**
     * Used to instantiate the test classes used by this test
     */
    TestClasses tC = new TestClasses();

    @Before
    public void init() {
        f = new Flattener();
    }

    // In the following test cases, the setValues(Object[]) and getObject()-methods are tested.
    /**
     * Tests importing and exporting of primitives into the FlatObjects
     */
    @Test
    public void testPrimitiveImport() {
        fO = f.flattenAndWrap(new Object[] { tC.new TestA() });
        Object[] export = fO.getObjects();
        fO.getFlatWrappers().get(0).set(2);
        fO.getFlatWrappers().get(1).set('b');
        fO.getFlatWrappers().get(2).set(3);
        fO.getFlatWrappers().get(3).set(false);
        fO.setValues(export);
        assertEquals(1, fO.getFlatWrappers().get(0).get());
        assertEquals('a', fO.getFlatWrappers().get(1).get());
        assertEquals(0.5, fO.getFlatWrappers().get(2).get());
        assertEquals(true, fO.getFlatWrappers().get(3).get());
    }

    /**
     * Tests importing and exporting of primitive arrays into the FlatObjects
     */
    @Test
    public void testPrimitiveArrayImport() {
        fO = f.flattenAndWrap(new Object[] { tC.new TestC() });
        Object[] export = fO.getObjects();
        int[] array = (int[]) fO.getFlatWrappers().get(0).get();
        array[0] = 2;
        array[1] = 2;
        array[2] = 2;
        array[3] = 2;
        fO.setValues(export);
        array = (int[]) fO.getFlatWrappers().get(0).get();
        assertEquals(1, array[0]);
        assertEquals(3, array[1]);
        assertEquals(5, array[2]);
        assertEquals(7, array[3]);
    }

    /**
     * Tests importing and exporting of non-primitive arrays into the FlatObjects
     */
    @Test
    public void testNonPrimitiveArrayImport() {
        Object[] oArray = new TestD[] { tC.new TestD(), tC.new TestD() };
        fO = f.flattenAndWrap(new Object[] { oArray });
        Object[] export = fO.getObjects();
        int[] array = (int[]) ((ObjectArrayFlatWrapper) fO.getFlatWrappers()
                .get(0)).get(0).getFlatWrappers().get(0).get();
        array = new int[] { 2, 2, 2, 2 };
        array = (int[]) ((ObjectArrayFlatWrapper) fO.getFlatWrappers().get(0))
                .get(1).getFlatWrappers().get(0).get();
        array = new int[] { 2, 2, 2, 2 };
        fO.setValues(export);
        System.out.println(export.toString());
        array = (int[]) ((ObjectArrayFlatWrapper) fO.getFlatWrappers().get(0))
                .get(0).getFlatWrappers().get(0).get();
        assertEquals(1, array[0]);
        assertEquals(3, array[1]);
        assertEquals(5, array[2]);
        assertEquals(7, array[3]);
        array = (int[]) ((ObjectArrayFlatWrapper) fO.getFlatWrappers().get(0))
                .get(1).getFlatWrappers().get(0).get();
        assertEquals(1, array[0]);
        assertEquals(3, array[1]);
        assertEquals(5, array[2]);
        assertEquals(7, array[3]);
    }
}
