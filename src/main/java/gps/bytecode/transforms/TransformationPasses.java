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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Expression;
import gps.bytecode.expressions.FunctionCall;
import gps.bytecode.expressions.IExpressionVisitor.IDefaultExpressionVisitor;
import gps.bytecode.expressions.IExpressionVisitor;
import gps.bytecode.expressions.OperationExpression;
import gps.bytecode.expressions.Operator;
import gps.bytecode.expressions.Operator.Heap;
import gps.bytecode.expressions.Operator.Reference;
import gps.bytecode.expressions.Operator.Undef;
import gps.bytecode.expressions.ProcessedFunctionCall;
import gps.bytecode.expressions.Variable;
import gps.bytecode.symexec.SEFunction;
import gps.bytecode.symexec.SatisfactionProblem;
import gps.bytecode.transforms.PartialEvaluation.PartialCallEvaluator;
import gps.bytecode.transforms.PartialEvaluation.VariableReplacer;
import gps.bytecode.symexec.Typeconversions;
import gps.util.Tuple;

/**
 * 
 * @author mfunk@tzi.de
 *
 */
public class TransformationPasses {

    /**
     * A TransformationPass analyzes a collection of SEFunctions (in most cases
     * all of them) and creates an ExpressionTransformer that can be applied to
     * Expressions to transform them with the knowledge of the Transformation
     * Pass
     * 
     * @author mfunk@tzi.de
     */
    static interface ITransformationPass {
        /**
         * The PreTransform function is called on a SEFunction before
         * SEFunctions it calls are transformed
         * 
         * @return
         */
        public default Consumer<SEFunction> getPreTransformFunction() {
            return (e) -> {
            };
        }

        /**
         * The PostTransform function is called on a SEFunction after all
         * SEFunctions it calls are transformed
         * 
         * @return
         */
        public default Consumer<SEFunction> getPostTransformFunction() {
            return (e) -> {
            };
        }

        /**
         * onMaxDepth Function is called on a SEFunction after reaching the step
         * limit
         * 
         * @return
         */
        public default Consumer<SEFunction> getOnMaxDepthFunction() {
            return (e) -> {
            };
        }

        /**
         * Create a ProblemTransformer from the TransformationPass
         * 
         * @return
         */
        public default ProblemTransformer getProblemTransformer() {
            return new ProblemTransformer(this);
        }
    }

    /**
     * 
     * Utilityfunction that inlines a functiocall into a expression
     * 
     * @param call
     * @return
     */
    public static Expression inlineCall(ProcessedFunctionCall call) {
        Map<String, Expression> mapping = new HashMap<>();
        List<Variable> paramVars = new ArrayList<>(
                call.getTargetFunction().getParameters());
        List<Expression> paramValues = new ArrayList<>(call.getParameters());
        for (int i = 0; i < call.getParameters().size(); ++i) {
            mapping.put(paramVars.get(i).toString(), paramValues.get(i));
        }

        VariableReplacer vr = new VariableReplacer(mapping);
        ExpressionTransformer et = new ExpressionTransformer(vr);
        return et.transform(call.getTargetFunction().asExpression());
    }

    /**
     * Inlines Functions that are identity functions
     * 
     * @author mfunk@tzi.de
     */
    public static class InlineId implements ITransformationPass {

        static boolean isIdentityFunction(SEFunction p) {
            Expression e = p.asExpression();
            // Only expression is a variable
            if (!(e instanceof Variable)) {
                return false;
            }
            Variable v = (Variable) e;
            // Just one parameter
            if (p.getParameters().size() != 1) {
                return false;
            }
            // Parameter is the only variable in expression
            return p.getParameters().iterator().next().toString()
                    .equals(v.toString());
        }

        @Override
        public Consumer<SEFunction> getPostTransformFunction() {
            Function<Expression, Expression> removeIdentities = (
                    Expression e) -> {
                if (e instanceof ProcessedFunctionCall) {
                    ProcessedFunctionCall pfc = (ProcessedFunctionCall) e;
                    if (isIdentityFunction(pfc.getTargetFunction())) {
                        // Replace the functioncall with its parameter
                        return pfc.getParameters().iterator().next();
                    }
                }
                return e;
            };
            return new ExpressionTransformer(removeIdentities);
        }

    }

