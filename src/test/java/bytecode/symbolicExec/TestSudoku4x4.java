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

import bytecode.symbolicExec.Sudoku4x4;

/**
 * Created by max on 1/29/16.
 */
public class TestSudoku4x4 {
    private static Sudoku4x4 sudoku = new Sudoku4x4();

    public static void main(String[] args) {
        sudoku.print();
        System.out.println("Is valid: " + sudoku.valid());
        System.out.println("Is solved: " + sudoku.solved());
    }
}
