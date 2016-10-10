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
 * It works without using any method calls, nor bitfield-operators (&, |, ^,
 * ...) in the Constraint-method.
 * 
 * The expression string has to be in conjunctive normal form. Braces may be
 * added for readability, but are not interpreted: All ANDs are grouped and
 * evaluated, then all these blocks are OR-ed.
 * 
 * @author jowlo@uni-bremen.de
 *
 */
@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class TestCNFNoMethods {

    @Variable
    boolean[] vars;

    int[] literals;
    boolean[] inverts;
    boolean[] disjuncts;

    int numLiterals;

    public TestCNFNoMethods() {
        String expression = "(#-1 AND #1) OR (#-2 AND #-2)";
        System.out.println("" + expression);

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
        vars = new boolean[varIds.size()];

        // Parse expression
        numLiterals = expression.length()
                - expression.replace("#", "").length();

        literals = new int[numLiterals];
        inverts = new boolean[numLiterals];
        disjuncts = new boolean[numLiterals];

        int index = 0;
        int literal = 0;
        while ((index = expression.indexOf('#', index)) != -1) {
            index++;
            if (expression.charAt(index) == '-') {
                inverts[literal] = true;
                index++;
            } else {
                inverts[literal] = false;
            }
            int endIndex = expression.indexOf(' ', index) != -1
                    ? expression.indexOf(' ', index) : expression.length();
            literals[literal] = Integer
                    .parseInt(expression.substring(index, endIndex)) - 1;

            if (expression.indexOf('O', index) >= 0 && expression.indexOf('O',
                    index) < expression.indexOf('A', index)) {
                disjuncts[literal] = true;
            }
            literal++;
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (boolean var : vars) {
            b.append(var + "\n");
        }
        return b.toString();
    }

    @Constraint
    boolean testcnf() {
        boolean ret = false;
        boolean tmp = true;
        for (int i = 0; i < numLiterals; i++) {
            boolean val = (vars[literals[i]] && !inverts[i])
                    || (!vars[literals[i]] && inverts[i]);
            tmp = tmp && val;
            if (disjuncts[i]) {
                ret = ret || tmp;
                tmp = true;
            }
        }
        return ret || tmp;
    }

}
