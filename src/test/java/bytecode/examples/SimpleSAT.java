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
package bytecode.examples;

import java.util.HashMap;
import java.util.Map;

import gps.annotations.Constraint;
import gps.annotations.Variable;
import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;

/**
 * 
 * @author mfunk@tzi.de
 *
 */
@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class SimpleSAT {

    static class Disjunction {
        int[] literals;
        boolean[] negated;
    }

    @Variable
    private final boolean[] variables;
    private final Disjunction[] disjunctions;

    public SimpleSAT() {
        String expression = "A or B and A";

        Map<String, Integer> varmap = new HashMap<>();
        int numvars = 0;

        String a1 = expression.replaceAll("(|)", "");
        String[] djs = a1.split(" and ");

        disjunctions = new Disjunction[djs.length];

        for (int djsIndex = 0; djsIndex < djs.length; ++djsIndex) {
            Disjunction d = new Disjunction();
            String[] tokens = djs[djsIndex].split(" or ");

            d.literals = new int[tokens.length];
            d.negated = new boolean[tokens.length];

            for (int i = 0; i < tokens.length; ++i) {
                if (tokens[i].startsWith("not ")) {
                    d.negated[i] = true;
                    tokens[i] = tokens[i].replaceFirst("not ", "");
                }
                if (!varmap.containsKey(tokens[i])) {
                    varmap.put(tokens[i], numvars);
                    numvars += 1;
                }
                d.literals[i] = varmap.get(tokens[i]);
            }
            disjunctions[djsIndex] = d;
        }

        variables = new boolean[numvars];

    }

    @Constraint
    public boolean check() {
        for (Disjunction d : disjunctions) {
            boolean satis = false;
            for (int literal : d.literals) {
                satis = satis || variables[literal];
            }
            if (!satis) {
                return false;
            }
        }
        return true;
    }

}
