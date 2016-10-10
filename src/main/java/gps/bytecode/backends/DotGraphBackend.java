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
package gps.bytecode.backends;

import gps.bytecode.expressions.ProcessedFunctionCall;
import gps.bytecode.expressions.IExpressionVisitor.IDefaultExpressionVisitor;
import gps.bytecode.symexec.SEFunction;
import gps.bytecode.symexec.SatisfactionProblem;
import gps.bytecode.transforms.PartialEvaluation;
import gps.bytecode.transforms.TransformationPasses;

/**
 * This backend outputs a dotgraph to stdout where nodes represent SEFunctions and Edges represent function calls
 *
 * Used to visualize and debug function call structure in large problems
 * 
 * @author mfunk@tzi.de
 *
 */
public class DotGraphBackend implements IBytecodeBackend {

    static class DotVisitor implements IDefaultExpressionVisitor {
        private final SEFunction f;

        public DotVisitor(SEFunction f) {
            this.f = f;
        }

        @Override
        public void visitFunctionCallExpression(ProcessedFunctionCall fc) {
            System.out.println(f.getShortName() + " -> "
                    + fc.getTargetFunction().getShortName());
        }
    }

    @Override
    public BackendResult solve(SatisfactionProblem problem) {
        TransformationPasses.transform(problem);
        System.out.println("digraph {");

        for (final SEFunction f : BackendUtil.getAllFunctions(problem)) {
            if (PartialEvaluation.isStrongRecursive(f)) {
                System.out.println(
                        f.getShortName() + " [style=filled, fillcolor=red]");
            }
            DotVisitor d = new DotVisitor(f);
            f.asExpression().accept(d);
        }
        System.out.println("}");
        return BackendResult.UNABLE_TO_SOLVE;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

}
