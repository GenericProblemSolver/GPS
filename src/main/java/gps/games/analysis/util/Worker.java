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
package gps.games.analysis.util;

import gps.games.MemorySavingMode;
import gps.games.util.GameTree;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;
import gps.games.wrapper.Player;
import gps.games.wrapper.successor.INode;
import gps.util.Tuple;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * A worker-thread class. When calling {@code start()} this worker will begin to
 * randomly select successors of the given game object for all of his paths. 
 * The Worker will work as long as there are still trees left that have not 
 * reached an end or until the time limit expired.
 * 
 * @author Fabian
 */
public class Worker<T> extends Thread {

    /**
     * The root game object, representing the main problem
     */
    private final Game<T> game;

    /**
     * An array of the paths to iterate through
     */
    private final INode<T>[] paths;

    /**
     * The tree object the results are saved in
     */
    private final GameTree<T> tree;

    /**
     * The maximum depth for each path
     */
    private final Integer maxDepth;

    /**
     * {@code True} if game.hasTerminalMethod()
     */
    private final boolean problemHasTerminal;

    /**
     * {@code true} if game.hasUtilityMethod()
     */
    private final boolean problemHasUtility;

    /**
     * {@code true} if game.hasPlayerUtilityMethod()
     */
    private final boolean problemHasPlayerUtility;

    /**
     * Saves the depth range of the nodes this worker thread worked on
     */
    private Tuple<Integer, Integer> depthRange;

    /**
     * Saves the minimum utility in X and the maximum utility in Y
     */
    private Tuple<Number, Number> utilityRange;

    /**
     * Set containing all found players
     */
    private HashSet<Player> playerSet;

    /**
     * Saves the added branching factor of all nodes
     */
    private Integer addedBranching;

    /**
     * Saves the amount of nodes for which the terminal test was {@code true}
     */
    private Integer addedTerminations;

    /**
     * Saves the addedDepth of all nodes
     */
    private Integer addedDepth;

    /**
     * The amount of nodes visited by this worker
     */
    private Integer visitedNodeCount;

    /**
     * The time this thread ran in milliseconds
     */
    private long timeUsed = -1;

    /**
     * Magic number to offset the random seed. Change "random" outcome!
     */
    private static long SEED_OFFSET = 27l;

    /**
     * The ID of this worker
     */
    private static int threadID;

    /**
     * A sort of "magic number" which determines the percentage of heap
     * that has to be filled in order for the main part of the analysis to stop. 
     * The analysis will stop to generate more nodes when this is reached.
     * 
     * The number should:
     * - Be large enough to explore the problem
     * - Can be large since the tree will shrink in size after merging threads
     * - Be small enough for the algorithm to run on the problem
     * - Be small enough to not run into a heap overflow (multithreaded)
     * 
     * Given value was determined empirically on problems that ran out of heap before.
     * Seemingly ~0.85 is necessary. Value was chosen to be smaller than that for obvious reasons.
     */
    private double HEAP_PERCENTAGE_FULL = 0.75d;

    /**
     * Costructor of a worker thread needed for the game analysis
     * 
     * @param pGame
     *            The game object to work on. Can not be {@code null}.
     * @param pPathCount
     *            The amount of paths to be iterated over. Has to be > 0
     * @param pTree
     *            The GameTree in which the paths will be saved. Can not be
     *            {@code null} .
     * @param pMaxDepth
     * 			  The maximum depth for each path generated. Can not be < 0.
     * @param pThreadID
     * 			  The ID this thread has. Used to calculate different seeds per thread.
     */
    @SuppressWarnings("unchecked")
    public Worker(final Game<T> pGame, final int pPathCount,
            final GameTree<T> pTree, final Integer pMaxDepth,
            final int pThreadID) {
        if (pGame == null || pTree == null) {
            throw new NullPointerException("One of the parameters was null.");
        }
        if (pPathCount <= 0 || pMaxDepth < 0) {
            throw new IllegalArgumentException("Incorrect parameter values.");
        }
        game = pGame;
        tree = pTree;
        paths = new INode[pPathCount];
        problemHasTerminal = game.hasTerminalMethod();
        problemHasUtility = game.hasUtilityMethod();
        problemHasPlayerUtility = game.hasUtilityPlayerMethod();
        visitedNodeCount = 0;
        maxDepth = pMaxDepth;
        threadID = pThreadID;

        INode<T> rootNode = game.asRoot();
        // Initialize paths with root
        for (int i = 0; i < paths.length; i++) {
            paths[i] = rootNode;
        }
        tree.insertRoot(game.asRoot());

        // Initializing analysis informations
        addedBranching = 0;
        addedDepth = 0;
        addedTerminations = 0;

        depthRange = new Tuple<Integer, Integer>(Integer.MAX_VALUE,
                Integer.MIN_VALUE);
        utilityRange = new Tuple<Number, Number>(Double.MAX_VALUE,
                Double.MIN_VALUE);
        playerSet = new HashSet<Player>();
    }

