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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Expression;
import gps.bytecode.expressions.IExpressionVisitor.IDefaultExpressionVisitor;
import gps.bytecode.expressions.OperationExpression;
import gps.bytecode.expressions.Operator;
import gps.bytecode.expressions.Operator.Heap;
import gps.bytecode.expressions.Operator.Reference;
import gps.bytecode.expressions.Operator.Undef;
import gps.bytecode.expressions.ProcessedFunctionCall;
import gps.bytecode.expressions.Variable;
import gps.bytecode.symexec.SEFunction;

/**
 * Implements a static method that partially evaluates all SEFunctions in a
 * Satisfactionproblem
 * 
 * @author mfunk@tzi.de
 *
 */
public class PartialEvaluation {
    static Map<String, SEFunction> instantiationCache = new HashMap<>();

    static class IsConstantVisitor implements IDefaultExpressionVisitor {
        boolean constant = true;

        @Override
        public void visitVariableExpression(Variable v) {
            constant = false;
        }

        @Override
        public void visitConstant(Constant c) {
            if (c.getType() == Heap.class) {
                constant = false;
            }
        }
    }

    static class IsRecursiveVisitor implements IDefaultExpressionVisitor {
        private Stack<SEFunction> recStack = new Stack<>();
        boolean recursive = false;
        int maxdepth = -1;

        @Override
        public void visitOperatorExpression(OperationExpression op) {
            for (Expression e : op.getParameters()) {
                e.accept(this);
                if (recursive) {
                    return;
                }
            }
        }

        @Override
        public void visitProcessedFunctionCall(ProcessedFunctionCall fc) {
            for (Expression e : fc.getParameters()) {
                e.accept(this);
                if (recursive) {
                    return;
                }
            }
            if (recStack.contains(fc.getTargetFunction())) {
                recursive = true;
                return;
            }
            if (!fc.getTargetFunction().recursiveFlag) {
                return;
            }

            if (recStack.size() + 1 > maxdepth && maxdepth != -1) {
                return;
            }
            recStack.add(fc.getTargetFunction());
            fc.getTargetFunction().asExpression().accept(this);
            recStack.pop();
        }
    }

    static class IsStrongRecursiveVisitor implements IDefaultExpressionVisitor {
        private Stack<SEFunction> recStack = new Stack<>();
        private Set<SEFunction> visited = new HashSet<>();
        boolean strongrecursive = false;
        int maxdepth = -1;

        @Override
        public void visitOperatorExpression(OperationExpression op) {
            for (Expression e : op.getParameters()) {
                e.accept(this);
                if (strongrecursive) {
                    return;
                }
            }
        }

        @Override
        public void visitProcessedFunctionCall(ProcessedFunctionCall fc) {
            for (Expression e : fc.getParameters()) {
                e.accept(this);
                if (strongrecursive) {
                    return;
                }
            }
            if (visited.contains(fc.getTargetFunction())) {
                return;
            }
            if (recStack.contains(fc.getTargetFunction())) {
                if (recStack.elementAt(0) == fc.getTargetFunction()) {
                    strongrecursive = true;
                }
                return;
            }
            if (!fc.getTargetFunction().recursiveFlag) {
                return;
            }

            if (recStack.size() + 1 > maxdepth && maxdepth != -1) {
                return;
            }
            recStack.add(fc.getTargetFunction());
            if (maxdepth == -1) {
                visited.add(fc.getTargetFunction());
            }
            fc.getTargetFunction().asExpression().accept(this);
            recStack.pop();
        }

    }

    /**
     * Checks whether a given Expression is constant and does not depend on any
     * variables
     * 
     * @param e
     * @return true or false
     */
    static boolean isConstant(Expression e) {
        IsConstantVisitor v = new IsConstantVisitor();
        e.accept(v);
        return v.constant;
    }

    static boolean isRecursive(Expression e) {
        IsRecursiveVisitor v = new IsRecursiveVisitor();
        e.accept(v);
        return v.recursive;
    }

    public static boolean isStrongRecursive(SEFunction e) {
        //TODO: replace with Tarjans Algorithm for SCCs (because that is what we are looking for here)
        //TODO: if replaced with Tarjan, add additional self-loop case
        //Incremental DFS
        IsStrongRecursiveVisitor v = new IsStrongRecursiveVisitor();
        for (int i = 1; i < 10; ++i) {
            v.maxdepth = i;
            v.recStack.push(e);
            e.asExpression().accept(v);
            if (v.strongrecursive) {
                return true;
            }
            v.recStack.clear();
        }
        //Run without depth limit
        v.maxdepth = -1;
        v.recStack.push(e);
        e.asExpression().accept(v);
        return v.strongrecursive;
    }

    /**
     * Replaces instaces of a given Variable with a given value for the
     * expression
     * 
     * @author mfunk@tzi.de
     */
    public static class VariableReplacer
            implements Function<Expression, Expression> {
        private final Map<String, Expression> varMap;

        public VariableReplacer(Map<String, Expression> varMap) {
            this.varMap = varMap;
        }

        @Override
        public Expression apply(Expression expr) {
            if (expr instanceof Variable) {
                Variable v = (Variable) expr;
                if (varMap.containsKey(v.toString())) {
                    return varMap.get(v.toString());
                }
            }
            return expr;
        }
    }

