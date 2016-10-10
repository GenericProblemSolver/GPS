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
import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Operator;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Maps used by the Z3BitVectorJavaApiBackend.
 *
 * @author jowlo
 */
public class Z3BitVectorJavaApiBackendMaps {

    interface IBVOperatorMapping {
        void map(Context ctx, Stack<Expr> paramStack, int bvSize);

        /**
         * Convenience function to wrap a Operation into a masking AND
         * operation, effectively masking all higher bits.
         *
         * Useful to model overflow behaviour, e.g. 'cut off' higher bits.
         *
         * @param ctx the Context to create expressions
         * @param e the expression to wrap
         * @param bvSize the length of the resulting bitvector. All higher
         *               bits are masked
         * @return a BitVecExpr containing the masked version of Expr e
         */
        static BitVecExpr andWrap(Context ctx, BitVecExpr e, int bvSize) {
            BitVecExpr and;
            switch (bvSize) {
            case 8:
                and = ctx.mkBV(0xFF, 8);
                break;
            case 16:
                and = ctx.mkBV(0xFFFF, 16);
                break;
            case 32:
                and = ctx.mkBV(0xFFFFFFFF, 32);
                break;
            case 64:
                and = ctx.mkBV(0xFFFFFFFFFFFFFFFFL, 64);
                break;
            default:
                and = ctx.mkBV(0xFFFFFFFF, e.getSortSize());
            }
            return ctx.mkBVAND(e, and);
        }

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

    /**
     * Mapping of java datatypes to their bitvector length.
     */
    static Map<Class<?>, Integer> sizeMap = new HashMap<>();

    static {
        sizeMap.put(byte.class, 8);
        sizeMap.put(short.class, 16);
        sizeMap.put(int.class, 32);
        sizeMap.put(char.class, 32);
        sizeMap.put(long.class, 64);
        sizeMap.put(float.class, 32);
        sizeMap.put(double.class, 64);
    }

    /**
     * Mapping of GPS-internal operators to their Z3-Bitvector equivalents.
     */
    static Map<Operator, IBVOperatorMapping> operatorMap = new HashMap<>();

