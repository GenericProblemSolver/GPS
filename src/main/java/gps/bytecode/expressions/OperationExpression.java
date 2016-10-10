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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import gps.bytecode.expressions.Operator.Heap;
import gps.bytecode.expressions.Operator.Reference;
import gps.bytecode.expressions.Operator.Undef;

/**
 * Class representing an expression that uses an operator and multiple
 * parameters
 * 
 * @author marlonfl
 *
 */
public class OperationExpression extends Expression {

    /**
     * parameters used in the expression, subexpressions or constants/variables
     */
    private ArrayList<Expression> params = new ArrayList<>();

    /**
     * the operator used in the expression
     */
    private Operator operator;

    /**
     * output type of the expressions, basically output type of the operator
     */
    private Class<?> type;

    /**
     * Type hint provided from outside to help some operators to deduce their
     * correct type
     */
    private Class<?> typeHint = Undef.class;

    /**
     * Constructor for expressions
     */
    public OperationExpression(Operator operator, Expression... exps) {
        createExpression(operator, exps);
    }

    /**
     * Constructor for expressions with additional type hint
     */
    public OperationExpression(Operator operator, Class<?> typeHint,
            Expression... exps) {
        this.typeHint = typeHint;
        createExpression(operator, exps);
    }

    /**
     * Constructor taking a collection of parameters
     * 
     * @param operator
     * @param params
     */
    public OperationExpression(Operator operator,
            Collection<Expression> params) {
        createExpression(operator,
                params.toArray(new Expression[params.size()]));
    }

    /**
     * Another constructor for expressions that you can use if u don't want to
     * pass the Operator-object when creating a new OperationExpression
     * 
     * Instead you can just pass the symbol of the operator as a string, might
     * be more intuitive to use, will throw an exception if there's no operator
     * with this symbol
     */
    public OperationExpression(String operator, Expression... exps) {
        createExpression(Operator.getOpBySymbol(operator), exps);
    }

    public Operator getOperator() {
        return operator;
    }

