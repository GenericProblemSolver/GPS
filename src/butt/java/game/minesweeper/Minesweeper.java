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
package game.minesweeper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gps.annotations.Action;
import gps.annotations.Move;
import gps.annotations.TerminalTest;
import gps.annotations.Utility;
import gps.util.IButtSampleProblem;

/**
 * A game of Minesweeper.
 *
 * The goal is to uncover all black fields without stepping on mines.
 *
 * @author wahler@tzi.de
 */
public class Minesweeper implements IButtSampleProblem {

    /**
     * the hex value for the block char
     */
    private static final int BLOCK_SYMBOL = 0x2588;

    /**
     * the hex value for the mine char
     */
    private static final int MINE_SYMBOL = 0x002A;

    /**
     * the hex value for the flag char
     */
    private static final int FLAG_SYMBOL = 0x2691;

    /**
     * the board
     */
    private int[][] board;

    /**
     * the random number generator used to populate the board
     */
    public Random r;

    /**
     * the amount of mines left on the board
     */
    private int minesLeft;

    /**
     * the amount of lives the player has
     */
    private int lives;

    /**
     * the amount of uncovered fields
     */
    private int uncovered;

    /**
     * Creates a new game of Minesweeper with the given board size and number of
     * mines. If you choose to play a classic game you have one life, otherwise
     * between 3 and 7, depending on the amount of mines in combination with the
     * amount of fields.
     *
     * Throws an {@link IllegalArgumentException} if the size of the board is
     * less than 10 * 10, more than 60 * 60, the amount of mines is less than 10
     * or more than the amount of fields - 9.
     *
     * @param length
     *            the length of the board
     *
     * @param height
     *            the length of the board
     *
     * @param mines
     *            the amount of mines on the board
     *
     * @param classic
     *            determines if you get more than one life.
     */
    public Minesweeper(final int length, final int height, final int mines,
            final boolean classic) {
        if (length < 10 || length > 60) {
            throw new IllegalArgumentException(
                    "The length needs to be between 10 and 60.");
        }
        if (height < 10 || height > 60) {
            throw new IllegalArgumentException(
                    "The height needs to be between 10 and 60.");
        }
        if (mines < 10 || mines > length * height - 9) {
            throw new IllegalArgumentException(
                    "The amount of mines needs to be between 10 and "
                            + (length * height - 9) + ".");
        }
        if (classic) {
            lives = 1;
        } else {
            lives = 8 - Math.floorDiv(length * height, mines);
            if (lives < 3) {
                lives = 3;
            }
        }
        board = new int[length][height];
        minesLeft = mines;
        uncovered = 0;
        r = new Random(System.nanoTime());
        System.out.println(this.toString());
    }

    /**
     * Alternate constructor where the seed can be given.
     *
     * @param length
     *            the length of the board
     *
     * @param height
     *            the length of the board
     *
     * @param mines
     *            the amount of mines on the board
     *
     * @param classic
     *            determines if you get more than one life.
     *
     * @param seed
     *            seed for the random number generator
     */
    public Minesweeper(final int length, final int height, final int mines,
            final boolean classic, final long seed) {
        if (length < 10 || length > 60) {
            throw new IllegalArgumentException(
                    "The length needs to be between 10 and 60.");
        }
        if (height < 10 || height > 60) {
            throw new IllegalArgumentException(
                    "The height needs to be between 10 and 60.");
        }
        if (mines < 10 || mines > length * height - 9) {
            throw new IllegalArgumentException(
                    "The amount of mines needs to be between 10 and the "
                            + "maximum number of fields.");
        }
        if (classic) {
            lives = 1;
        } else {
            lives = 8 - Math.floorDiv(length * height, mines);
            if (lives < 3) {
                lives = 3;
            }
        }
        board = new int[length][height];
        minesLeft = mines;
        uncovered = 0;
        r = new Random(seed);
        System.out.println(this.toString());
    }

