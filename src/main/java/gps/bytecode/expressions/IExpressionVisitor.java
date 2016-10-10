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
package gps.bytecode.expressions;

/**
 * A visitor for Expression
 * @author mfunk@tzi.de
 *
 */
public interface IExpressionVisitor {
    void visitFunctionCall(FunctionCall fc);

    void visitConstant(Constant c);

    void visitOperatorExpression(OperationExpression op);

    void visitVariableExpression(Variable v);

    void visitProcessedFunctionCall(ProcessedFunctionCall fc);

    /**
     * Implements post-order defaults for tree visiting
     * @author mfunk@tzi.de
     *
     */
    public static interface IDefaultExpressionVisitor
            extends IExpressionVisitor {
        @Override
        default void visitFunctionCall(FunctionCall fc) {
            visitProcessedFunctionCall(new ProcessedFunctionCall(
                    fc.getTargetFunction(), fc.getParameters()));
        }

        @Override
        default void visitOperatorExpression(OperationExpression op) {
            for (Expression e : op.getParameters()) {
                e.accept(this);
            }
            visitOperationExpression(op);
        }

        @Override
        default void visitProcessedFunctionCall(ProcessedFunctionCall fc) {
            for (Expression e : fc.getParameters()) {
                e.accept(this);
            }
            visitFunctionCallExpression(fc);
        }

        default void visitFunctionCallExpression(ProcessedFunctionCall fc) {

        }

        default void visitOperationExpression(OperationExpression op) {

        }

        @Override
        default void visitConstant(Constant c) {
        }

        @Override
        default void visitVariableExpression(Variable v) {
        }
    }
}
