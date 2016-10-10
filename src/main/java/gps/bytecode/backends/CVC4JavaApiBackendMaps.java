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

import edu.nyu.acsys.CVC4.*;
import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Operator;
import gps.bytecode.expressions.Variable;

import java.lang.Integer;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by jowlo on 4/23/16.
 */
public class CVC4JavaApiBackendMaps {

    interface IOperatorMapping {
        void map(ExprManager ctx, Stack<Expr> paramStack);

        /**
         * Retrieves parameters from stack and returns them in an correctly
         * typed Array, in insertion (!) order from the stack.
         *
         * @param currentParams
         *         Stack with parameters
         * @param target
         *         correctly typed Array to insert into
         *
         * @return Array of Type <code>type</code> with retrieved parameters in
         * insertion order.
         */
        @SuppressWarnings("unchecked")
        static <T> T[] collect(Stack<Expr> currentParams, T[] target) {
            for (int i = target.length - 1; i >= 0; i--) {
                target[i] = (T) currentParams.pop();
            }
            return target;
        }
    }

    interface IResultMapping {
        public Constant map(Expr result);
    }

    static Map<Class<?>, IResultMapping> resultMap = new HashMap<>();

    static {
        resultMap.put(byte.class,
                (e) -> new Constant(Byte.parseByte(e.toString())));
        resultMap.put(short.class,
                (e) -> new Constant(Short.parseShort(e.toString())));
        resultMap.put(int.class,
                (e) -> new Constant(Integer.parseInt(e.toString())));
        resultMap.put(long.class,
                (e) -> new Constant(Long.parseLong(e.toString())));
        resultMap.put(double.class,
                (e) -> new Constant(Double.parseDouble(e.toString())));
        resultMap.put(float.class,
                (e) -> new Constant(Float.parseFloat(e.toString())));
        resultMap.put(boolean.class,
                (e) -> new Constant(e.toString().equals("TRUE")));
        resultMap.put(char.class,
                (e) -> new Constant(Integer.parseInt(e.toString())));
        // TODO: This is probably not the expected functionality.
        resultMap.put(Operator.Reference.class,
                (e) -> new Constant(Integer.parseInt(e.toString())));

        // TODO: Add more return types and get rid of String parsing
    }

    interface IVariableMapping {
        public Expr map(ExprManager ctx, Variable v);
    }

    static Map<Class<?>, IVariableMapping> variableTypeMap = new HashMap<>();

    static {
        Class<?>[] toInt = { int.class, byte.class, short.class, long.class,
                Operator.Reference.class, char.class };
        for (Class<?> cl : toInt) {
            variableTypeMap.put(cl,
                    (ctx, v) -> ctx.mkVar(v.toString(), ctx.integerType()));
        }
        variableTypeMap.put(double.class,
                (ctx, v) -> ctx.mkVar(v.toString(), ctx.realType()));
        variableTypeMap.put(float.class,
                (ctx, v) -> ctx.mkVar(v.toString(), ctx.realType()));
        variableTypeMap.put(boolean.class,
                (ctx, v) -> ctx.mkVar(v.toString(), ctx.booleanType()));
    }

    interface IConstantMapping {
        public Expr map(ExprManager ctx, Constant c);
    }

    static Map<Class<?>, IConstantMapping> constantTypeMap = new HashMap<>();

    static {
        Class<?>[] asInt = { int.class, byte.class, Operator.Reference.class };
        for (Class<?> cl : asInt) {
            constantTypeMap.put(cl,
                    (ctx, c) -> ctx.mkConst(new Rational((int) c.getValue())));
        }
        constantTypeMap.put(char.class,
                (ctx, c) -> ctx.mkConst(new Rational((char) c.getValue())));
        constantTypeMap.put(short.class,
                (ctx, c) -> ctx.mkConst(new Rational((Short) c.getValue())));
        constantTypeMap.put(long.class,
                (ctx, c) -> ctx.mkConst(new Rational((long) c.getValue())));
        constantTypeMap.put(double.class, (ctx, c) -> ctx
                .mkConst(Rational.fromDouble((double) c.getValue())));
        constantTypeMap.put(float.class, (ctx, c) -> ctx
                .mkConst(Rational.fromDouble((float) c.getValue())));
        constantTypeMap.put(boolean.class,
                (ctx, c) -> ctx.mkConst((boolean) c.getValue()));

        // TODO: Get rid of Undef.class, but support is given! :)
        // Mapping undef to int for now
        constantTypeMap.put(Operator.Undef.class,
                (ctx, c) -> ctx.mkConst(new Rational((int) c.getValue())));
        /*
        constantTypeMap.put(Operator.Undef.class,
                (ctx, c) -> ctx.mkConst(Kind.UNDEFINED_KIND));
         */
    }

    static Map<Operator, Kind> CVCoperatorMap = new HashMap<>();

