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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sun.tools.classfile.ClassFile;
import com.sun.tools.classfile.Instruction;

import gps.annotations.Constraint;
import gps.bytecode.disassemble.ClassDisassembler;
import gps.bytecode.disassemble.DBlock;
import gps.bytecode.disassemble.DMethod;
import gps.bytecode.disassemble.MethodRef;
import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Expression;
import gps.bytecode.expressions.FunctionCall;
import gps.bytecode.expressions.Operator.Heap;
import gps.bytecode.expressions.Operator.Reference;
import gps.bytecode.expressions.Variable;
import gps.bytecode.symexec.HeapContext.InsertPoint;
import gps.bytecode.symexec.HeapContext.NopInsertPoint;
import gps.util.Tuple;

/**
 *
 * This class is the main entry point to the symbolic execution backend.
 * 
 * @author jowlo@uni-bremen.de
 *
 */
public class SymbolicExec {

    ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    // Maps known SEFunctions for a given bytecodeblock and context to return to
    // at the end of execution
    Map<Tuple<DBlock, Context>, SEFunction> knownSEFuntions = new HashMap<>();

    // Cache for disassembly of Methods
    Map<MethodRef, DMethod> disassembledMethods = new HashMap<>();

    //Original problem instance supplied by the user
    Object thisObject;

    /**
     * Free variables of the problem
     */
    Map<Variable, InsertPoint> freeVariables = new HashMap<>();

    /**
     * Type information
     */
    Map<Class<?>, Integer> typeIds = new HashMap<>();
    Map<Integer, Class<?>> reverseTypeIds = new HashMap<>();
    Map<Integer, List<Integer>> inheritMap = new HashMap<>();
    int nextId = 0;

    /**
     * Main entry point to the symbolic execution backend. To start building a
     * functional representation of a class, constructFunctional is called to
     * analyze a specific object.
     * 
     * From this object local variables are gathered, the class of this object
     * and the bytecode of methods is retrieved. The dissembling packages takes
     * care of reading in these Methods, relevant constants and grouping the
     * individual instructions from method into executable blocks (without
     * conditional jumps, branches or equivalent).
     * 
     * These Blocks are then transformed into functional representations, these
     * are chained and returned as a SESatisfactionProblem, which itself
     * contains functional representations of all relevant instruction sets to
     * be parsed into backend solver datastructure by an adapter class.
     * 
     * @param o
     *            An object on which to start gathering constants, annotations
     *            and code to transform into a functional representation.
     * @return An object representing the symbolic execution of this method.
     * 
     *         TODO: Exactly clarify the SESatisfactionProblem class's role.
     */
    public SatisfactionProblem constructFunctional(final Object o) {
        thisObject = o;

        final Class<?> problemClass = o.getClass();
        classLoader = problemClass.getClassLoader();

        ClassFile cf = ClassDisassembler.getClassFile(problemClass.getName());

        if (cf == null) {
            cf = ClassDisassembler.getClassFile(problemClass);
        }

        final List<DMethod> methods = ClassDisassembler.disassembleMethods(cf);
        methods.removeIf((m) -> !m.hasAnnotation(Constraint.class));

        final List<SEFunction> constraintFunctions = new ArrayList<>();
        for (DMethod m : methods) {
            constraintFunctions.add(constructConstraint(m));
        }

        // Update parameters of all functions:
        for (SEFunction knownSEFunction : knownSEFuntions.values()) {
            knownSEFunction.notify(null);
        }

        // Register parameters
        for (SEFunction constraint : constraintFunctions) {
            for (Variable v : constraint.getParameters()) {
                if (!freeVariables.containsKey(v)) {
                    Variable nv = new Variable(constraint, v.toString(),
                            v.getType());
                    freeVariables.put(nv, new NopInsertPoint());
                }
            }
        }

        return new SatisfactionProblem(constraintFunctions, freeVariables);

    }

