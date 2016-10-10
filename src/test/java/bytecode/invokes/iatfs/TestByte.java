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
public class TestByte {
    private Byte bytevalue = new Byte((byte) 5);
    private byte i = 17;
    private byte byteValue;
    private byte a;
    private String parse = "3";

    public TestByte() {
        a = Byte.parseByte(parse);
        byteValue = bytevalue.byteValue();

    }

    @Constraint
    public boolean testValueOf() {
        Byte wrap = Byte.valueOf(i);
        return wrap.byteValue() == 17;
    }

    @Constraint
    public boolean testParseByte() {
        return 3 == a;
    }

    @Constraint
    public boolean testByteValue() {
        return 5 == byteValue;
    }

    @Constraint
    public boolean testEquals() {
        Byte Byte1 = new Byte((byte) 17);
        Byte Byte2 = new Byte((byte) -13);
        Byte Byte3 = new Byte((byte) (17 - 30));
        boolean con1 = !Byte1.equals(Byte3);
        boolean con2 = Byte2.equals(Byte3);
        boolean con3 = Byte2.hashCode() == Byte3.hashCode();
        return con1 && con2 && con3;
    }

    @Constraint
    public boolean testCompare() {
        Byte Byte1 = new Byte((byte) 17);
        Byte Byte2 = new Byte((byte) -13);
        return Byte1.compareTo(Byte2) == 1;
    }

}