    static {
        operatorMap.put(Operator.ADD, (ctx, paramStack, size) -> {
            BitVecExpr[] params = IBVOperatorMapping.collect(paramStack,
                    new BitVecExpr[2]);
            paramStack.push(IBVOperatorMapping.andWrap(ctx,
                    ctx.mkBVAdd(params[0], params[1]), size));
        });
        operatorMap.put(Operator.SUB, (ctx, paramStack, size) -> {
            BitVecExpr[] params = IBVOperatorMapping.collect(paramStack,
                    new BitVecExpr[2]);
            paramStack.push(ctx.mkBVSub(params[0], params[1]));
        });
        operatorMap.put(Operator.DIV, (ctx, paramStack, size) -> {
            BitVecExpr[] params = IBVOperatorMapping.collect(paramStack,
                    new BitVecExpr[2]);
            paramStack.push(ctx.mkBVSDiv(params[0], params[1]));
        });
        operatorMap.put(Operator.REM, (ctx, paramStack, size) -> {
            BitVecExpr[] params = IBVOperatorMapping.collect(paramStack,
                    new BitVecExpr[2]);
            paramStack.push(ctx.mkBVSRem(params[0], params[1]));
        });
        operatorMap.put(Operator.MUL, (ctx, paramStack, size) -> {
            BitVecExpr[] params = IBVOperatorMapping.collect(paramStack,
                    new BitVecExpr[2]);
            paramStack.push(IBVOperatorMapping.andWrap(ctx,
                    ctx.mkBVMul(params[0], params[1]), size));
        });
        operatorMap.put(Operator.AND, (ctx, paramStack, size) -> {
            BitVecExpr[] params = IBVOperatorMapping.collect(paramStack,
                    new BitVecExpr[2]);
            paramStack.push(ctx.mkBVAND(params[0], params[1]));
        });
        operatorMap.put(Operator.OR, (ctx, paramStack, size) -> {
            BitVecExpr[] params = IBVOperatorMapping.collect(paramStack,
                    new BitVecExpr[2]);
            paramStack.push(ctx.mkBVOR(params[0], params[1]));
        });
        operatorMap.put(Operator.XOR, (ctx, paramStack, size) -> {
            BitVecExpr[] params = IBVOperatorMapping.collect(paramStack,
                    new BitVecExpr[2]);
            paramStack.push(ctx.mkBVXOR(params[0], params[1]));
        });
        operatorMap.put(Operator.BAND, (ctx, paramStack, size) -> {
            paramStack.push(ctx.mkAnd(
                    IBVOperatorMapping.collect(paramStack, new BoolExpr[2])));
        });
        operatorMap.put(Operator.BOR, (ctx, paramStack, size) -> {
            paramStack.push(ctx.mkOr(
                    IBVOperatorMapping.collect(paramStack, new BoolExpr[2])));
        });
        operatorMap.put(Operator.NOT, (ctx, paramStack, size) -> {
            paramStack.push(ctx.mkNot((BoolExpr) paramStack.pop()));
        });
        operatorMap.put(Operator.EQUAL, (ctx, paramStack, size) -> {
            BitVecExpr[] params = IBVOperatorMapping.collect(paramStack,
                    new BitVecExpr[2]);
            paramStack.push(ctx.mkEq(ctx.mkBVSLE(params[0], params[1]),
                    ctx.mkBVSGE(params[0], params[1])));
        });
        operatorMap.put(Operator.GREATER, (ctx, paramStack, size) -> {
            BitVecExpr[] params = IBVOperatorMapping.collect(paramStack,
                    new BitVecExpr[2]);
            paramStack.push(ctx.mkBVSGT(
                    IBVOperatorMapping.andWrap(ctx, params[0], size),
                    IBVOperatorMapping.andWrap(ctx, params[1], size)));
        });
        operatorMap.put(Operator.GREATER_OR_EQUAL, (ctx, paramStack, size) -> {
            BitVecExpr[] params = IBVOperatorMapping.collect(paramStack,
                    new BitVecExpr[2]);
            paramStack.push(ctx.mkBVSGE(params[0], params[1]));
        });
        operatorMap.put(Operator.LESS, (ctx, paramStack, size) -> {
            BitVecExpr[] params = IBVOperatorMapping.collect(paramStack,
                    new BitVecExpr[2]);
            paramStack.push(ctx.mkBVSLT(params[0], params[1]));
        });
        operatorMap.put(Operator.LESS_OR_EQUAL, (ctx, paramStack, size) -> {
            BitVecExpr[] params = IBVOperatorMapping.collect(paramStack,
                    new BitVecExpr[2]);
            paramStack.push(ctx.mkBVSLE(params[0], params[1]));
        });
        operatorMap.put(Operator.ITE, (ctx, paramStack, size) -> {
            Expr[] params = IBVOperatorMapping.collect(paramStack, new Expr[3]);
            paramStack.push(
                    ctx.mkITE((BoolExpr) params[0], params[1], params[2]));
        });
        operatorMap.put(Operator.TO_INT, (ctx, paramStack, size) -> {
            if (paramStack.peek().isReal()) {
                paramStack.push(ctx.mkReal2Int((RealExpr) paramStack.pop()));
            } else if (paramStack.peek().isInt()) {
                // Do nothing
            } else {
                paramStack.push(ctx.mkBVAND((BitVecExpr) paramStack.pop(),
                        ctx.mkBV(0x00000000FFFFFFFFL, 64)));
            }
        });
        operatorMap.put(Operator.TO_DOUBLE, (ctx, paramStack, size) -> {
            if (paramStack.peek().isInt()) {
                paramStack.push(ctx.mkInt2Real((IntExpr) paramStack.pop()));
            } else {
                throw new Operator.InvalidOperatorException(
                        "TO_DOUBLE on non int Variable");
            }
        });
        operatorMap.put(Operator.TO_FLOAT, (ctx, paramStack, size) -> {
            if (paramStack.peek().isInt()) {
                paramStack.push(ctx.mkInt2Real((IntExpr) paramStack.pop()));
            } else {
                throw new Operator.InvalidOperatorException(
                        "TO_FLOAT on non int Variable");
            }
        });
        operatorMap.put(Operator.NEG, (ctx, paramStack, size) -> {
            if (paramStack.peek().isBV()) {
                paramStack.push(ctx.mkBVNeg((BitVecExpr) paramStack.pop()));
            } else {
                throw new Operator.InvalidOperatorException(
                        "Operator NEG not implemented for non BV Expr");
            }
        });
        operatorMap.put(Operator.TO_LONG, (ctx, paramStack, size) -> {
            throw new Operator.InvalidOperatorException(
                    "Operator TO_LONG not implemented");
        });
        operatorMap.put(Operator.TO_SHORT, (ctx, paramStack, size) -> {
            throw new Operator.InvalidOperatorException(
                    "Operator TO_SHORT not implemented");
        });
        operatorMap.put(Operator.TO_CHAR, (ctx, paramStack, size) -> {
            throw new Operator.InvalidOperatorException(
                    "Operator TO_CHAR not implemented");
        });

        // TODO: Implement Bitvector operations
        // case BAND:
        // return ctx.mkBVAND();
        // case BOR:
        // return ctx.mkBVOR();

    }

    /**
     * Mapping to retrieve results from a Model created by Z3 into the
     * GPS-internal structure.
     */
    static Map<Class<? extends Expr>, APIMapsUtil.IResultMapping> resultMap = new HashMap<>();

