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

import gps.annotations.Constraint;
import org.junit.runner.RunWith;
import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class TestDouble {

    private double i = 16;

    private String parse = "3";
    private double a;

    private double doubleValue;
    private Double doublevalue = new Double(5);

    private Double bytes = new Double(79);
    private Byte bytezz;

    public TestDouble() {
        a = Double.parseDouble(parse);
        doubleValue = doublevalue.intValue();
        bytezz = bytes.byteValue();
    }

    @Constraint
    public boolean testValueOf() {

        Double wrap = Double.valueOf(i);
        return wrap.doubleValue() == 16;

    }

    @Constraint
    public boolean testParseDouble() {
        return 3 == a;
    }

    @Constraint
    public boolean testDoubleValue() {
        return 5 == doubleValue;
    }

    @Constraint
    public boolean testbyteValue() {
        return (byte) 0x4f == bytezz;
    }

    @Constraint
    public boolean testEquals() {
        Double Double1 = new Double(17);
        Double Double2 = new Double(-13);
        Double Double3 = new Double((17 - 30));
        boolean con1 = !Double1.equals(Double3);
        boolean con2 = Double2.equals(Double3);
        boolean con3 = Double2.hashCode() == Double3.hashCode();
        return con1 && con2 && con3;
    }

    @Constraint
    public boolean testCompare() {
        Double Double1 = new Double(17);
        Double Double2 = new Double(-13);
        return Double1.compareTo(Double2) == 1;
    }
}
