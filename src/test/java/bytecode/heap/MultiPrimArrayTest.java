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
package bytecode.heap;

import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class MultiPrimArrayTest {

    @Constraint
    public boolean intTest(int i) {
        int[][] array = new int[4][4];
        array[2][3] = 13;

        return i == array[3][3] && i != array[2][3];
    }

    @Constraint
    public boolean shortTest(short i) {
        short[][] array = new short[4][4];
        return i == array[3][3];
    }

    @Constraint
    public boolean longTest(long i) {
        long[][] array = new long[4][4];
        return i == array[3][3];
    }

    @Constraint
    public boolean charTest(char i) {
        char[][] array = new char[4][4];
        return i == array[3][3];
    }

    @Constraint
    public boolean doubleTest(double i) {
        double[][] array = new double[4][4];
        return i == array[3][3];
    }

    @Constraint
    public boolean floatTest(float i) {
        float[][] array = new float[4][4];
        return i == array[3][3];
    }

    //@Constraint
    public boolean byteTest(byte i) {
        byte[][] array = new byte[4][4];
        return i == array[3][3];
    }

    @Constraint
    public boolean boolTest(boolean i) {
        boolean[][] array = new boolean[4][4];
        return i == array[3][3];
    }

    @Constraint
    public boolean lengthTest(int i) {
        int[][] array = new int[4][4];
        return i == array.length && i == array[3].length;
    }

}
