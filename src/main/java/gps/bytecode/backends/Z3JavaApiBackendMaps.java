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

import com.microsoft.z3.*;
import com.microsoft.z3.Context;

import gps.bytecode.exceptions.IncoherentBytecodeException;
import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Operator;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by jowlo on 4/23/16.
 */
public class Z3JavaApiBackendMaps {

    interface IOperatorMapping {
        void map(Context ctx, Stack<Expr> paramStack);

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

    static Map<Operator, IOperatorMapping> operatorMap = new HashMap<>();

    static {
        operatorMap.put(Operator.ADD, (ctx, paramStack) -> {
            paramStack.push(ctx.mkAdd(
                    IOperatorMapping.collect(paramStack, new ArithExpr[2])));
        });
        operatorMap.put(Operator.SUB, (ctx, paramStack) -> {
            paramStack.push(ctx.mkSub(
                    IOperatorMapping.collect(paramStack, new ArithExpr[2])));
        });
        operatorMap.put(Operator.DIV, (ctx, paramStack) -> {
            ArithExpr[] params = IOperatorMapping.collect(paramStack,
                    new ArithExpr[2]);
            paramStack.push(ctx.mkDiv(params[0], params[1]));
        });
        operatorMap.put(Operator.REM, (ctx, paramStack) -> {
            IntExpr[] params = IOperatorMapping.collect(paramStack,
                    new IntExpr[2]);
            paramStack.push(ctx.mkRem(params[0], params[1]));
        });
        operatorMap.put(Operator.MUL, (ctx, paramStack) -> {
            paramStack.push(ctx.mkMul(
                    IOperatorMapping.collect(paramStack, new ArithExpr[2])));
        });
        operatorMap.put(Operator.BAND, (ctx, paramStack) -> {
            paramStack.push(ctx.mkAnd(
                    IOperatorMapping.collect(paramStack, new BoolExpr[2])));
        });
        operatorMap.put(Operator.BOR, (ctx, paramStack) -> {
            paramStack.push(ctx.mkOr(
                    IOperatorMapping.collect(paramStack, new BoolExpr[2])));
        });
        operatorMap.put(Operator.NOT, (ctx, paramStack) -> {
            paramStack.push(ctx.mkNot((BoolExpr) paramStack.pop()));
        });
        operatorMap.put(Operator.EQUAL, (ctx, paramStack) -> {
            Expr[] params = IOperatorMapping.collect(paramStack, new Expr[2]);
            paramStack.push(ctx.mkEq(params[0], params[1]));
        });
        operatorMap.put(Operator.GREATER, (ctx, paramStack) -> {
            ArithExpr[] params = IOperatorMapping.collect(paramStack,
                    new ArithExpr[2]);
            paramStack.push(ctx.mkGt(params[0], params[1]));
        });
        operatorMap.put(Operator.GREATER_OR_EQUAL, (ctx, paramStack) -> {
            ArithExpr[] params = IOperatorMapping.collect(paramStack,
                    new ArithExpr[2]);
            paramStack.push(ctx.mkGe(params[0], params[1]));
        });
        operatorMap.put(Operator.LESS, (ctx, paramStack) -> {
            ArithExpr[] params = IOperatorMapping.collect(paramStack,
                    new ArithExpr[2]);
            paramStack.push(ctx.mkLt(params[0], params[1]));
        });
        operatorMap.put(Operator.LESS_OR_EQUAL, (ctx, paramStack) -> {
            ArithExpr[] params = IOperatorMapping.collect(paramStack,
                    new ArithExpr[2]);
            paramStack.push(ctx.mkLe(params[0], params[1]));
        });
        operatorMap.put(Operator.ITE, (ctx, paramStack) -> {
            Expr[] params = IOperatorMapping.collect(paramStack, new Expr[3]);
            paramStack.push(
                    ctx.mkITE((BoolExpr) params[0], params[1], params[2]));
        });
        operatorMap.put(Operator.TO_INT, (ctx, paramStack) -> {
            if (paramStack.peek().isReal()) {
                paramStack.push(ctx.mkReal2Int((RealExpr) paramStack.pop()));
            } else if (paramStack.peek().isInt()) {
                // Do nothing
            } else {
                throw new IncoherentBytecodeException(
                        "TO_INT on non real Variable");
            }
        });
        operatorMap.put(Operator.TO_DOUBLE, (ctx, paramStack) -> {
            if (paramStack.peek().isInt()) {
                paramStack.push(ctx.mkInt2Real((IntExpr) paramStack.pop()));
            } else {
                throw new IncoherentBytecodeException(
                        "TO_DOUBLE on non int Variable");
            }
        });
        operatorMap.put(Operator.TO_FLOAT, (ctx, paramStack) -> {
            if (paramStack.peek().isInt()) {
                paramStack.push(ctx.mkInt2Real((IntExpr) paramStack.pop()));
            } else {
                throw new IncoherentBytecodeException(
                        "TO_FLOAT on non int Variable");
            }
        });
        operatorMap.put(Operator.NEG, (ctx, paramStack) -> {
            if (paramStack.peek().isInt()) {
                paramStack.push(
                        ctx.mkMul(ctx.mkInt(-1), (IntExpr) paramStack.pop()));
            } else if (paramStack.peek().isBV()) {
                paramStack.push(ctx.mkBVNeg((BitVecExpr) paramStack.pop()));
            }
        });
        operatorMap.put(Operator.TO_LONG, (ctx, paramStack) -> {
        });
        operatorMap.put(Operator.TO_SHORT, (ctx, paramStack) -> {
        });
        operatorMap.put(Operator.TO_CHAR, (ctx, paramStack) -> {
        });

        // TODO: Implement Bitvector operations
        // case BAND:
        // return ctx.mkBVAND();
        // case BOR:
        // return ctx.mkBVOR();

    }

