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

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

import gps.bytecode.backends.BackendUtil;
import gps.bytecode.expressions.IExpressionVisitor.IDefaultExpressionVisitor;
import gps.bytecode.expressions.ProcessedFunctionCall;
import gps.bytecode.symexec.SEFunction;
import gps.bytecode.symexec.SatisfactionProblem;
import gps.bytecode.transforms.TransformationPasses.ITransformationPass;

/**
 * Transforms a problem by applying the pre- and post- transforms specified by a transformationpass
 * @author mfunk@tzi.de
 */
public class ProblemTransformer implements Consumer<SatisfactionProblem> {
    private final Set<SEFunction> transformedFunctions = new HashSet<>();
    private final Stack<SEFunction> candidates = new Stack<>();
    private final Stack<SEFunction> postTransformCandidates = new Stack<>();

    //Not static since it accesses the candidates stack of the parent
    class RecursiveTransformVisitor implements IDefaultExpressionVisitor {
        @Override
        public void visitFunctionCallExpression(ProcessedFunctionCall fc) {
            candidates.push(fc.getTargetFunction());
        }

    }

    private final Consumer<SEFunction> preTransform;
    private final Consumer<SEFunction> postTransform;
    private final Consumer<SEFunction> onMaxDepth;

    public ProblemTransformer(ITransformationPass pass) {
        preTransform = pass.getPreTransformFunction();
        postTransform = pass.getPostTransformFunction();
        onMaxDepth = pass.getOnMaxDepthFunction();
    }

    private ProblemTransformer(Consumer<SEFunction> preTransform,
            Consumer<SEFunction> postTransform, Consumer<SEFunction> maxdepth) {
        this.preTransform = preTransform;
        this.postTransform = postTransform;
        this.onMaxDepth = maxdepth;
    }

    /**
     * Transforms a problem with a default maxsteps
     * @param problem
     */
    public void transform(SatisfactionProblem problem) {
        //Try to estimate a suitable maxsteps
        int problemsize = BackendUtil.getAllFunctions(problem).size();
        transform(problem, problemsize * 160);
    }

    /**
     * Transforms a problem
     * @param problem
     * @param maxsteps maximum number of steps to be executed
     */
    public void transform(SatisfactionProblem problem, int maxsteps) {
        RecursiveTransformVisitor visitor = new RecursiveTransformVisitor();
        for (SEFunction f : problem.constraints) {
            candidates.push(f);
            int remainingSteps = maxsteps;
            while (!candidates.isEmpty()) {
                SEFunction candidate = candidates.pop();
                if (candidate == null) {
                    postTransform.accept(postTransformCandidates.pop());
                    remainingSteps += 1;
                    continue;
                }
                if (transformedFunctions.contains(candidate)) {
                    continue;
                }
                //Marker for posttransform
                candidates.push(null);
                postTransformCandidates.push(candidate);

                transformedFunctions.add(candidate);
                preTransform.accept(candidate);
                remainingSteps -= 1;
                if (remainingSteps <= 1) {
                    onMaxDepth.accept(candidate);
                }
                if (remainingSteps > 0) {
                    candidate.asExpression().accept(visitor);
                }
            }
            preTransform.accept(f);
            f.asExpression().accept(visitor);
            postTransform.accept(f);
        }

    }

    @Override
    public void accept(SatisfactionProblem t) {
        transform(t);
    }

    @Override
    public Consumer<SatisfactionProblem> andThen(
            Consumer<? super SatisfactionProblem> after) {
        if (!(after instanceof ProblemTransformer)) {
            return Consumer.super.andThen(after);
        }
        ProblemTransformer afterTrans = (ProblemTransformer) after;
        Consumer<SEFunction> preTrans = this.preTransform
                .andThen(afterTrans.preTransform);
        Consumer<SEFunction> postTrans = this.postTransform
                .andThen(afterTrans.postTransform);
        Consumer<SEFunction> maxDepth = this.onMaxDepth
                .andThen(afterTrans.onMaxDepth);
        return new ProblemTransformer(preTrans, postTrans, maxDepth);
    }

}
