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

/**
 * Checks that conversion operators are created correctly
 * 
 * @author mfunk@tzi.de
 */
@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class Typeconversions {

    @Constraint
    double int2double(int i) {
        return (double) i;
    }

    @Constraint
    float int2float(int i) {
        return (float) i;
    }

    @Constraint
    short int2short(int i) {
        return (short) i;
    }

    @Constraint
    byte int2byte(int i) {
        return (byte) i;
    }

    @Constraint
    long int2long(int i) {
        return (long) i;
    }
}
