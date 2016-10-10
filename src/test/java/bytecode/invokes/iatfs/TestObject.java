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
package bytecode.invokes.iatfs;

import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class TestObject {

    Object o = new Object();

    @Constraint
    public boolean test() {
        Object o2 = new Object();
        boolean eq1 = o.equals(o);
        boolean hash1 = o.hashCode() == o.hashCode();
        boolean hash2 = o.hashCode() != o2.hashCode();
        boolean hash3 = o.hashCode() > 0;
        boolean ret = hash1 && hash2 && hash3 && eq1;
        return ret;
    }

    public boolean testGetClass() {
        Object o2 = new Object();
        return o.getClass().equals(o2.getClass());
    }

}
