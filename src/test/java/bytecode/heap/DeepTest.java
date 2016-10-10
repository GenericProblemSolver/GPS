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
package bytecode.heap;

import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;
import gps.annotations.Variable;
import gps.annotations.Variable.VariableDepth;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class DeepTest {

    static class Point {
        int x;
        int y;

        @Variable(VariableDepth.None)
        int num = 17;
    }

    Point p1 = new Point();

    @Variable(VariableDepth.Deep)
    Point p2 = new Point();

    public DeepTest() {
        p1.x = 17;
        p1.y = 18;
    }

    @Constraint
    boolean check() {
        return p1.x == p2.x && p1.y == p2.y && p1.num == p2.num;
    }
}
