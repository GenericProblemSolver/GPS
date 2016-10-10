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
package gps.attribute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class AttributeGraphTest {

    TestClass tc;

    TestClass tc2;

    TestClassPrim tcp;

    TestClassPrim tcp2;

    @Before
    public void setup() {
        tc = new TestClass();
        tc2 = new TestClass();
        tcp = new TestClassPrim();
        tcp2 = new TestClassPrim();
    }

    /**
     * Tests if {@link AttributeValue#setValue(Object)} can change the value of
     * an <code>Object</code>.
     */
    @Test
    public void testSetValue() {
        for (int i = 0; i < tc.intArray.length; i++) {
            assertEquals(tc.intArray[i], i + 1);
        }
        AttributeGraph tcg = new AttributeGraph(tc);
        tcg.buildGraph();
        tcg.getRoot().getFields().get(0).setValue(new int[] { 5, 6, 7, 8 });
        for (int i = 0; i < tc.intArray.length; i++) {
            assertEquals(tc.intArray[i], i + 5);
        }
    }

    /**
     * Tests if
     * {@link AttributeGraph#matchPrimitives(AttributeGraph, IAttributeMatcher)}
     * matches all {@link AttributeValue}s with the same
     * {@link AttributeValue#identifier}.
     */
    @Test
    public void testPrimitiveMatching() {
        AttributeGraph tcg = new AttributeGraph(tc);
        AttributeGraph tcg2 = new AttributeGraph(tc2);
        tcg.matchPrimitives(tcg2, (a, b) -> {
            if (!a.getValue().equals(b.getValue())) {
                fail("All values should be equal.");
            }
            return true;
        });
        for (IObject io : tcg.getRoot().getFields().get(0).getFields()) {
            if (io.getValue().getClass() == Integer.class) {
                io.setValue((Integer) io.getValue() + 5);
            }
        }
        tcg.matchPrimitives(tcg2, (a, b) -> {
            if (a.getValue().getClass() == Integer.class
                    && a.getValue().equals(b.getValue())) {
                fail("All values should be different.");
            }
            assertFalse(a.getValue().getClass() == Integer.class
                    && a.getValue().equals(b.getValue()));
            return true;
        });
    }

    /**
     * Tests if
     * {@link AttributeGraph#matchAllUncached(AttributeGraph, IAttributeMatcher)}
     * matches all {@link AttributeValue}s with the same
     * {@link AttributeValue#identifier} and checks if the setting of a value
     * works correctly.
     */
    @Test
    public void testUncachedMatching() {
        AttributeGraph tcpg = new AttributeGraph(tcp);
        AttributeGraph tcpg2 = new AttributeGraph(tcp2);
        tcpg.matchAllUncached(tcpg2, (a, b) -> {
            if (a.getValue().getClass() == String.class) {
                a.setValue("Matched!");
                return true;
            }
            if (a.getValue().getClass() == Integer.class) {
                a.setValue(-1);
                return true;
            }
            return a.getValue().getClass() == TestClassPrim.class;
        });
        for (IObject io : tcpg.createdNodes) {
            if (io.getValue().getClass() == String.class) {
                assertTrue(io.getValue() == "Matched!");
            } else if (io.getValue().getClass() == Integer.class) {
                assertTrue((int) io.getValue() == -1);
            } else if (io.getValue().getClass() == TestClassPrim.class) {
                continue;
            } else {
                fail("Setting of values did not work.");
            }
        }
    }

    @Test
    public void testDepthFirstTraversal() {
        AttributeGraph tcg = AttributeGraph.fromObject(tc);
        assertTrue(tcg.depthFirstTraversal(false).size() == tcg.createdNodes
                .size());
        tcg.remove(tcg.getRoot().getFields().get(1).getFields().get(3));
        assertTrue(tcg.depthFirstTraversal(false).size() == tcg.createdNodes
                .size());
        tcg.removeList(tcg.getRoot().getFields().get(1).getFields());
        assertTrue(tcg.depthFirstTraversal(false).size() == tcg.createdNodes
                .size());
    }
}

class TestClass {

    int[] intArray = new int[] { 1, 2, 3, 4 };

    String[][] stringArray = new String[][] { { "one", "two" },
            { "three", "four" }, { "five", "six" } };

}

class TestClassPrim {

    int i = 4;

    String s = "six";

}
