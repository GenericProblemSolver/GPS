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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import gps.optimization.flattening.ArrayFlatWrapper;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.flattening.Flattener;
import gps.optimization.flattening.ObjectArrayFlatWrapper;
import optimization.flattening.TestClasses.TestB;
import optimization.clustering.Point;
import optimization.flattening.TestClasses.TestA;

/**
 * Tests the {@link gps.optimization.flattening.Flattener} class 
 * and implicitly the whole flattening process.
 */
public class FlatteningTest {

    /**
     * The instance of the Flattener used for the tests
     */
    Flattener f;

    /**
     * The instance of the {@link gps.optimization.flattening.FlatObjects} 
     * used to store the output of the Flattener.
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

    // In the following test cases, the flattenAndWrap(Object[])-method of the 
    // Flattener is tested. 
    /**
     * Test the flattening with a single primitive 
     * (an Integer was arbitrarily chosen)
     */
    @Test
    public void testSinglePrimitive() {
        fO = f.flattenAndWrap(new Object[] { 1 });
        assertTrue(fO.getFlatWrappers().size() == 1);
        assertTrue(fO.getRootObjects().length == 1);
        assertTrue(fO.getRootObjects()[0].getClass().equals(Integer.class));
    }

    /**
     * Test the flattening with multiple primitives
     * (Integer, Character, Double and Boolean were chosen arbitrarily)
     */
    @Test
    public void testMultiplePrimitives() {
        fO = f.flattenAndWrap(new Object[] { 1, 'a', 0.5, false });
        assertTrue(fO.getFlatWrappers().size() == 4);
        assertTrue(fO.getRootObjects().length == 4);
        assertTrue(fO.getRootObjects()[0].getClass().equals(Integer.class));
        assertTrue(fO.getRootObjects()[1].getClass().equals(Character.class));
        assertTrue(fO.getRootObjects()[2].getClass().equals(Double.class));
        assertTrue(fO.getRootObjects()[3].getClass().equals(Boolean.class));
    }

    /**
     * Test a simple class containing multiple primitive attributes. 
     * (int, char, double and boolean were arbitrarily chosen)
     */
    @Test
    public void testClassContainingPrimitives() {
        TestA t = tC.new TestA();
        fO = f.flattenAndWrap(new Object[] { t });
        assertTrue(fO.getFlatWrappers().size() == 4);
        assertTrue(fO.getRootObjects().length == 1);
        assertTrue(fO.getRootObjects()[0].getClass().equals(TestA.class));
        assertTrue(fO.getFlatWrappers().get(0).getWrappedClass()
                .equals(int.class));
        assertTrue(fO.getFlatWrappers().get(1).getWrappedClass()
                .equals(char.class));
        assertTrue(fO.getFlatWrappers().get(2).getWrappedClass()
                .equals(double.class));
        assertTrue(fO.getFlatWrappers().get(3).getWrappedClass()
                .equals(boolean.class));
    }

    /**
     * Test a simple class containing a class attribute containing multiple primitive
     * attributes. The class also contains a float attribute. 
     * (int, char, double and boolean were arbitrarily chosen) 
     */
    @Test
    public void testClassContainingClassContainingPrimitives() {
        TestB t = tC.new TestB();
        fO = f.flattenAndWrap(new Object[] { t });
        assertTrue(fO.getFlatWrappers().size() == 5);
        assertTrue(fO.getRootObjects().length == 1);
        assertTrue(fO.getRootObjects()[0].getClass().equals(TestB.class));
        assertTrue(fO.getFlatWrappers().get(0).getWrappedClass()
                .equals(float.class));
        assertTrue(fO.getFlatWrappers().get(1).getWrappedClass()
                .equals(int.class));
        assertTrue(fO.getFlatWrappers().get(2).getWrappedClass()
                .equals(char.class));
        assertTrue(fO.getFlatWrappers().get(3).getWrappedClass()
                .equals(double.class));
        assertTrue(fO.getFlatWrappers().get(4).getWrappedClass()
                .equals(boolean.class));
    }

    /**
     * Test a primitive array.
     * (int was chosen arbitrarily)
     */
    @Test
    public void testPrimitiveArray() {
        int[] i = new int[] { 1, 2, 3, 10 };
        fO = f.flattenAndWrap(new Object[] { i });
        assertTrue(fO.getRootObjects().length == 1);
        assertTrue(fO.getRootObjects()[0].getClass().equals(int[].class));
        assertTrue(fO.getFlatWrappers().size() == 1);
        assertTrue(fO.getFlatWrappers().get(0) instanceof ArrayFlatWrapper);
        ArrayFlatWrapper afW = (ArrayFlatWrapper) fO.getFlatWrappers().get(0);
        assertTrue(afW.get(0).getClass().equals(Integer.class));
        assertTrue(afW.get(1).getClass().equals(Integer.class));
    }