    /**
     * Populates the field with mines. Will never cover the field which was
     * clicked first and all direct neighbors to it.
     *
     * @param x
     *            the x coordinate of the first click
     *
     * @param y
     *            the y coordinate of the first click
     */
    private void populate(final int x, final int y) {
        long time = System.nanoTime();
        int mineX;
        int mineY;
        int col = 0;
        for (int m = minesLeft; m > 0; m--) {
            mineX = r.nextInt(board.length);
            mineY = r.nextInt(board[0].length);
            while (mineX == x && mineY == y || board[mineX][mineY] == 9
                    || surroundingFirst(x, y, mineX, mineY)) {
                mineX = r.nextInt(board.length);
                mineY = r.nextInt(board[0].length);
                col++;
            }
            board[mineX][mineY] = 9;
            for (int addX = -1; addX <= 1; addX++) {
                for (int addY = -1; addY <= 1; addY++) {
                    int xAdd = mineX + addX;
                    int yAdd = mineY + addY;
                    if (xAdd < 0 || yAdd < 0 || xAdd >= board.length
                            || yAdd >= board[0].length) {
                        continue;
                    }
                    if (board[xAdd][yAdd] == 9) {
                        continue;
                    }
                    board[xAdd][yAdd]++;
                }
            }
        }
        uncovered++;
        cover(x, y);
        uncoverNeighbors(x, y);
        System.out.println("Population took " + (System.nanoTime() - time)
                + "ns and \n" + "had " + col + " collision(s).");
    }

    private void populate2(final int x, final int y) {
        long time = System.nanoTime();
        List<Integer> empty = new ArrayList<>();

        for (int xe = 0; xe < board.length; xe++) {
            for (int ye = 0; ye < board[0].length; ye++) {
                if (!surroundingFirst(x, y, xe, ye)) {
                    empty.add(xe | ye << 6);
                }
            }
        }
        for (int m = minesLeft; m > 0; m--) {
            int index = r.nextInt(empty.size());
            int i = empty.get(index);
            empty.remove(index);
            int mineX = i & 0xfff >> 6;
            int mineY = i >> 6 & 0x3f;
            board[mineX][mineY] = 9;
            for (int addX = -1; addX <= 1; addX++) {
                for (int addY = -1; addY <= 1; addY++) {
                    int xAdd = mineX + addX;
                    int yAdd = mineY + addY;
                    if (xAdd < 0 || yAdd < 0 || xAdd >= board.length
                            || yAdd >= board[0].length) {
                        continue;
                    }
                    if (board[xAdd][yAdd] == 9) {
                        continue;
                    }
                    board[xAdd][yAdd]++;
                }
            }
        }

        uncovered++;
        cover(x, y);
        uncoverNeighbors(x, y);
        System.out.println(
                "Population2 took " + (System.nanoTime() - time) + "ns.");
    }

