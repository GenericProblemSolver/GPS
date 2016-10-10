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
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class SwitchTest {

    @Constraint
    public boolean tableSwitch(int i) {
        switch (i) {
        case 1:
        case 2:
        case 4:
        case 5:
            return false;
        case 6:
            return true;
        default:
            return false;
        }
    }

    @Constraint
    public boolean tableSwitch2(int i) {
        switch (i) {
        case 0:
            return true;
        case 1:
        case 2:
        case 4:
            return false;
        case 5:
            return true;
        default:
            return false;
        }
    }

    @Constraint
    public boolean lookupSwitch(int i) {
        switch (i) {
        case 1:
        case 10:
        case 20:
        case 100:
        case 1000:
            return false;
        case 1003:
            return true;
        default:
            return false;
        }
    }

}
