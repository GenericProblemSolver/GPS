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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;

import gps.annotations.Action;
import gps.annotations.Move;
import gps.annotations.Player;
import gps.annotations.TerminalTest;
import gps.annotations.Utility;

/**
 * Go, modeled in such a way that each instance of the game is a self-contained,
 * deeply copied object that automatic solvers can work with
 * 
 * @author kohorst@uni-bremen.de
 */
// @PMCTS
public class Go {

    /**
     * The class's logger
     */
    private static Logger LOGGER = Logger.getAnonymousLogger();

    /**
     * Represents an unoccupied field
     */
    final static int BLANK = 0;

    /**
     * Represents a field occupied by black
     */
    final static int BLACK = 1;

    /**
     * Represents a field occupied by white
     */
    final static int WHITE = 2;

    /**
     * When scoring a terminated game, this indicates neutral territory
     */
    final static int NEUTRAL = 3;

    /**
     * Board size is always a square
     */
    static int BOARD_SIZE = 9;

    /**
     * Initializes the board. Valid values are 0 = unoccupied 1 = BLACK 2 =
     * WHITE
     */
    // @RelevantField
    private int[][] board;

    /**
     * Stores the board situation that led to the current board. This is
     * necessary because it is forbidden to recreate a state that existed before
     */
    private int[][] previousBoard;

    /**
     * Indicates, whether the last move was a pass
     */
    private boolean lastWasPass; // this is somewhat redundant, given that we
                                 // have both the current and the previous
                                 // board, but it should be way faster than
                                 // comparing the two

    /**
     * Stores the current player. Valid values are 1 and 2. A value of 0
     * indicates that the game is over
     */
    // @RelevantField
    private int currentPlayer; // same goes for the current player, as black
                               // always begins

    /**
     * A player can always pass. A pass is modeled by the coordinates (-1, -1)
     */
    private final Coords pass = new Coords(-1, -1);

    /**
     * Instantiates a new Game with the default board and prints it to stdout
     */
    public Go() {
        // LOGGER.setUseParentHandlers(false);
        board = new int[BOARD_SIZE][BOARD_SIZE];
        previousBoard = new int[BOARD_SIZE][BOARD_SIZE];
        currentPlayer = BLACK;
        lastWasPass = false;
        System.out.println(toString());
    }

    /**
     * Instantiates a new Game with the given board size.
     * 
     * @param pBoardSize
     *            The size of the new game's board. Must be between 9 and 19.
     *            Otherwise creates a new game with a board size of 9x9.
     */
    public Go(final int pBoardSize) {
        this();
        if (pBoardSize < 9 || pBoardSize > 19) {
            LOGGER.info(
                    "Board size must be between 9 and 19. Creating 9x9 board");
            BOARD_SIZE = 9;
        } else {
            BOARD_SIZE = pBoardSize;
        }
    }

    /**
     * Instantiates a new Game with the given parameters and prints it to stdout
     */
    public Go(final int[][] pBoard, final int[][] pPreviousBoard,
            final boolean pPass, final int pPlayer) {
        board = pBoard;
        previousBoard = pPreviousBoard;
        lastWasPass = pPass;
        currentPlayer = pPlayer;
        System.out.println(toString());
    }

    /**
     * Play Go!
     * 
     * @param args
     *            args are discarded
     */
    public static void main(String[] args) {
        (play(null, 9)).areaScoring(BLACK);
    }

    /**
     * Lets two users play interactively via command line
     */
    public static Go play(final Scanner pScanner, final int pBoardSize) {
        Scanner scanner;
        if (pScanner == null) {
            scanner = new Scanner(System.in);
        } else {
            scanner = pScanner;
        }
        Go go = new Go(pBoardSize);
        do {
            go = gameLoop(go, scanner);
        } while (!go.terminalTest());
        scanner.close();
        return go;
    }