    /**
     * Checks if the given coordinates are direct neighbors to the first click
     *
     * @param firstX
     *            the x coordinate of the first click
     *
     * @param firstY
     *            the y coordinate of the first click
     *
     * @param checkX
     *            the x coordinate of the field to check
     *
     * @param checkY
     *            the y coordinate of the field to check
     *
     * @return <code>true</code>, when next to the first click,
     *         <code>false</code> otherwise
     */
    private boolean surroundingFirst(final int firstX, final int firstY,
            final int checkX, final int checkY) {
        for (int surX = -1; surX <= 1; surX++) {
            for (int surY = -1; surY <= 1; surY++) {
                if (firstX + surX == checkX && firstY + surY == checkY) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Covers all fields (except the first clicked) after the initial population
     * of mines.
     *
     * @param x
     *            the x coordinate of the first click
     *
     * @param y
     *            the y coordinate of the first click
     */
    private void cover(final int x, final int y) {
        for (int cx = 0; cx < board.length; cx++) {
            for (int cy = 0; cy < board[0].length; cy++) {
                if (cx == x && cy == y) {
                    continue;
                }
                if (board[cx][cy] == 0) {
                    board[cx][cy] = -10;
                } else {
                    board[cx][cy] = -board[cx][cy];
                }
            }
        }
    }

    /**
     * Uncovers all neighbors of the given field. Will also uncover the
     * neighbors of the uncovered fields if they are also 0s.
     *
     * @param x
     *            the x coordinate of the field whose neighbors ought to be
     *            checked
     *
     * @param y
     *            the y coordinate of the field whose neighbors ought to be
     *            checked
     */
    private void uncoverNeighbors(final int x, final int y) {
        if (x < 0 || x > board.length || y < 0 || y > board[0].length) {
            return;
        }
        if (board[x][y] == 0) {
            for (int nx = x - 1; nx <= x + 1; nx++) {
                for (int ny = y - 1; ny <= y + 1; ny++) {
                    if (nx >= 0 && nx < board.length && ny >= 0
                            && ny < board[0].length) {
                        if (board[nx][ny] >= 0) {
                            continue;
                        }
                        if (board[nx][ny] == -10) {
                            board[nx][ny] = 0;
                            uncovered++;
                            uncoverNeighbors(nx, ny);
                        } else {
                            uncovered++;
                            board[nx][ny] = -board[nx][ny];
                        }
                    }
                }
            }
        }
    }

    /**
     * Clicks on a given field on the board. A click opens a covered field (and
     * all neighbors if it is a 0) and will reveal what is below it. If you
     * click on a mine, you'll lose a life.
     *
     * @param x
     *            the x coordinate of the click
     *
     * @param y
     *            the y coordinate of the click
     */
    private void click(final int x, final int y) {
        if (x < 0 || x > board.length || y < 0 || y > board[0].length) {
            throw new IllegalArgumentException(
                    "Both coordinates have to be within the grid.");
        }
        if (board[x][y] == -10) {
            board[x][y] = 0;
            uncovered++;
            uncoverNeighbors(x, y);
        } else if (board[x][y] == -9) {
            board[x][y] = -board[x][y];
            lives--;
            minesLeft--;
            uncovered++;
            System.out.println(
                    "You have lost a life. You have " + lives + " remaining.");
        } else if (board[x][y] < -10) {
            return;
        } else if (board[x][y] < 0) {
            board[x][y] = -board[x][y];
            uncovered++;
        }
        System.out.println(toString());
    }

    /**
     * Flags a given field on the board. A flagged field cannot be opened with a
     * click and shall be used to mark a mine that lies underneath. If you want
     * to click a flagged field you need to flag it again to unflag it.
     *
     * @param x
     *            the x coordinate of the flag
     *
     * @param y
     *            the y coordinate of the flag
     */
    private void flag(final int x, final int y) {
        if (x < 0 || x > board.length || y < 0 || y > board[0].length) {
            throw new IllegalArgumentException(
                    "Both coordinates have to be within the grid.");
        }
        if (board[x][y] < -10) {
            board[x][y] += 10;
        } else if (board[x][y] < 0) {
            board[x][y] -= 10;
        }
        System.out.println(toString());
    }

    /**
     * Makes a given move. Throws a {@link IllegalArgumentException} if one
     * value in the given Integer is not valid.
     *
     * @param move
     *            the encoded move
     */
    @Move
    public void move(final Integer move) {
        int x = move & 0xfff >> 6;
        int y = move >> 6 & 0x3f;
        int t = move >> 12 & 0x1;
        if (x < 0 || y < 0 || t < 0 || x > board.length || y > board[0].length
                || t > 1 || t != 0 && uncovered == 0) {
            throw new IllegalArgumentException();
        }
        if (uncovered == 0) {
            if ((double) minesLeft
                    / (double) (board.length * board[0].length) >= 0.8) {
                populate2(x, y);
            } else {
                populate(x, y);
            }
            return;
        }
        if (t == 0) {
            click(x, y);
            return;
        }
        flag(x, y);
    }

    /**
     * Returns a list of all possible Actions. All entries are encoded in the
     * following format: <1 bit type><6 bit y><6 bit x>
     *
     * If type is 0, it is a click, if it is 1, it is a flag.
     *
     * @return a list of all possible Actions encoded as Integers
     */
    @Action
    public List<Integer> actions() {
        List<Integer> act = new ArrayList<>();
        if (lives == 0) {
            return act;
        }
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[0].length; y++) {
                if (uncovered == 0) {
                    act.add(x | y << 6 | 0 << 12);
                } else if (board[x][y] < -10) {
                    act.add(x | y << 6 | 1 << 12);
                } else if (board[x][y] < 0) {
                    act.add(x | y << 6 | 0 << 12);
                    act.add(x | y << 6 | 1 << 12);
                }
            }
        }
        return act;
    }

    /**
     * Checks if the game is over and the player has won. The player wins when
     * all fields, which not cover a mine, are uncovered.
     *
     * @return <code>true</code>, when the player won, <code>false</code>
     *         otherwise
     */
    private boolean winCheck() {
        return uncovered == board.length * board[0].length - minesLeft
                && !loseCheck();
    }

    /**
     * Checks if the game is over and the player has lost. The player loses if
     * all his lives are gone. Losing has a higher priority than winning.
     *
     * @return <code>true</code>, when the player lost, <code>false</code>
     *         otherwise
     */
    private boolean loseCheck() {
        return lives == 0;
    }

    /**
     * Allows one to play the game. The board size and amount of mines is read
     * in from the console.
     */
    public static void play() {
        Scanner s = new Scanner(System.in);
        int length;
        int height;
        int mines;

        // Reads length from console
        while (true) {
            System.out.println("Please enter the length of the board\n"
                    + "(between 10 and 60): ");
            if (s.hasNextInt()) {
                length = s.nextInt();
                if (length >= 10 && length <= 60) {
                    s.nextLine();
                    break;
                }
            }
            System.out
                    .println("The entered value is invalid. Please try again.");
            s.nextLine();
        }

        // Reads height from console
        while (true) {
            System.out.println("\nPlease enter the height of the board\n"
                    + "(between 10 and 60): ");
            if (s.hasNextInt()) {
                height = s.nextInt();
                if (height >= 10 && height <= 60) {
                    s.nextLine();
                    break;
                }
            }
            System.out.println(
                    "\nThe entered value is invalid. Please try again.");
            s.nextLine();
        }

        // Reads mines from console
        while (true) {
            System.out.println("\nPlease enter the number of mines on the "
                    + "board\n(between 10 and " + (length * height - 9)
                    + "): ");
            if (s.hasNextInt()) {
                mines = s.nextInt();
                if (mines >= 10 && mines <= length * height - 9) {
                    s.nextLine();
                    break;
                }
            }
            System.out.println(
                    "\nThe entered value is invalid. Please try again.");
            s.nextLine();
        }

        Minesweeper m = new Minesweeper(length, height, mines, false);
        System.out.println("You have " + m.lives + " lives.");

        Pattern p = Pattern.compile("\\b([1-5][0-9]|[0-9])\\b(?: *),(?: *)"
                + "\\b([1-5][0-9]|[0-9])\\b");
        Matcher mat;
        int x;
        int y;

        // Reading first click coordinates from console
        while (true) {
            System.out.println(
                    "\nPlease enter your first click " + "like this: x, y");
            if (s.hasNext(p)) {
                mat = p.matcher(s.next(p));

                if (mat.matches()) {
                    x = Integer.parseInt(mat.group(1));
                    y = Integer.parseInt(mat.group(2));
                    if (x < 0 || x >= m.board.length) {
                        System.out.println(
                                "\nThe x value is invalid. Try again.");
                        s.nextLine();
                        continue;
                    } else if (y < 0 || y >= m.board[0].length) {
                        System.out.println(
                                "\nThe y value is invalid. Try again.");
                        s.nextLine();
                        continue;
                    }
                    if ((double) m.minesLeft / (double) (m.board.length
                            * m.board[0].length) >= 0.8) {
                        m.populate2(x, y);
                    } else {
                        m.populate(x, y);
                    }
                    System.out.println(m.toString());
                    s.nextLine();
                    break;
                }
            }
            System.out.println("\nInvalid input. Try again.");
            s.nextLine();
        }

        // Reading instructions from console
        // Game loop
        boolean click;
        Pattern p2 = Pattern.compile("\\b([1-5][0-9]|[0-9])\\b(?: *),(?: *)"
                + "\\b([1-5][0-9]|[0-9])\\b(?: *),(?: *)(c|f)");

        while (true) {
            System.out.println("\nSyntax for instructions: x, y, t in "
                    + "which\n[x] determines the x coordinate,\n"
                    + "[y] the y coordinate and\n" + "[t] the type of click");
            System.out.println("Please enter your instructions: ");
            if (s.hasNext(p2)) {
                mat = p2.matcher(s.next(p2));
                if (mat.matches()) {
                    x = Integer.parseInt(mat.group(1));
                    y = Integer.parseInt(mat.group(2));
                    click = mat.group(3).matches("c");
                    if (x < 0 || x >= m.board.length) {
                        System.out.println(
                                "\nThe x value is invalid. Try again.");
                    } else if (y < 0 || y >= m.board[0].length) {
                        System.out.println(
                                "\nThe y value is invalid. Try again.");
                    }
                    if (click) {
                        m.click(x, y);
                    } else {
                        m.flag(x, y);
                    }
                    s.nextLine();
                    if (m.gameOver()) {
                        if (m.utility() == 1) {
                            System.out.println("You win! :)");
                            s.close();
                            break;
                        } else if (m.utility() == -1) {
                            System.out.println("You lose! :(");
                            s.close();
                            break;
                        }
                    }
                    continue;
                }
            }
            System.out.println("\nInvalid input. Try again.");
            s.nextLine();
        }
    }

    /**
     * Checks if the game is over.
     *
     * @return <code>true</code>, when the player lost or won,
     *         <code>false</code> otherwise
     */
    @TerminalTest
    public boolean gameOver() {
        return winCheck() || loseCheck();
    }

    /**
     * Reward method.
     *
     * @return 1 if the player won, -1 if he lost and 0 otherwise
     */
    @Utility
    public int utility() {
        return winCheck() ? 1 : loseCheck() ? -1 : 0;
    }

    /**
     * {@inheritDoc}
     *
     * Returns a string representing the board.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int rc = 0;

        for (int i = -1; i < board[0].length; i++) {
            if (i == -1) {
                sb.append("   ");
                for (int k = 0; k < board.length; k++) {
                    sb.append(String.format("%3s", k));
                }
                sb.append("\n");
                continue;
            }
            sb.append(String.format("%2s", rc) + " ");
            rc++;
            for (int j = 0; j < board.length; j++) {
                if (board[j][i] < 0 && board[j][i] >= -10 || uncovered == 0) {
                    sb.append("|" + (char) BLOCK_SYMBOL + (char) BLOCK_SYMBOL);
                } else if (board[j][i] == 0) {
                    sb.append("|  ");
                } else if (board[j][i] == 9) {
                    sb.append("|" + String.format("%2s", (char) MINE_SYMBOL));
                } else if (board[j][i] < -10) {
                    sb.append("|" + String.format("%2s", (char) FLAG_SYMBOL));
                } else {
                    sb.append("| " + board[j][i]);
                }
            }
            sb.append("|\n");
        }
        return sb.toString();
    }

    @Override
    public Object getIdentifier() {
        return "MS" + Objects.hash(Arrays.deepHashCode(board), lives, minesLeft,
                uncovered);
    }
}
