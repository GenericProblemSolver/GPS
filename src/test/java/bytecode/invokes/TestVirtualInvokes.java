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

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class TestVirtualInvokes {

    static class A {
        int blub() {
            return 3;
        }
    }

    static class B extends A {
        @Override
        int blub() {
            return 4;
        }
    }

    static class C extends B {
        @Override
        int blub() {
            return 5;
        }
    }

    A a = new B();
    A c = new C();

    @Constraint
    boolean test() {
        return a.blub() == 4;
    }

    @Constraint
    boolean test2() {
        A a = new B();
        return a.blub() == 4;
    }

    @Constraint
    boolean test3(int n) {
        B a = new C();
        return a.blub() == n;
    }

    @Constraint
    boolean test4(int n) {
        A a;
        if (n == 2) {
            a = new B();
        } else {
            a = new C();
        }
        return a.blub() == 4;
    }

}