    public ArrayList<Expression> getParameters() {
        return params;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public void setType(Class<?> type) {
        this.type = type;
    }

    /**
     * is called by both constructors to not have duplicate code
     */
    public void createExpression(Operator operator, Expression[] exps) {
        this.operator = operator;
        for (Expression e : exps) {
            params.add(e);
        }
        isValidExpression();
        num_called += 1;
    }

    public static int num_called = 0;

    /**
     * Makes sure the expression is valid, always called in constructor, also
     * sets the type of the expression if considered valid
     * 
     * checks if the parameter number matches the one required by the operator,
     * also checks if the types of the subexpressions match the input type of
     * the operator, and if the parameter types differ
     * 
     * @throws InvalidExpressionException
     *             with additional information if something is wrong
     */
    public void isValidExpression() {
        /**
         * checking if parameter count is as specified by operator
         */
        if (operator.getParameterCount() != params.size()) {
            throw new InvalidExpressionException(this,
                    "Parameter count " + params.size()
                            + " doesn't match number specified by operator "
                            + operator.toString() + " ("
                            + operator.getParameterCount() + ")");
        }

        /**
         * checking if types of parameters match input types of the operator and
         * at the same time building the parameterTypes-List for next check
         */
        List<Class<?>> opInputTypes = Arrays.asList(operator.getInputTypes());
        List<Class<?>> parameterTypes = new ArrayList<>();
        for (Expression parameter : params) {
            parameterTypes.add(parameter.getType());
            if (!opInputTypes.contains(parameter.getType())) {
                System.out.println(parameter);
                System.out.println(operator);
                throw new InvalidExpressionException(this,
                        "Parameter type " + parameter.getType().getSimpleName()
                                + " doesn't match any of the input types specified by operator "
                                + operator.toString());
            }
        }

        if (operator == Operator.PUT) {
            if (parameterTypes.get(0) != Heap.class) {
                throw new InvalidExpressionException(this,
                        "First param not a heap");
            }

            if (parameterTypes.get(1) != Reference.class) {
                if (parameterTypes.get(1) == Undef.class) {
                    params.get(1).setType(Reference.class);
                } else {
                    throw new InvalidExpressionException(this,
                            "Second param is " + parameterTypes.get(1)
                                    + " should be Reference");
                }
            }

            this.type = Heap.class;
            return;
        }

        if (operator == Operator.GET) {
            if (parameterTypes.get(0) != Heap.class) {
                throw new InvalidExpressionException(this,
                        "First param not a heap");
            }

            if (parameterTypes.get(1) != Reference.class) {
                if (parameterTypes.get(1) == Undef.class) {
                    params.get(1).setType(Reference.class);
                } else {
                    throw new InvalidExpressionException(this,
                            "Second param is " + parameterTypes.get(1)
                                    + " should be Reference");
                }
            }
            // Respect the type hint
            this.type = typeHint;
            return;
        }

        /**
         * Checking if all the parameters have the same type (for expressions
         * with 2 parameters). For expressions with only 1 parameter this check
         * is obv not nescessary.
         * 
         * Only operator with 3 parameters is currently ITE, where the first
         * parameter needs to be a boolean and the remaining two parameters need
         * to have the same type. ITE is checked separately, might need changing
         * if we add more operators with 3 parameters
         */
        if (operator.getParameterCount() == 2) {
            Class<?> pType1 = parameterTypes.get(0);
            Class<?> pType2 = parameterTypes.get(1);
            if (operator == Operator.ADD) {
                if (pType1 == Reference.class && pType2 == int.class) {
                    this.type = Reference.class;
                    return;
                } else if (pType1 == int.class && pType2 == Reference.class) {
                    this.type = Reference.class;
                    return;
                }
            }
            if (pType1 != pType2) {
                // not sure if the whole undef check here is still needed or if
                // it can't happen anymore anyway because of the
                // reqStackParams-Method
                if (pType1 == Undef.class) {
                    params.get(0).setType(pType2);
                } else if (pType2 == Undef.class) {
                    params.get(1).setType(pType1);
                } else {
                    throw new InvalidExpressionException(this,
                            "Expressions parameters have different type: "
                                    + pType1.getSimpleName() + " != "
                                    + pType2.getSimpleName());
                }
            }

        } else if (operator.getParameterCount() == 3) {
            // specifically for ITE
            if (parameterTypes.get(0) != boolean.class
                    && parameterTypes.get(0) != Undef.class) {
                throw new InvalidExpressionException(this,
                        "First parameter of ITE needs to be a boolean");
            }

            Class<?> pType1 = parameterTypes.get(1);
            Class<?> pType2 = parameterTypes.get(2);

            if (pType1 != pType2) {
                // see above
                if (pType1 == Undef.class) {
                    this.type = typeHint;
                    return;
                } else if (pType2 == Undef.class) {
                    this.type = typeHint;
                    return;
                } else {
                    // With the heap stuff, ite can contain parameters of
                    // different types, use the typehint
                    this.type = typeHint;
                    return;
                }
            }

        }

        /**
         * As it is implemented now, the type of an expression whose operator
         * has identical input and output types is determined by the type of its
         * parameters. Thats why for expressions like that we can just get the
         * type of one of its parameters and have the type for the whole
         * expression.
         * 
         * There are, however, certain operators like > that evaluate the
         * parameters given and based on those will give back an expression with
         * a type different from those of the input parameters. For now these
         * operators only have a single output type(+undef), so the return type
         * of an expression using that operator will be that exact return type.
         * This might change in future implementations, so this method will have
         * to be edited, but for now it works this way.
         */
        List<Class<?>> opOutputTypes = Arrays.asList(operator.getOutputTypes());
        if (opOutputTypes.equals(opInputTypes)) {
            this.type = parameterTypes.get(parameterTypes.size() - 1);
            // Special case for better typededuction in ITE
            if (type == Undef.class && parameterTypes.size() == 3) {
                type = Undef.class;
            }
        } else if (opOutputTypes.size() == 2 /*
                                              * means it only has 1 return type,
                                              * since 2nd one is Undef.class
                                              */) {
            this.type = opOutputTypes.get(0);
        } else {
            // can probably not happen anymore
            throw new InvalidExpressionException(this,
                    "Undef is Operator result");
        }
    }

    /**
     * formats the expression into some internal string representation, kinda
     * looks like lisp or z3 input
     */
    @Override
    public String toString() {
        StringBuilder expr = new StringBuilder();
        expr.append("(");
        expr.append(operator.toString());
        for (Expression parameter : params) {
            expr.append(" ");
            expr.append(parameter.toString());
        }
        expr.append(")");
        return expr.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, params, type);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OperationExpression)) {
            return false;
        }
        OperationExpression e = (OperationExpression) o;
        if (e.hashCode() != this.hashCode()) {
            return false;
        }
        if (!e.operator.equals(this.operator)) {
            return false;
        }
        if (!e.params.equals(this.params)) {
            return false;
        }
        if (!e.type.equals(this.type)) {
            return false;
        }
        return true;
    }

    public static class InvalidExpressionException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public InvalidExpressionException(Expression expr, String desc) {
            super("\n\nInvalid Expression: " + expr.toString() + "\n" + desc
                    + "\n");
        }
    }

    @Override
    public void accept(IExpressionVisitor visitor) {
        visitor.visitOperatorExpression(this);
    }

    @Override
    public List<FunctionCall> getFunctionCalls() {
        ArrayList<FunctionCall> fcalls = new ArrayList<>();
        for (Expression param : params) {
            fcalls.addAll(param.getFunctionCalls());
        }
        return fcalls;
    }
}
