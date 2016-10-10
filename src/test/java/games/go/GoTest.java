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
package games.go;

import static org.junit.Assert.*;

import java.util.Scanner;

import org.junit.Test;

public class GoTest {

    private static String moveFormatter(final String[] moves) {
        StringBuilder sb = new StringBuilder();
        for (String move : moves) {
            sb.append(move);
            sb.append("\n");
        }
        return sb.toString();
    }

    @Test
    /**
     * Performs a sample capture and checks if all white stones are removed (and
     * if no black stones are removed)
     */
    public void testCapture() {
        // Board situation:
        // . A B C D E F G H I
        // 1 . . . . . ? O O O 1
        // 2 . . . . . X O O O 2
        // 3 . . . . . X O O O 3
        // 4 . . . . . X X X X 4
        // 5 . . . . . . . . . 5
        // 6 . . . . . . . . . 6
        // 7 . . . . . . . . . 7
        // 8 . . . . . . . . . 8
        // 9 . . . . . . . . . 9
        // . A B C D E F G H I
        //
        // Black (X) moving to "?" should yield
        //
        // . A B C D E F G H I
        // 1 . . . . . X . . . 1
        // 2 . . . . . X . . . 2
        // 3 . . . . . X . . . 3
        // 4 . . . . . X X X X 4
        // 5 . . . . . . . . . 5
        // 6 . . . . . . . . . 6
        // 7 . . . . . . . . . 7
        // 8 . . . . . . . . . 8
        // 9 . . . . . . . . . 9
        // . A B C D E F G H I
        final String[] moves = new String[] {
                // establish situation
                "4I", "3I", "4H", "3H", "4G", "3G", "4F", "2I", "3F", "2H",
                "2F", "2G", "Pass", "1I", "Pass", "1H", "Pass", "1G",
                // perform move in question
                "1F",
                // terminate game
                "Pass", "Pass" };
        final String moveSequence = moveFormatter(moves);
        final Scanner scanner = new Scanner(moveSequence);
        int[][] result = Go.play(scanner, 9).getBoard();
        int blackStones = 0;
        for (int x = 0; x < Go.BOARD_SIZE; x++) {
            for (int y = 0; y < Go.BOARD_SIZE; y++) {
                if (result[x][y] == Go.WHITE) {
                    fail("There is a white stone on the board, but there should be none.");
                } else if (result[x][y] == Go.BLACK) {
                    blackStones++;
                }
            }
        }
        if (blackStones != 7) {
            fail("There should be exactly 7 black stones on the board now, but there are "
                    + String.valueOf(blackStones));
        }
    }

}
