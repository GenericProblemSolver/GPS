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

import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;
import gps.annotations.Variable;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class SimpleSort {
    int[] initFields = new int[6];

    @Variable
    int[] fields = new int[initFields.length];

    public SimpleSort() {
        initFields[0] = 1;
        initFields[1] = 2;
        initFields[2] = 128;
        initFields[3] = 16;
        initFields[4] = 32;
        initFields[5] = 256;
    }

    @Constraint
    boolean testSorted() {

        for (int i = 0; i < fields.length - 1; ++i) {
            if (fields[i] <= fields[i + 1]) {
                return false;
            }
        }

        for (int i = 0; i < initFields.length; ++i) {
            int occurs = 0;
            for (int j = 0; j < fields.length; ++j) {
                if (initFields[i] == fields[j]) {
                    occurs += 1;
                }
            }
            if (occurs != 1) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (int i : fields) {
            b.append("" + i + ", ");
        }
        b.deleteCharAt(b.length() - 2);
        return b.toString();
    }
}
