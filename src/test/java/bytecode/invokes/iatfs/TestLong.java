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
public class TestLong {
    private Long longvalue = new Long((long) 5);
    private long i = 17;
    private long longValue;
    private long a;
    private Byte bytezz;
    private String parse = "3";
    private Long bytes = new Long((long) 79);

    public TestLong() {
        a = Long.parseLong(parse);
        longValue = longvalue.longValue();
        bytezz = bytes.byteValue();

    }

    @Constraint
    public boolean testValueOf() {
        Long wrap = Long.valueOf(i);
        return wrap.longValue() == 17;
    }

    @Constraint
    public boolean testParseLong() {
        return 3 == a;
    }

    @Constraint
    public boolean testLongValue() {
        return 5 == longValue;
    }

    @Constraint
    public boolean testByteValue() {
        return (byte) 0x4f == bytezz;
    }

    @Constraint
    public boolean testEquals() {
        Long Long1 = new Long(17);
        Long Long2 = new Long(-13);
        Long Long3 = new Long((17 - 30));
        boolean con1 = !Long1.equals(Long3);
        boolean con2 = Long2.equals(Long3);
        // does not work until lushr is merged
        // boolean con3 = Long2.hashCode() == Long3.hashCode();
        return con1 && con2;// && con3;
    }

    @Constraint
    public boolean testCompare() {
        Long Long1 = new Long(17);
        Long Long2 = new Long(-13);
        return Long1.compareTo(Long2) == 1;
    }

}