    /**
     * Inlines Functions that only consist of a tail call (there are no
     * operations perfomed after the call, just before)
     * 
     * @author mfunk
     */
    public static class InlineTailCall implements ITransformationPass {
        static boolean hasTailCall(SEFunction p) {
            Expression e = p.asExpression();
            return e instanceof FunctionCall
                    || e instanceof ProcessedFunctionCall;
        }

        @Override
        public Consumer<SEFunction> getPostTransformFunction() {
            Function<Expression, Expression> inlineTailCalls = (e) -> {
                if (!(e instanceof ProcessedFunctionCall)) {
                    return e;
                }
                ProcessedFunctionCall pfc = (ProcessedFunctionCall) e;
                if (!hasTailCall(pfc.getTargetFunction())) {
                    return e;
                }
                e = inlineCall(pfc);
                return e;
            };
            return new ExpressionTransformer(inlineTailCalls);
        }

    }

    static class InlineTailCall2 implements ITransformationPass {
        @Override
        public Consumer<SEFunction> getPostTransformFunction() {
            return (f) -> {
                Expression e = f.asExpression();
                if (!(e instanceof ProcessedFunctionCall)) {
                    return;
                }
                ProcessedFunctionCall pfc = (ProcessedFunctionCall) e;
                f.setExpression(inlineCall(pfc));
            };
        }

    }

    /**
     * Inlines constants
     * 
     * @author mfunk@tzi.de
     */
    public static class InlineConstants implements ITransformationPass {
        static boolean isConstant(SEFunction f) {
            // A function without parameters is a constant
            return f.asExpression() instanceof Constant
                    || f.getParameters().size() == 0;
        }

        @Override
        public Consumer<SEFunction> getPostTransformFunction() {
            Function<Expression, Expression> inlineConstants = (
                    Expression e) -> {
                if (e instanceof ProcessedFunctionCall) {
                    ProcessedFunctionCall pfc = (ProcessedFunctionCall) e;
                    if (isConstant(pfc.getTargetFunction())) {
                        // Replace the function call with the expression of the
                        // function
                        return pfc.getTargetFunction().asExpression();
                    }
                }
                return e;
            };
            return (new ExpressionTransformer(inlineConstants))
                    .andThen(new ExpressionTransformer(
                            ConstantExpressionEvaluator::eval));
        }

    }

    /**
     * Wraps constant evaluation in a transformation pass
     * 
     * @author mfunk@tzi.de
     */
    public static class ConstantEval implements ITransformationPass {
        @Override
        public Consumer<SEFunction> getPostTransformFunction() {
            return new ExpressionTransformer(ConstantExpressionEvaluator::eval);
        }

    }

    /**
     * A transformation pass that removes unused parameters from function calls
     * and definitions
     * 
     * @author mfunk@tzi.de
     *
     */
    public static class RemoveUnusedParameters implements ITransformationPass {
        static class UnusedVisitor implements IDefaultExpressionVisitor {
            Set<String> used = new HashSet<>();

            @Override
            public void visitVariableExpression(Variable v) {
                used.add(v.toString());
            }
        }

        static Set<Integer> getUnusedParameters(SEFunction f) {
            Set<Integer> unused = new HashSet<>();
            UnusedVisitor uv = new UnusedVisitor();
            f.asExpression().accept(uv);
            for (int i = 0; i < f.getParameters().size(); ++i) {
                if (!uv.used.contains(f.getParameters().get(i).toString())) {
                    unused.add(i);
                }
            }
            return unused;
        }

        static SEFunction generateNewVersion(SEFunction f,
                Set<Integer> unusedParameters) {
            if (unusedParameters.size() == 0) {
                return f;
            }
            ArrayList<Variable> newArgList = new ArrayList<>();
            for (int i = 0; i < f.getParameters().size(); ++i) {
                if (!unusedParameters.contains(i)) {
                    newArgList.add(f.getParameters().get(i));
                }
            }
            return new SEFunction(f.getName(), f.asExpression(), newArgList);
        }

