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
package gps.bytecode.symexec;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.tools.classfile.ConstantPool;
import com.sun.tools.classfile.ConstantPool.CONSTANT_Class_info;
import com.sun.tools.classfile.ConstantPool.CONSTANT_Double_info;
import com.sun.tools.classfile.ConstantPool.CONSTANT_Fieldref_info;
import com.sun.tools.classfile.ConstantPool.CONSTANT_Float_info;
import com.sun.tools.classfile.ConstantPool.CONSTANT_Integer_info;
import com.sun.tools.classfile.ConstantPool.CONSTANT_Long_info;
import com.sun.tools.classfile.ConstantPool.CONSTANT_NameAndType_info;
import com.sun.tools.classfile.ConstantPool.CPInfo;
import com.sun.tools.classfile.ConstantPool.CPRefInfo;
import com.sun.tools.classfile.ConstantPool.InvalidIndex;
import com.sun.tools.classfile.ConstantPool.UnexpectedEntry;
import com.sun.tools.classfile.ConstantPoolException;
import com.sun.tools.classfile.Instruction;
import com.sun.tools.classfile.Opcode;

import gps.bytecode.disassemble.ClassDisassembler;
import gps.bytecode.disassemble.DMethod;
import gps.bytecode.disassemble.MethodRef;
import gps.bytecode.disassemble.MethodRef.ConstructedMethodRef;
import gps.bytecode.exceptions.IncoherentBytecodeException;
import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Expression;
import gps.bytecode.expressions.FunctionCall;
import gps.bytecode.expressions.OperationExpression;
import gps.bytecode.expressions.Operator;
import gps.bytecode.expressions.Operator.Reference;
import gps.bytecode.expressions.Operator.RuntimeType;
import gps.bytecode.expressions.Operator.Undef;

/**
 * Maps bytecode instructions to their alterations to the stack, local
 * variables, static variables and the heap.
 * 
 * @author ihritil@tzi.de
 *
 */
public final class FunctionalMapping {

    /**
     * Functional Interface to implement the state changes of a context of a
     * single instruction
     */
    interface IContextMapping {
        void map(Context ctx, Instruction instr);
    }

    /**
     * This Map maps Bytecode instruction onto effects they have on the Context
     * of the function.
     * 
     * 
     * The JVM instruction set is described at
     * https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html
     */
    static final Map<Opcode, IContextMapping> mapping = new HashMap<>();

