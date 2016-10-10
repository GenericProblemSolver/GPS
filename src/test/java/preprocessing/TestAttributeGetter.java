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
package preprocessing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import gps.GPS;
import gps.IWrappedProblem;

/**
 * Class for testing the attribute getter.
 * {@link gps.IWrappedProblem#getAttributes()}.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class TestAttributeGetter {
    public class TestClass {

        /**
         * A public annotated integer field (primitive)
         */
        @gps.annotations.Variable
        public int i = 1337;

        /**
         * A public float field (primitive)
         */
        public float fl = 1.337f;

        /**
         * A string
         */
        public String str = "1337";

        /**
         * A more complex field (an array)
         */
        public String[] strings = new String[] { "13", "37" };

        /**
         * A wrapped integer
         */
        public Integer wrapped = 7331;

        /**
         * Some private variable
         */
        @SuppressWarnings("unused")
        private int somePrivate = -1;

        /**
         * Some final variable
         */
        public final int someFinal = -2;
    }

    /**
     * Tests the getAttributes method if it returns all public elements
     */
    @Test
    public void testAttributesLen() {
        IWrappedProblem<TestClass> prob = GPS.wrap(new TestClass());
        assertEquals(6, prob.getAttributes().length);
    }

    /**
     * Tests the getAttributes method if it contains the fields
     */
    @Test
    public void testAttributes() {
        IWrappedProblem<TestClass> prob = GPS.wrap(new TestClass());
        assertTrue(Arrays.asList(prob.getAttributes()).contains(1337));
        assertTrue(Arrays.asList(prob.getAttributes()).contains(1.337f));
        assertTrue(Arrays.asList(prob.getAttributes()).contains("1337"));
        assertTrue(Arrays.asList(prob.getAttributes()).contains(7331));
        assertFalse(Arrays.asList(prob.getAttributes()).contains(-1));
        // is contained but not writable
        assertTrue(Arrays.asList(prob.getAttributes()).contains(-2));
    }

    /**
     * Tests whether the array is contained
     */
    @Test
    public void testArray() {
        IWrappedProblem<TestClass> prob = GPS.wrap(new TestClass());
        Object[] objects = prob.getAttributes();
        for (Object o : objects) {
            if (o instanceof String[]) {
                String[] strings = (String[]) o;
                assertEquals(strings[0], "13");
                assertEquals(strings[1], "37");
                return;
            }
        }
        fail("did not contain a string array");
    }

    /**
     * Tests the {@link gps.IWrappedProblem#isAttributeFinal(int)} method.
     */
    @Test
    public void testIsFinal() {
        IWrappedProblem<TestClass> prob = GPS.wrap(new TestClass());
        Object[] attributes = prob.getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].equals(-2)) {
                assertTrue(prob.isAttributeFinal(i));
            } else {
                assertFalse(prob.isAttributeFinal(i));
            }
        }
    }

    /**
     * Tests whether the fields can be set using
     * {@link gps.IWrappedProblem#setAttribute(int, Object)}. And if a cast is
     * performed properly.
     */
    @Test
    public void testSetterCast() {
        IWrappedProblem<TestClass> prob = GPS.wrap(new TestClass());
        Object[] attributes = prob.getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].equals(1337)) {
                prob.setAttribute(i, 1234);
                assertEquals(prob.getAttributes()[i], 1234);
            }
        }
    }

    /**
     * Tests whether the fields can be set using
     * {@link gps.IWrappedProblem#setAttribute(int, Object)}. And if a cast is
     * performed properly.
     */
    @Test(expected = ClassCastException.class)
    public void testSetterCastException() {
        IWrappedProblem<TestClass> prob = GPS.wrap(new TestClass());
        Object[] attributes = prob.getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].equals(1337)) {
                prob.setAttribute(i, 1234f); // write a float
            }
        }
    }
}