        @Override
        public Consumer<SEFunction> getPostTransformFunction() {
            Map<SEFunction, Set<Integer>> unusedParameters = new HashMap<>();
            Map<SEFunction, SEFunction> newVersions = new HashMap<>();

            Function<Expression, Expression> removeUnused = (Expression e) -> {
                if (!(e instanceof ProcessedFunctionCall)) {
                    return e;
                }
                ProcessedFunctionCall pfc = (ProcessedFunctionCall) e;
                SEFunction f = pfc.getTargetFunction();
                if (!unusedParameters.containsKey(f)) {
                    Set<Integer> unused = getUnusedParameters(f);
                    unusedParameters.put(f, unused);
                    if (unused.size() > 0) {
                        newVersions.put(f, generateNewVersion(f, unused));
                    }
                }

                if (!newVersions.containsKey(f)) {
                    return e;
                }

                List<Expression> params = pfc.getParameters();
                List<Expression> newParams = new ArrayList<>();
                Set<Integer> unused = unusedParameters.get(f);
                for (int i = 0; i < params.size(); ++i) {
                    if (unused.contains(i)) {
                        continue;
                    }
                    newParams.add(params.get(i));
                }
                return new ProcessedFunctionCall(newVersions.get(f), newParams);
            };

            return new ExpressionTransformer(removeUnused);
        }

    }

    /**
     * Replaces put/get Operators with z3 compatible nested ITEs
     * 
     * @author mfunk@tzi.de
     *
     */
    static class ReplacePutGet implements ITransformationPass {

        static Expression transformGet(OperationExpression op) {
            Expression heap = op.getParameters().get(0);
            if (!(heap instanceof Constant)) {
                return op;
            }
            List<Expression> heapList = new ArrayList<>(
                    Constant.getHeap((Constant) heap));
            Expression selector = op.getParameters().get(1);
            Expression result = Typeconversions
                    .getDefaultInvalidValue(op.getType());
            for (int i = 0; i < heapList.size(); ++i) {
                Expression condition = new OperationExpression(Operator.EQUAL,
                        selector, new Constant(Reference.class, i));

                // This prunes all heap entries with incorrect type
                if (heapList.get(i).getType() == Undef.class
                        || heapList.get(i).getType() == op.getType()) {
                    result = new OperationExpression(Operator.ITE, op.getType(),
                            condition, heapList.get(i), result);
                }
            }
            return result;
        }

        static Expression transformPut(OperationExpression op) {
            Expression heap = op.getParameters().get(0);
            if (!(heap instanceof Constant)) {
                return op;
            }
            List<Expression> heapList = new ArrayList<>(
                    Constant.getHeap((Constant) heap));

            Expression selector = op.getParameters().get(1);
            Expression newValue = op.getParameters().get(2);

            for (int index = 0; index < heapList.size(); ++index) {
                Expression oldValue = heapList.get(index);
                Expression correctAddress = new OperationExpression(
                        Operator.EQUAL, selector,
                        new Constant(Reference.class, index));
                Expression ifeq = new OperationExpression(Operator.ITE,
                        correctAddress, newValue, oldValue);
                heapList.set(index, ifeq);
            }

            return new Constant(Heap.class, heapList);
        }

        @Override
        public Consumer<SEFunction> getPostTransformFunction() {
            Function<Expression, Expression> transform = (Expression e) -> {

                if (!(e instanceof OperationExpression)) {
                    return e;
                }
                OperationExpression op = (OperationExpression) e;
                if (op.getOperator() == Operator.PUT) {
                    return transformPut(op);
                } else if (op.getOperator() == Operator.GET) {
                    return transformGet(op);
                } else {
                    return op;
                }
            };

            return new ExpressionTransformer(transform);
        }
    }

    /**
     * Replace boolean ite with and/or
     * 
     * @author mfunk@tzi.de
     *
     */
    public static class ReplaceITE implements ITransformationPass {
        @Override
        public Consumer<SEFunction> getPostTransformFunction() {
            final Function<Expression, Expression> id = (e) -> {
                if (!(e instanceof OperationExpression)) {
                    return e;
                }
                if (e.getType() != boolean.class) {
                    return e;
                }
                OperationExpression op = (OperationExpression) e;
                if (op.getOperator() != Operator.ITE) {
                    return e;
                }
                Expression p0 = op.getParameters().get(0);
                Expression notp0 = new OperationExpression(Operator.NOT, p0);
                Expression p1 = op.getParameters().get(1);
                Expression p2 = op.getParameters().get(2);
                OperationExpression fh = new OperationExpression(Operator.BAND,
                        p0, p1);
                OperationExpression sh = new OperationExpression(Operator.BAND,
                        notp0, p2);
                return new OperationExpression(Operator.BOR, fh, sh);
            };
            return new ExpressionTransformer(id);

        }
    }

