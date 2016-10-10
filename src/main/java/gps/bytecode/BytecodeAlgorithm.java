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
package gps.bytecode;

import java.util.Map;
import java.util.Optional;

import gps.ResultEnum;
import gps.bytecode.backends.Backends;
import gps.bytecode.backends.IBytecodeBackend;
import gps.bytecode.backends.IBytecodeBackend.BackendResult;
import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Variable;
import gps.bytecode.symexec.SatisfactionProblem;
import gps.bytecode.symexec.SymbolicExec;
import gps.common.AbstractAlgorithm;

/**
 * I have no idea of the purpose of this class
 * @author mfunk
 *
 * @param <T>
 */
public class BytecodeAlgorithm<T> extends AbstractAlgorithm
        implements IBytecodeResult<T> {
    BytecodeModule<T> module;

    public BytecodeAlgorithm(BytecodeModule<T> m) {
        module = m;
    }

    @Override
    public Optional<Map<Variable, Constant>> satisfyingModel() {
        T t = module.problem.getSource();

        Iterable<IBytecodeBackend> backends = Backends.getAvailableBackends();
        for (IBytecodeBackend b : backends) {
            SatisfactionProblem problem = SymbolicExec
                    .constructFunctionalSatisfactionProblem(t);
            BackendResult result = b.solve(problem);
            if (result == BackendResult.SOLVED) {
                return Optional.of(problem.solution.variableValues);
            }
        }

        return Optional.empty();
    }

    @Override
    public String getName() {
        return "BYTECODE, BITCHEZ!";
    }

    @Override
    public boolean isApplicable(ResultEnum type) {
        return type == ResultEnum.SATISFYING_MODEL;
    }

}
