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

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class DoubleCompare {

    Double test = new Double(0);
    Double test2 = new Double(0);

    public DoubleCompare() {
        test = new Double(test.doubleValue() + new Double(2));
        test2 = new Double(test2.doubleValue() + new Double(2));

        // if (Double.compare(equal,
        // new Double(Double.compare(test, test2))) == 0) {
        // test = 0.0;
        // } else {
        // test = 1.0;
        // }

    }

    @Constraint
    public boolean equals() {
        return test.equals(test2);
    }

}