    /**
     * Transforms the Problem from Int returntypes to Boolean. This assumes that
     * the functions were generated by the current version of symbolic exec, so
     * that the returnpath is always int
     * 
     * @author mfunk@tzi.de
     *
     */
    public static class BooleanTransform implements ITransformationPass {

        @Override
        public Consumer<SEFunction> getPostTransformFunction() {
            Consumer<SEFunction> bt = (f) -> {
                Expression e = f.asExpression();
                if (e instanceof Constant) {
                    Constant c2 = (Constant) e;
                    if (c2.getNumber().intValue() == 1) {
                        e = new Constant(boolean.class, true);
                    } else if (c2.getNumber().intValue() == 0) {
                        e = new Constant(boolean.class, false);
                    }
                    f.setExpression(e);
                    return;
                }

                if (!(e instanceof OperationExpression)) {
                    return;
                }
                OperationExpression op = (OperationExpression) e;
                if (op.getOperator() != Operator.ITE) {
                    return;
                }
                Expression p1 = op.getParameters().get(1);
                if (p1.getType() == int.class
                        && !PartialEvaluation.isRecursive(p1)) {
                    // Convert JVM booleans to our booleans
                    p1 = new OperationExpression(Operator.EQUAL, p1,
                            new Constant(int.class, 1));
                }

                Expression p2 = op.getParameters().get(2);
                if (p2.getType() == int.class
                        && !PartialEvaluation.isRecursive(p1)) {
                    // Convert JVM booleans to our booleans
                    p2 = new OperationExpression(Operator.EQUAL, p2,
                            new Constant(int.class, 1));
                }
                f.setExpression(new OperationExpression(Operator.ITE,
                        op.getParameters().get(0), p1, p2));
            };
            return new ExpressionTransformer(Function.identity()).andThen(bt);
        }
    }

    /**
     * Inlines all functioncalls of functions that do not start with an "and"
     * operator
     * 
     * "and" was choosen arbitrarily, but provides acceptable results for many
     * problems
     */
    static class InlineNotAnd implements ITransformationPass {
        @Override
        public Consumer<SEFunction> getPostTransformFunction() {
            final Function<Expression, Expression> inlineTailCalls = (
                    Expression e) -> {
                if (!(e instanceof ProcessedFunctionCall)) {
                    return e;
                }
                ProcessedFunctionCall pfc = (ProcessedFunctionCall) e;
                Expression exp = pfc.getTargetFunction().asExpression();
                if (exp instanceof OperationExpression) {
                    OperationExpression o = (OperationExpression) exp;
                    if (o.getOperator() != Operator.BAND) {
                        e = inlineCall(pfc);
                    }
                }
                return e;
            };
            return new ExpressionTransformer(inlineTailCalls);
        }

    }

    static class InlineNotITE implements ITransformationPass {
        @Override
        public Consumer<SEFunction> getPostTransformFunction() {
            final Function<Expression, Expression> inlineTailCalls = (
                    Expression e) -> {
                if (!(e instanceof ProcessedFunctionCall)) {
                    return e;
                }
                ProcessedFunctionCall pfc = (ProcessedFunctionCall) e;
                Expression exp = pfc.getTargetFunction().asExpression();
                if (exp instanceof OperationExpression) {
                    OperationExpression o = (OperationExpression) exp;
                    if (o.getOperator() != Operator.ITE) {
                        e = inlineCall(pfc);
                    }
                }
                return e;
            };
            return new ExpressionTransformer(inlineTailCalls);
        }

    }

    /**
     * Wraps partial evaluation as a transformationpass
     * 
     * @author mfunk@tzi.de
     *
     */
    static class PartiallyEvaluate implements ITransformationPass {

