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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Expression;
import gps.bytecode.expressions.OperationExpression;
import gps.bytecode.expressions.Operator;
import gps.bytecode.expressions.Operator.Heap;
import gps.bytecode.expressions.Operator.Reference;
import gps.bytecode.expressions.Operator.Undef;
import gps.bytecode.symexec.Typeconversions;
import gps.bytecode.expressions.Variable;
import gps.bytecode.transforms.PartialEvaluation.VariableReplacer;

/**
 * Evaluates constant parts of expressions as far as possible
 * 
 * @author mfunk@tzi.de
 */
public class ConstantExpressionEvaluator {

    /**
     * Create a constant of the requested type from a number
     * 
     * @param n
     * @param type
     * @return
     */
    private static Constant toConstantOfType(Number n, Class<?> type) {
        return new Constant(type, numberToType(n, type));
    }

    /**
     * 
     * @param n
     * @param type
     * @return
     */
    private static Number numberToType(Number n, Class<?> type) {
        if (type == int.class) {
            return n.intValue();
        }
        if (type == double.class) {
            return n.doubleValue();
        }
        if (type == Reference.class) {
            return n.intValue();
        }
        if (type == float.class) {
            return n.floatValue();
        }
        if (type == long.class) {
            return n.longValue();
        }
        if (type == short.class) {
            return n.shortValue();
        }
        if (type == byte.class) {
            return n.byteValue();
        }
        throw new RuntimeException("Unknown expression type: " + type);
    }

    private static Constant constantAdd(Constant a, Constant b) {
        Number sum;
        //We need to differantiate the types for correct overflow behaviour
        if (a.getType() == int.class) {
            sum = a.getNumber().intValue() + b.getNumber().intValue();
        } else if (a.getType() == long.class) {
            sum = a.getNumber().longValue() + b.getNumber().longValue();
        } else if (a.getType() == short.class) {
            sum = a.getNumber().shortValue() + b.getNumber().shortValue();
        } else if (a.getType() == byte.class) {
            sum = a.getNumber().byteValue() + b.getNumber().byteValue();
        } else if (a.getType() == float.class) {
            sum = a.getNumber().floatValue() + b.getNumber().floatValue();
        } else {
            sum = a.getNumber().doubleValue() + b.getNumber().doubleValue();
        }

        if (a.getType() == Reference.class || b.getType() == Reference.class) {
            return toConstantOfType(sum, Reference.class);
        }
        return toConstantOfType(sum, a.getType());
    }

    private static Constant constantSub(Constant a, Constant b) {
        Number sum;
        if (a.getType() == int.class) {
            sum = a.getNumber().intValue() - b.getNumber().intValue();
        } else if (a.getType() == long.class) {
            sum = a.getNumber().longValue() - b.getNumber().longValue();
        } else if (a.getType() == short.class) {
            sum = a.getNumber().shortValue() - b.getNumber().shortValue();
        } else if (a.getType() == byte.class) {
            sum = a.getNumber().byteValue() - b.getNumber().byteValue();
        } else if (a.getType() == float.class) {
            sum = a.getNumber().floatValue() - b.getNumber().floatValue();
        } else {
            sum = a.getNumber().doubleValue() - b.getNumber().doubleValue();
        }
        return toConstantOfType(sum, a.getType());
    }

    private static Constant constantMul(Constant a, Constant b) {
        Number sum;
        if (a.getType() == int.class) {
            sum = a.getNumber().intValue() * b.getNumber().intValue();
        } else if (a.getType() == long.class) {
            sum = a.getNumber().longValue() * b.getNumber().longValue();
        } else if (a.getType() == short.class) {
            sum = a.getNumber().shortValue() * b.getNumber().shortValue();
        } else if (a.getType() == byte.class) {
            sum = a.getNumber().byteValue() * b.getNumber().byteValue();
        } else if (a.getType() == float.class) {
            sum = a.getNumber().floatValue() * b.getNumber().floatValue();
        } else {
            sum = a.getNumber().doubleValue() * b.getNumber().doubleValue();
        }
        return toConstantOfType(sum, a.getType());
    }

