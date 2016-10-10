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
package gps.bytecode.transforms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;

import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Expression;
import gps.bytecode.expressions.IExpressionVisitor.IDefaultExpressionVisitor;
import gps.bytecode.expressions.Operator.Heap;
import gps.bytecode.expressions.OperationExpression;
import gps.bytecode.expressions.Operator;
import gps.bytecode.expressions.ProcessedFunctionCall;
import gps.bytecode.expressions.Variable;
import gps.bytecode.symexec.SEFunction;

/**
 *
 * Helper class to transform complex expressions correctly given a simple
 * non-recursive transformation function
 * 
 * @author mfunk@tzi.de
 *
 */
public class ExpressionTransformer implements Consumer<SEFunction> {

    /**
     * This visitor transforms the expression tree post-order and uses a Stack
     * to construct a new transformed expression tree
     *
     */
    private class TransformVisitor implements IDefaultExpressionVisitor {
        Stack<Expression> exprs = new Stack<>();

        @Override
        public void visitConstant(Constant c) {
            if (c.getType() == Heap.class) {
                List<Expression> hlist = Constant.getHeap(c);
                int size = hlist.size();
                for (Expression e : hlist) {
                    e.accept(this);
                }
                ArrayList<Expression> nHeap = new ArrayList<>(hlist.size());
                for (int i = 0; i < size; ++i) {
                    nHeap.add(null);
                }
                for (int i = size - 1; i >= 0; --i) {
                    nHeap.set(i, exprs.pop());
                }
                exprs.push(new Constant(Heap.class, nHeap));
            } else {
                exprs.push(transform.apply(c));
            }
        }

        @Override
        public void visitVariableExpression(Variable v) {
            exprs.push(transform.apply(v));
        }

        @Override
        public void visitFunctionCallExpression(ProcessedFunctionCall fc) {
            int paramcount = fc.getParameters().size();
            Expression[] parameters = new Expression[paramcount];

            // Get Parameters in reverse order, since they are on a stack
            for (int i = paramcount - 1; i >= 0; --i) {
                parameters[i] = exprs.pop();
            }
            // Reconstruct new PFC with new parameters and transform it
            exprs.push(transform.apply(new ProcessedFunctionCall(
                    fc.getTargetFunction(), Arrays.asList(parameters))));
        }

        @Override
        public void visitOperatorExpression(OperationExpression op) {
            final Operator operator = op.getOperator();

            //Special case to evaluate the first parameter of an ITE first, since otherwise illegal expressions may be created in some rare cases
            if (operator == Operator.ITE) {
                op.getParameters().get(0).accept(this);
                OperationExpression op2 = new OperationExpression(operator,
                        exprs.pop(), op.getParameters().get(1),
                        op.getParameters().get(2));
                Expression e = transform.apply(op2);
                //For everything but the same 
                if (!(e instanceof OperationExpression)) {
                    e.accept(this);
                    return;
                }
            }
            int paramcount = op.getParameters().size();
            Expression[] parameters = new Expression[paramcount];

            for (Expression e : op.getParameters()) {
                e.accept(this);
            }

            // Get Parameters in reverse order, since they are on a stack
            for (int i = paramcount - 1; i >= 0; --i) {
                parameters[i] = exprs.pop();
            }

            // Reconstruct new OperationExpression with new parameters and
            // transform it, keep the old type as a typehint
            exprs.push(transform.apply(new OperationExpression(op.getOperator(),
                    op.getType(), parameters)));

        }

    }

    private final Function<Expression, Expression> transform;

    /**
     * Construct a new ExpressionTransformer that uses the given transform
     * 
     * @param transform
     */
    public ExpressionTransformer(Function<Expression, Expression> transform) {
        this.transform = transform;
    }

    /**
     * Transforms the given Expression with the transform function
     * 
     * @param e
     * @return
     */
    public Expression transform(Expression e) {
        try {
            TransformVisitor v = new TransformVisitor();
            e.accept(v);
            return v.exprs.pop();
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(
                    "Error transforming expression " + e.toString() + "\n" + t);
        }
    }

    /**
     * Transforms the SEFunction
     *
     * WARNING! This modifies the SEFunction
     * 
     * @param f
     * @return
     */
    public SEFunction transform(SEFunction f) {
        //TODO: figure out a way to make SEFunction immutable
        f.setExpression(transform(f.asExpression()));
        return f;
    }

    @Override
    public void accept(SEFunction f) {
        transform(f);
    }

    @Override
    public Consumer<SEFunction> andThen(Consumer<? super SEFunction> after) {
        if (!(after instanceof ExpressionTransformer)) {
            return Consumer.super.andThen(after);
        }
        ExpressionTransformer afterTrans = (ExpressionTransformer) after;
        return new ExpressionTransformer(
                this.transform.andThen(afterTrans.transform));
    }
}
