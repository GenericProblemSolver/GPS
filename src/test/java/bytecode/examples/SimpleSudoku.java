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
package bytecode.examples;

import org.junit.runner.RunWith;

import bytecode.AssertSatisfiable;
import bytecode.ConstraintProblemRunner;
import gps.annotations.Constraint;
import gps.annotations.Variable;

@RunWith(ConstraintProblemRunner.class)
@AssertSatisfiable
public class SimpleSudoku {
    int[][] initFields = new int[4][4];

    @Variable
    int[][] fields = new int[4][4];
    //Store this since we can't calculate sqrt right now
    int blocksize = (int) Math.sqrt(initFields.length);

    public SimpleSudoku() {
        initFields[0][0] = 1;
        initFields[1][0] = 0;
        initFields[2][0] = 0;
        initFields[3][0] = 4;
        initFields[0][1] = 0;
        initFields[1][1] = 4;
        initFields[2][1] = 0;
        initFields[3][1] = 0;
        initFields[0][2] = 0;
        initFields[1][2] = 0;
        initFields[2][2] = 0;
        initFields[3][2] = 0;
        initFields[0][3] = 0;
        initFields[1][3] = 1;
        initFields[2][3] = 0;
        initFields[3][3] = 2;

        print();

    }

    @Constraint
    boolean testSudoku() {
        for (int x = 0; x < fields.length; ++x) {
            for (int y = 0; y < fields.length; ++y) {
                if (initFields[x][y] != 0 && fields[x][y] != initFields[x][y]) {
                    return false;
                }
                if (fields[x][y] > fields.length || fields[x][y] < 1) {
                    return false;
                }
            }
        }

        //Rows
        for (int x = 0; x < fields.length; ++x) {
            for (int i = 0; i < fields.length; ++i) {
                for (int j = i + 1; j < fields.length; ++j) {
                    if (fields[x][i] == fields[x][j]) {
                        return false;
                    }
                }
            }
        }
        // Columns
        for (int y = 0; y < fields.length; ++y) {
            for (int i = 0; i < fields.length; ++i) {
                for (int j = i + 1; j < fields.length; ++j) {
                    if (fields[i][y] == fields[j][y]) {
                        return false;
                    }
                }
            }
        }

        //Boxes
        for (int i = 0; i < fields.length; ++i) {
            int bx = (i % blocksize) * blocksize;
            int by = (i / blocksize) * blocksize;
            for (int j = 0; j < fields.length; ++j) {
                int x = bx + (j % blocksize);
                int y = by + (j / blocksize);

                for (int k = j + 1; k < fields.length; ++k) {
                    int x2 = bx + (k % blocksize);
                    int y2 = by + (k / blocksize);

                    if (fields[x][y] == fields[x2][y2]) {
                        return false;
                    }

                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            for (int j = 0; j < fields[i].length; j++) {
                b.append(fields[i][j] + " ");
            }
            b.append("\n");
        }
        return b.toString();
    }

    public void print() {
        for (int i = 0; i < fields.length; i++) {
            for (int j = 0; j < fields[i].length; j++) {
                System.out.print(initFields[i][j] + " ");
            }
            System.out.println();
        }
    }
}
