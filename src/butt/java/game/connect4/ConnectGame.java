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
package game.connect4;

import gps.annotations.*;
import gps.util.IButtSampleProblem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A basic and more generic implementation for the connect4 and connect6 game.
 *
 * @author haker@uni-bremen.de
 */
public class ConnectGame implements IButtSampleProblem {

    /**
     * The playing field which holds all the tokens the players played. Elements
     * are -1 if no player has yet set a token to a specific position. The field
     * is stored as an consecutive sequence of arrays which contain the
     * {@width} number of Elements. The first row is stored in the first
     * Elements of the field.
     */
    private final int fields[];

    /**
     * The heights of the piles in the field. Each column has a pile of tokens
     * with a height which changes after players add tokens to the piles.
     */
    private final int heights[];

    /**
     * The current player who inserts his token by calling the {@link #move}
     * method. Gets set to -1 if no player can play or if the game is over and
     * no winner exists.
     */
    private int currentPlayer;

    /**
     * The width of the playfield. This number represents the number of columns
     * the board has.
     */
    private final int width;

    /**
     * The height of the playfield. This number represents the number of rows
     * the board has.
     */
    private final int height;

    /**
     * The number of players who play the game. Must be at least 1. Each player
     * can put his own tokens.
     */
    private final int players;

    /**
     * The streak length a player must achieve using his tokens to win the game.
     * Standard connect4 game has a win streak of 4 connect 6 has a win streak
     * of 6.
     */
    private final int winStreakLen;

    /**
     * flag that gets set when the game is over. The winner will be stored in
     * {@link #currentPlayer}. If no winner exists {@link #currentPlayer} will
     * be -1.
     */
    private boolean gameOver;

    /**
     * Instantiate a standard Connect4 game with 7 columns and 6 rows.
     *
     * @return The game.
     */
    public static ConnectGame createConnect4() {
        return new ConnectGame(2, 7, 6, 4);
    }

    /**
     * Instantiate a Connect6 game with 19 columns and 19 rows.
     *
     * @return The game.
     */
    public static ConnectGame createConnect6() {
        return new ConnectGame(2, 19, 19, 6);
    }

    /**
     * Create a new Connect Game. The playing field will be empty.
     *
     * @param pPlayers
     *            The number of players who participate in the game. Must be at
     *            least 1. Each player can set his own tokens into the field.
     *            Players turn change in round robin order.
     * @param pFieldWidth
     *            The number of columns the playing field contains. Players can
     *            choose the column in which they want to place there tokens.
     *            Must be at least 1.
     * @param pFieldHeight
     *            The number of rows the playing field has. Must be at least 1.
     * @param pWinStreakLen
     *            The number of tokens a player must achieve to place side by
     *            side in order to win the game. Must be at least 1.
     */
    public ConnectGame(int pPlayers, int pFieldWidth, int pFieldHeight,
            int pWinStreakLen) {
        super();
        if (pPlayers < 1) {
            throw new IllegalArgumentException(
                    "The amount of players must be at least 1");
        }
        if (pFieldWidth < 1) {
            throw new IllegalArgumentException(
                    "The width of the playing field must be at least 1");
        }
        if (pFieldHeight < 1) {
            throw new IllegalArgumentException(
                    "The height of the playing field must be at least 1");
        }
        if (pWinStreakLen < 1) {
            throw new IllegalArgumentException(
                    "The win streak len a palyer must achieve to win must be "
                            + "at least 1");
        }
        width = pFieldWidth;
        height = pFieldHeight;
        currentPlayer = 0;
        fields = new int[pFieldWidth * pFieldHeight];
        players = pPlayers;
        winStreakLen = pWinStreakLen;
        heights = new int[pFieldWidth];
        Arrays.fill(fields, -1);
        Arrays.fill(heights, 0);
        gameOver = false;
    }

