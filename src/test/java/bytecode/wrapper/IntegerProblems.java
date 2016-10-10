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
package bytecode.wrapper;

import org.junit.Ignore;
import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;

import gps.annotations.Constraint;
import gps.annotations.Variable;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
@Ignore
public class IntegerProblems {

    private Integer[] initNumbers = new Integer[6];

    @Variable
    private Integer[] numbers = new Integer[initNumbers.length];

    public IntegerProblems() {
        initNumbers[0] = new Integer(2);
        initNumbers[1] = new Integer(4);
        initNumbers[2] = new Integer(3);
        initNumbers[3] = new Integer(1);
        initNumbers[4] = new Integer(5);
        initNumbers[5] = new Integer(9);

    }

    @Constraint
    boolean testSorted() {

        for (int i = 0; i < initNumbers.length - 1; ++i) {
            if (numbers[i].intValue() <= numbers[i + 1].intValue()) {
                return false;
            }
        }

        for (int i = 0; i < initNumbers.length; ++i) {
            int occurs = 0;
            for (int j = 0; j < numbers.length; ++j) {
                if (initNumbers[i] == numbers[j]) {
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
        for (int i = 0; i < numbers.length; i++) {
            b.append("" + i + ", ");
        }
        b.deleteCharAt(b.length() - 2);
        return b.toString();
    }

}
