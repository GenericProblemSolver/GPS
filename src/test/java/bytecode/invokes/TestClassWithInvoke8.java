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

import gps.annotations.Constraint;

import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class TestClassWithInvoke8 {

    @Constraint
    boolean test() {
        int prod = 7;
        id(18);
        id(19);
        return prod == 7;

    }

    @Constraint
    boolean test1(int n) {
        return id(n) == n;
    }

    @Constraint
    boolean test2(int n) {
        return neg(n) == n;
    }

    @Constraint
    boolean test3() {
        return neg(neg(1023)) == 1023;
    }

    @Constraint
    boolean test4() {
        int sum = add(4, 5);
        int prod = mul(3, 6);
        return sum == 9 && prod == 18;
    }

    @Constraint
    boolean test5() {
        int sum = add(4, 5);
        int sumprod = mul(3, add(6, 1));
        int prod = mul(3, 6);
        return sum == 9 && prod == 18 && sumprod == 21;
    }

    int id(int a) {
        return a;
    }

    int neg(int a) {
        return -a;
    }

    int add(int a, int b) {
        return a + b;
    }

    int mul(int a, int b) {
        return a * b;
    }

}