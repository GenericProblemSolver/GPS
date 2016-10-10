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

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;

import gps.bytecode.expressions.IExpressionVisitor;
import gps.bytecode.expressions.ProcessedFunctionCall;
import gps.bytecode.symexec.SEFunction;
import gps.bytecode.symexec.SatisfactionProblem;

/**
 * Some utils for the backends
 * @author mfunk@tzi.de
 *
 */
public class BackendUtil {
    /**
     * Return all functions in a problem ordered. This version can handle
     * recursion, but is a lot slower.
     * 
     * @param p
     * @return
     */
    public static List<SEFunction> getOrderedFunctionsRec(
            SatisfactionProblem p) {
        final ArrayList<SEFunction> collectedFunctions = new ArrayList<>();

        Stack<SEFunction> candidates = new Stack<>();
        Stack<SEFunction> currentPath = new Stack<>();
        // Add initial functions
        candidates.addAll(p.constraints);
        collectedFunctions.addAll(p.constraints);

        // How does this work?
        // This function theoretically outputs a topological order of the DAG
        // that is the Function call-graph of the Satisfaction problem
        // The problem is that the function call graph may contain circles in
        // case of recursion, thus isn't a DAG
        // Since Z3 can't deal with recursion the ordering of functions in that
        // case doesn't matter
        // In this function we use a Depth-First-Search to output the DAG in
        // topological order (using the candidates stack), since we have to
        // detect and avoid circles there is a second stack "currentPath" that
        // contains the currentPath of the Depth-First-Search
        // If we walk in a circle and try to add a Function to the candidates
        // that is already in the path (should be impossible in a DAG), we leave
        // it out and thus avoid an infinite loop while keeping the correct
        // topological ordering for DAGs. A special null-marker is used on the
        // candidates stack to simplify updating the currentPath

        final IExpressionVisitor.IDefaultExpressionVisitor collectFunctions = new IExpressionVisitor.IDefaultExpressionVisitor() {
            @Override
            public void visitFunctionCallExpression(ProcessedFunctionCall fc) {
                // Move the Function to the back of the list
                collectedFunctions.remove(fc.getTargetFunction());
                collectedFunctions.add(fc.getTargetFunction());

                if (!currentPath.contains(fc.getTargetFunction())) {
                    candidates.remove(fc.getTargetFunction());
                    candidates.push(fc.getTargetFunction());
                }
            }
        };
        // Visit all Functions
        while (!candidates.isEmpty()) {
            SEFunction f = candidates.pop();
            if (f == null) {
                currentPath.pop();
                continue;
            }
            currentPath.push(f);
            candidates.push(null);
            f.asExpression().accept(collectFunctions);
        }

        Collections.reverse(collectedFunctions);
        return collectedFunctions;
    }

    /**
     * Returns all functions in a problem unordered.
     * 
     * @param p
     * @return
     */
    public static Set<SEFunction> getAllFunctions(SatisfactionProblem p) {
        final Set<SEFunction> functions = new HashSet<>();
        final Queue<SEFunction> queue = new ArrayDeque<>();
        final IExpressionVisitor.IDefaultExpressionVisitor collectFunctions = new IExpressionVisitor.IDefaultExpressionVisitor() {
            @Override
            public void visitFunctionCallExpression(ProcessedFunctionCall fc) {
                queue.add(fc.getTargetFunction());
            }
        };
        queue.addAll(p.constraints);
        while (!queue.isEmpty()) {
            SEFunction f = queue.poll();
            if (!functions.contains(f)) {
                functions.add(f);
                f.asExpression().accept(collectFunctions);
            }
        }
        return functions;
    }

    /**
     * Returns all functions in a problem ordered. Will run into an infinite
     * loop if the problem contains any form of recursion
     * 
     * @param p
     * @return
     */
    public static List<SEFunction> getOrderedFunctions(SatisfactionProblem p) {
        final LinkedHashSet<SEFunction> collectedFunctions = new LinkedHashSet<>();
        Queue<SEFunction> c = new ArrayDeque<>();
        c.addAll(p.constraints);
        collectedFunctions.addAll(p.constraints);

        final IExpressionVisitor.IDefaultExpressionVisitor collectFunctions = new IExpressionVisitor.IDefaultExpressionVisitor() {
            @Override
            public void visitFunctionCallExpression(ProcessedFunctionCall fc) {
                // Move the Function to the back of the list
                if (collectedFunctions.contains(fc.getTargetFunction())) {
                    collectedFunctions.remove(fc.getTargetFunction());
                }
                collectedFunctions.add(fc.getTargetFunction());
                c.remove(fc.getTargetFunction());
                c.add(fc.getTargetFunction());
            }
        };
        // Visit all Functions
        while (!c.isEmpty()) {
            SEFunction f = c.poll();
            f.asExpression().accept(collectFunctions);
        }
        ArrayList<SEFunction> result = new ArrayList<>(collectedFunctions);
        Collections.reverse(result);
        return result;

    }
}
