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
public class OverloadInvoke2 {

    public class Inner1 {
        int value1;
        int value2;

        public Inner1(int i, int j) {
            value1 = i;
            value2 = j;
        }

        public Inner1(int i) {
            value1 = i;
            value2 = i * 3;
        }

        public int getSum() {
            return value1 + value2;
        }

        public int getSum(int i) {
            return value1 + value2 + i;
        }
    }

    public class Inner2 {
        int value;

        public Inner2(Inner1 in) {
            value = in.getSum();
        }

        public Inner2(Inner1 in, int i) {
            value = in.getSum(i);
        }

        public int getValue() {
            return value;
        }

    }

    @Constraint
    public boolean sameValues() {
        Inner1 in1 = new Inner1(5);
        Inner2 in2 = new Inner2(in1);
        return in1.getSum() == in2.getValue();
    }

    @Constraint
    public boolean sameValues2() {
        Inner1 in1 = new Inner1(5, 6);
        Inner2 in2 = new Inner2(in1);
        return in1.getSum() == in2.getValue();
    }

    @Constraint
    public boolean sameValues3(int i) {
        Inner1 in1 = new Inner1(2, 6);
        Inner1 in12 = new Inner1(2);
        Inner2 in2 = new Inner2(in1, 5);
        boolean eq1 = in1.getSum() == in12.getSum();
        boolean eq2 = in1.getSum(i) == in2.getValue();
        return eq1 && eq2;
    }

}