    /**
     * Creates a new Go instance with a new stone set to the parameter
     * coordinates of the board and all opponent stones captured. This method
     * assumes the input is sane, as all the checking is done in the
     * {@link Go#getValidEmptyFields()} method
     * 
     * @param pCoords
     *            The coordinate to place the stone for the current player
     * @return A new Go instance with the same board as before and a @link
     *         {@link Go#currentPlayer} value of 0 if the receive coordinate
     *         indicates a {@link Go#pass}. The Game is over then. Otherwise
     *         sets the stone, performs a capture and returns an instance with
     *         the resulting board
     */
    @Move
    public Go setStone(final Coords pCoords) {
        if (pCoords.equals(pass)) {
            LOGGER.info("We received a pass.");
            if (lastWasPass) {
                return new Go(cloneBoard(board), cloneBoard(board), true, 0);
            } else {
                return new Go(cloneBoard(board), cloneBoard(board), true,
                        opponentPlayer(currentPlayer));
            }
        }
        LOGGER.info("Setting stone to " + pCoords.toString());
        final int[][] newPreviousBoard = cloneBoard(board);
        final int[][] newBoard = cloneBoard(board);
        newBoard[pCoords.x][pCoords.y] = currentPlayer;
        capture(pCoords, newBoard);
        return new Go(newBoard, newPreviousBoard, false,
                opponentPlayer(currentPlayer));
    }