        @Override
        public Consumer<SEFunction> getPreTransformFunction() {
            return new ExpressionTransformer(new PartialCallEvaluator());
        }

        @Override
        public Consumer<SEFunction> getOnMaxDepthFunction() {
            return (f) -> {
                System.out.println(
                        "Warning: partial evalutation hit max depth. Result may be incorrect!");
                Expression e = f.asExpression();
                if (!(e instanceof OperationExpression)) {
                    return;
                }
                OperationExpression op = (OperationExpression) e;
                if (op.getOperator() != Operator.ITE) {
                    return;
                }
                Expression p0 = op.getParameters().get(0);
                Expression p1 = op.getParameters().get(1);
                Expression p2 = op.getParameters().get(2);

                boolean p1Recursive = PartialEvaluation.isRecursive(p1);
                boolean p2Recursive = PartialEvaluation.isRecursive(p2);
                if (p1Recursive) {
                    p1 = new Constant(false);
                }
                if (p2Recursive) {
                    p2 = new Constant(false);
                }

                f.setExpression(
                        new OperationExpression(Operator.ITE, p0, p1, p2));
            };
        }
    }

    /**
     * A transformation pass that unrolls recursion to a specified depth and eliminates the remaining recursion
     * @author mfunk
     *
     */
    static class UnrollRemainingRecursion implements ITransformationPass {
        final int depth;

        public UnrollRemainingRecursion(int depth) {
            this.depth = depth;
        }

        @Override
        public Consumer<SEFunction> getPostTransformFunction() {
            Map<Tuple<SEFunction, Integer>, SEFunction> unfoldMap = new HashMap<>();

            Function<Expression, Expression> f = (e) -> {
                if (!(e instanceof ProcessedFunctionCall)) {
                    return e;
                }
                ProcessedFunctionCall pfc = (ProcessedFunctionCall) e;
                if (unfoldMap.containsKey(
                        new Tuple<>(pfc.getTargetFunction(), -1))) {
                    return new ProcessedFunctionCall(
                            unfoldMap.get(
                                    new Tuple<>(pfc.getTargetFunction(), -1)),
                            pfc.getParameters());
                }
                if (PartialEvaluation
                        .isStrongRecursive(pfc.getTargetFunction())) {
                    SEFunction nfunction = copyFunction(pfc.getTargetFunction(),
                            depth, unfoldMap);
                    nfunction.recursiveFlag = false;
                    unfoldMap.put(new Tuple<>(pfc.getTargetFunction(), -1),
                            nfunction);
                    return new ProcessedFunctionCall(nfunction,
                            pfc.getParameters());
                } else {
                    pfc.getTargetFunction().recursiveFlag = false;
                }
                return e;
            };
            return new ExpressionTransformer(f);
        }

    }

    static SEFunction copyFunction(SEFunction f, int depth,
            Map<Tuple<SEFunction, Integer>, SEFunction> map) {
        if (depth == 0) {
            Expression e = new Constant(false);
            return new SEFunction(f.getName() + "false", e, f.getParameters());
        }
        final Function<Expression, Expression> copy = (e) -> {
            if (!(e instanceof ProcessedFunctionCall)) {
                return e;
            }
            ProcessedFunctionCall pfc = (ProcessedFunctionCall) e;
            if (map.containsKey(new Tuple<>(pfc.getTargetFunction(), depth))) {
                return new ProcessedFunctionCall(
                        map.get(new Tuple<>(pfc.getTargetFunction(), depth)),
                        pfc.getParameters());
            }
            if (PartialEvaluation.isStrongRecursive(pfc.getTargetFunction())) {
                SEFunction targetcopy = copyFunction(pfc.getTargetFunction(),
                        depth - 1, map);
                targetcopy.recursiveFlag = false;
                map.put(new Tuple<>(pfc.getTargetFunction(), depth),
                        targetcopy);
                return new ProcessedFunctionCall(targetcopy,
                        pfc.getParameters());
            } else {
                return e;
            }
        };
        ExpressionTransformer et = new ExpressionTransformer(copy);
        Expression e = et.transform(f.asExpression());
        return new SEFunction(f.getName() + depth, e, f.getParameters());
    }

    static class RemoveRemainingRecursion implements ITransformationPass {