    static {

        // ---- int-opcodes ----

        // Because constant numbers that are within the range of byte are pushed
        // with bipush instead of iconst, this only pushes an int on the stack
        // without remembering that it's a byte
        mapping.put(Opcode.BIPUSH, (ctx, instr) -> {
            ctx.stack.push(new Constant(instr.getByte(1)));
        });

        mapping.put(Opcode.IADD, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.ADD, int.class);
        });

        // TODO: Implement bitwise operations with BitVectors in Z3
        mapping.put(Opcode.IAND, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.AND, int.class);
        });

        mapping.put(Opcode.ICONST_0, (ctx, instr) -> {
            ctx.stack.push(new Constant(0));
        });

        mapping.put(Opcode.ICONST_1, (ctx, instr) -> {
            ctx.stack.push(new Constant(1));
        });

        mapping.put(Opcode.ICONST_2, (ctx, instr) -> {
            ctx.stack.push(new Constant(2));
        });

        mapping.put(Opcode.ICONST_3, (ctx, instr) -> {
            ctx.stack.push(new Constant(3));
        });

        mapping.put(Opcode.ICONST_4, (ctx, instr) -> {
            ctx.stack.push(new Constant(4));
        });

        mapping.put(Opcode.ICONST_5, (ctx, instr) -> {
            ctx.stack.push(new Constant(5));
        });

        mapping.put(Opcode.ICONST_M1, (ctx, instr) -> {
            ctx.stack.push(new Constant(-1));
        });

        mapping.put(Opcode.IDIV, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.DIV, int.class);
        });

        mapping.put(Opcode.ILOAD, (ctx, instr) -> {
            load(ctx, instr.getUnsignedByte(1), int.class);
        });

        mapping.put(Opcode.ILOAD_W, ((ctx, instr) -> {
            load(ctx, instr.getShort(1), int.class);
        }));

        mapping.put(Opcode.ILOAD_0, (ctx, instr) -> {
            load(ctx, 0, int.class);
        });

        mapping.put(Opcode.ILOAD_1, (ctx, instr) -> {
            load(ctx, 1, int.class);
        });

        mapping.put(Opcode.ILOAD_2, (ctx, instr) -> {
            load(ctx, 2, int.class);
        });

        mapping.put(Opcode.ILOAD_3, (ctx, instr) -> {
            load(ctx, 3, int.class);
        });

        mapping.put(Opcode.IMUL, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.MUL, int.class);
        });

        // TODO: Implement bitwise operations with BitVectors in Z3
        mapping.put(Opcode.IOR, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.OR, int.class);
        });

        mapping.put(Opcode.IREM, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.REM, int.class);
        });

        mapping.put(Opcode.ISUB, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.SUB, int.class);
        });

        mapping.put(Opcode.ISHL, (ctx, instr) -> {
            ctx.stack.push(new OperationExpression(Operator.SHL,
                    reqStackParams(ctx, 1, int.class)));
        });

        mapping.put(Opcode.ISHR, (ctx, instr) -> {
            ctx.stack.push(new OperationExpression(Operator.SHR,
                    reqStackParams(ctx, 1, int.class)));
        });

        mapping.put(Opcode.ISTORE, (ctx, instr) -> {
            store(ctx, instr.getUnsignedByte(1), int.class);
        });

        mapping.put(Opcode.ISTORE_W, ((ctx, instr) -> {
            store(ctx, instr.getShort(1), int.class);
        }));

        mapping.put(Opcode.ISTORE_0, (ctx, instr) -> {
            store(ctx, 0, int.class);
        });

        mapping.put(Opcode.ISTORE_1, (ctx, instr) -> {
            store(ctx, 1, int.class);
        });

        mapping.put(Opcode.ISTORE_2, (ctx, instr) -> {
            store(ctx, 2, int.class);
        });

        mapping.put(Opcode.ISTORE_3, (ctx, instr) -> {
            store(ctx, 3, int.class);
        });

        mapping.put(Opcode.IXOR, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.XOR, int.class);
        });

        mapping.put(Opcode.INEG, (ctx, instr) -> {
            ctx.stack.push(new OperationExpression(Operator.NEG,
                    reqStackParams(ctx, 1, int.class)));
        });

        mapping.put(Opcode.IINC, (ctx, instr) -> {
            Expression[] exprs = new Expression[2];
            exprs[0] = getLocal(ctx, instr.getUnsignedByte(1), int.class);
            exprs[1] = new Constant((int) instr.getByte(2));

            ctx.locals[instr.getUnsignedByte(1)] = new OperationExpression(
                    Operator.ADD, exprs);
        });

        mapping.put(Opcode.IRETURN, (ctx, instr) -> {
            Expression returnValue = reqStackParams(ctx, 1, int.class)[0];
            // Special case if we hit the top of the call stack
            if (ctx.previousFrame == null) {
                ctx.invoker.expression = returnValue;
            } else {
                invReturn(ctx, returnValue);
            }
        });

        mapping.put(Opcode.RETURN, (ctx, instr) -> {
            if (ctx.previousFrame == null) {
                throw new IncoherentBytecodeException(
                        "RETURNADRESS/CONTEXT NOT SET");
            } else {
                invReturn(ctx, null);
            }
        });

        mapping.put(Opcode.ARETURN, (ctx, instr) -> {
            Expression returnValue = reqStackParams(ctx, 1, Reference.class)[0];
            if (ctx.previousFrame == null) {
                throw new IncoherentBytecodeException(
                        "RETURNADRESS/CONTEXT NOT SET");
            } else {
                invReturn(ctx, returnValue);
            }
        });

        // Because constant numbers that are within the range of short are
        // pushed with
        // sipush instead of iconst, this only pushes an int on the stack
        // without remembering that it's a short
        mapping.put(Opcode.SIPUSH, (ctx, instr) -> {
            ctx.stack.push(new Constant(instr.getShort(1)));
        });

        mapping.put(Opcode.DADD, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.ADD, double.class);
        });

        mapping.put(Opcode.DCMPG, (ctx, instr) -> {
            compare(ctx, double.class, 1);
        });

        mapping.put(Opcode.DCMPL, (ctx, instr) -> {
            compare(ctx, double.class, -1);
        });

        mapping.put(Opcode.DCONST_0, (ctx, instr) -> {
            ctx.stack.push(new Constant((double) 0.0));
        });

        mapping.put(Opcode.DCONST_1, (ctx, instr) -> {
            ctx.stack.push(new Constant((double) 1.0));
        });

        mapping.put(Opcode.DDIV, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.DIV, double.class);
        });

        mapping.put(Opcode.DLOAD, (ctx, instr) -> {
            load(ctx, instr.getUnsignedByte(1), double.class);
        });

        mapping.put(Opcode.DLOAD_W, ((ctx, instr) -> {
            load(ctx, instr.getShort(1), double.class);
        }));

        mapping.put(Opcode.DLOAD_0, (ctx, instr) -> {
            load(ctx, 0, double.class);
        });

        mapping.put(Opcode.DLOAD_1, (ctx, instr) -> {
            load(ctx, 1, double.class);
        });

        mapping.put(Opcode.DLOAD_2, (ctx, instr) -> {
            load(ctx, 2, double.class);
        });

        mapping.put(Opcode.DLOAD_3, (ctx, instr) -> {
            load(ctx, 3, double.class);
        });

        mapping.put(Opcode.DMUL, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.MUL, double.class);
        });

        mapping.put(Opcode.DNEG, (ctx, instr) -> {
            ctx.stack.push(new OperationExpression(Operator.NEG,
                    reqStackParams(ctx, 1, double.class)));
        });

        mapping.put(Opcode.DREM, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.REM, double.class);
        });

        mapping.put(Opcode.DRETURN, (ctx, instr) -> {
            Expression returnValue = reqStackParams(ctx, 1, double.class)[0];
            if (ctx.previousFrame == null) {
                throw new IncoherentBytecodeException(
                        "RETURNADRESS/CONTEXT NOT SET");
            } else {
                invReturn(ctx, returnValue);
            }
        });

        mapping.put(Opcode.DSTORE, (ctx, instr) -> {
            store(ctx, instr.getUnsignedByte(1), double.class);
        });

        mapping.put(Opcode.DSTORE_W, ((ctx, instr) -> {
            store(ctx, instr.getShort(1), double.class);
        }));

        mapping.put(Opcode.DSTORE_1, (ctx, instr) -> {
            store(ctx, 1, double.class);
        });

        mapping.put(Opcode.DSTORE_2, (ctx, instr) -> {
            store(ctx, 2, double.class);
        });

        mapping.put(Opcode.DSTORE_3, (ctx, instr) -> {
            store(ctx, 3, double.class);
        });

        mapping.put(Opcode.DSUB, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.SUB, double.class);
        });

        // ---- float opcodes ----

        mapping.put(Opcode.FADD, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.ADD, float.class);
        });

        mapping.put(Opcode.FCONST_0, (ctx, instr) -> {
            ctx.stack.push(new Constant((float) 0.0));
        });

        mapping.put(Opcode.FCONST_1, (ctx, instr) -> {
            ctx.stack.push(new Constant((float) 1.0));
        });

        mapping.put(Opcode.FCONST_2, (ctx, instr) -> {
            ctx.stack.push(new Constant((float) 2.0));
        });

        mapping.put(Opcode.FCMPG, (ctx, instr) -> {
            compare(ctx, float.class, 1);
        });

        mapping.put(Opcode.FCMPL, (ctx, instr) -> {
            compare(ctx, float.class, -1);
        });

        mapping.put(Opcode.FDIV, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.DIV, float.class);
        });

        mapping.put(Opcode.FLOAD, (ctx, instr) -> {
            load(ctx, instr.getUnsignedByte(1), float.class);
        });

        mapping.put(Opcode.FLOAD_W, ((ctx, instr) -> {
            load(ctx, instr.getShort(1), float.class);
        }));

        mapping.put(Opcode.FLOAD_0, (ctx, instr) -> {
            load(ctx, 0, float.class);
        });

        mapping.put(Opcode.FLOAD_1, (ctx, instr) -> {
            load(ctx, 1, float.class);
        });

        mapping.put(Opcode.FLOAD_2, (ctx, instr) -> {
            load(ctx, 2, float.class);
        });

        mapping.put(Opcode.FLOAD_3, (ctx, instr) -> {
            load(ctx, 3, float.class);
        });

        mapping.put(Opcode.FMUL, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.MUL, float.class);
        });

        mapping.put(Opcode.FNEG, (ctx, instr) -> {
            ctx.stack.push(new OperationExpression(Operator.NEG,
                    reqStackParams(ctx, 1, float.class)));
        });

        mapping.put(Opcode.FREM, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.REM, float.class);
        });

        mapping.put(Opcode.FRETURN, (ctx, instr) -> {
            Expression returnValue = reqStackParams(ctx, 1, float.class)[0];
            if (ctx.previousFrame == null) {
                throw new IncoherentBytecodeException(
                        "RETURNADRESS/CONTEXT NOT SET");
            } else {
                invReturn(ctx, returnValue);
            }
        });

        mapping.put(Opcode.FSUB, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.SUB, float.class);
        });

        mapping.put(Opcode.FSTORE, (ctx, instr) -> {
            store(ctx, instr.getUnsignedByte(1), float.class);
        });

        mapping.put(Opcode.FSTORE_W, ((ctx, instr) -> {
            store(ctx, instr.getShort(1), float.class);
        }));

        mapping.put(Opcode.FSTORE_0, (ctx, instr) -> {
            store(ctx, 0, float.class);
        });

        mapping.put(Opcode.FSTORE_1, (ctx, instr) -> {
            store(ctx, 1, float.class);
        });

        mapping.put(Opcode.FSTORE_2, (ctx, instr) -> {
            store(ctx, 2, float.class);
        });

        mapping.put(Opcode.FSTORE_3, (ctx, instr) -> {
            store(ctx, 3, float.class);
        });

        // ---- long opcodes ----

        mapping.put(Opcode.LADD, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.ADD, long.class);
        });

        // nanValue 1 is not used for this instruction
        mapping.put(Opcode.LCMP, (ctx, instr) -> {
            compare(ctx, long.class, 1);
        });

        mapping.put(Opcode.LCONST_0, (ctx, instr) -> {
            ctx.stack.push(new Constant((long) 0));
        });

        mapping.put(Opcode.LCONST_1, (ctx, instr) -> {
            ctx.stack.push(new Constant((long) 1));
        });

        mapping.put(Opcode.LDIV, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.DIV, long.class);
        });

        mapping.put(Opcode.LLOAD, (ctx, instr) -> {
            load(ctx, instr.getUnsignedByte(1), long.class);
        });

        mapping.put(Opcode.LLOAD_W, ((ctx, instr) -> {
            load(ctx, instr.getShort(1), long.class);
        }));

        mapping.put(Opcode.LLOAD_0, (ctx, instr) -> {
            load(ctx, 0, long.class);
        });

        mapping.put(Opcode.LLOAD_1, (ctx, instr) -> {
            load(ctx, 1, long.class);
        });

        mapping.put(Opcode.LLOAD_2, (ctx, instr) -> {
            load(ctx, 2, long.class);
        });

        mapping.put(Opcode.LLOAD_3, (ctx, instr) -> {
            load(ctx, 3, long.class);
        });

        mapping.put(Opcode.LMUL, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.MUL, long.class);
        });

        mapping.put(Opcode.LNEG, (ctx, instr) -> {
            ctx.stack.push(new OperationExpression(Operator.NEG,
                    reqStackParams(ctx, 1, long.class)));
        });

        mapping.put(Opcode.LREM, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.REM, long.class);
        });

        mapping.put(Opcode.LRETURN, (ctx, instr) -> {
            Expression returnValue = reqStackParams(ctx, 1, long.class)[0];
            if (ctx.previousFrame == null) {
                throw new IncoherentBytecodeException(
                        "RETURNADRESS/CONTEXT NOT SET");
            } else {
                invReturn(ctx, returnValue);
            }
        });

        mapping.put(Opcode.LSUB, (ctx, instr) -> {
            pushTwoOpExp(ctx, Operator.SUB, long.class);
        });

        mapping.put(Opcode.LSTORE, (ctx, instr) -> {
            store(ctx, instr.getUnsignedByte(1), long.class);
        });

        mapping.put(Opcode.LSTORE_W, ((ctx, instr) -> {
            store(ctx, instr.getShort(1), long.class);
        }));

        mapping.put(Opcode.LSTORE_0, (ctx, instr) -> {
            store(ctx, 0, long.class);
        });

        mapping.put(Opcode.LSTORE_1, (ctx, instr) -> {
            store(ctx, 1, long.class);
        });

        mapping.put(Opcode.LSTORE_2, (ctx, instr) -> {
            store(ctx, 2, long.class);
        });

        mapping.put(Opcode.LSTORE_3, (ctx, instr) -> {
            store(ctx, 3, long.class);
        });

        mapping.put(Opcode.BIPUSH, (ctx, instr) -> {
            ctx.stack.push(new Constant(instr.getByte(1)));
        });

        // ---- type conversion opcodes ----

        mapping.put(Opcode.D2F, (ctx, instr) -> {
            convert(ctx, double.class, Operator.TO_FLOAT);
        });

        mapping.put(Opcode.D2I, (ctx, instr) -> {
            convert(ctx, double.class, Operator.TO_INT);
        });

        mapping.put(Opcode.D2L, (ctx, instr) -> {
            convert(ctx, double.class, Operator.TO_LONG);
        });

        mapping.put(Opcode.F2D, (ctx, instr) -> {
            convert(ctx, float.class, Operator.TO_DOUBLE);
        });

        mapping.put(Opcode.F2I, (ctx, instr) -> {
            convert(ctx, float.class, Operator.TO_INT);
        });

        mapping.put(Opcode.F2L, (ctx, instr) -> {
            convert(ctx, float.class, Operator.TO_LONG);
        });

        mapping.put(Opcode.I2B, (ctx, instr) -> {
            convert(ctx, int.class, Operator.TO_BYTE);
        });

        mapping.put(Opcode.I2C, (ctx, instr) -> {
            convert(ctx, int.class, Operator.TO_CHAR);
        });

        mapping.put(Opcode.I2D, (ctx, instr) -> {
            convert(ctx, int.class, Operator.TO_DOUBLE);
        });

        mapping.put(Opcode.I2F, (ctx, instr) -> {
            convert(ctx, int.class, Operator.TO_FLOAT);
        });

        mapping.put(Opcode.I2L, (ctx, instr) -> {
            convert(ctx, int.class, Operator.TO_LONG);
        });

        mapping.put(Opcode.I2S, (ctx, instr) -> {
            convert(ctx, int.class, Operator.TO_SHORT);
        });

        mapping.put(Opcode.L2D, (ctx, instr) -> {
            convert(ctx, long.class, Operator.TO_DOUBLE);
        });

        mapping.put(Opcode.L2F, (ctx, instr) -> {
            convert(ctx, long.class, Operator.TO_FLOAT);
        });

        mapping.put(Opcode.L2I, (ctx, instr) -> {
            convert(ctx, long.class, Operator.TO_INT);
        });

        // ---- other opcodes -----

        mapping.put(Opcode.NOP, (ctx, instr) -> {
        });

        mapping.put(Opcode.GETSTATIC, (ctx, instr) -> {
            ConstantPool cp = ctx.getConstantPool();
            int index = instr.getUnsignedShort(1);
            Expression staticExp = null;
            try {
                CONSTANT_Fieldref_info con = (CONSTANT_Fieldref_info) cp
                        .get(index);
                staticExp = getStatic(ctx, con, int.class);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Error retrieving Fieldref from CostantPool: "
                                + e.getMessage());
            }
            if (staticExp != null) {
                ctx.stack.push(staticExp);
            } else {
                ctx.stack.push(new Constant(Reference.class, -1));
            }
        });

        mapping.put(Opcode.PUTSTATIC, (ctx, instr) -> {
            int index = instr.getUnsignedShort(1);
            Expression staticExp = reqStackParams(ctx, 1, Undef.class)[0];
            ConstantPool cp = ctx.getConstantPool();
            try {
                CONSTANT_Fieldref_info con = (CONSTANT_Fieldref_info) cp
                        .get(index);
                FieldRef ref = new FieldRef(con);
                ctx.statics.put(ref, staticExp);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Error retrieving Fieldref from ConstantPool: "
                                + e.getMessage());
            }
        });

        mapping.put(Opcode.LDC, (ctx, instr) -> {
            int index = instr.getUnsignedByte(1);
            Expression constant = getConstantSlot(ctx.getConstantPool(), index);
            if (constant != null) {
                ctx.stack.push(constant);
            }
        });

        mapping.put(Opcode.LDC_W, (ctx, instr) -> {
            int index = instr.getUnsignedShort(1);
            Expression constant = getConstantSlot(ctx.getConstantPool(), index);
            if (constant != null) {
                ctx.stack.push(constant);
            }
        });

        mapping.put(Opcode.LDC2_W, (ctx, instr) -> {
            int index = instr.getUnsignedShort(1);
            Expression constant = getConstantSlot(ctx.getConstantPool(), index);
            if (constant != null) {
                ctx.stack.push(constant);
            }
        });

        mapping.put(Opcode.POP, (ctx, instr) -> {
            reqStackParams(ctx, 1, Undef.class);
        });

        mapping.put(Opcode.DUP, (ctx, instr) -> {
            Expression expr = reqStackParams(ctx, 1, Undef.class)[0];
            ctx.stack.push(expr);
            ctx.stack.push(expr);
        });

        mapping.put(Opcode.DUP_X1, (ctx, instr) -> {
            Expression exprFirst = reqStackParams(ctx, 1, Undef.class)[0];
            Expression exprSecond = reqStackParams(ctx, 1, Undef.class)[0];
            ctx.stack.push(exprFirst);
            ctx.stack.push(exprSecond);
            ctx.stack.push(exprFirst);
        });

        mapping.put(Opcode.DUP_X2, (ctx, instr) -> {
            Expression exprFirst = reqStackParams(ctx, 1, Undef.class)[0];
            Expression exprSecond = reqStackParams(ctx, 1, Undef.class)[0];
            Expression exprThird = reqStackParams(ctx, 1, Undef.class)[0];
            ctx.stack.push(exprFirst);
            ctx.stack.push(exprThird);
            ctx.stack.push(exprSecond);
            ctx.stack.push(exprFirst);
        });

        mapping.put(Opcode.DUP2, (ctx, instr) -> {
            Expression exprFirst = reqStackParams(ctx, 1, Undef.class)[0];
            Expression exprSecond = reqStackParams(ctx, 1, Undef.class)[0];
            ctx.stack.push(exprSecond);
            ctx.stack.push(exprFirst);
            ctx.stack.push(exprSecond);
            ctx.stack.push(exprFirst);
        });

        mapping.put(Opcode.DUP2_X1, (ctx, instr) -> {
            Expression exprFirst = reqStackParams(ctx, 1, Undef.class)[0];
            Expression exprSecond = reqStackParams(ctx, 1, Undef.class)[0];
            Expression exprThird = reqStackParams(ctx, 1, Undef.class)[0];
            ctx.stack.push(exprSecond);
            ctx.stack.push(exprFirst);
            ctx.stack.push(exprThird);
            ctx.stack.push(exprSecond);
            ctx.stack.push(exprFirst);
        });

        mapping.put(Opcode.DUP2_X2, (ctx, instr) -> {
            Expression exprFirst = reqStackParams(ctx, 1, Undef.class)[0];
            Expression exprSecond = reqStackParams(ctx, 1, Undef.class)[0];
            Expression exprThird = reqStackParams(ctx, 1, Undef.class)[0];
            Expression exprFourth = reqStackParams(ctx, 1, Undef.class)[0];
            ctx.stack.push(exprSecond);
            ctx.stack.push(exprFirst);
            ctx.stack.push(exprFourth);
            ctx.stack.push(exprThird);
            ctx.stack.push(exprSecond);
            ctx.stack.push(exprFirst);
        });

        mapping.put(Opcode.TABLESWITCH, (ctx, instr) -> {
            // calculate offset for padding
            int offset = 4 - (instr.getPC() % 4);
            int defJumpInt = instr.getInt(offset) + instr.getPC();
            FunctionCall defaultJump = getJumpToTargetFunction(defJumpInt, ctx);
            int lowInt = instr.getInt(offset + 4);
            int highInt = instr.getInt(offset + 8);
            Expression index = reqStackParams(ctx, 1, int.class)[0];
            ArrayList<OperationExpression> conditions = new ArrayList<>();
            ArrayList<FunctionCall> jumps = new ArrayList<>();
            OperationExpression ite;

            // get the jump targets for the respective keys
            for (int i = 0; i < highInt - lowInt + 1; i++) {
                Expression key = new Constant(lowInt + i);
                OperationExpression condition = new OperationExpression(
                        Operator.EQUAL, index, key);
                conditions.add(condition);
                int iJump = instr.getInt(12 + 4 * i + offset) + instr.getPC();
                FunctionCall jumpTo = getJumpToTargetFunction(iJump, ctx);
                jumps.add(jumpTo);
            }

            // emulate the jump table with multiple ITE expressions
            FunctionCall jump = jumps.get(highInt - lowInt);
            OperationExpression condition = conditions.get(highInt - lowInt);
            ite = new OperationExpression(Operator.ITE, condition, jump,
                    defaultJump);
            for (int i = jumps.size() - 2; i >= 0; i--) {
                jump = jumps.get(i);
                condition = conditions.get(i);
                ite = new OperationExpression(Operator.ITE, condition, jump,
                        ite);
            }

            ctx.invoker.expression = ite;
        });

        mapping.put(Opcode.LOOKUPSWITCH, (ctx, instr) -> {
            // 0-3 bit padding
            int offset = 4 - (instr.getPC() % 4);
            int defJumpInt = instr.getInt(offset) + instr.getPC();
            FunctionCall defaultJump = getJumpToTargetFunction(defJumpInt, ctx);
            int nPairsInt = instr.getInt(4 + offset);
            Expression key = reqStackParams(ctx, 1, int.class)[0];
            ArrayList<OperationExpression> conditions = new ArrayList<>();
            ArrayList<FunctionCall> jumps = new ArrayList<>();
            OperationExpression ite;

            // get the keys and respective jump targets from the jump table
            for (int i = 0; i < nPairsInt; i++) {
                int mapKey = instr.getInt(8 + offset + 8 * i);
                int jumpTo = instr.getInt(12 + offset + 8 * i) + instr.getPC();
                Constant cKey = new Constant(mapKey);
                FunctionCall cJump = getJumpToTargetFunction(jumpTo, ctx);
                OperationExpression condition = new OperationExpression(
                        Operator.EQUAL, key, cKey);
                conditions.add(condition);
                jumps.add(cJump);
            }

            // emulate the jump table with multiple ITE expressions
            FunctionCall jump = jumps.get(nPairsInt - 1);
            OperationExpression condition = conditions.get(nPairsInt - 1);
            ite = new OperationExpression(Operator.ITE, condition, jump,
                    defaultJump);
            for (int i = jumps.size() - 2; i >= 0; i--) {
                jump = jumps.get(i);
                condition = conditions.get(i);
                ite = new OperationExpression(Operator.ITE, condition, jump,
                        ite);
            }

            ctx.invoker.expression = ite;
        });

        // POP2/dup___: needs implementation of the different lengths of the
        // types on
        // the stack

        // ---- if/jump/branch stuff -----

        // The Opcodes JSR, JSR_W and RET are not in use since Java 6

        mapping.put(Opcode.GOTO, (ctx, instr) -> {
            int target = instr.getPC() + instr.getShort(1);
            FunctionCall fc = getJumpToTargetFunction(target, ctx);
            ctx.invoker.expression = fc;
        });

        mapping.put(Opcode.GOTO_W, (ctx, instr) -> {
            int target = instr.getPC() + instr.getInt(1);
            FunctionCall fc = getJumpToTargetFunction(target, ctx);
            ctx.invoker.expression = fc;
        });

        mapping.put(Opcode.IF_ICMPEQ, (ctx, instr) -> {
            Expression eq = createTwoOpExp(ctx, Operator.EQUAL, int.class);
            createConditionalJumpExpression(instr, eq, ctx);
        });

        mapping.put(Opcode.IF_ICMPGE, (ctx, instr) -> {
            Expression geq = createTwoOpExp(ctx, Operator.GREATER_OR_EQUAL,
                    int.class);
            createConditionalJumpExpression(instr, geq, ctx);
        });

        mapping.put(Opcode.IF_ICMPGT, (ctx, instr) -> {
            Expression gt = createTwoOpExp(ctx, Operator.GREATER, int.class);
            createConditionalJumpExpression(instr, gt, ctx);
        });

        mapping.put(Opcode.IF_ICMPLE, (ctx, instr) -> {
            Expression leq = createTwoOpExp(ctx, Operator.LESS_OR_EQUAL,
                    int.class);
            createConditionalJumpExpression(instr, leq, ctx);
        });

        mapping.put(Opcode.IF_ICMPLT, (ctx, instr) -> {
            Expression lt = createTwoOpExp(ctx, Operator.LESS, int.class);
            createConditionalJumpExpression(instr, lt, ctx);
        });

        mapping.put(Opcode.IF_ICMPNE, (ctx, instr) -> {
            Expression ne = createTwoOpExp(ctx, Operator.NOT_EQUAL, int.class);
            createConditionalJumpExpression(instr, ne, ctx);
        });

        mapping.put(Opcode.IFEQ, (ctx, instr) -> {
            Expression stackTop = reqStackParams(ctx, 1, int.class)[0];
            Expression equalZero = new OperationExpression(Operator.EQUAL,
                    stackTop, new Constant(0));

            createConditionalJumpExpression(instr, equalZero, ctx);
        });

        mapping.put(Opcode.IFGE, (ctx, instr) -> {
            Expression stackTop = reqStackParams(ctx, 1, int.class)[0];
            Expression greaterEqualZero = new OperationExpression(
                    Operator.GREATER_OR_EQUAL, stackTop, new Constant(0));

            createConditionalJumpExpression(instr, greaterEqualZero, ctx);
        });

        mapping.put(Opcode.IFGT, (ctx, instr) -> {
            Expression stackTop = reqStackParams(ctx, 1, int.class)[0];
            Expression greaterThanZero = new OperationExpression(
                    Operator.GREATER, stackTop, new Constant(0));

            createConditionalJumpExpression(instr, greaterThanZero, ctx);
        });

        mapping.put(Opcode.IFLE, (ctx, instr) -> {
            Expression stackTop = reqStackParams(ctx, 1, int.class)[0];
            Expression lessEqualZero = new OperationExpression(
                    Operator.LESS_OR_EQUAL, stackTop, new Constant(0));

            createConditionalJumpExpression(instr, lessEqualZero, ctx);
        });

        mapping.put(Opcode.IFLT, (ctx, instr) -> {
            Expression stackTop = reqStackParams(ctx, 1, int.class)[0];
            Expression lessThanZero = new OperationExpression(Operator.LESS,
                    stackTop, new Constant(0));

            createConditionalJumpExpression(instr, lessThanZero, ctx);
        });

        mapping.put(Opcode.IFNE, (ctx, instr) -> {
            Expression stackTop = reqStackParams(ctx, 1, int.class)[0];
            Expression notEqualZero = new OperationExpression(
                    Operator.NOT_EQUAL, stackTop, new Constant(0));

            createConditionalJumpExpression(instr, notEqualZero, ctx);
        });

        mapping.put(Opcode.IFNONNULL, (ctx, instr) -> {
            Expression stackTop = reqStackParams(ctx, 1, Reference.class)[0];
            Expression notNull = new OperationExpression(Operator.NOT_EQUAL,
                    stackTop, new Constant(Reference.class, -1));

            createConditionalJumpExpression(instr, notNull, ctx);
        });

        mapping.put(Opcode.IFNULL, (ctx, instr) -> {
            Expression stackTop = reqStackParams(ctx, 1, Reference.class)[0];
            Expression isNull = new OperationExpression(Operator.EQUAL,
                    stackTop, new Constant(Reference.class, -1));

            createConditionalJumpExpression(instr, isNull, ctx);
        });

        mapping.put(Opcode.AALOAD, (ctx, instr) -> {
            xALoad(ctx, Reference.class);
        });

        mapping.put(Opcode.AASTORE, (ctx, instr) -> {
            xAStore(ctx, Reference.class);
        });

        mapping.put(Opcode.BALOAD, (ctx, instr) -> {
            xALoad(ctx, boolean.class);
        });

        mapping.put(Opcode.BASTORE, (ctx, instr) -> {
            xAStore(ctx, boolean.class);
        });

        mapping.put(Opcode.CALOAD, (ctx, instr) -> {
            xALoad(ctx, char.class);
        });

        mapping.put(Opcode.CASTORE, (ctx, instr) -> {
            xAStore(ctx, char.class);
        });

        mapping.put(Opcode.DALOAD, (ctx, instr) -> {
            xALoad(ctx, double.class);
        });

        mapping.put(Opcode.DASTORE, (ctx, instr) -> {
            xAStore(ctx, double.class);
        });

        mapping.put(Opcode.FALOAD, (ctx, instr) -> {
            xALoad(ctx, float.class);
        });

        mapping.put(Opcode.FASTORE, (ctx, instr) -> {
            xAStore(ctx, float.class);
        });

        mapping.put(Opcode.IALOAD, (ctx, instr) -> {
            xALoad(ctx, int.class);
        });

        mapping.put(Opcode.IASTORE, (ctx, instr) -> {
            xAStore(ctx, int.class);
        });

        mapping.put(Opcode.LALOAD, (ctx, instr) -> {
            xALoad(ctx, long.class);
        });

        mapping.put(Opcode.LASTORE, (ctx, instr) -> {
            xAStore(ctx, long.class);
        });

        mapping.put(Opcode.SALOAD, (ctx, instr) -> {
            xALoad(ctx, short.class);
        });

        mapping.put(Opcode.SASTORE, (ctx, instr) -> {
            xAStore(ctx, short.class);
        });

        mapping.put(Opcode.ARRAYLENGTH, (ctx, instr) -> {
            // First expression of a symbolic array is its type
            // Second one is its length
            Expression arrayRef = reqStackParams(ctx, 1, Reference.class)[0];
            Expression lengthRef = new OperationExpression(Operator.ADD,
                    arrayRef, new Constant(Reference.class, 1));
            Expression get = new OperationExpression(Operator.GET, int.class,
                    ctx.heap, lengthRef);
            ctx.stack.push(get);
        });

        mapping.put(Opcode.ANEWARRAY, (ctx, instr) -> {
            Expression count = reqStackParams(ctx, 1, int.class)[0];
            Class<?> c = getClassFromCtx(ctx, instr.getUnsignedShort(1));
            Class<?> arrayType = Typeconversions.toArrayType(c);
            Expression address = ctx.heapsize;
            ctx.stack.push(address);
            if (count instanceof Constant) {
                Constant cCount = (Constant) count;
                int aLength = (int) cCount.getNumber();
                appendArray(ctx, aLength, Reference.class, arrayType);
            } else {
                appendArraySpace(ctx, count, Reference.class, arrayType);
            }
        });

        mapping.put(Opcode.MULTIANEWARRAY, (ctx, instr) -> {
            Expression address = ctx.heapsize;
            Class<?> c = getClassFromCtx(ctx, instr.getUnsignedShort(1));
            int dimensions = instr.getUnsignedByte(3);
            Expression[] count = reqStackParams(ctx, dimensions, int.class);
            ctx.stack.push(address);
            if (count[0] instanceof Constant) {
                Constant cLength = (Constant) count[0];
                ctx.heapsize = appendArray(ctx, (int) cLength.getValue(),
                        Reference.class, c);
            } else {
                appendArraySpace(ctx, count[0], Reference.class, c);
            }
            addSubArray(ctx, c, count, address, dimensions - 1, 0);

        });

        mapping.put(Opcode.NEW, (ctx, instr) -> {
            final Expression address = ctx.heapsize;
            // Push new objects address
            ctx.stack.push(address);
            Class<?> c = getClassFromCtx(ctx, instr.getUnsignedShort(1));
            int objectSize = 1;
            // Put type information onto the heap
            Expression runtimeType = new Constant(RuntimeType.class,
                    ctx.getTypeId(c));
            ctx.heap = new OperationExpression(Operator.PUT, ctx.heap,
                    ctx.heapsize, runtimeType);
            for (Field f : HeapContext.getAllNonStaticFields(c)) {
                Class<?> type = Reference.class;
                if (f.getType().isPrimitive()) {
                    type = f.getType();
                }

                Expression fieldAddress = new OperationExpression(Operator.ADD,
                        address, new Constant(Reference.class, objectSize));
                ctx.heap = new OperationExpression(Operator.PUT, ctx.heap,
                        fieldAddress, Typeconversions.getDefaultValue(type));

                objectSize += 1;
            }
            ctx.heapsize = new OperationExpression(Operator.ADD, address,
                    new Constant(Reference.class, objectSize));

        });

        mapping.put(Opcode.NEWARRAY, (ctx, instr) -> {
            Expression address = ctx.heapsize;
            Expression count = reqStackParams(ctx, 1, int.class)[0];
            ctx.stack.push(address);
            if (count instanceof Constant) {
                Constant cCount = (Constant) count;
                int aLength = (int) cCount.getNumber();
                int code = instr.getByte(1);
                switch (code) {
                case 4:
                    // boolean
                    appendArray(ctx, aLength, boolean.class, boolean[].class);
                    break;
                case 5:
                    // char
                    appendArray(ctx, aLength, char.class, char[].class);
                    break;
                case 6:
                    // float
                    appendArray(ctx, aLength, float.class, float[].class);
                    break;
                case 7:
                    // double
                    appendArray(ctx, aLength, double.class, double[].class);
                    break;
                case 8:
                    // byte
                    appendArray(ctx, aLength, byte.class, byte[].class);
                    break;
                case 9:
                    // short
                    appendArray(ctx, aLength, short.class, short[].class);
                    break;
                case 10:
                    // int
                    appendArray(ctx, aLength, int.class, int[].class);
                    break;
                case 11:
                    // long
                    appendArray(ctx, aLength, long.class, long[].class);
                    break;
                }
            } else {
                appendArraySpace(ctx, count, int.class, int[].class);
            }

        });

        mapping.put(Opcode.GETFIELD, (ctx, instr) -> {
            Expression baseAddr = reqStackParams(ctx, 1, Reference.class)[0];
            // Add one to field index since we need to skip the runtimetype at
            // offset 0
            int fieldIndex = getFieldIndex(ctx, instr.getUnsignedShort(1)) + 1;
            Expression selector = new OperationExpression(Operator.ADD,
                    baseAddr, new Constant(Reference.class, fieldIndex));

            // Provide a type hint to the Get Operation
            Class<?> fieldType = getFieldType(ctx, instr.getUnsignedShort(1));
            Expression get = new OperationExpression(Operator.GET, fieldType,
                    ctx.heap, selector);
            // Convert type to potentially larger stack type (byte -> int)
            Expression typedGet = Typeconversions.toStackType(get);
            ctx.stack.push(typedGet);
        });

        mapping.put(Opcode.PUTFIELD, (ctx, instr) -> {
            Class<?> fieldType = getFieldType(ctx, instr.getUnsignedShort(1));
            Class<?> valueType = Typeconversions.getStackType(fieldType);

            Expression value = reqStackParams(ctx, 1, valueType)[0];

            Expression baseAddr = reqStackParams(ctx, 1, Reference.class)[0];
            // Add one to field index since we need to skip the runtimetype at
            // offset 0
            Expression fieldIndex = new Constant(Reference.class,
                    getFieldIndex(ctx, instr.getUnsignedShort(1)) + 1);
            OperationExpression fieldAddr = new OperationExpression(
                    Operator.ADD, baseAddr, fieldIndex);
            // Correctly shrink the value (int -> byte)
            Expression typedValue = Typeconversions.castToType(value,
                    fieldType);
            ctx.heap = new OperationExpression(Operator.PUT, ctx.heap,
                    fieldAddr, typedValue);
        });

        mapping.put(Opcode.INVOKESPECIAL, (ctx, instr) -> {
            invoke(ctx, instr, false);
        });

        mapping.put(Opcode.INVOKEVIRTUAL, (ctx, instr) -> {
            invoke(ctx, instr, false);
        });

        mapping.put(Opcode.INVOKESTATIC, (ctx, instr) -> {
            invoke(ctx, instr, true);
        });

        mapping.put(Opcode.INVOKEINTERFACE, (ctx, instr) -> {
            invoke(ctx, instr, false);
        });

        mapping.put(Opcode.ACONST_NULL, (ctx, instr) -> {
            // We represent null as -1
            ctx.stack.push(new Constant(Reference.class, -1));
        });

        mapping.put(Opcode.ASTORE, (ctx, instr) -> {
            store(ctx, instr.getUnsignedByte(1), Reference.class);
        });

        mapping.put(Opcode.ASTORE_W, ((ctx, instr) -> {
            store(ctx, instr.getUnsignedByte(1), Reference.class);
            store(ctx, instr.getUnsignedByte(2), Reference.class);
        }));

        mapping.put(Opcode.ASTORE_0, (ctx, instr) -> {
            store(ctx, 0, Reference.class);
        });
        mapping.put(Opcode.ASTORE_1, (ctx, instr) -> {
            store(ctx, 1, Reference.class);
        });
        mapping.put(Opcode.ASTORE_2, (ctx, instr) -> {
            store(ctx, 2, Reference.class);
        });
        mapping.put(Opcode.ASTORE_3, (ctx, instr) -> {
            store(ctx, 3, Reference.class);
        });

        mapping.put(Opcode.ALOAD, (ctx, instr) -> {
            load(ctx, instr.getUnsignedByte(1), Reference.class);
        });

        mapping.put(Opcode.ALOAD_W, ((ctx, instr) -> {
            load(ctx, instr.getUnsignedByte(1), Reference.class);
            load(ctx, instr.getUnsignedByte(2), Reference.class);
        }));

        mapping.put(Opcode.ALOAD_0, (ctx, instr) -> {
            load(ctx, 0, Reference.class);
        });
        mapping.put(Opcode.ALOAD_1, (ctx, instr) -> {
            load(ctx, 1, Reference.class);
        });
        mapping.put(Opcode.ALOAD_2, (ctx, instr) -> {
            load(ctx, 2, Reference.class);
        });
        mapping.put(Opcode.ALOAD_3, (ctx, instr) -> {
            load(ctx, 3, Reference.class);
        });
        mapping.put(Opcode.IF_ACMPNE, (ctx, instr) -> {
            Expression ne = createTwoOpExp(ctx, Operator.NOT_EQUAL,
                    Reference.class);
            createConditionalJumpExpression(instr, ne, ctx);
        });
        mapping.put(Opcode.IF_ACMPEQ, (ctx, instr) -> {
            Expression ne = createTwoOpExp(ctx, Operator.EQUAL,
                    Reference.class);
            createConditionalJumpExpression(instr, ne, ctx);
        });

        mapping.put(Opcode.INSTANCEOF, (ctx, instr) -> {
            Expression ref = reqStackParams(ctx, 1, Reference.class)[0];
            Class<?> rightc = getClassFromCtx(ctx, instr.getUnsignedShort(1));

            // RuntimeType is at offset 0
            Expression lefttypeid = new OperationExpression(Operator.GET,
                    ctx.heap, ref);

            int rtypeid = ctx.getTypeId(rightc);

            Expression check = new OperationExpression(Operator.EQUAL,
                    lefttypeid, new Constant(RuntimeType.class, rtypeid));
            // Generate a check against each of the subtypes of rightc
            List<Integer> typeids = ctx.getSubTypes(rtypeid);
            for (int typeid : typeids) {
                final Expression righttypeid = new Constant(RuntimeType.class,
                        typeid);
                final Expression eq = new OperationExpression(Operator.EQUAL,
                        lefttypeid, righttypeid);
                check = new OperationExpression(Operator.BOR, check, eq);
            }

            check = Typeconversions.castToType(check, int.class);
            ctx.stack.push(check);
        });

        // swap opcode
        mapping.put(Opcode.SWAP, (ctx, instr) -> {
            Expression exprFirst = reqStackParams(ctx, 1, Undef.class)[0];
            Expression exprSecond = reqStackParams(ctx, 1, Undef.class)[0];
            ctx.stack.push(exprFirst);
            ctx.stack.push(exprSecond);
        });

        mapping.put(Opcode.CHECKCAST, (ctx, instr) -> {
        });
    }

    /**
     * Calculates the offset from the objects address of a field in our heap
     * simulation
     * 
     * @param ctx
     *            Context
     * @param fieldRefIndex
     *            field description we are looking for
     * @return
     */
    public static int getFieldIndex(Context ctx, int fieldRefIndex) {
        CONSTANT_Fieldref_info fi;
        try {
            fi = (CONSTANT_Fieldref_info) ctx.getConstantPool()
                    .get(fieldRefIndex);
            Class<?> c = getClassFromCtx(ctx, fi.class_index);
            CONSTANT_NameAndType_info type = fi.getNameAndTypeInfo();
            String fieldname = ctx.getConstantPool()
                    .getUTF8Value(type.name_index);
            Field[] fields = HeapContext.getAllNonStaticFields(c);
            // The fields are traversed backwards to find the field of the most
            // specific class
            // Lower indices are fields from superclasses and higher indices
            // from subclasses
            // We want to find the highest matching index
            for (int i = fields.length - 1; i >= 0; --i) {
                if (fieldname.equals(fields[i].getName())) {
                    return i;
                }
            }
            throw new IncoherentBytecodeException(
                    "Cannot find field " + fieldname);
        } catch (ConstantPoolException e) {
            throw new RuntimeException(
                    "Error while getting FieldIndex: " + e.getMessage());
        }
    }

    /**
     * Returns the type of a field at a given fieldRefIndex
     * 
     * @param ctx
     * @param fieldRefIndex
     * @return
     */
    public static Class<?> getFieldType(Context ctx, int fieldRefIndex) {
        CONSTANT_Fieldref_info fi;
        try {
            fi = (CONSTANT_Fieldref_info) ctx.getConstantPool()
                    .get(fieldRefIndex);
            Class<?> c = getClassFromCtx(ctx, fi.class_index);
            CONSTANT_NameAndType_info type = fi.getNameAndTypeInfo();
            String fieldname = ctx.getConstantPool()
                    .getUTF8Value(type.name_index);
            Field[] fields = HeapContext.getAllNonStaticFields(c);
            for (int i = fields.length - 1; i >= 0; --i) {
                if (fieldname.equals(fields[i].getName())) {
                    Class<?> fieldType = fields[i].getType();
                    if (!fieldType.isPrimitive()) {
                        fieldType = Reference.class;
                    }
                    return fieldType;
                }
            }
        } catch (ConstantPoolException e) {
            throw new RuntimeException(
                    "Error while getting field type: " + e.getMessage());
        }
        throw new IncoherentBytecodeException("Cannot find field");
    }

    /**
     * Creates a conditional jump and initializes the given function to
     * consistent in an if-then-else expression that emulates the (imperative)
     * jump symbolically.
     * 
     * @param instr
     *            instruction with branchbytes - necessary for finding out where
     *            the jump is to
     * @param jumpCondition
     *            The if-clause
     * @return
     */
    public static void createConditionalJumpExpression(Instruction instr,
            Expression jumpCondition, Context ctx) {
        // Possible jump target one is specified by the parameters of this
        // instruction
        int jumpAdress = instr.getShort(1) + instr.getPC();
        FunctionCall jump = getJumpToTargetFunction(jumpAdress, ctx);
        // The other possible jump target is simply the next command
        int noJumpAdress = instr.getPC() + instr.length();
        FunctionCall noJump = getJumpToTargetFunction(noJumpAdress, ctx);
        Expression ite = new OperationExpression(Operator.ITE,
                Typeconversions.castToType(jumpCondition, boolean.class), jump,
                noJump);
        ctx.invoker.expression = ite;
    }

    /**
     * Returns a number of stack values
     * 
     * If there are not enough present on the local stack, new stack parameters
     * are requested
     * 
     * @param ctx
     * @param no
     *            number of requested stack values
     * @param cl
     * @return
     */
    public static Expression[] reqStackParams(Context ctx, int no,
            Class<?> cl) {
        Expression[] ret = new Expression[no];
        for (int i = 0; i < no; i++) {
            if (ctx.stack.empty()) {
                ctx.stack.push(ctx.invoker.requestStackParameter(cl));
            }
            ret[i] = ctx.stack.pop();

            if (ret[i].getType() == Undef.class) {
                ret[i].setType(cl);
            }
        }
        return ret;
    }

    /**
     * Constructs a call-expression of a SEFunction representing the code of a
     * specific PC
     * 
     * @param targetAdress
     * @param ctx
     * @return
     */
    public static FunctionCall getJumpToTargetFunction(int targetAdress,
            Context ctx) {
        SEFunction jumpTargetFunction = ctx.getSEFunctionforPC(targetAdress);
        jumpTargetFunction.register(ctx.invoker);
        FunctionCall fc = new FunctionCall(jumpTargetFunction, ctx);
        return fc;
    }

    /**
     * Returns expression of a specific local variable and type
     * 
     * @param ctx
     * @param index
     * @param cl
     * @return
     */
    public static Expression getLocal(Context ctx, int index, Class<?> cl) {
        if (ctx.locals[index] == null) {
            ctx.locals[index] = ctx.invoker.requestLocalVariableParameter(cl,
                    index);
        }
        return ctx.locals[index];
    }

    public static Expression getStatic(Context ctx, CPInfo con, Class<?> cl) {
        FieldRef staticField = new FieldRef(con);
        if (ctx.statics.get(staticField) == null) {
            ctx.statics.put(staticField,
                    ctx.invoker.requestStaticParameter(cl, staticField));
        }
        return ctx.statics.get(staticField);
    }

    public static void load(Context ctx, int index, Class<?> cl) {
        ctx.stack.push(Typeconversions.toStackType(getLocal(ctx, index, cl)));
    }

    public static void store(Context ctx, int index, Class<?> cl) {
        ctx.locals[index] = reqStackParams(ctx, 1, cl)[0];
    }

    public static void convert(Context ctx, Class<?> type1, Operator op) {
        ctx.stack.push(
                new OperationExpression(op, reqStackParams(ctx, 1, type1)[0]));

    }

    /**
     * Returns the constant at index from the ConstantPool as an Expression
     * 
     * @param cp
     * @param index
     * @return
     */
    public static Expression getConstantSlot(ConstantPool cp, int index) {
        try {
            CPInfo con = cp.get(index);

            // TODO: Handle the "stranger" types, e.g. Strings and Method
            // references
            switch (con.getTag()) {
            case ConstantPool.CONSTANT_Float:
                CONSTANT_Float_info cfloat = (CONSTANT_Float_info) con;
                return new Constant(cfloat.value);
            case ConstantPool.CONSTANT_Double:
                CONSTANT_Double_info cdouble = (CONSTANT_Double_info) con;
                return new Constant(cdouble.value);
            case ConstantPool.CONSTANT_Long:
                CONSTANT_Long_info clong = (CONSTANT_Long_info) con;
                return new Constant(clong.value);
            case ConstantPool.CONSTANT_Integer:
                CONSTANT_Integer_info cint = (CONSTANT_Integer_info) con;
                return new Constant(cint.value);
            default:
                throw new RuntimeException(
                        "Accessing not implemented Constant type");
            }
        } catch (InvalidIndex e) {
            throw new IncoherentBytecodeException(
                    "Invalid index of constant slot: " + index);
        }
    }

    /**
     * Create an operation expression using the top two values on the stack with
     * the top value being the second operator and push it on the stack. This
     * applies to the operations Xadd, Xdiv, Xrem, Xsub, Xmul, Xand, Xor and
     * Xxor
     * 
     * @param ctx
     *            The context of the function
     * @param op
     *            The operator used for the operation expression
     * @param cl
     *            The class of the operation (needs to match the class of the
     *            top two values on the stack)
     */
    public static void pushTwoOpExp(Context ctx, Operator op, Class<?> cl) {
        Expression val2 = reqStackParams(ctx, 1, cl)[0];
        Expression val1 = reqStackParams(ctx, 1, cl)[0];
        ctx.stack.push(Typeconversions
                .toStackType(new OperationExpression(op, val1, val2)));
    }

    /**
     * Create an operation expression using the top two values on the stack with
     * the top value being the second operator. Needed for if_XcmpX jumps.
     * 
     * @param ctx
     *            The context of the function
     * @param op
     *            The operator used for the operation expression
     * @param cl
     *            The class of the operation (needs to match the class of the
     *            top two values on the stack)
     * @return the created OperationExpression
     */
    public static Expression createTwoOpExp(Context ctx, Operator op,
            Class<?> cl) {
        Expression val2 = reqStackParams(ctx, 1, cl)[0];
        Expression val1 = reqStackParams(ctx, 1, cl)[0];
        return Typeconversions
                .toStackType(new OperationExpression(op, val1, val2));
    }

    /**
     * returns the Class referenced in the ConstantPool in the given Context at
     * the given index
     * 
     * @param ctx
     * @param index
     * @return
     */
    public static Class<?> getClassFromCtx(Context ctx, int index) {
        ConstantPool cp = ctx.getConstantPool();
        try {
            CPInfo con = cp.get(index);
            CONSTANT_Class_info cl = (CONSTANT_Class_info) con;
            int index2 = cl.name_index;
            String classname = cp.getUTF8Value(index2);
            classname = classname.replaceAll("/", "\\.");
            // Class.forName loads array classes properly
            final Class<?> c = ctx.symbolicExec.classLoader
                    .loadClass(classname);
            return c;
        } catch (InvalidIndex | UnexpectedEntry | ClassNotFoundException e) {
            throw new IncoherentBytecodeException(
                    "Class not found in Constant Pool.");
        }
    }

    /**
     * append a given amount of primitive array elements to the heap.
     * 
     * @param ctx
     * @param aLength
     *            amount of array elements to be added
     * @param c
     *            class of the array elements
     * @param pAddress
     *            address of the array
     * @return
     */
    public static Expression appendArray(Context ctx, int aLength, Class<?> c,
            Class<?> arrayType) {
        Expression address = ctx.heapsize;
        Expression runtimeType = new Constant(RuntimeType.class,
                ctx.getTypeId(arrayType));
        // At index 0: runtimetype
        ctx.heap = new OperationExpression(Operator.PUT, ctx.heap, address,
                runtimeType);
        address = new OperationExpression(Operator.ADD, address,
                new Constant(Reference.class, 1));
        // At index 1: length
        ctx.heap = new OperationExpression(Operator.PUT, ctx.heap, address,
                new Constant(int.class, aLength));
        address = new OperationExpression(Operator.ADD, address,
                new Constant(Reference.class, 1));
        for (int i = 0; i < aLength; i++) {
            ctx.heap = new OperationExpression(Operator.PUT, ctx.heap, address,
                    Typeconversions.getDefaultValue(c));
            address = new OperationExpression(Operator.ADD, address,
                    new Constant(Reference.class, 1));

            ctx.heapsize = address;
        }
        return address;
    }

    /**
     * Stores a value from the stack into a given index in a given array (both
     * on the stack)
     * 
     * Stack: ..., arrayref, index, value -> ...
     * 
     * @param ctx
     * @param c
     *            class of value
     */
    public static void xAStore(Context ctx, Class<?> c) {
        Class<?> stackType = Typeconversions.getStackType(c);
        Expression value = reqStackParams(ctx, 1, stackType)[0];
        // Convert value from bigger stack type to smaller heap type
        Expression typedValue = Typeconversions.castToType(value, c);

        Expression index = reqStackParams(ctx, 1, int.class)[0];
        Expression arrayref = reqStackParams(ctx, 1, Reference.class)[0];
        OperationExpression combinedAddr = new OperationExpression(Operator.ADD,
                index, arrayref);
        // Add 2 because the length of the array is at the 1st position
        // And the type is at the 0th
        combinedAddr = new OperationExpression(Operator.ADD, combinedAddr,
                new Constant(Reference.class, 2));
        ctx.heap = new OperationExpression(Operator.PUT, ctx.heap, combinedAddr,
                typedValue);
    }

    /**
     * Load a value from a given index in a given array (both on the stack) onto
     * the Stack
     * 
     * Stack: ..., arrayref, index -> ..., value
     * 
     * @param ctx
     * @param c
     *            class of value
     */
    public static void xALoad(Context ctx, Class<?> c) {
        Expression index = reqStackParams(ctx, 1, int.class)[0];
        Expression arrayref = reqStackParams(ctx, 1, Reference.class)[0];
        OperationExpression combinedAddr = new OperationExpression(Operator.ADD,
                index, arrayref);
        // Add 2 because the length of the array is at the 1st position
        // And the type is at the 0th
        combinedAddr = new OperationExpression(Operator.ADD, combinedAddr,
                new Constant(Reference.class, 2));
        Expression get = new OperationExpression(Operator.GET, c, ctx.heap,
                combinedAddr);
        // Convert the result to a bigger type if smaller than int
        Expression typedGet = Typeconversions.toStackType(get);
        ctx.stack.push(typedGet);
    }

    /**
     * Create a subArray for a multidimensional Array
     * 
     * @param ctx
     * @param c
     *            the Class of the subArray
     * @param dimLengths
     *            the number of Elements in each dimension of the
     *            multidimensional Array
     * @param superAddress
     *            the address of the superArray
     * @param dimensions
     *            the number of dimensions in this subArray (each subArray has 1
     *            dimension less than its superArray)
     * @param superElement
     *            the index of the element in the superArray where the address
     *            of this array will be stored.
     */
    public static void addSubArray(Context ctx, Class<?> c,
            Expression[] dimLengths, Expression superAddress, int dimensions,
            int superElement) {
        if (dimensions == 0) {
            return;
        }
        Expression address = ctx.heapsize;
        Expression superLength = dimLengths[dimLengths.length - dimensions - 1];
        Expression length = dimLengths[dimLengths.length - dimensions];

        OperationExpression superElemAdd = new OperationExpression(Operator.ADD,
                new Constant(Reference.class, superElement + 1), superAddress);
        // fills the elements of the superArray with references to this subArray
        ctx.heap = new OperationExpression(Operator.PUT, ctx.heap, superElemAdd,
                address);

        if (length instanceof Constant && superLength instanceof Constant) {
            Constant cLength = (Constant) length;
            int aLength = (int) cLength.getNumber();
            // if this is the lowest level of the multidimensional array,
            // multidimensional primitive arrays need to be initialized with 0
            if (dimensions == 1) {
                switch (c.getName()) {
                case "[[I": // int
                    appendArray(ctx, aLength, int.class, int[].class);
                    break;
                case "[[S": // short
                    appendArray(ctx, aLength, short.class, short[].class);
                    break;
                case "[[B": // byte
                    appendArray(ctx, aLength, byte.class, byte[].class);
                    break;
                case "[[C": // char
                    appendArray(ctx, aLength, char.class, char[].class);
                    break;
                case "[[Z": // boolean
                    appendArray(ctx, aLength, boolean.class, boolean[].class);
                    break;
                case "[[D": // double
                    appendArray(ctx, aLength, double.class, double[].class);
                    break;
                case "[[F": // float
                    appendArray(ctx, aLength, float.class, float[].class);
                    break;
                case "[[J": // long
                    appendArray(ctx, aLength, long.class, long[].class);
                    break;
                default:
                    appendArray(ctx, aLength, Reference.class,
                            Typeconversions.toArrayType(c));
                    break;
                }
            } else {
                appendArray(ctx, aLength, Reference.class,
                        Typeconversions.toMultiArrayType(c, dimensions));
            }

            Constant cSuperLength = (Constant) superLength;
            // last Element in superArray reached. Initialize next subArray
            if ((int) cSuperLength.getValue() - 1 <= superElement) {
                addSubArray(ctx, c, dimLengths, address, dimensions - 1, 0);
            } else {
                // last Element in superArray not yet reached. Initialize
                // next Element in superArray.
                addSubArray(ctx, c, dimLengths, superAddress, dimensions,
                        superElement + 1);
            }
            // if length or superLength are not constant, try symbolically
            // allocating Heap spaces
        } else {
            // Add 1 to the first length to account for the additional spot
            // needed to display the length of each (sub-)array.
            Expression fullLength = new OperationExpression(Operator.ADD,
                    dimLengths[0], new Constant(1));
            for (int i = 1; i < dimLengths.length - dimensions + 1; i++) {
                fullLength = new OperationExpression(Operator.MUL,
                        dimLengths[i], fullLength);
            }
            appendArraySpace(ctx, fullLength, Reference.class,
                    Typeconversions.toMultiArrayType(c, dimensions));
            putSymArrayRefs(ctx, superAddress, address, length);
            addSubArray(ctx, c, dimLengths, address, dimensions - 1, 0);
        }
    }

    public static void putSymArrayRefs(Context ctx, Expression superAddress,
            Expression address, Expression length) {
        for (int i = 1; i < 5; i++) {
            OperationExpression elemAdd = new OperationExpression(Operator.ADD,
                    superAddress, new Constant(Reference.class, i));
            OperationExpression condition = new OperationExpression(
                    Operator.LESS, elemAdd, address);
            OperationExpression get = new OperationExpression(Operator.GET,
                    ctx.heap, elemAdd);
            OperationExpression ite = new OperationExpression(Operator.ITE,
                    condition, new OperationExpression(Operator.MUL,
                            new Constant(Reference.class, i), address),
                    get);
            ctx.heap = new OperationExpression(Operator.PUT, ctx.heap, elemAdd,
                    ite);
        }
    }

    /**
     * Put the length of an array of not constant length to the current address
     * and add the length of that array to the heapsize.
     * 
     * @param ctx
     * @param aLength
     * @param c
     * @return
     */
    public static Expression appendArraySpace(Context ctx, Expression aLength,
            Class<?> c, Class<?> arrayType) {
        Expression address = ctx.heapsize;
        Expression runtimeType = new Constant(RuntimeType.class,
                ctx.getTypeId(arrayType));
        ctx.heap = new OperationExpression(Operator.PUT, ctx.heap, address,
                runtimeType);
        address = new OperationExpression(Operator.ADD, address,
                new Constant(Reference.class, 1));
        ctx.heap = new OperationExpression(Operator.PUT, ctx.heap, address,
                aLength);
        address = new OperationExpression(Operator.ADD, address,
                new Constant(Reference.class, 1));
        address = new OperationExpression(Operator.ADD, address, aLength);
        ctx.heapsize = address;
        return address;
    }

    /**
     * Compares 2 numbers and pushes 1 on the stack if val1 is bigger, 0 if they
     * are equal and -1 if val2 is bigger. If either of them are NaN, nanValue
     * is pushed on the stack instead.
     * 
     * NaN checking is only possible if both values are Constants (theoretically
     * one is enough, but that would be too much code for too little solving
     * capabilities gained).
     * 
     * @param ctx
     *            the used Context
     * @param c
     *            the class of the instruction (double for DCMPG/DCMPL, float
     *            for FCMPG/FCMPL, long for LCMP)
     * @param nanValue
     *            the value to be pushed onto the stack if a value is NaN(1 for
     *            DCMPG/FCMPG and -1 for DCMPL/FCMPL)
     */
    public static void compare(Context ctx, Class<?> c, int nanValue) {
        Expression val2 = reqStackParams(ctx, 1, c)[0];
        Expression val1 = reqStackParams(ctx, 1, c)[0];

        // NaN checking if both values are Constants and Class is not Long
        if (!c.equals(Long.class) && val1 instanceof Constant
                && val2 instanceof Constant) {
            Constant cVal2 = (Constant) val2;
            Constant cVal1 = (Constant) val1;
            if (Double.isNaN((double) cVal2.getValue())
                    || Double.isNaN((double) cVal1.getValue())) {
                ctx.stack.push(new Constant(nanValue));
                return;
            }
        }
        // Will be replaced in a later transform
        ctx.stack.push(new OperationExpression(Operator.COMPARE, val1, val2));
    }

    /**
     * returns from an invoked method to the invoking method
     * 
     * @param ctx
     * @param returnValue
     */
    public static void invReturn(Context ctx, Expression returnValue) {
        Context retCtx = ctx.previousFrame;

        SEFunction f = retCtx.getSEFunctionforPC(retCtx.nextAddress);
        f.register(ctx.invoker);
        ctx.invoker.expression = new FunctionCall.ReturnFunctionCall(f, ctx,
                returnValue);
    }

    /**
     * Generates an call expression that calls the correct method depending on
     * the type of the object it is called on (you know, virtual calls)
     * 
     * @param ctx
     *            current Context
     * @param mref
     *            Method reference
     * @param params
     *            Parameters passed to the call
     * @return
     */
    public static Expression virtualCall(Context ctx, MethodRef mref,
            Expression[] params) {
        Expression object = params[0];
        Expression chainedif = new Constant(false);
        List<Class<?>> usedClasses = new ArrayList<>(ctx.getUsedTypes());
        for (Class<?> c : usedClasses) {
            if (c.isArray()) {
                continue;
            }
            DMethod m = ClassDisassembler.disassembleVirtualMethod(mref,
                    c.getName());
            if (m == null) {
                continue;
            }
            ConstructedMethodRef cmref = new ConstructedMethodRef(mref,
                    m.getClassname());
            int typeid = ctx.getTypeId(c);
            SEFunction f = ctx.getSEFunctionForMethod(cmref, params);
            f.register(ctx.invoker);

            FunctionCall fc = new FunctionCall.InvokeFunctionCall(f, ctx,
                    params);
            Expression typeeq = new OperationExpression(Operator.EQUAL,
                    new OperationExpression(Operator.GET, ctx.heap, object),
                    new Constant(RuntimeType.class, typeid));
            chainedif = new OperationExpression(Operator.ITE, typeeq, fc,
                    chainedif);
        }
        return chainedif;
    }

    /**
     * invokes a method
     * 
     * @param ctx
     * @param instr
     */
    public static void invoke(Context ctx, Instruction instr,
            boolean staticInvoke) {
        try {
            // index into the ConstantPool
            int index = instr.getUnsignedShort(1);

            ConstantPool cp = ctx.getConstantPool();
            MethodRef mref = new MethodRef((CPRefInfo) cp.get(index), cp);

            int paramNr = mref.getNumberOfParameters();
            // Add the object reference if the invoke is not static
            if (!staticInvoke) {
                paramNr += 1;
            }

            // the number of parameters needed off the stack equals the number
            // of parameters for the method call
            // TODO: type of the parameters
            Expression[] params = reqStackParams(ctx, paramNr, Undef.class);

            Expression[] rparams = new Expression[params.length];
            for (int i = 0; i < params.length; ++i) {
                rparams[i] = params[params.length - 1 - i];
            }

            // Special handling is needed for java.lang.Object
            if (mref.getClassname().equals("java.lang.Object")) {
                CONSTANT_NameAndType_info ntInfo = mref.ref
                        .getNameAndTypeInfo();
                String methodName = ntInfo.getName();
                if (methodName.equals("<init>")) {
                    // not needed for our implementation, needs to be caught so
                    // there will not be an infinite loop
                    return;
                } else if (methodName.equals("hashCode")) {
                    Expression exp = new OperationExpression(Operator.TO_INT,
                            rparams[0]);
                    ctx.stack.push(exp);
                    FunctionCall fc = getJumpToTargetFunction(
                            instr.length() + instr.getPC(), ctx);
                    ctx.invoker.expression = fc;
                    return;
                } else if (methodName.equals("getClass")) {
                    // runtime type information are at offset 0 from object
                    // reference
                    // this only returns our runtime representation, so a
                    // comparison to e.g. int.class will fail
                    OperationExpression getType = new OperationExpression(
                            Operator.GET, RuntimeType.class, ctx.heap,
                            rparams[0]);
                    ctx.stack.push(getType);
                    FunctionCall fc = getJumpToTargetFunction(
                            instr.length() + instr.getPC(), ctx);
                    ctx.invoker.expression = fc;
                    return;
                } else if (methodName.equals("equals")) {
                    // the iatfs Object implementation of equals should be
                    // possible, but it doesn't work for some reason, so this is
                    // also treated as a special case.
                    // this also does not work in every case
                    OperationExpression equal = new OperationExpression(
                            Operator.EQUAL, rparams[0], rparams[1]);
                    ctx.stack.push(Typeconversions.toStackType(equal));
                    FunctionCall fc = getJumpToTargetFunction(
                            instr.length() + instr.getPC(), ctx);
                    ctx.invoker.expression = fc;
                    return;
                }
            }

            // For invokevirtual, interface and special (special onyl if it is
            // not a constructor)
            if (!staticInvoke && !mref.isConstructor()) {
                ctx.invoker.expression = virtualCall(ctx, mref, rparams);
            } else {
                SEFunction f = ctx.getSEFunctionForMethod(mref, rparams);
                f.register(ctx.invoker);
                ctx.invoker.expression = new FunctionCall.InvokeFunctionCall(f,
                        ctx, rparams);
            }

        } catch (Exception e) {
            throw new IncoherentBytecodeException(
                    "Error while invoking a Method: " + e.getMessage());
        }
    }

}