    static Map<Class<? extends Expr>, APIMapsUtil.IResultMapping> resultMap = new HashMap<>();

    static {
        resultMap.put(IntNum.class, (e) -> new Constant(((IntNum) e).getInt()));
        resultMap.put(BoolExpr.class,
                (e) -> new Constant(((BoolExpr) e).isTrue()));
        resultMap.put(RatNum.class,
                (e) -> new Constant(((RatNum) e).getNumerator().getInt()
                        / ((RatNum) e).getDenominator().getInt()));
        // TODO: Add more return types
    }

    static Map<Class<?>, APIMapsUtil.IVariableMapping> variableTypeMap = new HashMap<>();

    static {
        Class<?>[] toInt = { int.class, byte.class, short.class, long.class,
                Operator.Reference.class, char.class };
        for (Class<?> cl : toInt) {
            variableTypeMap.put(cl, (ctx, v) -> ctx.mkIntConst(v.toString()));
        }

        variableTypeMap.put(double.class,
                (ctx, v) -> ctx.mkRealConst(v.toString()));
        variableTypeMap.put(float.class,
                (ctx, v) -> ctx.mkRealConst(v.toString()));

        variableTypeMap.put(boolean.class,
                (ctx, v) -> ctx.mkBoolConst(v.toString()));
    }

    static Map<Class<?>, APIMapsUtil.IConstantMapping> constantTypeMap = new HashMap<>();

    static {
        constantTypeMap.put(byte.class,
                (ctx, c) -> ctx.mkInt((byte) c.getValue()));
        constantTypeMap.put(char.class,
                (ctx, c) -> ctx.mkInt((char) c.getValue()));
        constantTypeMap.put(short.class,
                (ctx, c) -> ctx.mkInt((short) c.getValue()));
        constantTypeMap.put(int.class,
                (ctx, c) -> ctx.mkInt((int) c.getValue()));
        constantTypeMap.put(Operator.Reference.class,
                (ctx, c) -> ctx.mkInt((int) c.getValue()));
        constantTypeMap.put(long.class,
                (ctx, c) -> ctx.mkInt(((Long) c.getValue()).intValue()));

        // TODO: Get rid of String usage. For this we would need to get
        //       nominator und denominator from the double.
        constantTypeMap.put(double.class, (ctx, c) -> ctx.mkReal(c.toString()));
        constantTypeMap.put(float.class, (ctx, c) -> ctx.mkReal(c.toString()));

        constantTypeMap.put(boolean.class,
                (ctx, c) -> ctx.mkBool((boolean) c.getValue()));

        // TODO: Get rid of Undef.class
        constantTypeMap.put(Operator.Undef.class,
                (ctx, c) -> ctx.mkReal(c.toString()));
    }

    public Map<Operator, IOperatorMapping> getOperatorMap() {
        return this.operatorMap;
    }

    public Map<Class<? extends Expr>, APIMapsUtil.IResultMapping> getResultMap() {
        return this.resultMap;
    }

    public Map<Class<?>, APIMapsUtil.IVariableMapping> getVariableTypeMap() {
        return this.variableTypeMap;
    }

    public Map<Class<?>, APIMapsUtil.IConstantMapping> getConstantTypeMap() {
        return this.constantTypeMap;
    }
}
