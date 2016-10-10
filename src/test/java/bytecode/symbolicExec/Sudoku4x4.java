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

import java.util.Arrays;

/**
 * @author Maximilian Luenert
 */
public class Sudoku4x4 {

    // the playing field with [row][column]
    int[][] field;

    /**
     * construct a new 4x4 sudoku
     */
    public Sudoku4x4() {
        field = new int[4][4];
        init();
    }

    /**
     * init the field
     */
    public void init() {
        //        field[0][0] = 1;
        //        field[0][1] = 2;
        //        field[0][2] = 3;
        //        field[0][3] = 4;
        //        field[1][0] = 3;
        //        field[1][1] = 4;
        //        field[1][2] = 1;
        //        field[1][3] = 2;
        //        field[2][0] = 2;
        //        field[2][1] = 3;
        //        field[2][2] = 4;
        //        field[2][3] = 1;
        //        field[3][0] = 4;
        //        field[3][1] = 1;
        //        field[3][2] = 2;
        //        field[3][3] = 3;

        field[0][0] = 3;
        field[0][1] = 4;
        field[0][2] = 1;
        field[1][1] = 2;
        field[2][2] = 2;
        field[3][1] = 1;
        field[3][2] = 4;
        field[3][3] = 3;
    }

    /**
     * check if the field is valid
     */
    public boolean valid() {
        for (int i = 0; i < field.length; i++) {

            int[] row = new int[4];
            int[] box = new int[4];
            int[] column = field[i].clone();

            for (int j = 0; j < field[i].length; j++) {
                row[j] = field[j][i];
                box[j] = field[(i / 2) * 2 + j / 2][i * 2 % field.length
                        + j % 2];
            }
            if (!(validate(column) && validate(row) && validate(box))) {
                return false;
            }
        }
        return true;
    }

    /**
     * check if the sudoku is solved
     */
    public boolean solved() {
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                if (field[i][j] == 0) {
                    return false;
                }
            }
        }
        return valid();
    }

    /**
     * Validate an array of 4 numbers.
     * This method is used to check a block, a column and a row
     */
    public boolean validate(int[] check) {
        int i = 0;
        Arrays.sort(check);
        for (int number : check) {
            if (number == 0) {
                break;
            }
            if (number != ++i) {
                return false;
            }
        }
        return true;
    }

    /**
     * print the array
     */
    public void print() {
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                System.out.print(field[i][j] + " ");
            }
            System.out.println();
        }
    }
}