    /**
     * Test an array of non primitive type.
     * ({@link optimization.clustering.Point} was chosen arbitrarily}
     */
    @Test
    public void testNonPrimitiveArray() {
        Point[] p = new Point[] { new Point(0, 0), new Point(0, 1) };
        fO = f.flattenAndWrap(new Object[] { p });
        assertTrue(fO.getRootObjects().length == 1);
        assertTrue(fO.getRootObjects()[0].getClass().equals(Point[].class));
        assertTrue(fO.getFlatWrappers().size() == 1);
        assertTrue(
                fO.getFlatWrappers().get(0) instanceof ObjectArrayFlatWrapper);
        ObjectArrayFlatWrapper afW = (ObjectArrayFlatWrapper) fO
                .getFlatWrappers().get(0);
        assertTrue(
                afW.get(0).getRootObjects()[0].getClass().equals(Point.class));
        assertTrue(
                afW.get(1).getRootObjects()[0].getClass().equals(Point.class));
    }

    /**
     * Test an object array containing multiple primitives of different types. 
     * (types int, char, double, boolean and {@link optimization.clustering.Point} 
     * were chosen arbitrarily)
     */
    @Test
    public void testObjectArrays() {
        Object[] o = new Object[] { 1, 'a', 0.5, false, new Point(0, 0) };
        fO = f.flattenAndWrap(new Object[] { o });
        assertTrue(fO.getRootObjects().length == 1);
        assertTrue(fO.getFlatWrappers().size() == 1);
        assertTrue(
                fO.getFlatWrappers().get(0) instanceof ObjectArrayFlatWrapper);
        ObjectArrayFlatWrapper afW = (ObjectArrayFlatWrapper) fO
                .getFlatWrappers().get(0);
        assertTrue(afW.get(0).getRootObjects()[0].getClass()
                .equals(Integer.class));
        assertTrue(afW.get(1).getRootObjects()[0].getClass()
                .equals(Character.class));
        assertTrue(
                afW.get(2).getRootObjects()[0].getClass().equals(Double.class));
        assertTrue(afW.get(3).getRootObjects()[0].getClass()
                .equals(Boolean.class));
        assertTrue(
                afW.get(4).getRootObjects()[0].getClass().equals(Point.class));
    }

    /**
     * Tests a list of primitives.
     */
    @Test
    public void testPrimitiveList() {
        List<Integer> l = new ArrayList<Integer>();
        l.add(1);
        l.add(2);
        fO = f.flattenAndWrap(new Object[] { l });
        assertTrue(fO.getRootObjects().length == 1);
        assertTrue(fO.getRootObjects()[0].getClass().equals(ArrayList.class));
        assertTrue(
                fO.getFlatWrappers().get(0) instanceof ObjectArrayFlatWrapper);
        ObjectArrayFlatWrapper afW = (ObjectArrayFlatWrapper) fO
                .getFlatWrappers().get(0);

        assertTrue(afW.get(0).getRootObjects()[0].getClass()
                .equals(Integer.class));
        assertTrue(afW.get(1).getRootObjects()[0].getClass()
                .equals(Integer.class));

        assertTrue(afW.get(0).getFlatWrappers().size() == 1);
        assertTrue(afW.get(1).getFlatWrappers().size() == 1);
    }

    /**
     * Tests a list of non-primitives.
     */
    @Test
    public void testNonPrimitiveList() {
        List<Point> l = new ArrayList<Point>();
        l.add(new Point(0, 0));
        l.add(new Point(0, 0));
        fO = f.flattenAndWrap(new Object[] { l });
        assertTrue(fO.getRootObjects().length == 1);
        assertTrue(fO.getRootObjects()[0].getClass().equals(ArrayList.class));
        assertTrue(
                fO.getFlatWrappers().get(0) instanceof ObjectArrayFlatWrapper);
        ObjectArrayFlatWrapper afW = (ObjectArrayFlatWrapper) fO
                .getFlatWrappers().get(0);

        assertTrue(
                afW.get(0).getRootObjects()[0].getClass().equals(Point.class));
        assertTrue(
                afW.get(1).getRootObjects()[0].getClass().equals(Point.class));

        assertTrue(afW.get(0).getFlatWrappers().size() == 2);
        assertTrue(afW.get(1).getFlatWrappers().size() == 2);

        assertTrue(afW.get(0).getFlatWrappers().get(0).getWrappedClass()
                .equals(double.class));
        assertTrue(afW.get(1).getFlatWrappers().get(0).getWrappedClass()
                .equals(double.class));
    }

