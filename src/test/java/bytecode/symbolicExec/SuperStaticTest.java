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

import org.junit.Ignore;
import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

@Ignore
@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class SuperStaticTest {

    public static class SuperClass {
        public static int i = 13;
    }

    public static class SubClass extends SuperClass {
    }

    public static class TestClass {
        static void change() {
            SuperClass.i = 273;
        }
    }

    @Constraint
    public boolean test() {
        boolean boo1 = SubClass.i == 13;
        TestClass.change();
        boolean boo2 = SuperClass.i == 273;
        return boo1 && boo2;
    }
}