    private static Constant constantDiv(Constant a, Constant b) {
        Number sum;
        if (a.getType() == int.class) {
            sum = a.getNumber().intValue() / b.getNumber().intValue();
        } else if (a.getType() == long.class) {
            sum = a.getNumber().longValue() / b.getNumber().longValue();
        } else if (a.getType() == short.class) {
            sum = a.getNumber().shortValue() / b.getNumber().shortValue();
        } else if (a.getType() == byte.class) {
            sum = a.getNumber().byteValue() / b.getNumber().byteValue();
        } else if (a.getType() == float.class) {
            sum = a.getNumber().floatValue() / b.getNumber().floatValue();
        } else {
            sum = a.getNumber().doubleValue() / b.getNumber().doubleValue();
        }
        return toConstantOfType(sum, a.getType());
    }

    private static Constant constantRem(Constant a, Constant b) {
        Number sum;
        if (a.getType() == int.class) {
            sum = a.getNumber().intValue() % b.getNumber().intValue();
        } else if (a.getType() == long.class) {
            sum = a.getNumber().longValue() % b.getNumber().longValue();
        } else if (a.getType() == short.class) {
            sum = a.getNumber().shortValue() % b.getNumber().shortValue();
        } else if (a.getType() == byte.class) {
            sum = a.getNumber().byteValue() % b.getNumber().byteValue();
        } else if (a.getType() == float.class) {
            sum = a.getNumber().floatValue() % b.getNumber().floatValue();
        } else {
            sum = a.getNumber().doubleValue() % b.getNumber().doubleValue();
        }
        return toConstantOfType(sum, a.getType());
    }

    private static Constant constantLess(Constant a, Constant b) {
        boolean bool = a.getNumber().doubleValue() < b.getNumber()
                .doubleValue();
        return new Constant(bool);
    }

    private static Constant constantLessE(Constant a, Constant b) {
        boolean bool = a.getNumber().doubleValue() <= b.getNumber()
                .doubleValue();
        return new Constant(bool);
    }

    private static Constant constantGreater(Constant a, Constant b) {
        boolean bool = a.getNumber().doubleValue() > b.getNumber()
                .doubleValue();
        return new Constant(bool);
    }

    private static Constant constantGreaterE(Constant a, Constant b) {
        boolean bool = a.getNumber().doubleValue() >= b.getNumber()
                .doubleValue();
        return new Constant(bool);
    }

    private static Constant constantEqual(Constant a, Constant b) {
        boolean bool = a.getNumber().doubleValue() == b.getNumber()
                .doubleValue();
        return new Constant(bool);
    }

    private static Constant constantNotEqual(Constant a, Constant b) {
        boolean bool = a.getNumber().doubleValue() != b.getNumber()
                .doubleValue();
        return new Constant(bool);
    }

    /**
     * This replaces compare operators inside of IFXX operators
     * 
     * General idea is that the symbolic execution generates code like this
     * (>= (cmp a b) 0)
     * which is tranformed to
     * (>= a b)
     *
     * that makes the code more readable and more easy for backends to solve
     * @param e
     * @return
     */
    private static OperationExpression replaceCmp(OperationExpression e) {
        //Is first parameter a CMP
        Expression cmpe = e.getParameters().get(0);
        if (!(cmpe instanceof OperationExpression)) {
            return e;
        }
        OperationExpression cmp = (OperationExpression) cmpe;
        if (cmp.getOperator() != Operator.COMPARE) {
            return e;
        }
        //Is second parameter a 0 constant
        Expression zeroe = e.getParameters().get(1);
        if (!(zeroe instanceof Constant)) {
            return e;
        }
        if (((Constant) zeroe).getNumber().intValue() != 0) {
            return e;
        }
        return new OperationExpression(e.getOperator(), cmp.getParameters());
    }

    /**
     * Helper function to apply a function that evaluates a constant
     * OperationExpression
     * 
     * @param f
     *            Function to be applied to the parameters of the
     *            OperationExpression
     * @param opExpr
     *            OperationExpression to be evaluated
     * @return the evaluation result
     */
    private static Expression binaryFunction(
            BiFunction<Constant, Constant, Constant> f,
            OperationExpression opExpr) {
        Expression e1 = opExpr.getParameters().get(0);
        Expression e2 = opExpr.getParameters().get(1);
        if (!(e1 instanceof Constant && e2 instanceof Constant)) {
            return opExpr;
        }
        return f.apply((Constant) e1, (Constant) e2);
    }

