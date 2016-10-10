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
public class RecursiveInvoke {

    public int method1(int i) {
        if (i > 10) {
            return method2(i / 2);
        } else {
            return i;
        }

    }

    public int method2(int i) {
        if (i > 10) {
            return method3(i - 1);
        } else {
            return method1(i);
        }

    }

    public int method3(int i) {
        return method1(10);
    }

    @Constraint
    public boolean testLoop() {
        return method1(20) <= 10;
    }

}