        @Override
        public Consumer<SEFunction> getPostTransformFunction() {
            return (f) -> {
                Expression e = f.asExpression();
                if (!(e instanceof OperationExpression)) {
                    return;
                }
                OperationExpression op = (OperationExpression) e;
                if (op.getOperator() != Operator.ITE) {
                    return;
                }
                Expression p0 = op.getParameters().get(0);
                Expression p1 = op.getParameters().get(1);
                Expression p2 = op.getParameters().get(2);

                boolean p1Recursive = PartialEvaluation.isRecursive(p1);
                boolean p2Recursive = PartialEvaluation.isRecursive(p2);
                if (p1Recursive) {
                    p1 = new Constant(false);
                }
                if (p2Recursive) {
                    p2 = new Constant(false);
                }
                if (p1Recursive || p2Recursive) {
                    System.out.println("Warning: removing recursion");
                    f.setExpression(
                            new OperationExpression(Operator.ITE, p0, p1, p2));
                } else {

                }
            };
        }

    }

    /**
     * Deduces parameter types by looking at the expressions being used for the
     * parameters
     */
    public static class TypeUndefParams implements ITransformationPass {
        @Override
        public Consumer<SEFunction> getPreTransformFunction() {
            Function<Expression, Expression> typeCalls = (e) -> {
                if (!(e instanceof ProcessedFunctionCall)) {
                    return e;
                }
                ProcessedFunctionCall pfc = (ProcessedFunctionCall) e;
                SEFunction f = pfc.getTargetFunction();
                for (int i = 0; i < pfc.getParameters().size(); ++i) {
                    Variable v = f.getParameters().get(i);
                    if (v.getType() == Undef.class) {
                        v.setType(pfc.getParameters().get(i).getType());
                    }
                }
                return e;
            };
            return new ExpressionTransformer(typeCalls);
        }
    }

    /**
     * Transformation pass that replaces all left over COMPAREs with equivalent
     * ITE instructions that Z3 can understand
     * 
     * @author mfunk@tzi.de
     *
     */
    public static class ReplaceCmp implements ITransformationPass {
        @Override
        public Consumer<SEFunction> getPostTransformFunction() {
            Function<Expression, Expression> transform = (Expression e) -> {

                if (!(e instanceof OperationExpression)) {
                    return e;
                }
                OperationExpression op = (OperationExpression) e;
                if (op.getOperator() != Operator.COMPARE) {
                    return e;
                }
                Expression val1 = op.getParameters().get(0);
                Expression val2 = op.getParameters().get(1);

                // Conditions for the ITE Expressions
                OperationExpression val1Greater = new OperationExpression(
                        Operator.GREATER, val1, val2);
                OperationExpression equal = new OperationExpression(
                        Operator.EQUAL, val1, val2);

                // ITE Expressions representing the semantics of XCMP_
                OperationExpression ifEqual = new OperationExpression(
                        Operator.ITE, equal, new Constant(0), new Constant(-1));
                OperationExpression ifGreater = new OperationExpression(
                        Operator.ITE, val1Greater, new Constant(1), ifEqual);
                return ifGreater;
            };

            return new ExpressionTransformer(transform);
        }
    }

    /**
     * Collect possible return values of a (possibly recursive) SEFunction
     * Set of expressions is provided in map.get(f) on return
     * @param f
     * @param mp
     */
    static void getPossibleReturnValues(SEFunction f,
            Map<SEFunction, Set<Expression>> mp) {
        if (mp.containsKey(f)) {
            return;
        }
        mp.put(f, new HashSet<>());
        List<SEFunction> others = new ArrayList<SEFunction>();

        final IExpressionVisitor visitor = new IExpressionVisitor() {
            @Override
            public void visitVariableExpression(Variable v) {
                mp.get(f).add(v);
            }

            @Override
            public void visitProcessedFunctionCall(ProcessedFunctionCall fc) {
                others.add(fc.getTargetFunction());
            }

            @Override
            public void visitOperatorExpression(OperationExpression op) {
                if (op.getOperator() == Operator.ITE) {
                    op.getParameters().get(1).accept(this);
                    op.getParameters().get(2).accept(this);
                } else {
                    mp.get(f).add(op);
                }
            }

            @Override
            public void visitFunctionCall(FunctionCall fc) {
                others.add(fc.getTargetFunction());
            }

            @Override
            public void visitConstant(Constant c) {
                mp.get(f).add(c);
            }
        };
        f.asExpression().accept(visitor);

        for (SEFunction fu : others) {
            getPossibleReturnValues(fu, mp);
            mp.get(f).addAll(mp.get(fu));
        }
    }

