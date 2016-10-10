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
package gps.bytecode.interfaces;

import gps.bytecode.backends.Backends;
import gps.bytecode.backends.IBytecodeBackend;
import gps.bytecode.backends.IBytecodeBackend.BackendResult;
import gps.bytecode.backends.IBytecodeBackend.SatisfactionProblemSolution;
import gps.bytecode.symexec.SatisfactionProblem;
import gps.bytecode.symexec.SymbolicExec;
import gps.optimization.wrapper.Optimizable;

/**
 * Provides an interface for optimization problems. 
 * Evaluates, whether the {@link gps.optimization.wrapper.Optimizable#objectiveFunction(Object[])} 
 * of the given {@link gps.optimization.wrapper.Optimizable} can exceed the given threshold by 
 * constructing a {@link gps.bytecode.symexec.SatisfactionProblem} and testing it 
 * for satisfiability. 
 * 
 * @author mfluegge, mburri
 *
 */
public class BytecodeOptimizationHelper {

    /**
     * Returns the constructed SatisfactionProblem solved by the selected 
     * backend. 
     * 
     * @param opt
     * 			the Optimizable the SatisfactionProblem is to be created for
     * @param threshold
     * 			the threshold that is to be tested
     * @return
     * 			the constructed SatisfactionProblem
     */
    public static SatisfactionProblemSolution canBeGreaterThan(
            Optimizable<?> opt, final double threshold) {
        opt.setThresholdForObjectiveFunction(threshold);
        Iterable<IBytecodeBackend> backends = Backends.getAvailableBackends();
        for (IBytecodeBackend b : backends) {
            SatisfactionProblem problem = SymbolicExec
                    .constructFunctionalSatisfactionProblem(opt);
            BackendResult result = b.solve(problem);
            if (result == BackendResult.SOLVED) {
                return problem.solution;
            }
        }
        return null;
    }

}
