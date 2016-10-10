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
package game.gempuzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import gps.annotations.Heuristic;
import gps.annotations.Move;
import gps.annotations.TerminalTest;
import gps.util.IButtSampleProblem;

/**
 * GemPuzzle represents a simple 15puzzle. It can be constructed with any radix
 * thus having a variable difficulty for the solver.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class GemPuzzle implements IButtSampleProblem {

    /**
     * The width of the puzzle
     */
    private final int width;

    /**
     * The height of the puzzle
     */
    private final int height;

    /**
     * The index of the free field in the puzzle
     */
    private int freeField;

    /**
     * The board of the puzzle represented as a 1-dimensional array
     */
    private final int[] board;

    /**
     * Copy constructor
     * 
     * @param w
     *            the width
     * @param h
     *            the height
     * @param free
     *            the index of the free field
     * @param pBoard
     *            the board
     */
    private GemPuzzle(int w, int h, int free, int[] pBoard) {
        width = w;
        height = h;
        freeField = free;
        board = Arrays.copyOf(pBoard, pBoard.length);
    }

    /**
     * Construct a square shaped board from array. Lowest Value represents the
     * empty field.
     * 
     * @param pBoard
     *            The board represented as one-dimensional array.
     */
    public GemPuzzle(final int[] pBoard) {
        if (pBoard == null) {
            throw new IllegalArgumentException("board must not be null");
        }

        if (pBoard.length < 4) {
            throw new IllegalArgumentException(
                    "board must be at least 2x2 sized");
        }

        board = Arrays.copyOf(pBoard, pBoard.length);

        // determine the radix
        int radix = (int) Math.sqrt(board.length);
        if (radix * radix != board.length) {
            throw new IllegalArgumentException("board must be a squared");
        }
        width = radix;
        height = radix;

        // determine the free field (lowest value)
        {
            int freeField = 0;
            int lowestValue = Integer.MAX_VALUE;
            for (int i = 0; i < board.length; i++) {
                if (board[i] < lowestValue) {
                    freeField = i;
                    lowestValue = board[i];
                }
            }
            this.freeField = freeField;
        }
    }

    /**
     * Contructs a random puzzle
     * 
     * @param radix
     *            the radix of the random puzzle.
     */
    public GemPuzzle(final int radix) {
        this(radix, System.currentTimeMillis());
    }

    /**
     * Constructs a random puzzle
     * 
     * @param radix
     *            the radix of the random puzzle
     * @param seed
     *            the seed for the randomness. Same seed means same generated
     *            puzzle.
     */
    public GemPuzzle(final int radix, final long seed) {
        if (radix <= 1) {
            throw new IllegalArgumentException(
                    "radix must not be lower than 2");
        }

        width = radix;
        height = radix;

        freeField = 0;
        board = new int[width * height];
        for (int i = 0; i < board.length; i++) {
            board[i] = i;
        }

        shuffle(seed, width * height * 1000);
    }

    /**
     * Apply a number of random moves to the board
     * 
     * @param seed
     *            the seed for the random moves
     * @param shuffles
     *            the amount of moves to perform
     */
    public void shuffle(final long seed, final int shuffles) {
        final Random rand = new Random();
        rand.setSeed(seed);

        for (int i = 0; i < shuffles; i++) {
            switch (rand.nextInt(4)) {
            case 0:
                moveUp();
                break;
            case 1:
                moveDown();
                break;
            case 2:
                moveLeft();
                break;
            case 3:
                moveRight();
                break;
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof GemPuzzle)) {
            return false;
        } else {
            final GemPuzzle o = ((GemPuzzle) other);
            return freeField == o.freeField && width == o.width
                    && height == o.height && Arrays.equals(board, o.board);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(freeField, width, height, Arrays.hashCode(board));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n===========\n");
        for (int j = 0; j < width * height; j += width) {
            sb.append('|');
            for (int i = 0; i < width; i++) {
                if (freeField != j + i) {
                    sb.append(String.format("%2d ", board[j + i]));
                } else {
                    sb.append("   ");
                }
            }
            sb.append('|');
            sb.append('\n');
        }
        sb.append("===========\n");
        return sb.toString();
    }

    /**
     * Swap the values of the array in the specified fields
     * 
     * @param array
     *            the array
     * @param idx1
     *            the first index
     * @param idx2
     *            the second index
     */
    private void swap(int array[], int idx1, int idx2) {
        int tmp1 = array[idx1];
        int tmp2 = array[idx2];
        array[idx1] = tmp2;
        array[idx2] = tmp1;
    }

    /**
     * Checks whether the puzzle is solved. The puzzle is considered solved when
     * the array is sorted in ascending order.
     * 
     * @return {@code true} if puzzle is solved, {@code false} otherwise.
     */
    @TerminalTest
    public boolean finished() {
        int[] a = Arrays.copyOf(board, board.length);
        Arrays.sort(a);
        return Arrays.equals(a, board);
    }

    /**
     * Moves the free Field up if possible
     */
    @Move
    public void moveUp() {
        int ff = freeField - width;
        if (ff >= 0) {
            swap(board, ff, freeField);
            freeField = ff;
        }
    }

    /**
     * Moves the free Field down if possible
     */
    @Move
    public void moveDown() {
        int ff = freeField + width;
        if (ff < width * height) {
            swap(board, ff, freeField);
            freeField = ff;
        }
    }

    /**
     * Moves the free Field left if possible
     */
    @Move
    public void moveLeft() {
        if (freeField % width > 0) {
            swap(board, freeField, --freeField);
        }
    }

    /**
     * Moves the free Field right if possible
     */
    @Move
    public void moveRight() {
        if (freeField % width < width - 1) {
            swap(board, freeField, ++freeField);
        }
    }

    @Override
    public Object clone() {
        return new GemPuzzle(width, height, freeField, board);
    }

    /**
     * Returns a list of successors which are full clones
     */
    public List<GemPuzzle> getSuccessors() {
        List<GemPuzzle> lst = new ArrayList<GemPuzzle>(4);
        GemPuzzle a = (GemPuzzle) clone();
        GemPuzzle b = (GemPuzzle) clone();
        GemPuzzle c = (GemPuzzle) clone();
        GemPuzzle d = (GemPuzzle) clone();
        a.moveDown();
        b.moveUp();
        c.moveLeft();
        d.moveRight();
        if (a.freeField != freeField) {
            lst.add(a);
        }
        if (b.freeField != freeField) {
            lst.add(b);
        }
        if (c.freeField != freeField) {
            lst.add(c);
        }
        if (d.freeField != freeField) {
            lst.add(d);
        }
        return lst;
    }

    @Heuristic
    public int heuristic() {
        // we get this from bytecode
        int dest[] = new int[board.length];
        for (int i = 0; i < board.length; i++) {
            dest[i] = i;
        }

        // compare that with the current state
        int ret = 0;
        for (int i = 0; i < board.length; i++) {
            int s = dest[i] - board[i];
            s = s < 0 ? -s : s;
            ret += s;
        }
        return -ret;
    }

    @Override
    public Object getIdentifier() {
        return "Gem" + Objects.hash(Arrays.hashCode(board), freeField, height,
                width);
    }
}