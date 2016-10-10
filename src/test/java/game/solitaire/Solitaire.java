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
package game.solitaire;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import gps.annotations.Action;
import gps.annotations.Move;
import gps.annotations.Utility;

import gps.util.Tuple;

/**
 * Go, modeled in such a way that each instance of the game is a self-contained,
 * deeply copied object that automatic solvers can work with
 * 
 * @author kohorst@uni-bremen.de
 */
// @PMCTS
public class Solitaire {

    /**
     * Represents an unoccupied field
     */
    final static int BLANK = 0;

    /**
     * Represents a field occupied by a peg
     */
    final static int PEG = 1;

    /**
     * Invalid fields are not part of the game.
     */
    final static int INVALID = 3;

    /**
     * Store directions as ints
     */
    final static int UP = 4;
    final static int RIGHT = 5;
    final static int DOWN = 6;
    final static int LEFT = 7;

    /**
     * Board is represented as a square the edge areas of which are filled with
     * {@link Solitaire#INVALID} Values other than the given one require a
     * rewrite of other code areas.
     */
    final static int BOARD_SIZE = 7;

    /**
     * Initializes the board. Valid values are 0 = BLANK, 1 = PEG, 2 = INVALID
     */
    private int[][] board = new int[BOARD_SIZE][BOARD_SIZE];

    /**
     * Stores a reference board that is printed when playing interactively
     */
    private char[][] referenceBoard = new char[BOARD_SIZE][BOARD_SIZE];

