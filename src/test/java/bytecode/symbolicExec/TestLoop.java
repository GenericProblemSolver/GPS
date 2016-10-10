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
import gps.annotations.Constraint;
import bytecode.ConstraintProblemRunner;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class TestLoop {
    @Constraint
    boolean test(int a) {
        for (int i = 0; i < 4; ++i) {
            a = a + 1;
        }
        return a > 1;
    }

    @Constraint
    boolean test2(int a) {
        for (int i = 4; i > 0; --i) {
            a = a + 1;
        }
        return a > 1;
    }

    @Constraint
    boolean test3(int a) {
        for (int i = 0; i < 4; ++i) {
            a = a * 2;
        }
        return a > 1;
    }

    @Constraint
    boolean test4(int a) {
        for (int x = 0; x < 2; ++x) {
            for (int y = 0; y < 2; ++y) {
                a += x * y;
            }
        }
        return a == 17;
    }

    @Constraint
    boolean test5(int n) {
        int a = 1;
        for (int i = 0; i < n; ++i) {
            a = a * 2;
        }
        return a == 128;
    }
}
