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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import gps.GPS;
import gps.optimization.OptimizationReturn;
import gps.optimization.algorithm.incrementalSat.IncrementalSat;
import gps.optimization.flattening.Flattener;
import gps.optimization.wrapper.Optimizable;
import optimization.pp.PlaygroundProblem;

/**
 * Tests the {@link gps.optimization.algorithm.incrementalSat.IncrementalSat} 
 * solver. This class can only be tested in a meaningful way if the backend 
 * used to solve the the constructed SatisfactionProblem 
 * is != the Z3StdoutBackend.
 * 
 * @author mburri
 *
 */
public class IncrementalSatTest {

    /**
     * The {@link gps.optimization.wrapper.Optimizable} used for the
     * test.
     */
    Optimizable<PlaygroundProblem> opt;

    /**
     * The instance of the incremental SAT solver used for the test
     */
    IncrementalSat<PlaygroundProblem> iS;

    @Before
    public void init() {
        opt = new Optimizable<>(GPS.wrap(new PlaygroundProblem(24)));
        iS = new IncrementalSat<PlaygroundProblem>(opt,
                new Flattener().flattenAndWrap(opt.getDefaultParams()), 1);
    }

    /**
     * Tests the incremental SAT solver with an instance of the PlaygroundProblem (z = 24). 
     * Asserts that the solver finds the best possible solution for this instance. 
     * Also asserts that the solver terminates if the threshold is not exceedable. 
     */
    @Ignore
    @Test
    public void testPP24() {
        iS.setMaxSteps(100);
        iS.maximize();
        if (iS.retrieveResult().isPresent()) {
            OptimizationReturn result = iS.retrieveResult().get();
            assertTrue(result.getBestSolutionEval() == 72.0);
            assertTrue(6 == (int) result.getBestSolution()[0]);
            assertTrue(12 == (int) result.getBestSolution()[1]);
            assertTrue(iS.getMaxSteps() == 0);
        }
    }

    /**
     * Tests the incremental SAT solver with an instance of the PlaygroundProblem (z = 24).
     * Asserts that the minimize-function returns a worse solution than the initial not optimal 
     * solution. Asserts that the function returns a solution with the value 0 since that 
     * is the worst possible solution value. Also asserts that the solver is terminated if 
     * the threshold is not exceedable.
     */
    @Ignore
    @Test
    public void testPP24Minimize() {
        // Initialize the solver with a suboptimal solution
        iS = new IncrementalSat<PlaygroundProblem>(opt,
                new Flattener().flattenAndWrap(new Object[] { 1, 1 }), 1);
        iS.setMaxSteps(100);
        iS.minimize();
        if (iS.retrieveResult().isPresent()) {
            OptimizationReturn result = iS.retrieveResult().get();
            assertTrue(result.getBestSolutionEval() == 0.0);
            assertTrue(iS.getMaxSteps() == 0);
        }
    }

    /**
     * Tests if the solver is not prematurely terminated when the best solution has 
     * not been found in the given amount of maximal steps. Somehow, git doesnt like this test.
     */
    @Ignore
    @Test
    public void testOptProcessNotTerminated() {
        iS.setMaxSteps(5);
        iS.maximize();
        assertTrue(iS.getMaxSteps() > 0);
    }

    /**
     * Tests the incremental SAT solver minimizing if the value that is to be tested 
     * is 0 (initially). Asserts that the best found value is 0 and the optimization 
     * process is terminated.
     */
    @Ignore
    @Test
    public void testValueZeroBehaviour() {
        iS.setMaxSteps(100);
        iS.minimize();
        if (iS.retrieveResult().isPresent()) {
            OptimizationReturn result = iS.retrieveResult().get();
            assertTrue(result.getBestSolutionEval() == 0.0);
            assertTrue(iS.getMaxSteps() == 0);
        }
    }

    /**
     * Tests the incremental SAT solver if it is initialized with the best possible 
     * solution. Asserts that the best found value is 72.0 (for our instance of 
     * the playground problem) and the optimization process is terminated. 
     */
    @Ignore
    @Test
    public void testInitialValueOptimalSolution() {
        iS = new IncrementalSat<PlaygroundProblem>(opt,
                new Flattener().flattenAndWrap(new Object[] { 6, 12 }), 1);
        iS.setMaxSteps(100);
        iS.maximize();
        if (iS.retrieveResult().isPresent()) {
            OptimizationReturn result = iS.retrieveResult().get();
            assertTrue(result.getBestSolutionEval() == 72.0);
            assertTrue(iS.getMaxSteps() == 0);
        }
    }

}