    public static SatisfactionProblem constructFunctionalSatisfactionProblem(
            final Object o) {
        SymbolicExec SE = new SymbolicExec();
        return SE.constructFunctional(o);
    }

    /**
     * Symbolically executes a single basic block
     * 
     * @param block
     * @return
     */
    public SEFunction constructFunction(final DBlock block, Context oldCtx) {
        SEFunction f = new SEFunction(block, this, oldCtx.previousFrame);
        Context ctx = f.executionContext;

        // Set up heap variables
        f.heapParams.put(1, new Variable("heap", Heap.class));
        f.heapParams.put(2, new Variable("heapsize", Reference.class));

        ctx.heap = new Variable("heap", Heap.class);
        ctx.heapsize = new Variable("heapsize", Reference.class);

        symExecFunction(f, ctx);
        return f;
    }

    /**
     * 
     * @param pMethod
     *            the DMethod that represents the invoked Method
     * @param pParams
     *            the parameters for the invoked Method
     * @param oldCtx
     *            the context from which the Method was invoked. Needed to
     *            return to the invoking Method.
     * @return the SEFunction for the first Block of the invoked Method
     */
    public SEFunction constructInvFunction(final DMethod pMethod,
            final Expression[] pParams, Context oldCtx) {
        SEFunction f = new SEFunction(pMethod.getBlock(0), this, oldCtx);
        Context ctx = f.executionContext;

        f.heapParams.put(1, new Variable("heap", Heap.class));
        f.heapParams.put(2, new Variable("heapsize", Reference.class));

        ctx.heap = new Variable("heap", Heap.class);
        ctx.heapsize = new Variable("heapsize", Reference.class);

        for (int i = 0; i < pParams.length; ++i) {
            ctx.locals[i] = f
                    .requestLocalVariableParameter(pParams[i].getType(), i);
        }

        symExecFunction(f, ctx);
        return f;
    }

    /**
     * Symbolically execute a constraint method
     * 
     * @param method
     * @return
     */
    public SEFunction constructConstraint(final DMethod method) {
        SEFunction f = new SEFunction(method.getBlock(0), this, null);
        Context ctx = f.executionContext;
        f.firstFunction = true;

        // Set up empty heap and insert the problem object
        HeapContext hc = new HeapContext();
        // the first function needs to know the HeapContext to get static
        // Objects from the ClassFile
        f.hc = hc;

        ctx.heap = new Constant(Heap.class, new ArrayList<>());
        ctx.heapsize = new Constant(Reference.class, 0);
        ctx.locals[0] = hc.addObjectToHeap(ctx, thisObject);

        for (Entry<Variable, InsertPoint> var : hc.insertVarMap.entrySet()) {
            freeVariables.put(var.getKey(), var.getValue());
        }

        symExecFunction(f, ctx);
        return f;
    }

    /**
     * Symbolically executes the bytecode instructions in a SEFunction
     * 
     * @param f
     * @param ctx
     */
    public void symExecFunction(SEFunction f, Context ctx) {
        DBlock block = f.dBlock;
        this.knownSEFuntions.put(new Tuple<>(block, ctx.previousFrame), f);
        for (Instruction instr : block.instructions) {
            FunctionalMapping.mapping.getOrDefault(instr.getOpcode(),
                    (lCtx, someInstruction) -> {
                        DMethod meth = lCtx.invoker.dBlock.parentMethod;
                        String location = meth.getClassname() + "."
                                + meth.getName() + "():"
                                + someInstruction.getPC();
                        throw new UnsupportedOperationException(
                                instr.getOpcode() + " at " + location);
                    }).map(ctx, instr);
        }

        // If the SEFunction had no proper jump expression at its end
        if (f.expression == null) {
            Instruction instr = block.instructions
                    .get(block.instructions.size() - 1);
            int noJumpAdress = instr.getPC() + instr.length();
            FunctionCall noJump = FunctionalMapping
                    .getJumpToTargetFunction(noJumpAdress, ctx);
            f.expression = noJump;
        }
    }
}