    @Override
    public void run() {
        long before, after;
        before = after = System.nanoTime();
        boolean continueRun = true;

        while (continueRun) {
            continueRun = false;

            // Iterate & extend paths if < maxDepth and thread not interrupted
            for (int i = 0; i < paths.length && !isInterrupted(); i++) {
                if (problemHasTerminal && !(paths[i].getGame().isTerminal())
                        && paths[i].getDepth() < maxDepth) {
                    // Continue if at least one path can be further extended
                    continueRun = true;
                    visitedNodeCount++;

                    List<Action> actions = paths[i].getGame().getActions();

                    // Generated long consists off of depth and ID of the node
                    // Resulting in a more diverse "random" compared to just i, or i+depth
                    int chosenNumber = depthBasedRng(actions.size(), new Long(
                            threadID * SEED_OFFSET + i * paths[i].getDepth()));
                    Action chosenAction = actions.get(chosenNumber);

                    // New node for tree and index i
                    INode<T> sucNode = paths[i].getSuccessor(chosenAction,
                            MemorySavingMode.NONE); // analysis is not influenced by memory
                    tree.insert(paths[i], sucNode);
                    paths[i] = sucNode;

                    // Collecting data
                    addedBranching += actions.size();

                    // Only check for player if not terminal. Terminal is no players turn!
                    if (paths[i].getGame().hasPlayerMethod()
                            && !(paths[i].getGame().isTerminal())) {
                        playerSet.add(paths[i].getGame().getPlayer());
                    }
                }
            }
            continueRun = continueRun && checkHeapUsage(HEAP_PERCENTAGE_FULL);
        }

        for (int i = 0; i < paths.length; i++) {
            Game<T> curGame = paths[i].getGame();

            // Calculate addedDepth
            addedDepth += paths[i].getDepth();

            // Calculate addedTermination if available
            if (problemHasTerminal && curGame.isTerminal()) {
                addedTerminations++;
            }

            // Calculating utilityRange if available

            if (problemHasPlayerUtility) {
                for (Player p : playerSet) {
                    // Needed. You cant call getPlayer in Terminal
                    // But getUtility is terminal-only!
                    addPlayerUtility(curGame, p);
                }
            }
            if (problemHasUtility) {
                if (curGame.getUtility().doubleValue() < utilityRange.getX()
                        .doubleValue()) {
                    utilityRange = new Tuple<Number, Number>(
                            curGame.getUtility(), utilityRange.getY());
                }
                if (curGame.getUtility().doubleValue() > utilityRange.getY()
                        .doubleValue()) {
                    utilityRange = new Tuple<Number, Number>(
                            utilityRange.getX(), curGame.getUtility());
                }
            }

            // Calculating depthRange
            if (paths[i].getDepth() < depthRange.getX().doubleValue()) {
                depthRange = new Tuple<Integer, Integer>(paths[i].getDepth(),
                        depthRange.getY());
            }
            if (paths[i].getDepth() > depthRange.getY().doubleValue()) {
                depthRange = new Tuple<Integer, Integer>(depthRange.getX(),
                        paths[i].getDepth());
            }
        }
        // Save run time
        after = System.nanoTime();
        setTimeUsed(((after - before) / 1000000));
    }