    static {
        CVCoperatorMap.put(Operator.ADD, Kind.PLUS);
        CVCoperatorMap.put(Operator.SUB, Kind.MINUS);
        CVCoperatorMap.put(Operator.DIV, Kind.DIVISION);
        CVCoperatorMap.put(Operator.REM, Kind.INTS_MODULUS);
        CVCoperatorMap.put(Operator.MUL, Kind.MULT);
        CVCoperatorMap.put(Operator.BAND, Kind.BITVECTOR_AND);
        CVCoperatorMap.put(Operator.BOR, Kind.BITVECTOR_OR);
        CVCoperatorMap.put(Operator.EQUAL, Kind.EQUAL);
        CVCoperatorMap.put(Operator.GREATER, Kind.GT);
        CVCoperatorMap.put(Operator.GREATER_OR_EQUAL, Kind.GEQ);
        CVCoperatorMap.put(Operator.LESS, Kind.LT);
        CVCoperatorMap.put(Operator.LESS_OR_EQUAL, Kind.LEQ);
    }

    static Map<Operator, IOperatorMapping> operatorMap = new HashMap<>();

    static {
        for (Map.Entry<Operator, Kind> entry : CVCoperatorMap.entrySet()) {
            operatorMap.put(entry.getKey(), (ctx, paramStack) -> {
                Expr[] collected = IOperatorMapping.collect(paramStack,
                        new Expr[2]);
                paramStack.push(ctx.mkExpr(entry.getValue(), collected[0],
                        collected[1]));
            });
        }
        operatorMap.put(Operator.ITE, (ctx, paramStack) -> {
            Expr[] collected = IOperatorMapping.collect(paramStack,
                    new Expr[3]);
            paramStack.push(ctx.mkExpr(Kind.ITE, collected[0], collected[1],
                    collected[2]));

        });
        operatorMap.put(Operator.NOT, (ctx, paramStack) -> {
            Expr[] collected = IOperatorMapping.collect(paramStack,
                    new Expr[1]);
            paramStack.push(ctx.mkExpr(Kind.NOT, collected[0]));

        });
        operatorMap.put(Operator.TO_INT, (ctx, paramStack) -> {
            Expr expr = paramStack.peek();
            if (expr.getType().isReal()) {
                paramStack.push(ctx.mkExpr(Kind.TO_INTEGER, paramStack.pop()));
            } else if (expr.getType().isInteger()) {
                // Do nothing.
            } else {
                throw new Operator.InvalidOperatorException(
                        "TO_INT on non real Variable");
            }
        });
        operatorMap.put(Operator.TO_DOUBLE, (ctx, paramStack) -> {
            if (paramStack.peek().getType().isInteger()) {
                paramStack.push(ctx.mkExpr(Kind.TO_REAL, paramStack.pop()));
            } else {
                throw new Operator.InvalidOperatorException(
                        "TO_DOUBLE on non int Variable");
            }
        });
        operatorMap.put(Operator.TO_FLOAT, (ctx, paramStack) -> {
            if (paramStack.peek().getType().isInteger()) {
                paramStack.push(ctx.mkExpr(Kind.TO_REAL, paramStack.pop()));
            } else {
                throw new Operator.InvalidOperatorException(
                        "TO_FLOAT on non int Variable");
            }
        });
        operatorMap.put(Operator.NEG, (ctx, paramStack) -> {
            Type topStackType = paramStack.peek().getType();
            if (topStackType.isBoolean()) {
                paramStack.push(ctx.mkExpr(Kind.NOT, paramStack.pop()));
            } else if (topStackType.isInteger() || topStackType.isReal()) {
                paramStack.push(ctx.mkExpr(Kind.MULT, paramStack.pop(),
                        ctx.mkConst(new Rational(-1))));
            }
        });
        // TODO: Only masking bitvectors for now. This Backend uses CVC's own
        // number types directly. So we are "adding precision" here.
        // The CVC & Z3 Bitvector API Backends represent all numbers as
        // Bitvectors with same precision (Over-/Underflow) as in Java
        operatorMap.put(Operator.TO_SHORT, (ctx, paramStack) -> {
            Type topStackType = paramStack.peek().getType();
            if (topStackType.isBitVector()) {
                paramStack.push(ctx.mkExpr(Kind.BITVECTOR_AND, paramStack.pop(),
                        ctx.mkConst(new BitVector(Short.MAX_VALUE))));
            }
        });
        operatorMap.put(Operator.TO_BYTE, (ctx, paramStack) -> {
            Type topStackType = paramStack.peek().getType();
            if (topStackType.isBitVector()) {
                paramStack.push(ctx.mkExpr(Kind.BITVECTOR_AND, paramStack.pop(),
                        ctx.mkConst(new BitVector(Byte.MAX_VALUE))));
            }
        });
        operatorMap.put(Operator.TO_LONG, (ctx, paramStack) -> {
            Type topStackType = paramStack.peek().getType();
            if (topStackType.isBitVector()) {
                paramStack.push(ctx.mkExpr(Kind.BITVECTOR_AND, paramStack.pop(),
                        ctx.mkConst(new BitVector(Long.MAX_VALUE))));
            }
        });
        operatorMap.put(Operator.TO_CHAR, (ctx, paramStack) -> {
            Type topStackType = paramStack.peek().getType();
            if (topStackType.isBitVector()) {
                paramStack.push(ctx.mkExpr(Kind.BITVECTOR_AND, paramStack.pop(),
                        ctx.mkConst(new BitVector(Character.MAX_VALUE))));
            }
        });
    }
}