    /**
     * Tests a list of objects.
     */
    @Test
    public void testObjectList() {
        List<Object> l = new ArrayList<Object>();
        l.add(1);
        l.add('a');
        l.add(0.5);
        l.add(false);
        l.add(new Point(0, 0));
        fO = f.flattenAndWrap(new Object[] { l });
        assertTrue(fO.getRootObjects().length == 1);
        assertTrue(fO.getRootObjects()[0].getClass().equals(ArrayList.class));
        assertTrue(
                fO.getFlatWrappers().get(0) instanceof ObjectArrayFlatWrapper);
        ObjectArrayFlatWrapper afW = (ObjectArrayFlatWrapper) fO
                .getFlatWrappers().get(0);

        assertTrue(afW.get(0).getRootObjects()[0].getClass()
                .equals(Integer.class));
        assertTrue(afW.get(1).getRootObjects()[0].getClass()
                .equals(Character.class));
        assertTrue(
                afW.get(2).getRootObjects()[0].getClass().equals(Double.class));
        assertTrue(afW.get(3).getRootObjects()[0].getClass()
                .equals(Boolean.class));
        assertTrue(
                afW.get(4).getRootObjects()[0].getClass().equals(Point.class));

        assertTrue(afW.get(0).getFlatWrappers().size() == 1);
        assertTrue(afW.get(1).getFlatWrappers().size() == 1);
        assertTrue(afW.get(2).getFlatWrappers().size() == 1);
        assertTrue(afW.get(3).getFlatWrappers().size() == 1);
        assertTrue(afW.get(4).getFlatWrappers().size() == 2);
    }

    // In the following test cases, the flattenAndWrap(Object[], Set<Class<?>>)-method 
    // that should not flatten any objects of a type that the given set contains is tested.
    /**
     * Tests multiple objects of a class that is not to be flattened.
     */
    @Test
    public void testNothingToBeFlattened() {
        Set<Class<?>> s = new HashSet<>();
        s.add(TestA.class);
        fO = f.flattenAndWrap(new Object[] { tC.new TestA(), tC.new TestA() },
                s);
        assertTrue(fO.getRootObjects().length == 2);
        assertTrue(fO.getFlatWrappers().size() == 2);
    }

    /**
     * Test multiple objects of different classes that are not to be flattened.
     */
    @Test
    public void testNothingToBeFlattened2() {
        Set<Class<?>> s = new HashSet<>();
        s.add(TestA.class);
        s.add(TestB.class);
        fO = f.flattenAndWrap(new Object[] { tC.new TestA(), tC.new TestB() },
                s);
        assertTrue(fO.getRootObjects().length == 2);
        assertTrue(fO.getFlatWrappers().size() == 2);
    }

    /**
     * Tests a class that contains a class that is not to be flattened in addition 
     * to a field that is to be flattened.
     */
    @Test
    public void testSomeAttributesToBeFlattened() {
        Set<Class<?>> s = new HashSet<>();
        s.add(TestA.class);
        fO = f.flattenAndWrap(new Object[] { tC.new TestB() }, s);
        assertTrue(fO.getRootObjects().length == 1);
        assertTrue(fO.getFlatWrappers().size() == 2);
        assertTrue(fO.getFlatWrappers().get(1).getWrappedClass()
                .equals(float.class));
    }

    /**
     * Test an object-array containing classes that are not be be flattened.
     */
    @Test
    public void testObjArrayNoObjectsToBeFlattened() {
        Set<Class<?>> s = new HashSet<>();
        s.add(TestA.class);
        s.add(Point.class);
        Object[] o = new Object[] { tC.new TestA(), new Point(0, 0) };
        fO = f.flattenAndWrap(new Object[] { o }, s);
        assertTrue(fO.getRootObjects().length == 1);
        assertTrue(fO.getFlatWrappers().size() == 1);
        assertTrue(
                fO.getFlatWrappers().get(0) instanceof ObjectArrayFlatWrapper);

        ObjectArrayFlatWrapper afW = (ObjectArrayFlatWrapper) fO
                .getFlatWrappers().get(0);
        assertTrue(
                afW.get(0).getRootObjects()[0].getClass().equals(TestA.class));
        assertTrue(
                afW.get(1).getRootObjects()[0].getClass().equals(Point.class));

        assertTrue(afW.get(0).getFlatWrappers().size() == 1);
        assertTrue(afW.get(1).getFlatWrappers().size() == 1);
    }

    /**
     * Tests an object-array containing classes that are to be flattened and also 
     * classes that are not to be flattened
     */
    @Test
    public void testObjArraySomeObjectsToBeFlattened() {
        Set<Class<?>> s = new HashSet<>();
        s.add(TestA.class);
        Object[] o = new Object[] { tC.new TestA(), new Point(0, 0) };
        fO = f.flattenAndWrap(new Object[] { o }, s);
        assertTrue(fO.getRootObjects().length == 1);
        assertTrue(fO.getFlatWrappers().size() == 1);
        assertTrue(
                fO.getFlatWrappers().get(0) instanceof ObjectArrayFlatWrapper);

        ObjectArrayFlatWrapper afW = (ObjectArrayFlatWrapper) fO
                .getFlatWrappers().get(0);
        assertTrue(
                afW.get(0).getRootObjects()[0].getClass().equals(TestA.class));
        assertTrue(
                afW.get(1).getRootObjects()[0].getClass().equals(Point.class));

        assertTrue(afW.get(0).getFlatWrappers().size() == 1);
        // since Point is to be flattened and contains 2 doubles
        assertTrue(afW.get(1).getFlatWrappers().size() == 2);
    }

}
