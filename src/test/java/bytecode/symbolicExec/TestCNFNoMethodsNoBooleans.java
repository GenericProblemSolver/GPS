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
package bytecode.symbolicExec;

import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;
import gps.annotations.Variable;

/**
 * Very unintuitive implementation of a SAT-Problem to prove, that we can
 * already solve this.
 * 
 * It works without using any method calls, boolean arrays or nor
 * bitfield-operators (&, |, ^, ...) in the Constraint-method.
 * 
 * The expression string has to be in conjunctive normal form. Braces may be
 * added for readability, but are not interpreted: All ANDs are grouped and
 * evaluated, then all these blocks are OR-ed.
 * 
 * All booleans are coded in ints, where only '1' is interpreted as true,
 * everything else as false.
 * 
 * 
 * @author jowlo@uni-bremen.de
 *
 */
@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class TestCNFNoMethodsNoBooleans {

    @Variable
    int[] vars;

    int[] literals;
    int[] inverts;
    int[] disjuncts;
    int numLiterals;

    public TestCNFNoMethodsNoBooleans() {
        String expression = "(#1 AND #2)";
        System.out.println(expression);
        // Clean up
        expression = expression.replace("(", "").replace(")", "");
        Set<Integer> varIds = new HashSet<>();

        // Count variables
        for (String slice : expression.replace("-", "").split("#")) {
            if (slice.length() != 0) {
                int endIndex = slice.indexOf(' ') != -1 ? slice.indexOf(' ')
                        : slice.length();
                varIds.add(Integer.parseInt(slice.substring(0, endIndex)));
            }
        }
        vars = new int[varIds.size()];

        // Parse expression
        numLiterals = expression.length()
                - expression.replace("#", "").length();

        literals = new int[numLiterals];
        inverts = new int[numLiterals];
        disjuncts = new int[numLiterals];

        int index = 0;
        int literal = 0;
        while ((index = expression.indexOf('#', index)) != -1) {
            index++;
            if (expression.charAt(index) == '-') {
                inverts[literal] = 1;
                index++;
            } else {
                inverts[literal] = 0;
            }

            int endIndex = expression.indexOf(' ', index) != -1
                    ? expression.indexOf(' ', index) : expression.length();
            literals[literal] = Integer
                    .parseInt(expression.substring(index, endIndex)) - 1;

            if (expression.indexOf('O', index) >= 0 && expression.indexOf('O',
                    index) < expression.indexOf('A', index)) {
                disjuncts[literal] = 1;
            }
            literal++;
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (int var : vars) {
            b.append(var + "\n");
        }
        return b.toString();
    }

    @Constraint
    boolean testcnf() {
        int ret = 0;
        int tmp = 1;
        for (int i = 0; i < numLiterals; i++) {
            int val = (vars[literals[i]] - inverts[i] >= 1) ? 1 : 0;
            tmp = (tmp + val > 1) ? 1 : 0;
            if (disjuncts[i] > 0) {
                ret = ret + tmp >= 1 ? 1 : 0;
                tmp = 1;
            }
        }
        ret = ret >= 1 ? 1 : ret + tmp;
        return ret >= 1;
    }

}
