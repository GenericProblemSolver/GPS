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
public class TestInteger {

    private int i = 17;

    private String parse = "3";
    private int a;

    private int intValue;
    private Integer intvalue = new Integer(5);

    private Integer bytes = new Integer(79);
    private Byte bytezz;

    public TestInteger() {
        a = Integer.parseInt(parse);
        intValue = intvalue.intValue();
        bytezz = bytes.byteValue();

    }

    /**
     * This doesn't work because the ClassDisassembler sees the return type as
     * gps/.../iatfs instead of java/lang/Integer
     * 
     * @return
     */
    @Constraint
    public boolean testValueOf() {

        Integer wrap = Integer.valueOf(i);
        return wrap.intValue() == 17;

    }

    @Constraint
    public boolean testParseInt() {
        return 3 == a;
    }

    @Constraint
    public boolean testIntValue() {
        return 5 == intValue;
    }

    @Constraint
    public boolean testbyteValue() {
        return (byte) 0x4f == bytezz;
    }

    @Constraint
    public boolean testEquals() {
        Integer int1 = new Integer(17);
        Integer int2 = new Integer(-13);
        Integer int3 = new Integer(17 - 30);
        boolean con1 = !int1.equals(int3);
        boolean con2 = int2.equals(int3);
        boolean con3 = int2.hashCode() == int3.hashCode();
        return con1 && con2 && con3;
    }

    @Constraint
    public boolean testCompare() {
        Integer int1 = new Integer(17);
        Integer int2 = new Integer(-13);
        return int1.compareTo(int2) == 1;
    }

}
