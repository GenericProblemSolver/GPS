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
public class TestShort {
    private Short shortvalue = new Short((short) 5);
    private short i = 17;
    private short shortValue;
    private short a;
    private String parse = "3";

    public TestShort() {
        a = Short.parseShort(parse);
        shortValue = shortvalue.shortValue();

    }

    @Constraint
    public boolean testValueOf() {
        Short wrap = Short.valueOf(i);
        return wrap.shortValue() == 17;
    }

    @Constraint
    public boolean testParseShort() {
        return 3 == a;
    }

    @Constraint
    public boolean testShortValue() {
        return 5 == shortValue;
    }

    @Constraint
    public boolean testEquals() {
        Short Short1 = new Short((short) 17);
        Short Short2 = new Short((short) -13);
        Short Short3 = new Short((short) (17 - 30));
        boolean con1 = !Short1.equals(Short3);
        boolean con2 = Short2.equals(Short3);
        boolean con3 = Short2.hashCode() == Short3.hashCode();
        return con1 && con2 && con3;
    }

    @Constraint
    public boolean testCompare() {
        Short Short1 = new Short((short) 17);
        Short Short2 = new Short((short) -13);
        return Short1.compareTo(Short2) > 0;
    }

}
