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
import java.lang.Exception;

/**
 * Created by max on 4/13/16.
 */
public class CVC4JavaApiBackend implements IBytecodeBackend {

    ExprManager ctx;

    @Override
    public BackendResult solve(SatisfactionProblem problem) {
        ctx = new ExprManager();
        SmtEngine smt = new SmtEngine(ctx);
        smt.setOption("produce-models", new SExpr(true));

        smt.setLogic("QF_LIRA"); // Set the logic

        CVC4APIVisitor visitor = new CVC4APIVisitor(ctx, problem);

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

        if (constraints.isEmpty()) {
            problem.assignSolution(new SatisfactionProblemSolution(true, ""));
            return BackendResult.SOLVED;
        } else {
            for (ProcessedFunction constraint : constraints) {
                if (constraint.function.getType().equals(ctx.booleanType())) {
                    smt.assertFormula(constraint.function);
                } else {
                    smt.assertFormula(ctx.mkExpr(Kind.EQUAL,
                            constraint.function, constraint.function));
                }
                System.out.println(constraint.function.toString());
            }
        }

        // smt.checkSat() will throw an exception if non-linear elements are found
        try {
            smt.push();
            if (smt.checkSat().toString().equals("sat")) {
                SatisfactionProblemSolution solution = new SatisfactionProblemSolution(
                        true, "");
                for (Variable var : problem.problemVariables.keySet()) {
                    Expr solutionValue = smt.getValue(problemVarsMap.get(var));
                    Class<?> t = var.getType();
                    solution.variableValues.put(var,
                            CVC4JavaApiBackendMaps.resultMap
                                    .getOrDefault(var.getType(), (cl) -> {
                                        System.out.println(var.getType());
                                        throw new RuntimeException(
                                                "No result extraction mapping"
                                                        + " for "
                                                        + cl.toString());
                                    }).map(solutionValue));
                }
                problem.assignSolution(solution);
                return BackendResult.SOLVED;
            } else {
                problem.assignSolution(new SatisfactionProblemSolution(false,
                        "No model found"));
                return BackendResult.SOLVED;
            }
        } catch (LogicException e) {
            // CVC4 cannot solve the problem
            System.out.println("CVC4 Exception: " + e.getMessage());
            return BackendResult.UNABLE_TO_SOLVE;
        } catch (Exception e) {
            return BackendResult.ERROR;
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            System.loadLibrary("cvc4jni");
            System.loadLibrary("cvc4");
            System.loadLibrary("cvc4parser");
            ExprManager ctx = new ExprManager();
        } catch (UnsatisfiedLinkError | NoClassDefFoundError error) {
            System.out.println(error.getMessage());
            return false;
        }
        return true;
    }

    static private class CVC4APIVisitor implements IDefaultExpressionVisitor {

        ExprManager ctx;
        Stack<Expr> currentParams = new Stack<>();
        Map<Variable, Expr> parameterMap = new HashMap<>();

        private Map<SEFunction, ProcessedFunction> processedFunctions = new HashMap<>();
        private SatisfactionProblem problem;
        private SEFunction currentSEFunction = null;

        CVC4APIVisitor(ExprManager ctx, SatisfactionProblem problem) {
            this.ctx = ctx;
            this.problem = problem;
        }

        public void setSEFunction(SEFunction e) {
            currentSEFunction = e;
        }

        @Override
        public void visitConstant(Constant c) {
            Expr constant = CVC4JavaApiBackendMaps.constantTypeMap
                    .getOrDefault(c.getType(), (ctx, cc) -> {
                        throw new RuntimeException(
                                "No Constant mapping for " + cc.getType());
                    }).map(ctx, c);
            currentParams.push(constant);
        }

        @Override
        public void visitOperatorExpression(OperationExpression op) {
            for (Expression e : op.getParameters()) {
                e.accept(this);
            }
            CVC4JavaApiBackendMaps.operatorMap
                    .getOrDefault(op.getOperator(), (ctx, paramStack) -> {
                        throw new RuntimeException("Operator "
                                + op.getOperator() + " not implemented.");
                    }).map(ctx, currentParams);

        }

        public Expr createVariableExpression(Variable v) {
            Variable global = globalParameterValue(problem, currentSEFunction,
                    v);
            return parameterMap.getOrDefault(global,
                    CVC4JavaApiBackendMaps.variableTypeMap.get(v.getType())
                            .map(ctx, v));
        }

        @Override
        public void visitVariableExpression(Variable v) {
            currentParams.push(createVariableExpression(v));
        }

        @Override
        public void visitProcessedFunctionCall(ProcessedFunctionCall fc) {
            ProcessedFunction targetFunction = processedFunctions
                    .get(fc.getTargetFunction());

            // TODO: Is it a problem to cast Expression to Variable here
            //       unchecked?
            Expr[] orig = fc.getTargetFunction().getParameters().stream()
                    .map(e -> targetFunction.parameterMap
                            .get(globalParameterValue(problem,
                                    fc.getTargetFunction(), (Variable) e)))
                    .toArray(Expr[]::new);

            Expr[] subs = fc.getParameters().stream().map(e -> {
                e.accept(this);
                return currentParams.pop();
            }).toArray(Expr[]::new);

            Expr target = targetFunction.function;
            for (int i = 0; i < orig.length; i++) {
                target = target.substitute(orig[i], subs[i]);
            }
            currentParams.push(target);
        }
    }

    private static Variable globalParameterValue(SatisfactionProblem problem,
            SEFunction f, Variable param) {
        if (problem.problemVariables.containsKey(param)) {
            return param;
        } else {
            return param.intoNamespacedVariable(f);
        }
    }

    class ProcessedFunction {
        public Expr function;
        public Map<Variable, Expr> parameterMap;

        ProcessedFunction(Expr function, Map<Variable, Expr> parameterMap) {
            this.function = function;
            this.parameterMap = parameterMap;
        }
    }
}
