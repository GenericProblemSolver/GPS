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

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.List;

import gps.ISolverModule;
import gps.IWrappedProblem;
import gps.ResultEnum;
import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Variable;
import gps.common.AbstractAlgorithm;

/**
 * 
 * I have no idea of the purpose of this class
 * @author mfunk@tzi.de
 *
 * @param <T>
 */
public class BytecodeModule<T> implements ISolverModule<T>, IBytecodeResult<T> {

    IWrappedProblem<T> problem;

    public BytecodeModule(IWrappedProblem<T> pProblem) {
        problem = pProblem;
    }

    @Override
    public boolean canSolve(ResultEnum type) {
        return ResultEnum.SATISFYING_MODEL == type;
    }

    @Override
    public List<? extends AbstractAlgorithm> getApplicableAlgorithms(
            ResultEnum pResultEnum) {
        return new ArrayList<>();
    }

    @Override
    public Optional<Map<Variable, Constant>> satisfyingModel() {
        BytecodeAlgorithm<T> b = new BytecodeAlgorithm<>(this);
        return b.satisfyingModel();
    }
}