    /**
     * Count the number of tokens a player has in a row starting from a specific
     * position and moving towards by x (column offset) and y (row offset).
     *
     * @param pos
     *            The position from where to start counting. Represented as
     *            index of the {@link #fields} array.
     * @param player
     *            The player whos tokens are of interest. If there is no token
     *            at the given position from that player 0 is returned.
     * @param offsetX
     *            The x offset (column) the method should go to count on.
     * @param offsetY
     *            The y offset (row) the method should go to count on.
     *
     * @return The number of tokens that match the player laying on that line.
     */
    private int countStreak(final int pos, final int player, final int offsetX,
            final int offsetY) {
        if (pos >= 0 && pos < width * height && fields[pos] == player) {
            int x = pos % width + offsetX;
            int y = pos / width + offsetY;
            if (x >= width || x < 0 || y >= height || y < 0) {
                return 1;
            }
            return 1 + countStreak(x + y * width, player, offsetX, offsetY);
        }
        return 0;
    }

    /**
     * Count the number of tokens a player has in a row starting from a specific
     * position and moving towards by x (column offset) and y (row offset). Also
     * starts towards the opposite direction thus giving the total length of a
     * streak with a specific slope.
     *
     * @param pos
     *            The position from where to start counting. Represented as
     *            index of the {@link #fields} array.
     * @param player
     *            The player whos tokens are of interest. If there is no token
     *            at the given position from that player 0 is returned.
     * @param offsetX
     *            The x offset (column) the method should go to count on.
     * @param offsetY
     *            The y offset (row) the method should go to count on.
     *
     * @return The total number of tokens that match the player laying on a line
     *         with the specified orientation.
     */
    private int countBiStreak(final int pos, final int player,
            final int offsetX, final int offsetY) {
        final int streakLen = countStreak(pos, player, offsetX, offsetY)
                + countStreak(pos, player, -offsetX, -offsetY);
        if (streakLen > 0) {
            return streakLen - 1; // since we count pos twice, decrement by one
        }
        return 0;
    }

    /**
     * Checks whether the current player has a streak at the giving position
     * that is long enough to win the game.
     *
     * @param pos
     *            A position of the streak. Represented as index of the
     *            {@link #fields} array.
     *
     * @return {@code true} if the length of the streak is sufficient to win the
     *         game. {@code false} otherwise.
     */
    private boolean isWinStreak(int pos) {
        final int player = fields[pos];
        if (player < 0) {
            return false;
        }
        return countBiStreak(pos, player, 1, 0) >= winStreakLen // horizontal
                || countBiStreak(pos, player, 0, 1) >= winStreakLen // vertical
                || countBiStreak(pos, player, 1, 1) >= winStreakLen
                // diagonal rising
                || countBiStreak(pos, player, 1, -1) >= winStreakLen; // diagonal
        // falling
    }