    static {
        resultMap.put(BitVecNum.class, (e) -> {
            int bitlength = ((BitVecNum) e).getSortSize();
            Constant c = null;
            switch (bitlength) {
            case 8:
                c = new Constant((byte) ((BitVecNum) e).getInt());
                break;
            case 16:
                c = new Constant((short) ((BitVecNum) e).getInt());
                break;
            case 32:
                long l = ((BitVecNum) e).getLong();
                c = new Constant((int) l);
                break;
            case 64:
                c = new Constant(((BitVecNum) e).getLong());
                break;
            default:
                throw new RuntimeException(
                        "No result extraction for bitvector of length "
                                + bitlength);
            }
            return c;
        });
        resultMap.put(BoolExpr.class,
                (e) -> new Constant(((BoolExpr) e).isTrue()));
        /*
        resultMap.put(RatNum.class, (e) -> new Constant(
                ((RatNum) e).getNumerator().getInt() / ((RatNum) e)
                        .getDenominator().getInt()));
         */

        // TODO: Add more return types
    }

    /**
     * Mapping from GPS-internal datatypes to Z3 datatypes.
     */
    static Map<Class<?>, APIMapsUtil.IVariableMapping> variableTypeMap = new HashMap<>();

    static {
        variableTypeMap.put(byte.class,
                (ctx, v) -> ctx.mkBVConst(v.toString(), 8));
        variableTypeMap.put(short.class,
                (ctx, v) -> ctx.mkBVConst(v.toString(), 16));
        variableTypeMap.put(int.class,
                (ctx, v) -> ctx.mkBVConst(v.toString(), 32));
        variableTypeMap.put(char.class,
                (ctx, v) -> ctx.mkBVConst(v.toString(), 32));
        variableTypeMap.put(long.class,
                (ctx, v) -> ctx.mkBVConst(v.toString(), 64));

        variableTypeMap.put(float.class,
                (ctx, v) -> ctx.mkBVConst(v.toString(), 32));
        variableTypeMap.put(double.class,
                (ctx, v) -> ctx.mkBVConst(v.toString(), 64));

        variableTypeMap.put(boolean.class,
                (ctx, v) -> ctx.mkBoolConst(v.toString()));

        variableTypeMap.put(Operator.Undef.class,
                (ctx, v) -> ctx.mkBVConst(v.toString(), 64));
    }

    /**
     * Mapping of GPS-internal constants to Z3 Constants.
     */
    static Map<Class<?>, APIMapsUtil.IConstantMapping> constantTypeMap = new HashMap<>();

    static {
        Class<?>[] toInt = { int.class, byte.class, short.class,
                Operator.Reference.class, char.class };
        for (Class<?> cl : toInt) {
            constantTypeMap.put(cl, (ctx, c) -> ctx.mkInt((int) c.getValue()));
        }

        constantTypeMap.put(byte.class,
                (ctx, c) -> ctx.mkBV((byte) c.getValue(), 8));
        constantTypeMap.put(short.class,
                (ctx, c) -> ctx.mkBV((short) c.getValue(), 16));
        constantTypeMap.put(int.class,
                (ctx, c) -> ctx.mkBV((int) c.getValue(), 32));
        constantTypeMap.put(char.class,
                (ctx, c) -> ctx.mkBV((int) c.getValue(), 32));
        constantTypeMap.put(long.class,
                (ctx, c) -> ctx.mkBV(((Long) c.getValue()).intValue(), 64));

        constantTypeMap.put(Operator.Reference.class,
                (ctx, c) -> ctx.mkBV(((Integer) c.getValue()), 32));

        // TODO: Get rid of String usage. For this we would need to get
        //       nominator und denominator from the double.
        constantTypeMap.put(float.class,
                (ctx, c) -> ctx.mkBV(c.toString(), 32));
        constantTypeMap.put(double.class,
                (ctx, c) -> ctx.mkBV(c.toString(), 64));

        constantTypeMap.put(boolean.class,
                (ctx, c) -> ctx.mkBool((boolean) c.getValue()));

        // TODO: Get rid of Undef.class
        constantTypeMap.put(Operator.Undef.class,
                (ctx, c) -> ctx.mkBV(c.toString(), 64));
    }

    public Map<Operator, IBVOperatorMapping> getOperatorMap() {
        return operatorMap;
    }

    public Map<Class<? extends Expr>, APIMapsUtil.IResultMapping> getResultMap() {
        return resultMap;
    }

    public Map<Class<?>, APIMapsUtil.IVariableMapping> getVariableTypeMap() {
        return variableTypeMap;
    }

    public Map<Class<?>, APIMapsUtil.IConstantMapping> getConstantTypeMap() {
        return constantTypeMap;
    }
}
