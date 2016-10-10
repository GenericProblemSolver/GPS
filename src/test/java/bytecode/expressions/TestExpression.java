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
package bytecode.expressions;

import org.junit.BeforeClass;
import org.junit.Test;

import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Expression;
import gps.bytecode.expressions.OperationExpression;
import gps.bytecode.expressions.OperationExpression.InvalidExpressionException;
import gps.bytecode.expressions.Operator;
import gps.bytecode.expressions.Variable;

/**
 * Little test class showing how the expression package works
 * 
 * @author marlonfl
 *
 */
public class TestExpression {

    static Expression expr1;
    static Expression expr2;

    @BeforeClass
    public static void setup() {

        expr1 = new OperationExpression(Operator.ADD,
                new Variable("a", int.class), new Variable("e", int.class));

        expr2 = new OperationExpression(Operator.GREATER_OR_EQUAL, expr1,
                new Constant(3));

    }

    @Test
    public void testExpressionCreation() {
        new Variable("a", boolean.class);
        new Constant(true);
    }

    @Test
    public void testExpressionCreationByString() {

        new OperationExpression(Operator.getOpBySymbol("*"), expr1,
                new Variable("b", int.class));
    }

    @Test
    public void testITECreation() {
        new OperationExpression(Operator.ITE, new Constant(true), expr1,
                new Variable("t", int.class));
    }

    @Test
    public void testEqualCreation() {
        new OperationExpression(Operator.EQUAL, expr2, new Constant(false));

    }

    @Test(expected = InvalidExpressionException.class)
    public void testExceptionWrongParameterCount() {
        new OperationExpression(Operator.GREATER_OR_EQUAL, expr1,
                new Constant(3), new Variable("q", int.class));
    }

}