    /**
     * Returns a List of coordinates that qualify for the next move. In oder to
     * qualify for a move, a coordinate must necessarily be {@link Go#BLANK} and
     * EITHER have more than one degree of freedom OR capture at least one
     * opponent stone.
     * 
     * @return A List of valid moves for the {@link Go#currentPlayer}
     */
    @Action
    public List<Coords> getValidEmptyFields() {
        // LOGGER.info("Determining valid next moves");
        List<Coords> actions = new ArrayList<>();
        // iterate through board
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                // if unoccupied field is encountered it is a candidate
                if (board[x][y] == BLANK) {
                    Coords candidate = new Coords(x, y);
                    // LOGGER.info("Encountered a blank field at "
                    // + candidate.toString());
                    if (hasDegreeOfFreedom(candidate, board)) {
                        // LOGGER.info(candidate.toString()
                        // + " has a least one degree of freedom");
                        actions.add(candidate);
                    } else if (capture(candidate, board, true)) {
                        // LOGGER.info(candidate.toString()
                        // + " has a ZERO degress of freedom, but would capture
                        // opponent stones");
                        // ...or captures at least one opponent stone and does
                        // not recreate the previous board situation
                        actions.add(candidate);
                    }
                }
            }
        }
        actions.add(pass);
        // LOGGER.info("The following fields are valid: " + actions.toString());
        return actions;
    }

    /**
     * States if game is over
     * 
     * @return {@code true} if game is over {@code false} otherwise
     */
    @TerminalTest
    public boolean terminalTest() {
        return currentPlayer == 0;
    }

    /**
     * Performs the traditional Chinese area scoring according to which a player
     * gets awarded all {@code Go#BLANK} intersections that are (directly and
     * indirectly) only adjacent to stones that belong to the very player
     * 
     * @param pPlayer
     *            The player from whose perspective to return the game result
     * 
     * @return 1 if pPlayer has the highest score, -1 if the opponent does and 0
     *         if the scoring result is a tie.
     * 
     */
    @Utility
    public int areaScoring(final int pPlayer) {
        // Idea: Search for a BLANK -> Form a chain of this and all adjacent
        // BLANKS -> Successively look at all stones neighboring the chain ->
        // EITHER find that the entire chain is circled by stones of only one
        // player (then declare it as the player's territory) OR find that both
        // players' stones are adjacent. In this case instantly declare the
        // entire area (chain) neutral.
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                Coords current = new Coords(x, y);
                if (getStone(current, board) == BLANK) {
                    List<Coords> blankChain = getChain(current, board);
                    int addToThisPlayer = -1; // indicates it has not been set
                    searchInChain: for (Coords coords : blankChain) {
                        for (Coords neighbor : coords.getAdjacentCoords()) {
                            int neighborStone = getStone(neighbor, board);
                            if (neighborStone != BLANK) {
                                if (addToThisPlayer < 0) {
                                    // this is the first time we've encountered
                                    // a stone adjacent to our area
                                    addToThisPlayer = neighborStone;
                                } else if (addToThisPlayer != neighborStone) {
                                    // the chain of blanks is adjacent to both
                                    // player and will not be added to any
                                    // player's territory
                                    LOGGER.info("The chain " + blankChain
                                            + " is neutral territory");
                                    addToThisPlayer = NEUTRAL;
                                    break searchInChain;
                                }
                            }
                        }
                    }
                    LOGGER.info("The chain " + blankChain
                            + " is awarded to player " + addToThisPlayer);
                    for (Coords coords : blankChain) {
                        board[coords.x][coords.y] = addToThisPlayer;
                    }
                }
            }
        }
        System.out
                .println("The resulting final board is this: \n" + toString());
        int blackCount = 0;
        int whiteCount = 0;
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                if (board[x][y] == BLACK) {
                    blackCount++;
                } else if (board[x][y] == WHITE) {
                    whiteCount++;
                }
            }
        }
        if (blackCount > whiteCount) {
            System.out.println("Black wins by " + String.valueOf(blackCount)
                    + ":" + String.valueOf(whiteCount) + ".");
            return pPlayer == BLACK ? 1 : -1;
        } else if (whiteCount > blackCount) {
            System.out.println("White wins by " + String.valueOf(whiteCount)
                    + ":" + String.valueOf(blackCount) + ".");
            return pPlayer == BLACK ? -1 : 1;
        } else {
            System.out.println("The Game result is a tie. Both players have "
                    + String.valueOf(blackCount) + " point(s).");
            return 0;
        }
    }

    /**
     * Determines the current player
     * 
     * @return the current player
     */
    @Player
    public int getPlayer() {
        return currentPlayer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Go)) {
            return false;
        }
        Go otherGo = (Go) other;
        return (Arrays.deepEquals(otherGo.board, board)
                && otherGo.currentPlayer == currentPlayer);
    }

    /**
     * Creates an ASCII representation of the current board
     * 
     * @return the board as String
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n ");
        sb.append(printVerticalIndices());
        sb.append(" \n");
        for (int x = 0; x < BOARD_SIZE; x++) {
            final String lineNumber = String.valueOf(x + 1);
            sb.append(lineNumber);
            sb.append(" ");
            for (int y = 0; y < BOARD_SIZE; y++) {
                if (board[x][y] == BLACK) {
                    sb.append('X');
                } else if (board[x][y] == WHITE) {
                    sb.append('O');
                } else {
                    sb.append('.');
                }
                sb.append(" ");
            }
            sb.append(lineNumber);
            sb.append("\n");
        }
        sb.append(" ");
        sb.append(printVerticalIndices());
        sb.append(" \n");
        return sb.toString();
    }

    /**
     * Returns the current board
     * 
     * @return the current board
     */
    int[][] getBoard() {
        return board;
    }

    /**
     * Captures all opponent stones the may possible be affected by placing the
     * given stone on the board
     * 
     * @param pCoords
     *            the coordinate that shall be occupied with our stone
     * @param pBoard
     *            the board to perform (or simulate, see below) the capture on
     * @param pDryRun
     *            if set to {@code true}, no stones will actually be removed
     *            from the pBoard
     * @return {@code true} if at least one opponent stone was captured and the
     *         board situation does not recreate the {@link previousBoard},
     *         {@code false} otherwise
     */
    private boolean capture(final Coords pCoords, final int[][] pBoard,
            final boolean pDryRun) {
        boolean sthCapturedAndNoRecreation = false;
        Set<List<Coords>> listOfChains = getAffectedOpponentChains(pCoords,
                pBoard);
        LOGGER.info(
                "List of chains contains " + listOfChains.size() + " elements");
        for (List<Coords> chain : listOfChains) {
            System.out.println(chain.toString());
            if (!hasDegreeOfFreedom(chain, pBoard)) {
                LOGGER.info(
                        "No chain element has a degree of freedom. Capturing...");
                if (pDryRun) {
                    LOGGER.info("...simulating on another board first");
                    int[][] prospectBoard = cloneBoard(pBoard);
                    prospectBoard[pCoords.x][pCoords.y] = currentPlayer;
                    for (Coords coords : chain) {
                        prospectBoard[coords.x][coords.y] = BLANK;
                    }
                    if (!Arrays.deepEquals(previousBoard, prospectBoard)) {
                        LOGGER.info(
                                "This move would recreate the previous board situation and is thus not allowed");
                        sthCapturedAndNoRecreation = true;
                    }
                } else {
                    pBoard[pCoords.x][pCoords.y] = currentPlayer;
                    for (Coords coords : chain) {
                        pBoard[coords.x][coords.y] = BLANK;
                    }
                }
            }
        }
        return sthCapturedAndNoRecreation;
    }

    /**
     * Determines if at least one element in the given chain has at least one
     * degree of freedom
     * 
     * @param pChain
     *            The chain of stones to search for degrees of freedom
     * @param pBoard
     *            The board to search on
     * @return {@code true} if at least one stone in the chain has a degree of
     *         freedom, {@code false} otherwise
     */
    private boolean hasDegreeOfFreedom(final List<Coords> pChain,
            final int[][] pBoard) {
        for (Coords coords : pChain) {
            if (hasDegreeOfFreedom(coords, pBoard)) {
                LOGGER.info("The chain element " + coords.toString()
                        + " has a degree of freedom. Checking next chain if applicable.");
                return true;
            }
        }
        return false;
    }

    /**
     * Equivalent to {@link Go#capture(Coords, boolean)}, but does not return
     * anything and never performs a dry run
     * 
     * @param pCoords
     */
    private void capture(final Coords pCoords, final int[][] pBoard) {
        capture(pCoords, pBoard, false);
    }

    /**
     * Determines all chains that can be formed from the ADJACENT STONES OF THE
     * OPPOSITE COLOR. A chain is a set of stones of the same color that are
     * adjacent to one another. This method effectively returns all opponent
     * chains that may be affected by a move.
     * 
     * @param pCoords
     *            "our" coordinate that is the starting point for the search
     * 
     * @return A List of Lists of mutually adjacent coordinates which are of the
     *         opposite color of the parameter coordinate. Will return a List of
     *         empty Lists if there is no adjacent opponent stone.
     */
    private Set<List<Coords>> getAffectedOpponentChains(final Coords pCoords,
            final int[][] pBoard) {
        Set<List<Coords>> listOfChains = new HashSet<>();
        List<Coords> possibleQueueStarts = pCoords.getAdjacentCoords();
        for (Coords startingPoint : possibleQueueStarts) {
            LOGGER.info("Starting point for chain search is "
                    + startingPoint.toString());
            if (getStone(startingPoint,
                    pBoard) == opponentPlayer(currentPlayer)) {
                LOGGER.info(startingPoint.toString()
                        + " is a possible queue start");
                List<Coords> chain = getChain(startingPoint, pBoard);
                boolean add = true;
                for (List<Coords> otherChain : listOfChains) {
                    if (otherChain.get(0).equals(chain.get(0))) {
                        add = false;
                    }
                }
                if (add) {
                    listOfChains.add(chain);
                }
                // if a chain represents a "tour" from one adjacent stone to
                // another, we don't have to search this very tour from the
                // other end
            }
        }
        return listOfChains;
    }

    /**
     * Returns a chain of adjacent stones that the given stone is part of (uses
     * depth-first search)
     * 
     * @param pCoords
     *            The stone to start the search with
     * @return A List of stones adjacent to one another. Will always include the
     *         parameter, thus minimum return List length is 1.
     */
    private List<Coords> getChain(final Coords pCoords, final int[][] pBoard) {
        final int ourColor = getStone(pCoords, pBoard);
        LOGGER.info("Searching " + pCoords.toString() + " (" + ourColor
                + ") for a chain");
        List<Coords> visited, queue;
        visited = new ArrayList<>();
        queue = new ArrayList<>();
        visited.add(pCoords);
        queue.add(pCoords);
        while (!queue.isEmpty()) {
            Coords current = queue.remove(0); // maybe adding to and removing
                                              // from the end is faster
            LOGGER.info("Investigating " + current.toString());
            for (Coords coords : current.getAdjacentCoords()) {
                if (getStone(coords, pBoard) == ourColor) {
                    if (!visited.contains(coords)) {
                        LOGGER.info("Found adjacent stone " + coords.toString()
                                + ", which we have not seen before.");
                        visited.add(coords);
                        // add at 0 -> depth first search
                        queue.add(0, coords);
                    } else {
                        LOGGER.info("We have seen " + coords.toString()
                                + " before. Skipping.");
                    }
                }
            }
        }
        LOGGER.info("Found " + visited.size() + " stone(s) in a chain.");
        Comparator<Coords> comp = new Comparator<Coords>() {
            @Override
            public int compare(Coords c1, Coords c2) {
                return ((Integer) (c1.x << 10 + c1.y))
                        .compareTo(c2.x << 10 + c2.y);
            }
        };
        Collections.sort(visited, comp);
        return visited;
    }

    /**
     * Determines the number of unoccupied coordinates out of the given List
     * 
     * @param pCoord
     *            The coordinates to check for adjacent degrees of freedom
     */
    private boolean hasDegreeOfFreedom(final Coords pCoords,
            final int[][] pBoard) {
        for (Coords coords : pCoords.getAdjacentCoords()) {
            if (getStone(coords, pBoard) == BLANK) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the Stone at the given coordinates or 0 if there is no stone
     * 
     * @param pCoord
     *            The coordinate to look for
     * @return
     */
    private int getStone(final Coords pCoords, final int[][] pBoard) {
        if (pCoords.x >= BOARD_SIZE || pCoords.x < 0 || pCoords.y > BOARD_SIZE
                || pCoords.y < 0) {
        }
        return pBoard[pCoords.x][pCoords.y];
    }

    /**
     * Convenient form to access the Integer assigned to the opponent player
     * (relative to current player)
     * 
     * @param pPlayer
     *            The Player to return the opponent of
     * @return The Integer value associated with the other player
     */
    private int opponentPlayer(final int pPlayer) {
        return currentPlayer ^ 3;
    }

    /**
     * Prints sequence of characters from capital 'A' to the
     * {@linkplain BOARD_SIZE}th letter of the alphabet
     * 
     * @return
     */
    private StringBuffer printVerticalIndices() {
        StringBuffer sb = new StringBuffer();
        char boardVertical = 'A';
        for (int i = 0; i < BOARD_SIZE; i++) {
            sb.append(" ");
            sb.append(boardVertical++);
        }
        return sb;
    }

    /**
     * Creates a deep copy of the current board.
     * 
     * @return a deep copy of {@link Go#board}
     */
    private int[][] cloneBoard(final int[][] pBoard) {
        return Arrays.stream(pBoard).map(x -> x.clone()).toArray(int[][]::new);
    }

    /**
     * Starts the main game loop for interactive or automated playing
     * 
     * @param pGo
     *            the previous game
     * @param pScanner
     *            the scanner to read the players moves from
     * @return the subsequent game
     */
    private static Go gameLoop(final Go pGo, final Scanner pScanner) {
        StringBuilder sb = new StringBuilder();
        sb.append("Player ");
        sb.append(String.valueOf(pGo.currentPlayer + ":\n"));
        sb.append("Choose one of the following moves: \n");
        List<Coords> possibleMoves = pGo.getValidEmptyFields();
        for (Coords coords : possibleMoves) {
            sb.append(coords.toString() + " ");
        }
        // break lines after 80 characters
        String moves = sb.toString().replaceAll("(.{80})", "$1\n");
        System.out.print(moves + "\n");
        Coords coords;
        do {
            String input = "";
            input = pScanner.nextLine();
            coords = Coords.fromString(input);
            if (coords == null) {
                System.out.println("Please specify a valid move!\n");
            }
        } while (coords == null);
        return pGo.setStone(coords);
    }
}