    private static Expression negateOperator(OperationExpression op) {
        List<Expression> parameters = op.getParameters();
        switch (op.getOperator()) {
        case EQUAL:
            return new OperationExpression(Operator.NOT_EQUAL,
                    parameters.get(0), parameters.get(1));
        case LESS:
            return new OperationExpression(Operator.GREATER_OR_EQUAL,
                    parameters.get(0), parameters.get(1));
        case LESS_OR_EQUAL:
            return new OperationExpression(Operator.GREATER, parameters.get(0),
                    parameters.get(1));
        case GREATER:
            return new OperationExpression(Operator.LESS_OR_EQUAL,
                    parameters.get(0), parameters.get(1));
        case GREATER_OR_EQUAL:
            return new OperationExpression(Operator.LESS, parameters.get(0),
                    parameters.get(1));
        case NOT_EQUAL:
            return new OperationExpression(Operator.EQUAL, parameters.get(0),
                    parameters.get(1));
        case NOT:
            return parameters.get(0);
        default:
            return new OperationExpression(Operator.NOT, op);
        }
    }

    /**
     * Maps Operators to evaluation functions
     *
     * To be used in an expression transformer
     * 
     * @param e
     * @return
     */
    public static Expression eval(Expression e) {
        if (!(e instanceof OperationExpression)) {
            return e;
        }
        OperationExpression op = (OperationExpression) e;
        // TODO: implement more operators.
        switch (op.getOperator()) {
        case BAND: {
            Expression e1 = op.getParameters().get(0);
            Expression e2 = op.getParameters().get(1);
            if (e1 instanceof Constant) {
                // 0 and e2 == 0
                if (((Constant) e1).getNumber().intValue() == 0) {
                    return e1;
                }
                // 1 and e2 == e2
                if (((Constant) e1).getNumber().intValue() == 1) {
                    return e2;
                }
            }
            if (e2 instanceof Constant) {
                // e1 and 0 == 0
                if (((Constant) e2).getNumber().intValue() == 0) {
                    return e2;
                }
                // e1 and 1 == e1
                if (((Constant) e2).getNumber().intValue() == 1) {
                    return e1;
                }
            }
        }
            break;
        case BOR: {
            Expression e1 = op.getParameters().get(0);
            Expression e2 = op.getParameters().get(1);
            if (e1 instanceof Constant) {
                // 0 or e2 == e2
                if (((Constant) e1).getNumber().intValue() == 0) {
                    return e2;
                }
                // 1 or e2 == 1
                if (((Constant) e1).getNumber().intValue() == 1) {
                    return e1;
                }
            }
            if (e2 instanceof Constant) {
                // e1 or 0 == e1
                if (((Constant) e2).getNumber().intValue() == 0) {
                    return e1;
                }
                // e1 or 1 == 1
                if (((Constant) e2).getNumber().intValue() == 1) {
                    return e2;
                }
            }
        }

            break;
        case ADD: {
            Expression first = op.getParameters().get(0);
            Expression second = op.getParameters().get(1);
            // ADD is not needed if one of the parameters is 0
            if (first instanceof Constant) {
                if (((Constant) first).getNumber().doubleValue() == 0) {
                    return second;
                }
            }
            if (second instanceof Constant) {
                if (((Constant) second).getNumber().doubleValue() == 0) {
                    return first;
                }
            }
            return binaryFunction(ConstantExpressionEvaluator::constantAdd, op);
        }
        case AND:
            break;
        case COMPARE:
            break;
        case DIV:
            return binaryFunction(ConstantExpressionEvaluator::constantDiv, op);
        case EQUAL:
            op = replaceCmp(op);
            Expression lhs = op.getParameters().get(0);
            Expression rhs = op.getParameters().get(1);
            if (lhs instanceof Constant && rhs instanceof Constant) {
                return binaryFunction(
                        ConstantExpressionEvaluator::constantEqual, op);
            }
            if (lhs instanceof OperationExpression) {
                OperationExpression lhsop = (OperationExpression) lhs;
                Expression lhs_p1 = lhsop.getParameters().get(0);
                if (lhsop.getOperator() == Operator.TO_INT
                        && lhs_p1.getType() == boolean.class) {
                    if (rhs.equals(new Constant(0))) {
                        return new OperationExpression(Operator.NOT, lhs_p1);
                    }
                    if (rhs.equals(new Constant(1))) {
                        return lhs_p1;
                    }
                }
            }
            break;
        case GREATER:
            op = replaceCmp(op);
            return binaryFunction(ConstantExpressionEvaluator::constantGreater,
                    op);
        case GREATER_OR_EQUAL:
            op = replaceCmp(op);
            return binaryFunction(ConstantExpressionEvaluator::constantGreaterE,
                    op);
        case ITE: {
            Expression condition = op.getParameters().get(0);
            Expression caseTrue = op.getParameters().get(1);
            Expression caseFalse = op.getParameters().get(2);
            // Both cases are constant and value is equal
            // TODO: what if they are more complex expression, but equal?
            if (caseTrue instanceof Constant && caseFalse instanceof Constant) {
                Constant c1 = (Constant) caseTrue;
                Constant c2 = (Constant) caseFalse;
                if (c1.getNumber().doubleValue() == c2.getNumber()
                        .doubleValue()) {
                    return caseTrue;
                }

                if (c1.getType() == boolean.class
                        && c2.getType() == boolean.class
                        && (boolean) c1.getValue()
                        && !(boolean) c2.getValue()) {
                    return condition;
                }
            }

            if (condition instanceof OperationExpression) {
                OperationExpression o0 = (OperationExpression) condition;
                //Remove NEQ by swapping caseFalse and caseTrue
                if (o0.getOperator() == Operator.NOT_EQUAL) {
                    OperationExpression eq = new OperationExpression(
                            Operator.EQUAL, o0.getParameters().get(0),
                            o0.getParameters().get(1));
                    return eval(new OperationExpression(Operator.ITE,
                            op.getType(), eq, caseFalse, caseTrue));
                }

                //If the condition contains an equality check of a variable, we can replace the variable with the expression it equals in caseTrue
                if (o0.getOperator() == Operator.EQUAL) {
                    Expression op0 = o0.getParameters().get(0);
                    Expression op1 = o0.getParameters().get(1);
                    Variable v = null;
                    Expression value = null;
                    if (op0 instanceof Variable) {
                        v = (Variable) op0;
                        value = op1;
                    } else if (op1 instanceof Variable) {
                        v = (Variable) op1;
                        value = op0;
                    }
                    if (v != null) {
                        Map<String, Expression> replace = new HashMap<>();
                        replace.put(v.toString(), value);
                        VariableReplacer vr = new VariableReplacer(replace);
                        ExpressionTransformer et = new ExpressionTransformer(
                                vr);
                        return new OperationExpression(Operator.ITE, condition,
                                et.transform(caseTrue), caseFalse);
                    }
                }
            }

            //If the condition is a constant
            if (!(condition instanceof Constant)) {
                return op;
            }
            boolean bool = (Boolean) ((Constant) condition).getValue();
            if (bool) {
                return caseTrue;
            } else {
                return caseFalse;
            }
        }
        case LESS:
            op = replaceCmp(op);
            return binaryFunction(ConstantExpressionEvaluator::constantLess,
                    op);
        case LESS_OR_EQUAL:
            op = replaceCmp(op);
            return binaryFunction(ConstantExpressionEvaluator::constantLessE,
                    op);
        case MUL:
            return binaryFunction(ConstantExpressionEvaluator::constantMul, op);
        case NEG:
            break;
        case NOT: {
            Expression e1 = op.getParameters().get(0);
            //if Parameter is a constant
            if (e1 instanceof Constant) {
                Constant c1 = (Constant) e1;
                return new Constant(!(boolean) c1.getValue());
            }
            //if Parameter is another not
            if (e1 instanceof OperationExpression) {
                OperationExpression o1 = (OperationExpression) e1;
                return negateOperator(o1);
            }
        }
            break;
        case NOT_EQUAL:
            op = replaceCmp(op);
            return binaryFunction(ConstantExpressionEvaluator::constantNotEqual,
                    op);
        case OR:
            break;
        case REM:
            return binaryFunction(ConstantExpressionEvaluator::constantRem, op);
        case SHL:
            break;
        case SHR:
            break;
        case SUB:
            return binaryFunction(ConstantExpressionEvaluator::constantSub, op);
        case TO_BYTE: {
            Expression e1 = op.getParameters().get(0);
            //if Parameter is a constant
            if (e1 instanceof Constant) {
                Constant c1 = (Constant) e1;
                return new Constant(c1.getNumber().byteValue());
            } else if (e1.getType() == byte.class) {
                return e1;
            }
            break;
        }
        case TO_CHAR:
            break;
        case TO_DOUBLE: {
            Expression e1 = op.getParameters().get(0);
            //if Parameter is a constant
            if (e1 instanceof Constant) {
                Constant c1 = (Constant) e1;
                return new Constant(c1.getNumber().doubleValue());
            } else if (e1.getType() == double.class) {
                return e1;
            }
            break;
        }
        case TO_FLOAT: {
            Expression e1 = op.getParameters().get(0);
            //if Parameter is a constant
            if (e1 instanceof Constant) {
                Constant c1 = (Constant) e1;
                return new Constant(c1.getNumber().floatValue());
            } else if (e1.getType() == float.class) {
                return e1;
            }
            break;
        }
        case TO_INT: {
            Expression e1 = op.getParameters().get(0);
            //if Parameter is a constant
            if (e1 instanceof Constant) {
                Constant c1 = (Constant) e1;
                return new Constant(c1.getNumber().intValue());
            } else if (e1.getType() == int.class) {
                return e1;
            }
            break;
        }
        case TO_LONG: {
            Expression e1 = op.getParameters().get(0);
            //if Parameter is a constant
            if (e1 instanceof Constant) {
                Constant c1 = (Constant) e1;
                return new Constant(c1.getNumber().longValue());
            } else if (e1.getType() == long.class) {
                return e1;
            }
            break;
        }
        case TO_SHORT: {
            Expression e1 = op.getParameters().get(0);
            //if Parameter is a constant
            if (e1 instanceof Constant) {
                Constant c1 = (Constant) e1;
                return new Constant(c1.getNumber().shortValue());
            } else if (e1.getType() == short.class) {
                return e1;
            }
            break;
        }
        case TO_BOOL: {
            Expression e1 = op.getParameters().get(0);
            return new OperationExpression(Operator.NOT_EQUAL, e1,
                    Typeconversions.getDefaultValue(e1.getType()));
        }
        case XOR:
            break;
        case PUT: {
            //Heap needs to be a constant (i.e. constant sized)
            Expression heap = op.getParameters().get(0);
            if (!(heap instanceof Constant)) {
                break;
            }
            //Address needs to be constant
            Expression address = op.getParameters().get(1);
            if (!(address instanceof Constant)) {
                break;
            }
            int addrValue = (Integer) ((Constant) address).getValue();
            //List is copied since we modify it
            ArrayList<Expression> eHeap = new ArrayList<>(
                    Constant.getHeap((Constant) heap));
            //Fill with uninitialized values, if out of bounds write
            while (eHeap.size() <= addrValue) {
                eHeap.add(new Constant(Undef.class, 0xdeadbeef));
            }
            eHeap.set(addrValue, op.getParameters().get(2));
            return new Constant(Heap.class, eHeap);
        }
        case GET: {
            //Heap needs to be a constant (i.e. constant sized)
            Expression heap = op.getParameters().get(0);
            if (!(heap instanceof Constant)) {
                break;
            }
            //Address needs to be constant
            Expression address = op.getParameters().get(1);
            if (!(address instanceof Constant)) {
                break;
            }
            List<Expression> heapAsList = Constant.getHeap((Constant) heap);
            int addrValue = (Integer) ((Constant) address).getValue();
            if (addrValue < 0 || addrValue >= heapAsList.size()) {
                return Typeconversions.getDefaultInvalidValue(op.getType());
            }
            return heapAsList.get(addrValue);
        }
        default:
            break;
        }
        return e;
    }

    /**
     * Creates a new Expression where constant operations are evaluated as far
     * as possible
     * 
     * @param f
     *            input expression
     * @return new expression
     */
    public static Expression evalExpression(Expression f) {
        ExpressionTransformer t = new ExpressionTransformer(
                ConstantExpressionEvaluator::eval);
        return t.transform(f);
    }

}