    /**
     * Instantiates a new Game with the traditional English layout
     */
    public Solitaire() {
        // Initialize the European board
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                // invalidate the corner areas that are in English layout
                if ((x == 0 && y == 0) || (x == 0 && y == 1)
                        || (x == 1 && y == 0) || (x == BOARD_SIZE - 1 && y == 0)
                        || (x == BOARD_SIZE - 1 && y == 1)
                        || (x == BOARD_SIZE - 2 && y == 0)
                        || (x == 0 && y == BOARD_SIZE - 1)
                        || (x == 0 && y == BOARD_SIZE - 2) || (x == 1 && y == 1)
                        || (x == BOARD_SIZE - 2 && y == BOARD_SIZE - 2)
                        || (x == BOARD_SIZE - 2 && y == 1)
                        || (x == 1 && y == BOARD_SIZE - 2)
                        || (x == 1 && y == BOARD_SIZE - 1)
                        || (x == BOARD_SIZE - 1 && y == BOARD_SIZE - 1)
                        || (x == BOARD_SIZE - 1 && y == BOARD_SIZE - 2)
                        || (x == BOARD_SIZE - 2 && y == BOARD_SIZE - 1)) {
                    board[x][y] = INVALID;
                } else {
                    // set pegs to all other fields
                    board[x][y] = PEG;
                }
            }
        }
        // finally, put a hole in the center
        board[3][3] = BLANK;
        setUpReferenceBoard();
    }

    /**
     * Determines the possible moves and returns them as a List of tuples. The
     * first element is a Tuple and represents the coordinates of the peg that
     * can be moved, the second is the direction in which it can be moved
     * 
     * @return A List of tuples that represents the peg and the direction to
     *         move it to. If the List is empty, the game is over
     */
    @Action
    public List<Tuple<Tuple<Integer, Integer>, Integer>> getMoves() {
        final ArrayList<Tuple<Tuple<Integer, Integer>, Integer>> moves = new ArrayList<>();
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                if (board[x][y] == BLANK) {
                    // we found y possible destination to jump to
                    if ((x + 2 < BOARD_SIZE) && (board[x + 1][y] == PEG)
                            && (board[x + 2][y] == PEG)) {
                        // we can jump to it from the right (going left)
                        Tuple<Integer, Integer> coords = new Tuple<>(x + 2, y);
                        Tuple<Tuple<Integer, Integer>, Integer> move = new Tuple<>(
                                coords, UP);
                        moves.add(move);
                    }
                    if ((x - 2 >= 0) && (board[x - 1][y] == PEG)
                            && (board[x - 2][y] == PEG)) {
                        // we can jump to it from the left (going right)
                        Tuple<Integer, Integer> coords = new Tuple<>(x - 2, y);
                        Tuple<Tuple<Integer, Integer>, Integer> move = new Tuple<>(
                                coords, DOWN);
                        moves.add(move);
                    }
                    if ((y + 2 < BOARD_SIZE) && (board[x][y + 1] == PEG)
                            && (board[x][y + 2] == PEG)) {
                        // we can jump to it from the bottom (going up)
                        Tuple<Integer, Integer> coords = new Tuple<>(x, y + 2);
                        Tuple<Tuple<Integer, Integer>, Integer> move = new Tuple<>(
                                coords, LEFT);
                        moves.add(move);
                    }
                    if ((y - 2 >= 0) && (board[x][y - 1] == PEG)
                            && (board[x][y - 2] == PEG)) {
                        // we can jump to it from the top (going down)
                        Tuple<Integer, Integer> coords = new Tuple<>(x, y - 2);
                        Tuple<Tuple<Integer, Integer>, Integer> move = new Tuple<>(
                                coords, RIGHT);
                        moves.add(move);
                    }
                }
            }
        }
        return moves;
    }

    /**
     * Picks out the peg contained in the move, makes it jump over the adjacent
     * peg in the given direction
     * 
     * @param pMove
     *            a Tuple containing the coordinates of the pair to pick up and
     *            the direction of the adjacent coordinate to jump over
     */
    @Move
    public void setPeg(final Tuple<Tuple<Integer, Integer>, Integer> pMove) {
        Tuple<Integer, Integer> coords = pMove.getX();
        int x = coords.getX();
        int y = coords.getY();
        board[x][y] = BLANK;
        switch (pMove.getY()) {
        case UP:
            board[x - 2][y] = PEG;
            board[x - 1][y] = BLANK;
            break;
        case DOWN:
            board[x + 2][y] = PEG;
            board[x + 1][y] = BLANK;
            break;
        case RIGHT:
            board[x][y + 2] = PEG;
            board[x][y + 1] = BLANK;
            break;
        case LEFT:
            board[x][y - 2] = PEG;
            board[x][y - 1] = BLANK;
        }
    }

    /**
     * Play Solitaire!
     * 
     * @param args
     *            args are discarded
     */
    public static void main(String[] args) {
        play(null);
    }

    /**
     * Lets a user play interactively via command line
     */
    public static void play(final Scanner pScanner) {
        Scanner scanner;
        if (pScanner == null) {
            scanner = new Scanner(System.in);
        } else {
            scanner = pScanner;
        }
        Solitaire sol = new Solitaire();
        do {
            System.out.println(sol.refToString());
            System.out.println(sol.toString());
            StringBuilder sb = new StringBuilder();
            List<Tuple<Tuple<Integer, Integer>, Integer>> moves = sol
                    .getMoves();
            if (moves.isEmpty()) {
                break;
            }
            System.out.println("Valid moves are: \n");
            for (Tuple<Tuple<Integer, Integer>, Integer> move : moves) {
                sb.append(sol.moveToString(move));
                sb.append(" ");
            }
            System.out.println(sb.toString());
            do {
                String input = "";
                input = scanner.nextLine();
                Tuple<Tuple<Integer, Integer>, Integer> move = sol
                        .moveFromString(input);
                if (move == null) {
                    System.out.println("Please specify a valid move!\n");
                } else {
                    sol.setPeg(move);
                    break;
                }
            } while (true);
        } while (true);
        scanner.close();
        System.out.println("You scored " + sol.getRating()
                + " Points. Remeber: The lower, the better.");
    }

    /**
     * Encode the given Move into a String
     * 
     * @param pMove
     *            the Move to encode
     * @return A String representing the move
     */
    private String moveToString(
            final Tuple<Tuple<Integer, Integer>, Integer> pMove) {
        StringBuilder sb = new StringBuilder();
        final Tuple<Integer, Integer> position = pMove.getX();
        sb.append(referenceBoard[position.getX()][position.getY()]);
        sb.append("->");
        switch (pMove.getY()) {
        case UP:
            sb.append("up");
            break;
        case DOWN:
            sb.append("down");
            break;
        case RIGHT:
            sb.append("right");
            break;
        case LEFT:
            sb.append("left");
            break;
        default:
            sb.append("invalid!");
        }
        return sb.toString();
    }

    /**
     * Creates a move out of a given String
     * 
     * @param pMove
     *            The String to encode in a move
     * @return The Move represented by the String. {@code null} if String is not
     *         a Move
     */
    private Tuple<Tuple<Integer, Integer>, Integer> moveFromString(
            final String pMove) {
        Tuple<Integer, Integer> coords = null;
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                if (referenceBoard[x][y] == pMove.charAt(0)) {
                    coords = new Tuple<>(x, y);
                }
            }
        }
        int direction = 0;
        System.out.print(pMove.substring(2));
        switch (pMove.substring(2)) {
        case "up":
            direction = UP;
            break;
        case "down":
            direction = DOWN;
            break;
        case "right":
            direction = RIGHT;
            break;
        case "left":
            direction = LEFT;
        }
        if (direction == 0 || coords == null) {
            return null;
        } else {
            System.out.println(new Tuple<>(coords, direction));
            return new Tuple<>(coords, direction);
        }
    }

    /**
     * Returns a String representing a reference board that is helpful when
     * playing interactively.
     * 
     * @return A String representing a reference board
     */
    private void setUpReferenceBoard() {
        final char[] chars = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
                'k', 'l', 'm', 'n', 'o', 'p', 'x', 'P', 'O', 'N', 'M', 'L', 'K',
                'J', 'I', 'H', 'G', 'F', 'E', 'D', 'C', 'B', 'A' };
        int counter = 0;
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                if (board[x][y] != INVALID) {
                    referenceBoard[x][y] = chars[counter];
                    counter++;
                } else {
                    referenceBoard[x][y] = ' ';
                }
            }
        }
    }

    /**
     * Returns the rating for the current board. Method call is only meaningful
     * when the board is in a terminal state
     * 
     * @return An Integer representing the goodness of the solution. The higher,
     *         the better. Non-optimal solutions are negative; optimal solution
     *         is 0.
     */
    @Utility
    public int getRating() {
        int rate = 0;
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                if (board[x][y] == PEG) {
                    rate++;
                }
            }
        }
        if (rate == 1 && board[3][3] == PEG) {
            rate = 0;
        }
        return rate * (-1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Solitaire)) {
            return false;
        }
        Solitaire otherSoli = (Solitaire) other;
        return Arrays.deepEquals(otherSoli.board, board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    /**
     * Creates an ASCII representation of the current board
     * 
     * @return the board as String
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                if (board[x][y] == PEG) {
                    sb.append(".");
                } else if (board[x][y] == BLANK) {
                    sb.append("o");
                } else {
                    sb.append(" ");
                }
            }
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Returns the reference board as a String
     * 
     * @return the reference board as a String
     */
    public String refToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                sb.append(referenceBoard[x][y]);
            }
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

}