    /**
     * This heuristic function computes a height score for every empty field.
     * The height score initially equals {@link #height}, but is decreased by
     * the number of missing discs between the field and the topmost disc of the
     * field's column. So the bigger the height difference between the win
     * position and the topmost disc, the less is the height score of this
     * position, resulting in a lower heuristic evaluation value.
     * <p>
     * Certain wins (currently evaluated player is current player and height
     * diff is 0) are rated much higher than normal win positions
     * (heightScore²).
     * <p>
     * Win position's height scores of the given player are multiplied with 100
     * and added to the result, win position's height scores of opponent players
     * are multiplicated with 100 and subtracted from the result.
     * <p>
     * This function should work best for 2 player ConnectGames.
     *
     * @param pPlayer
     *            the player from whose view the board should be evaluated
     *
     * @return evaluation of the board from pPlayer's view
     */
    @Heuristic
    public int heuristic(final int pPlayer) {
        if (pPlayer < 0 || pPlayer >= players) {
            throw new IllegalArgumentException(
                    "pPlayer must be a valid player");
        }
        ArrayList<Integer> heightScores = new ArrayList<>();
        ArrayList<Integer> opponentsHeightScores = new ArrayList<>();
        int x;
        int y;
        int columnHeight;
        int heightDiff;
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] < 0) {
                x = i % width;
                // +1, so that height score of
                y = i / width + 1;
                columnHeight = getHeightOfColumn(x);
                // number of missing discs between columnHeight and y
                heightDiff = (height - y) - columnHeight;
                // smaller multiplier for win positions with great height
                // difference to column height
                int heightScore = height - heightDiff;
                // certain win in this situation
                if (pPlayer == currentPlayer && heightDiff == 0) {
                    heightScore *= heightScore;
                }
                if (countHorizontal(i, pPlayer) == winStreakLen - 1) {
                    heightScores.add(heightScore);
                }
                if (countVertical(i, pPlayer) == winStreakLen - 1) {
                    heightScores.add(heightScore);
                }
                if (countDiagonallyRising(i, pPlayer) == winStreakLen - 1) {
                    heightScores.add(heightScore);
                }
                if (countDiagonallyFalling(i, pPlayer) == winStreakLen - 1) {
                    heightScores.add(heightScore);
                }
                // revert for other players
                if (pPlayer == currentPlayer && heightDiff == 0) {
                    heightScore /= heightScore;
                }
                for (int p = 0; p < players; p++) {
                    if (p != pPlayer) {
                        // certain win in this situation
                        if (p == currentPlayer && heightDiff == 0) {
                            heightScore *= heightScore;
                        }
                        if (countHorizontal(i, p) == winStreakLen - 1) {
                            opponentsHeightScores.add(heightScore);
                        }
                        if (countVertical(i, p) == winStreakLen - 1) {
                            opponentsHeightScores.add(heightScore);
                        }
                        if (countDiagonallyRising(i, p) == winStreakLen - 1) {
                            opponentsHeightScores.add(heightScore);
                        }
                        if (countDiagonallyFalling(i, p) == winStreakLen - 1) {
                            opponentsHeightScores.add(heightScore);
                        }
                        // revert for other players
                        if (p == currentPlayer && heightDiff == 0) {
                            heightScore /= heightScore;
                        }
                    }
                }
            }
        }
        int result = 0;
        for (int heightScore : heightScores) {
            result += heightScore * 100;
        }
        for (int opponentHeightScore : opponentsHeightScores) {
            result -= opponentHeightScore * 100;
        }
        return result;
    }

    /**
     * Counts the number of consecutive discs of pPlayer from position index to
     * the left and right and returns the sum. If index is the first field of
     * the row, only discs to the right are counted. If index is the last field
     * of the row, only discs to the left are counted. The position of index is
     * not counted, because it usually should be an empty field.
     *
     * @param index
     *            index of the field from which to count the consecutive discs
     *            to the left and right
     * @param pPlayer
     *            player for whom to count the discs
     *
     * @return sum of the number of consecutive discs of pPlayer from position
     *         index to the left and right
     */
    private int countHorizontal(final int index, final int pPlayer) {
        int pos = index % width;
        if (pos == 0) {
            return countStreak(index + 1, pPlayer, 1, 0);
        }
        if (pos == width - 1) {
            return countStreak(index - 1, pPlayer, -1, 0);
        }
        return countStreak(index - 1, pPlayer, -1, 0)
                + countStreak(index + 1, pPlayer, 1, 0);
    }

    /**
     * Counts the number of consecutive discs of pPlayer up- and downwards from
     * position index and returns the sum. The position of index is not counted,
     * because it usually should be an empty field.
     *
     * @param index
     *            index of the field from which to count the consecutive discs
     *            upwards and downwards
     * @param pPlayer
     *            player for whom to count the discs
     *
     * @return sum of the number of consecutive discs of pPlayer from position
     *         index upwards and downwards
     */
    private int countVertical(final int index, final int pPlayer) {
        return countStreak(index - width, pPlayer, 0, -1)
                + countStreak(index + width, pPlayer, 0, 1);
    }

    /**
     * Counts the number of consecutive discs of pPlayer from position index
     * diagonally upwards to the right and diagonally downwards to the left and
     * returns the sum. If index is the first field of the row, only discs
     * diagonally upwards to the right are counted. If index is the last field
     * of the row, only discs diagonally downwards to the left are counted. The
     * position of index is not counted, because it usually should be an empty
     * field.
     *
     * @param index
     *            index of the field from which to count the consecutive discs
     *            diagonally upwards to the right and diagonally downwards to
     *            the left
     * @param pPlayer
     *            player for whom to count the discs
     *
     * @return sum of the number of consecutive discs of pPlayer diagonally
     *         upwards to the right and diagonally downwards to the left from
     *         position index
     */
    private int countDiagonallyRising(int index, final int pPlayer) {
        int pos = index % 7;
        if (pos == 0) {
            return countStreak(index - width + 1, pPlayer, 1, -1);
        }
        if (pos == width - 1) {
            return countStreak(index + width - 1, pPlayer, -1, 1);
        }
        return countStreak(index - width + 1, pPlayer, 1, -1)
                + countStreak(index + width - 1, pPlayer, -1, 1);
    }

    /**
     * Counts the number of consecutive discs of pPlayer from position index
     * diagonally upwards to the left and diagonally downwards to the right and
     * returns the sum. If index is the first field of the row, only discs
     * diagonally downwards to the right are counted. If index is the last field
     * of the row, only discs diagonally upwards to the left are counted. The
     * position of index is not counted, because it usually should be an empty
     * field.
     *
     * @param index
     *            index of the field from which to count the consecutive discs
     *            diagonally upwards to the left and diagonally downwards to the
     *            right
     * @param pPlayer
     *            player for whom to count the discs
     *
     * @return sum of the number of consecutive discs of pPlayer diagonally
     *         upwards to the left and diagonally downwards to the right from
     *         position index
     */
    private int countDiagonallyFalling(int index, final int pPlayer) {
        int pos = index % 7;
        if (pos == 0) {
            return countStreak(index + width + 1, pPlayer, 1, 1);
        }
        if (pos == width - 1) {
            return countStreak(index - width - 1, pPlayer, -1, -1);
        }
        return countStreak(index - width - 1, pPlayer, -1, -1)
                + countStreak(index + width + 1, pPlayer, 1, 1);
    }

    /**
     * Switch turn to the next player in round robin treatment.
     */

    private void nextPlayer() {
        if (currentPlayer < 0) {
            throw new RuntimeException(
                    "Cannot change turn since currentPlayer has been set to "
                            + "-1. Probably because the game is already over?");
        }
        currentPlayer++;
        currentPlayer %= players;
    }

    /**
     * Get the height of a pile in a column.
     *
     * @param pCol
     *            The column.
     *
     * @return The number of tokens in the given column.
     */
    private int getHeightOfColumn(int pCol) {
        return heights[pCol]; // alternatively this can be done by interating
        // through the field
    }

    /**
     * Get all possible columns the current Player can place a token to.
     *
     * @return List of columns.
     */
    @Action
    public List<Integer> getMoves() {
        final List<Integer> moves = new ArrayList<>(width);
        for (int i = 0; i < width; i++) {
            if (getHeightOfColumn(i) < height) {
                moves.add(i);
            }
        }
        return moves;
    }

    /**
     * Put a token from the current player into the given column. The column
     * must not be full.
     * <p>
     * If the player wins because of this move the game is over. The current
     * player is the winner. Use {@link #getWinner()} to check for a winner.
     * <p>
     * If the game continues the next player gets a turn. Use
     * {@link #getMoves()} to get all possible moves for the next player after
     * calling {@code move}.
     * <p>
     * If the game ends if the entire field is full and the player has not made
     * a winning streak. In this case no winner exists.
     *
     * @param pColumn
     *            The column where the current player wants to put his token to.
     */
    @Move
    public void move(final Integer pColumn) {
        if (pColumn == null) {
            throw new IllegalArgumentException("column must not be null");
        }
        if (pColumn < 0 || pColumn > width) {
            throw new IllegalArgumentException(
                    "column to put token at must be within the width of the "
                            + "field");
        }
        final int h = getHeightOfColumn(pColumn);
        if (h >= height) {
            throw new IllegalArgumentException(
                    "the move cannot be performed because the column is "
                            + "already filled");
        }

        final int tokenPos = pColumn + width * (height - h - 1);
        fields[tokenPos] = getCurrentPlayer(); // add token to the field
        heights[pColumn]++;

        if (isWinStreak(tokenPos)) {
            // current player has won the game
            gameOver = true;
        } else {
            nextPlayer();

            // The game is over since no new tokens can be placed.
            if (getMoves().isEmpty()) {
                gameOver = true;
                currentPlayer = -1;
            }
        }
    }

    /**
     * Retrieve the reward for a given player. If the game has not yet endet 0
     * is returned. Otherwise 1 if the specified player has won the game. -1 if
     * he lost.
     *
     * @param pPlayer
     *            The player.
     *
     * @return The reward.
     */
    @Utility
    public int reward(final int pPlayer) {
        if (pPlayer < 0 || pPlayer >= players) {
            throw new IllegalArgumentException(
                    "pPlayer must be a valid player");
        }
        if (isGameOver()) {
            return pPlayer == getWinner() ? 100000 : -100000;
        } else {
            return 0;
        }
    }

    /**
     * Get the player whos turn is now. If the game ended with a winner this
     * will be the winner. Better use {@link #getWinner()} to get the winner.
     *
     * @return The current player or -1 if the game has ended with no winner.
     */
    @Player
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Checks whether the game is over either because a player has won the game
     * or no the playing field is full.
     *
     * @return {@code true} if the game is over {@code false} otherwise.
     */
    @TerminalTest
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Returns the winning player if the game is over and a player has won the
     * game. If the game has not yet ended or no player has won the game -1 is
     * returned.
     *
     * @return The winner.
     */
    public int getWinner() {
        return isGameOver() ? getCurrentPlayer() : -1;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ConnectGame)) {
            return false;
        }
        final ConnectGame o = (ConnectGame) other;
        return width == o.width && height == o.height
                && currentPlayer == o.currentPlayer && players == o.players
                && winStreakLen == o.winStreakLen && gameOver == o.gameOver
                && Arrays.equals(fields, o.fields)
                && Arrays.equals(heights, o.heights);
    }

    /**
     * Copy constructor. Create an instance with the same attributes as the
     * given ConnectGame.
     *
     * @param o
     *            The other game to copy the attributes from.
     */
    private ConnectGame(final ConnectGame o) {
        super();
        fields = Arrays.copyOf(o.fields, o.fields.length);
        heights = Arrays.copyOf(o.heights, o.heights.length);
        currentPlayer = o.currentPlayer;
        width = o.width;
        height = o.height;
        players = o.players;
        winStreakLen = o.winStreakLen;
        gameOver = o.gameOver;
    }

    @Override
    public ConnectGame clone() {
        return new ConnectGame(this);
    }

    /**
     * Get a clone of a successor if a specific action would have been
     * performed.
     *
     * @param pColumn
     *            The column of the move.
     *
     * @return a clone of the current game with the move applied to.
     */
    public ConnectGame getSuccessor(final Integer pColumn) {
        ConnectGame c4 = clone();
        c4.move(pColumn);
        return c4;
    }

    /**
     * Get a clone of all successors.
     *
     * @return a list of clones of the current game with all the possibles moves
     *         applied to.
     */
    public List<ConnectGame> getSuccessors() {
        List<Integer> moves = getMoves();
        return moves.stream().map(m -> {
            return getSuccessor(m);
        }).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (int i = 0; i < width; i++) {
            sb.append("--");
        }
        sb.append("-\n");
        for (int row = 0; row < height; row++) {
            sb.append("|");
            for (int col = 0; col < width; col++) {
                final int token = fields[col + row * width];
                if (token < 0) {
                    sb.append(" |");
                } else {
                    sb.append(String.format("%d|", token));
                }
            }
            sb.append("\n");
        }
        for (int i = 0; i < width; i++) {
            sb.append("--");
        }
        sb.append("-\n");
        sb.append(" ");
        for (int i = 0; i < width; i++) {
            sb.append(i);
            sb.append(" ");
        }
        sb.append("\n");
        if (isGameOver()) {
            sb.append("Game Over. Winner is Player ");
            sb.append(getWinner());
            sb.append(".\n");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(fields);
        result = 31 * result + Arrays.hashCode(heights);
        result = 31 * result + currentPlayer;
        result = 31 * result + (gameOver ? 1 : 0);
        return result;
    }

    @Override
    public Object getIdentifier() {
        return "Connect" + Objects.hash(players, winStreakLen, height, width,
                Arrays.hashCode(fields), Arrays.hashCode(heights),
                currentPlayer, gameOver);
    }
}