    /**
     * Transformation pass that prunes SEFunction with the onl possible return value of false
     * @author mfunk
     *
     */
    static class PruneFalse implements ITransformationPass {
        @Override
        public Consumer<SEFunction> getPostTransformFunction() {
            return (f) -> {
                HashMap<SEFunction, Set<Expression>> map = new HashMap<>();
                getPossibleReturnValues(f, map);
                Set<Expression> values = map.get(f);
                if (values.size() == 1
                        && values.contains(new Constant(false))) {
                    f.setExpression(new Constant(false));
                }
            };
        }
    }

    /**
     * Transformation pass that removes remaining heap arrays
     * @author mfunk
     */
    static class RemoveRemainingHeap implements ITransformationPass {

        @Override
        public Consumer<SEFunction> getPostTransformFunction() {
            return new ExpressionTransformer((e) -> {
                if (!(e instanceof ProcessedFunctionCall)) {
                    return e;
                }
                ProcessedFunctionCall pfc = (ProcessedFunctionCall) e;
                for (Expression param : pfc.getParameters()) {
                    if (param.getType() == Heap.class) {
                        return new Constant(false);
                    }
                }
                return pfc;
            });
        }
    }

    public static class ReplaceBooleanToInt implements ITransformationPass {

        @Override
        public Consumer<SEFunction> getPostTransformFunction() {
            return new ExpressionTransformer((e) -> {
                if (!(e instanceof OperationExpression)) {
                    return e;
                }
                OperationExpression pfc = (OperationExpression) e;
                if (pfc.getOperator() != Operator.TO_INT) {
                    return pfc;
                }
                Expression e1 = pfc.getParameters().get(0);
                if (e1.getType() == boolean.class) {
                    return new OperationExpression(Operator.ITE, e1,
                            new Constant(1), new Constant(0));
                }

                return pfc;
            });
        }
    }

    /**
     * Applies a set of Transformations to the SatisfactionProblem
     *
     * The satisfaction problem will be modified
     * 
     * @param p
     * @return
     */
    public static void transform(SatisfactionProblem p) {

        List<ITransformationPass> tp = Lists.newArrayList(new ReplaceCmp(),
                new ReplacePutGet(), new TypeUndefParams(),
                new RemoveRemainingHeap(), new ConstantEval(),
                new ReplaceBooleanToInt());

        List<ITransformationPass> reduceSteps = Lists.newArrayList(
                new RemoveUnusedParameters(), new InlineConstants(),
                new InlineId(), new InlineTailCall(), new ConstantEval());

        List<ITransformationPass> TenReduce = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            TenReduce.addAll(reduceSteps);
        }
        Consumer<SatisfactionProblem> tran = combine(tp);

        applySinglePass(p, new BooleanTransform());
        combine(TenReduce).accept(p);
        applySinglePass(p, new PartiallyEvaluate());
        combine(TenReduce).accept(p);
        applySinglePass(p, new PruneFalse());
        combine(TenReduce).accept(p);
        applySinglePass(p, new UnrollRemainingRecursion(5));
        combine(TenReduce).accept(p);
        tran.accept(p);
        combine(TenReduce).accept(p);
        PartialEvaluation.instantiationCache.clear();
    }

    static Consumer<SatisfactionProblem> combine(
            Collection<ITransformationPass> passes) {
        Stream<Consumer<SatisfactionProblem>> st = passes.stream()
                .map(ITransformationPass::getProblemTransformer);
        return st.collect(Collectors.reducing((p1, p2) -> p1.andThen(p2)))
                .get();
    }

    /**
     * Applies a single transformation pass to the given problem
     * 
     * @param p
     * @param pass
     */
    public static void applySinglePass(SatisfactionProblem p,
            ITransformationPass pass) {
        pass.getProblemTransformer().transform(p);
    }

}
