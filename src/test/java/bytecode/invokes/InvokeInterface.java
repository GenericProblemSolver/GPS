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
public class InvokeInterface {

    static interface IBlub {
        int blub();
    }

    static class ABlub implements IBlub {
        @Override
        public int blub() {
            return 7;
        }
    }

    static class BBlub implements IBlub {
        @Override
        public int blub() {
            return 8;
        }
    }

    IBlub b = new BBlub();

    @Constraint
    boolean test() {
        return b.blub() == 8;
    }

    @Constraint
    boolean test2() {
        return b.blub() != (new ABlub()).blub();
    }

    @Constraint
    boolean test3() {
        IBlub a = new ABlub();
        IBlub b = new BBlub();
        return b.blub() != a.blub() && b.blub() == 8;
    }
}
