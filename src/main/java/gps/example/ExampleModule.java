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
package gps.example;

import java.util.ArrayList;
import java.util.List;

import gps.ISolverModule;
import gps.IWrappedProblem;
import gps.ResultEnum;
import gps.common.AbstractAlgorithm;

public class ExampleModule<T> implements ISolverModule<T> {

    public ExampleModule(IWrappedProblem<T> pProblem) {
    }

    @Override
    public boolean canSolve(ResultEnum type) {
        return false;
    }

    @Override
    public List<? extends AbstractAlgorithm> getApplicableAlgorithms(
            ResultEnum pResultEnum) {
        return new ArrayList<>();
    }
}
