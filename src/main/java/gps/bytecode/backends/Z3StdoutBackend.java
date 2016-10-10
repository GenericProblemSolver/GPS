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

import java.io.PrintStream;
import java.util.List;

import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Expression;
import gps.bytecode.expressions.IExpressionVisitor.IDefaultExpressionVisitor;
import gps.bytecode.expressions.OperationExpression;
import gps.bytecode.expressions.Operator;
import gps.bytecode.expressions.Operator.Reference;
import gps.bytecode.expressions.Operator.RuntimeType;
import gps.bytecode.expressions.Operator.Undef;
import gps.bytecode.expressions.ProcessedFunctionCall;
import gps.bytecode.expressions.Variable;
import gps.bytecode.symexec.SEFunction;
import gps.bytecode.symexec.SatisfactionProblem;
import gps.bytecode.transforms.ExpressionTransformer;
import gps.bytecode.transforms.TransformationPasses;

/**
 * A backend implementation that prints the function definitions to stdout in an
 * z3-compatible format
 * 
 * @author mfunk@tzi.de
 *
 */
public class Z3StdoutBackend implements IBytecodeBackend {

    final PrintStream out;

    public Z3StdoutBackend() {
        this(System.out);
    }

    Z3StdoutBackend(PrintStream out) {
        this.out = out;
    }

    @Override
    public BackendResult solve(SatisfactionProblem problem) {
        TransformationPasses.transform(problem);
        System.out.println("Done");

        List<SEFunction> functions = BackendUtil.getOrderedFunctions(problem);

        System.out.println("Size: " + functions.size());
        PrintVisitor pv = new PrintVisitor(out);
        ExpressionTransformer trans = new ExpressionTransformer(
                Z3StdoutBackend::notEqToNegTransform);

        for (SEFunction e : functions) {
            out.print("(define-fun " + e.getShortName() + " (");
            for (Variable v : e.getParameters()) {
                out.print(" (");
                out.print(v.toString() + " " + getZ3Type(v));
                out.print(")");
            }
            out.print(" ) ");
            out.print(getZ3Type(e.asExpression()) + " ");
            out.println();
            out.print("  ");
            trans.transform(e.asExpression()).accept(pv);
            out.println();
            out.println(")");
        }

        for (Variable v : problem.problemVariables.keySet()) {
            out.print("(declare-fun ");
            out.print(v.toString());
            out.print(" () ");
            out.print(getZ3Type(v));
            out.println(")");
        }

        // Declare parameters
        for (SEFunction constraint : problem.constraints) {
            boolean hasParameter = constraint.getParameters().iterator()
                    .hasNext();
            out.print("(assert(= ");
            if (hasParameter) {
                out.print("(");
            }
            out.print(constraint.getShortName());
            for (Variable param : constraint.getParameters()) {
                String name = globalParameterName(problem, constraint, param);
                out.print(" ");
                out.print(name);
            }
            if (hasParameter) {
                out.print(")");
            }

            if (constraint.asExpression().getType() == boolean.class) {
                out.println(" true ))");
            } else if (getZ3Type(constraint.asExpression()).equals("Real")) {
                out.println(" 1.0 ))");
            } else {
                out.println(" 1 ))");
            }
        }

        out.println("(check-sat)");
        out.println("(get-model)");
        return BackendResult.UNABLE_TO_SOLVE;
    }

    /**
     * Converts internal operator names to Z3 compatible ones
     * 
     * @param op
     * @return
     */
    static String operatorToZ3Operator(Operator op) {
        // TODO: fix for using bitvectors
        switch (op) {
        case TO_INT:
            return "to_int";
        case TO_SHORT:
            return "to_int";
        case TO_LONG:
            return "to_int";
        case TO_BYTE:
            return "to_int";
        case TO_FLOAT:
            return "to_real";
        case TO_DOUBLE:
            return "to_real";
        case TO_CHAR:
            return "to_int";
        default:
            return op.toString();
        }
    }

    static private class PrintVisitor implements IDefaultExpressionVisitor {
        PrintStream out;

        public PrintVisitor(PrintStream out) {
            this.out = out;
        }

        @Override
        public void visitConstant(Constant c) {
            out.print(c.toString());
        }

        @Override
        public void visitOperatorExpression(OperationExpression op) {
            out.print("( " + operatorToZ3Operator(op.getOperator()));
            for (Expression e : op.getParameters()) {
                out.print(" ");
                e.accept(this);
            }
            out.print(" )");

        }

        @Override
        public void visitVariableExpression(Variable v) {
            out.print(v.toString());
        }

        @Override
        public void visitProcessedFunctionCall(ProcessedFunctionCall fc) {
            String name = fc.getTargetFunction().getShortName();

            if (!fc.getParameters().iterator().hasNext()) {
                // Syntax for "constants"
                out.print(name);
                return;
            }
            out.print("( " + name);

            for (Expression e : fc.getParameters()) {
                out.print(" ");
                e.accept(this);
            }
            out.print(" )");
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    static Expression notEqToNegTransform(Expression operation) {
        if (operation instanceof OperationExpression) {
            final OperationExpression o = (OperationExpression) operation;
            if (o.getOperator().equals(Operator.NOT_EQUAL)) {
                return new OperationExpression(Operator.NOT,
                        new OperationExpression(Operator.EQUAL,
                                o.getParameters()));
            }
        }
        return operation;
    }

    /**
     * Returns the name of a constraint parameter for global declaration
     * 
     * Heap parameters don't get a prefix, since they are shared by all
     * constraints
     * 
     * @param constraint
     * @param param
     * @return
     */
    static String globalParameterName(SatisfactionProblem problem, SEFunction c,
            Variable param) {
        if (problem.problemVariables.containsKey(param)) {
            return param.toString();
        } else {
            return param.intoNamespacedVariable(c).toString();
        }
    }

    /**
    *  Returns the String representation of a z3-compatible type for a given Expression
    * @param e
    * @return
    */
    public static String getZ3Type(final Expression e) {
        if (e.getType() == int.class || e.getType() == byte.class
                || e.getType() == short.class || e.getType() == long.class
                || e.getType() == char.class) {
            return "Int";
        } else if (e.getType() == Undef.class) {
            return "Undef";
        } else if (e.getType() == Reference.class) {
            return "Int";
        } else if (e.getType() == boolean.class) {
            return "Bool";
        } else if (e.getType() == double.class || e.getType() == float.class) {
            return "Real";
        } else if (e.getType() == RuntimeType.class) {
            return "Int";
        } else {
            return e.getType().toString();
        }
    }
}