    /**
     * Edits the utilityRange tuple. Used to insert all utility values of all players.
     * 
     * @param pGame The game to get the utility value from
     * @param pPlayer One of the players on above game
     */
    private void addPlayerUtility(final Game<T> pGame, final Player pPlayer) {
        if (pGame.getUtility(pPlayer).doubleValue() < utilityRange.getX()
                .doubleValue()) {
            utilityRange = new Tuple<Number, Number>(pGame.getUtility(pPlayer),
                    utilityRange.getY());
        }
        if (pGame.getUtility(pPlayer).doubleValue() > utilityRange.getY()
                .doubleValue()) {
            utilityRange = new Tuple<Number, Number>(utilityRange.getX(),
                    pGame.getUtility(pPlayer));
        }
    }

    /**
     * Saves the time this thread needed to run the current analysis task
     * 
     * @param pTime The time, set when finishing \{@code run()}
     */
    private void setTimeUsed(final long pTime) {
        timeUsed = pTime;
    }

    /**
     * Returns the time this thread ran
     * 
     * @return The time this thread needed to run the current analysis in milliseconds
     */
    public long getRunTime() {
        return timeUsed;
    }

    /**
     * Represents the added amount of branches of all nodes
     * 
     * @return Integer representing the added amount of branches of all nodes
     */
    public Integer getAddedBranching() {
        return addedBranching;
    }

    /**
     * The tuple that saves the minimum and maximum depth of all nodes.
     * 
     * @return A tuple containing minimum depth in X and the maximum depth in Y.
     *         Is never {@code null}.
     */
    public Tuple<Integer, Integer> getDepthRange() {
        return depthRange;
    }

    /**
     * Getter for the added depth of all nodes
     * 
     * @return The integer representing the added depth of all nodes
     */
    public Integer getAddedDepth() {
        return addedDepth;
    }

    /**
     * Returns the integer representing the amount of nodes where terminalTest
     * was {@code true}
     * 
     * @return The amount of nodes that terminated
     */
    public Integer getAddedTerminations() {
        return addedTerminations;
    }

    /**
     * Returns a tuple containing the minimum utility in X and the maximum
     * utility in Y
     * 
     * @return A tuple containing the utility range. Will never be {@code null}.
     */
    public Tuple<Number, Number> getUtilityRange() {
        return utilityRange;
    }

    /**
     * Returns a set containing all distinct players
     * 
     * @return A set containing all different players found. Is never
     *         {@code null}.
     */
    public HashSet<Player> getPlayers() {
        return playerSet;
    }

    /**
     * Returns the tree this worker has been generating
     * 
     * @return The tree object. Will never be {@code null}.
     */
    public GameTree<T> getTree() {
        return tree;
    }

    /**
     * Returns an integer representing the (total) amount of 
     * nodes visited by this worker
     * 
     * @return Number of all visited nodes
     */
    public Integer getVisitedNodeCount() {
        return visitedNodeCount;
    }

    /**
     * Uses the depth as seed. This way the result is random, 
     * but always the same (for the same input problem)
     * 
     * @param pUpTo -> nextInt(upTo)
     * @param pSeed The depth
     * @return A random number as if the random object was directly used
     */
    private int depthBasedRng(final int pUpTo, final long pSeed) {
        Random random = new Random(pSeed);
        return random.nextInt(pUpTo);
    }

    /**
     * Checks the currently used heap based on given percentage. Returns boolean value representing
     * {@code true} only if heap usage is smaller than given percentage, {@code false} otherwise.
     * 
     * @param pHeapPercentage A double. Has to be between 0 and 1. 
     * 		  Represents the percentage value used to determine the return value of this method.
     * @return Will be {@code true} if the amount of memory used 
     * 		   is lower than the percental value given in pHeapPercentage, {@code false} otherwise.
     */
    private boolean checkHeapUsage(final double pHeapPercentage) {
        if (pHeapPercentage > 1) {
            throw new IllegalArgumentException("Percentage cant be > 1");
        }
        Runtime runtime = Runtime.getRuntime();
        double total = runtime.totalMemory();
        double free = runtime.freeMemory();
        double usage = total - free;
        return (usage / total) < pHeapPercentage;
    }
}
