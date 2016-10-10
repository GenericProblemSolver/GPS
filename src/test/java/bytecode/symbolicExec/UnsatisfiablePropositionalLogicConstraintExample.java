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

import bytecode.AssertUnsatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

/**
 * Test class for propositional logic constraints.
 * 
 * @author caspar
 *
 */
@RunWith(ConstraintProblemRunner.class)
@AssertUnsatisfiable
public class UnsatisfiablePropositionalLogicConstraintExample {

    /**
     * Intuitively, this constraint is a and a --> b and b-->c and c --> !a
     * @param a
     * @param b
     * @param c
     * @return
     */
    @Constraint
    boolean constraint(boolean a, boolean b, boolean c) {
        return a && (!a || b) && (!b || c) && (!c || !a);
    }
}
