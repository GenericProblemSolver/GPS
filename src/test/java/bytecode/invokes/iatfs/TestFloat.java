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
public class TestFloat {
    private Float floatvalue = new Float((float) 5);
    private float i = 17;
    private float floatValue;
    private float a;
    private Byte bytezz;
    private String parse = "3";
    private Float bytes = new Float((float) 79);

    public TestFloat() {
        a = Float.parseFloat(parse);
        floatValue = floatvalue.floatValue();
        bytezz = bytes.byteValue();

    }

    @Constraint
    public boolean testValueOf() {
        Float wrap = Float.valueOf(i);
        return wrap.floatValue() == 17;
    }

    @Constraint
    public boolean testParseFloat() {
        return 3 == a;
    }

    @Constraint
    public boolean testFloatValue() {
        return 5 == floatValue;
    }

    @Constraint
    public boolean testByteValue() {
        return (byte) 0x4f == bytezz;
    }

    @Constraint
    public boolean testEquals() {
        Float Float1 = new Float(17);
        Float Float2 = new Float(-13);
        Float Float3 = new Float((17 - 30));
        boolean con1 = !Float1.equals(Float3);
        boolean con2 = Float2.equals(Float3);
        boolean con3 = Float2.hashCode() == Float3.hashCode();
        return con1 && con2 && con3;
    }

    @Constraint
    public boolean testCompare() {
        Float Float1 = new Float(17);
        Float Float2 = new Float(-13);
        return Float1.compareTo(Float2) == 1;
    }

}
