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
package bytecode.interfaces;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import gps.GPS;
import gps.bytecode.backends.IBytecodeBackend.SatisfactionProblemSolution;
import gps.bytecode.expressions.Constant;
import gps.bytecode.interfaces.BytecodeOptimizationHelper;
import gps.optimization.wrapper.Optimizable;
import optimization.pp.PlaygroundProblem;

/**
 * Tests the {@link gps.bytecode.interfaces.BytecodeOptimizationHelper} using the 
 * {@link optimization.pp.PlaygroundProblem}. This class can only be tested in a 
 * meaningful way if the backend used to solve the the constructed SatisfactionProblem 
 * is != the Z3StdoutBackend.
 * 
 * @author mburri
 *
 */
public class BytecodeHelperTest {

    /**
     * The {@link gps.optimization.wrapper.Optimizable} used for the
     * test.
     */
    Optimizable<?> opt;

    @Before
    public void init() {
        opt = new Optimizable<>(GPS.wrap(new PlaygroundProblem(24)));
        opt.setMaximize((byte) 1);
    }

    // maximize
    /**
     * Tests whether the {@link gps.bytecode.interfaces.BytecodeOptimizationHelper#canBeGreaterThan(Optimizable, double)}
     * returns a valid solution. Per default, there is a possible solution for our instance the PlaygroundProblem 
     * that is better than 0.
     */
    @Ignore
    @Test
    public void testGreaterThan0() {
        SatisfactionProblemSolution result = BytecodeOptimizationHelper
                .canBeGreaterThan(opt, 0);

        if (result != null) {
            assertTrue(result.satisfiable);
            Constant[] t = result.variableValues.values().toArray(
                    new Constant[result.variableValues.values().size()]);

            int firstResult = (int) t[0].getValue();
            int secondResult = (int) t[1].getValue();

            assertTrue(firstResult != 0);
            assertTrue(secondResult != 0);

            assertTrue(opt.objectiveFunction(
                    (new Object[] { firstResult, secondResult })) > 0);
        }
    }

    /**
     * Tests whether the {@link gps.bytecode.interfaces.BytecodeOptimizationHelper#canBeGreaterThan(Optimizable, double)}
     * finds the best valid solution. For our instance of the PlaygroundProblem, that solution is {6,12} with 
     * a value of 72.
     */
    @Ignore
    @Test
    public void testGreaterThanMaximalSolution() {
        SatisfactionProblemSolution result = BytecodeOptimizationHelper
                .canBeGreaterThan(opt, 71);

        if (result != null) {
            assertTrue(result.satisfiable);

            Constant[] t = result.variableValues.values().toArray(
                    new Constant[result.variableValues.values().size()]);

            int firstResult = (int) t[0].getValue();
            int secondResult = (int) t[1].getValue();

            assertTrue(firstResult == 6);
            assertTrue(secondResult == 12);
        }
    }

    /**
     * Tests whether the {@link gps.bytecode.interfaces.BytecodeOptimizationHelper#canBeGreaterThan(Optimizable, double)} 
     * returns a SatisfactionProblem that is not satisfiable if the value can not be exceeded. 
     * Per default, the maximum value for our instance of the PlaygroundProblem is 72.
     */
    @Ignore
    @Test
    public void testGreaterThanNotSatisfiable() {
        SatisfactionProblemSolution result = BytecodeOptimizationHelper
                .canBeGreaterThan(opt, 72);
        if (result != null) {
            assertFalse(result.satisfiable);
        }
    }

    // Minimize
    /**
     * Tests whether the {@link gps.bytecode.interfaces.BytecodeOptimizationHelper#canBeGreaterThan(Optimizable, double)}
     * returns a valid solution. Since we want to minimize, there is a possible solution for our instance the PlaygroundProblem 
     * that is better than -1, namely 0.
     */
    @Ignore
    @Test
    public void testGreaterThanMinimize() {
        opt.setMaximize((byte) -1);
        SatisfactionProblemSolution result = BytecodeOptimizationHelper
                .canBeGreaterThan(opt, -1);

        if (result != null) {
            assertTrue(result.satisfiable);

            Constant[] t = result.variableValues.values().toArray(
                    new Constant[result.variableValues.values().size()]);

            int firstResult = (int) t[0].getValue();
            int secondResult = (int) t[1].getValue();

            assertTrue(0 == opt.objectiveFunction(
                    new Object[] { firstResult, secondResult }));
        }
    }

    /**
     * Tests whether the {@link gps.bytecode.interfaces.BytecodeOptimizationHelper#canBeGreaterThan(Optimizable, double)} 
     * returns a SatisfactionProblem that is not satisfiable if the value can not be exceeded. 
     * Per default, the minimal value for our instance of the PlaygroundProblem is 0. Since, in this case, we want 
     * to minimize, 0 can not be exceeded.
     */
    @Ignore
    @Test
    public void testGreaterThanMinimizeNotSatisfiable() {
        opt.setMaximize((byte) -1);
        SatisfactionProblemSolution result = BytecodeOptimizationHelper
                .canBeGreaterThan(opt, 0);
        if (result != null) {
            assertFalse(result.satisfiable);
        }
    }

    /**
     * Tests if the BytecodeOptimizationHelper can handle classes containing 
     * non primitive fields if the right annotation is used.
     */
    @Ignore
    @Test
    public void testDeepExample1() {
        opt = new Optimizable<>(GPS.wrap(new DeepClass1()));
        opt.setMaximize((byte) 1);
        SatisfactionProblemSolution result = BytecodeOptimizationHelper
                .canBeGreaterThan(opt, 0);
        if (result != null) {
            assertTrue(result.satisfiable);
        }
    }

    /**
     * Tests if the BytecodeOptimizationHelper can handle classes containing 
     * multiple non primitive fields if the right annotations are used.
     */
    @Ignore
    @Test
    public void testDeepExample2() {
        opt = new Optimizable<>(GPS.wrap(new DeepClass2()));
        opt.setMaximize((byte) 1);
        SatisfactionProblemSolution result = BytecodeOptimizationHelper
                .canBeGreaterThan(opt, 0);
        if (result != null) {
            assertTrue(result.satisfiable);
        }
        for (Constant c : result.variableValues.values()) {
            System.out.println(c.getValue().toString());
        }
    }

}
