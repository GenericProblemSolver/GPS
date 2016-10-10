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

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

@RunWith(ConstraintProblemRunner.class)
public class StaticLoopTest {
    public static int i = 11;
    public static int f = 1;

    @Constraint
    public boolean loopAddTest() {
        for (int j = 0; j < 5; j++) {
            f = j;
        }
        return f == 4;
    }

    /**
     * hits max evaluation depth
     * 
     * @return
     */
    public boolean loopCancelTest1() {
        int j = 0;
        while (j < i) {
            j++;
        }
        return i == j++;
    }

    /**
     * also hits max evaluation depth
     * 
     * @return
     */
    public boolean loopCancelTest2() {
        int j = 0;
        for (int x = 0; x < i; x++) {
            j++;
        }
        return i == j++;
    }

    /**
     * does not hit max evaluation depth, but returns invalid output nonetheless
     * 
     * @return
     */
    @Constraint
    public boolean loopAddCancelTest() {
        while (i < 17) {
            i++;
        }
        return i == 17;
    }
}