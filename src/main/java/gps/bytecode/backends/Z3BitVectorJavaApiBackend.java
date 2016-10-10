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
import gps.bytecode.expressions.*;
import gps.bytecode.expressions.IExpressionVisitor.IDefaultExpressionVisitor;
import gps.bytecode.symexec.SEFunction;
import gps.bytecode.symexec.SatisfactionProblem;
import gps.bytecode.transforms.ExpressionTransformer;
import gps.bytecode.transforms.TransformationPasses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Backend using Z3 and modelling all numbers as bitvectors. Correct overflow
 * behaviour is simulated by masking all bitvector operations with their
 * respective length (see Z3BitVectorJavaApiMaps.andWrap for details)
 *
 *
 * @author jowlo
 */
public class Z3BitVectorJavaApiBackend implements IBytecodeBackend {

    Context ctx;
    public Z3BitVectorJavaApiBackendMaps mapClass;
    public String logic;

    public Z3BitVectorJavaApiBackend() {
        this.mapClass = new Z3BitVectorJavaApiBackendMaps();
        this.logic = "QF_BV";
    }

    @Override
    public BackendResult solve(SatisfactionProblem problem) {
        ctx = new Context();
        Z3APIVisitor visitor = new Z3APIVisitor(ctx, problem);

        TransformationPasses.transform(problem);

        List<SEFunction> functions = BackendUtil.getOrderedFunctions(problem);
        ExpressionTransformer trans = new ExpressionTransformer(
                Z3StdoutBackend::notEqToNegTransform);

        for (SEFunction e : functions) {
            visitor.setSEFunction(e);
            // TODO: Casting parameters of top level SEFunctions to Variable
            //       could break at some point.
            for (Variable var : e.getParameters()) {
                var = globalParameterValue(problem, e, var);
                Expr param = visitor.createVariableExpression(var);
                visitor.parameterMap.put(var, param);
            }
            trans.transform(e.asExpression()).accept(visitor);
            Expr expr = visitor.currentParams.pop();
            visitor.processedFunctions.put(e,
                    new ProcessedFunction(expr, visitor.parameterMap));
            visitor.parameterMap = new HashMap<>();
        }

        List<ProcessedFunction> constraints = problem.constraints.stream()
                .map(visitor.processedFunctions::get)
                .collect(Collectors.toList());

        Map<Variable, Expr> problemVarsMap = new HashMap<>();
        for (ProcessedFunction pf : constraints) {
            problemVarsMap.putAll(pf.parameterMap);
        }

        Solver s = ctx.mkSolver(logic);
        if (constraints.isEmpty()) {
            problem.assignSolution(new SatisfactionProblemSolution(true, ""));
            return BackendResult.SOLVED;
        } else {
            s.add(constraints.stream().map(e -> (BoolExpr) e.function)
                    .toArray(BoolExpr[]::new));
        }

        if (s.check() == Status.SATISFIABLE) {
            System.out.println(s.toString());
            Model m = s.getModel();
            SatisfactionProblemSolution solution = new SatisfactionProblemSolution(
                    true, "");
            for (Variable var : problem.problemVariables.keySet()) {
                Expr solutionValue = m.evaluate(problemVarsMap.get(var), false);
                solution.variableValues.put(var, mapClass.getResultMap()
                        .getOrDefault(solutionValue.getClass(), (v) -> {
                            // We run into this, if a variable is not bound in
                            // the model, since solutionValue.getClass() is
                            // still
                            // of Type *Expr, for which we do not have an
                            // extraction mapping.
                            System.out.println("Unbound variable of type "
                                    + v.getClass() + ".\n");
                            return new Constant(0);
                        }).map(solutionValue));
            }
            problem.assignSolution(solution);
            return BackendResult.SOLVED;
        } else {
            problem.assignSolution(
                    new SatisfactionProblemSolution(false, "No model found"));
            return BackendResult.SOLVED;
        }

    }

    @Override
    public boolean isAvailable() {
        try {
            System.loadLibrary("z3java");
        } catch (UnsatisfiedLinkError | NoClassDefFoundError error) {
            System.err.println("Z3JavaAPI not available.");
            return false;
        }
        return true;
    }

    private class Z3APIVisitor implements IDefaultExpressionVisitor {

        Context ctx;
        Stack<Expr> currentParams = new Stack<>();
        Map<Variable, Expr> parameterMap = new HashMap<>();

        private Map<SEFunction, ProcessedFunction> processedFunctions = new HashMap<>();
        private SatisfactionProblem problem;
        private SEFunction currentSEFunction = null;

        Z3APIVisitor(Context ctx, SatisfactionProblem problem) {
            this.ctx = ctx;
            this.problem = problem;
        }

        public void setSEFunction(SEFunction e) {
            currentSEFunction = e;
        }

        @Override
        public void visitConstant(Constant c) {
            Expr constant = mapClass.getConstantTypeMap()
                    .getOrDefault(c.getType(), (ctx, cc) -> {
                        throw new RuntimeException(
                                "No Constant mapping for " + cc.getType());
                    }).map(ctx, c);
            currentParams.push(constant);
        }

        @Override
        public void visitOperatorExpression(OperationExpression op) {
            int paramBVSize = 0;
            for (Expression e : op.getParameters()) {
                paramBVSize = paramBVSize > mapClass.sizeMap
                        .getOrDefault(e.getType(), 0) ? paramBVSize
                                : mapClass.sizeMap.getOrDefault(e.getType(), 0);
                e.accept(this);
            }
            mapClass.getOperatorMap()
                    .getOrDefault(op.getOperator(), (ctx, paramStack, size) -> {
                        throw new RuntimeException("Operator "
                                + op.getOperator() + " not implemented.");
                    }).map(ctx, currentParams,
                            mapClass.sizeMap.getOrDefault(paramBVSize, 0));

        }

        public Expr createVariableExpression(Variable v) {
            Variable global = globalParameterValue(problem, currentSEFunction,
                    v);
            return parameterMap.getOrDefault(global,
                    mapClass.getVariableTypeMap().get(v.getType()).map(ctx, v));
        }

        @Override
        public void visitVariableExpression(Variable v) {
            currentParams.push(createVariableExpression(v));
        }

        @Override
        public void visitProcessedFunctionCall(ProcessedFunctionCall fc) {
            ProcessedFunction targetFunction = processedFunctions
                    .get(fc.getTargetFunction());

            Expr[] orig = fc.getTargetFunction().getParameters().stream()
                    .map(e -> targetFunction.parameterMap
                            .get(globalParameterValue(problem,
                                    fc.getTargetFunction(), e)))
                    .toArray(Expr[]::new);

            Expr[] subs = fc.getParameters().stream().map(e -> {
                e.accept(this);
                return currentParams.pop();
            }).toArray(Expr[]::new);

            Expr target = targetFunction.function.substitute(orig, subs);
            currentParams.push(target);
        }
    }

    static Variable globalParameterValue(SatisfactionProblem problem,
            SEFunction f, Variable param) {
        if (problem.problemVariables.containsKey(param)) {
            return param;
        } else {
            return param.intoNamespacedVariable(f);
        }
    }

    private static class ProcessedFunction {
        public Expr function;
        public Map<Variable, Expr> parameterMap;

        ProcessedFunction(Expr function, Map<Variable, Expr> parameterMap) {
            this.function = function;
            this.parameterMap = parameterMap;
        }
    }
}
