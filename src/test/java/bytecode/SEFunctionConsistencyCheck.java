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
package bytecode;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gps.bytecode.backends.BackendUtil;
import gps.bytecode.backends.IBytecodeBackend;
import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Expression;
import gps.bytecode.expressions.IExpressionVisitor;
import gps.bytecode.expressions.FunctionCall;
import gps.bytecode.expressions.OperationExpression;
import gps.bytecode.expressions.ProcessedFunctionCall;
import gps.bytecode.expressions.Variable;
import gps.bytecode.symexec.SEFunction;
import gps.bytecode.symexec.SatisfactionProblem;

/**
 * This Class implements three checks that verify the consistency of SEFunctions
 *  * All used Variables have to be defined as Parameters
 *  * All Parameters have to be used
 *  * All FunctionCalls have to have the correct number of Parameters
 * 
 * @author mfunk@tzi.de
 */
public class SEFunctionConsistencyCheck {
    public static class ConsistencyException extends Exception {
        private static final long serialVersionUID = 3618590257456015981L;

        public ConsistencyException(String msg) {
            super(msg);
        }

    }

    static void check(SatisfactionProblem p) throws ConsistencyException {
        Collection<SEFunction> allFunctions = BackendUtil.getAllFunctions(p);

        for (SEFunction f : allFunctions) {
            Map<String, Boolean> vars = f.getParameters().stream().collect(
                    Collectors.toMap((v) -> v.toString(), (v) -> false));

            final IExpressionVisitor checkVisitor = new IExpressionVisitor() {
                @Override
                public void visitVariableExpression(Variable v) {
                    // Check if used variables are defined
                    if (vars.containsKey(v.toString())) {
                        vars.put(v.toString(), true);
                    } else {
                        throw new RuntimeException("Variable " + v
                                + " is unknown in SEFunction " + f.getName());
                    }
                }

                @Override
                public void visitOperatorExpression(OperationExpression op) {
                    for (Expression e : op.getParameters()) {
                        e.accept(this);
                    }
                }

                @Override
                public void visitFunctionCall(FunctionCall fc) {
                    checkFunction(fc.getTargetFunction(), fc.getParameters());
                }

                @Override
                public void visitConstant(Constant c) {
                }

                @Override
                public void visitProcessedFunctionCall(
                        ProcessedFunctionCall fc) {
                    checkFunction(fc.getTargetFunction(), fc.getParameters());
                }

                void checkFunction(SEFunction target,
                        List<Expression> parameters) {
                    int reqNumParams = target.getParameters().size();
                    int actualParams = parameters.size();

                    // Throw if number of Parameters doesn't Match
                    if (reqNumParams != actualParams) {
                        throw new RuntimeException("Function call to "
                                + target.getName() + " requires " + reqNumParams
                                + " but has " + actualParams
                                + "Parameters in SEFunction " + f.getName());
                    }

                    for (Expression e : parameters) {
                        e.accept(this);
                    }
                }
            };

            try {
                f.asExpression().accept(checkVisitor);
            } catch (Throwable t) {
                throw new ConsistencyException(t.getMessage());
            }

            //Dont complaint about unused parameters on top level functions
            if (p.constraints.contains(f)) {
                return;
            }
            // Check if all Parameters were used
            if (vars.values().stream().anyMatch((b) -> !b)) {
                String unusedParams = vars.entrySet().stream()
                        .filter((e) -> !e.getValue()).map((e) -> e.getKey())
                        .collect(Collectors.joining(", "));
                throw new ConsistencyException(
                        "Unused Parameters in SEFunction " + f.getName() + ": "
                                + unusedParams);
            }
        }

    }

}
