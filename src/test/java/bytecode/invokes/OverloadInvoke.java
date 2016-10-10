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
package bytecode.invokes;

import org.junit.runner.RunWith;
import org.junit.Ignore;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
@Ignore
public class OverloadInvoke {

    public int getNum(int i) {
        return Integer.valueOf(i);
    }

    public int getNum(int i, int j) {
        return Integer.valueOf(i) + Integer.sum(i, j);
    }

    public int getNum(double d) {
        return 3;
    }

    @Constraint
    public boolean correctPolyTest() {
        OverloadInvoke prob = new OverloadInvoke();
        boolean t1 = prob.getNum(1) == 1;
        boolean t2 = prob.getNum(1, 2) == 4;
        boolean t3 = prob.getNum(1.1) == 3;
        return t1 && t2 && t3;
    }

}