    /**
     * Generates a new Name postfix for a generated function indicating the
     * values that were statically assigned to parameters
     * 
     * @param constants
     * @return
     */
    static String getGeneratedFunctionPostfix(
            Map<String, Expression> constants) {
        StringBuilder b = new StringBuilder();
        for (Entry<String, Expression> constant : constants.entrySet()) {
            b.append("_");
            b.append(constant.getKey());
            b.append("_");
            Expression evaluatedExpression = ConstantExpressionEvaluator
                    .evalExpression(constant.getValue());
            b.append(evaluatedExpression);
        }

        return b.toString();
    }

    /**
     * Creates a new SEFunction where a Parameter is replaced by a value
     * 
     * @param in
     *            original SEFunction
     * @param paramn
     *            Index of the Parameter to be replaced
     * @param paramValue
     *            Value that the Parameter is replaced by
     * @return new SEFunction with one parameter less
     */
    static SEFunction instantiateSEFunction(SEFunction in,
            Map<Integer, Expression> paramValues) {

        // Get the names of the variables
        Map<String, Expression> constants = paramValues.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                entry -> in.getParameters().get(entry.getKey())
                                        .toString(),
                                entry -> entry.getValue()));

        String name = in.getName() + getGeneratedFunctionPostfix(constants);
        if (instantiationCache.containsKey(name)) {
            return instantiationCache.get(name);
        }

        // Replace the parameter in the Expression of the SEFunction and
        // evaluate constant expressions
        VariableReplacer crt = new VariableReplacer(constants);
        ExpressionTransformer parameterReplacer = new ExpressionTransformer(
                crt);
        Expression transformedExpression = parameterReplacer
                .transform(in.asExpression());
        Expression evaluatedExpression = ConstantExpressionEvaluator
                .evalExpression(transformedExpression);

        // Remove the parameter from the list of parameters
        ArrayList<Variable> newParamList = new ArrayList<>(in.getParameters());
        newParamList.removeIf(v -> constants.keySet().contains(v.toString()));

        return new SEFunction(name, evaluatedExpression, newParamList);
    }

    /**
     * Modifies a function by replacing the heap-parameter with n parameters
     * @param f
     * @param heapsize
     * @return new SEFunction 
     */
    static SEFunction instantiateWithHeapSize(SEFunction f, int heapsize) {
        Map<String, Expression> replace = new HashMap<>();
        replace.put("heapsize", new Constant(Reference.class, heapsize));

        Expression heap = new Constant(Heap.class, new ArrayList<>());

        List<Variable> heapArgs = new ArrayList<>();
        for (int i = 0; i < heapsize; ++i) {
            Variable v = new Variable("h" + i, Undef.class);
            heapArgs.add(v);
            heap = new OperationExpression(Operator.PUT, heap,
                    new Constant(Reference.class, i), v);
        }
        //Heap is replaced by a APPENDs of the new parameters to a empty list
        replace.put("heap", heap);

        VariableReplacer vr = new VariableReplacer(replace);
        ExpressionTransformer et = new ExpressionTransformer(vr);
        Expression e = et.transform(f.asExpression());

        ArrayList<Variable> params = new ArrayList<>(f.getParameters());
        params.removeIf((v) -> v.getType() == Heap.class);
        params.addAll(heapArgs);
        return new SEFunction(f.getName() + "_a" + heapsize, e, params);
    }

    /**
     * 
     * Replaces function calls with constant parameters with a specialized
     * function call to a newly generated function that contains the constant
     * parameters
     * 
     * Supposed to be used in a expression transformer
     *
     */
    public static class PartialCallEvaluator
            implements Function<Expression, Expression> {

        @Override
        public Expression apply(Expression e) {
            if (!(e instanceof ProcessedFunctionCall)) {
                return e;
            }
            ProcessedFunctionCall pfc = (ProcessedFunctionCall) e;

            List<Expression> params = pfc.getParameters();
            // Does this pfc contain a constant heap parameter?
            // It should only contain one if the current function was already
            // heap-expanded
            List<Expression> heap = null;
            for (Expression ex : params) {
                if (ex.getType() == Heap.class) {
                    Expression cHeap = ConstantExpressionEvaluator
                            .evalExpression(ex);
                    if (cHeap instanceof Constant) {
                        heap = Constant.getHeap((Constant) cHeap);
                    }
                    break;
                }
            }
            // If there is a heap parameter, expand it to a list of heap
            // parameters and instatiate a new function with the addrd
            // parameters
            if (heap != null) {
                SEFunction heapExpanded = instantiateWithHeapSize(
                        pfc.getTargetFunction(), heap.size());
                List<Expression> nparams = new ArrayList<>(params);
                nparams.removeIf((expr) -> expr.getType() == Heap.class);
                nparams.addAll(heap);
                pfc = new ProcessedFunctionCall(heapExpanded, nparams);
            }

            Map<Integer, Expression> constantParams = new HashMap<>();
            List<Expression> variableParams = new ArrayList<>();

            for (int i = 0; i < pfc.getParameters().size(); ++i) {
                Expression parameter = ConstantExpressionEvaluator
                        .evalExpression(pfc.getParameters().get(i));
                if (isConstant(parameter)) {
                    constantParams.put(i, parameter);
                } else {
                    variableParams.add(parameter);
                }
            }
            if (constantParams.size() != 0) {
                SEFunction nf = instantiateSEFunction(pfc.getTargetFunction(),
                        constantParams);
                pfc = new ProcessedFunctionCall(nf, variableParams);
            }
            if (!instantiationCache
                    .containsKey(pfc.getTargetFunction().getName())) {
                instantiationCache.put(pfc.getTargetFunction().getName(),
                        pfc.getTargetFunction());
            }
            return pfc;
        }
    }
}
