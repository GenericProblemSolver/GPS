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
package gps.games.algorithm.analysis;

import gps.ResultEnum;
import gps.games.GamesModule;
import gps.games.algorithm.AbstractGameAlgorithm;
import gps.games.analysis.util.MemoryAnalysisUtility;
import gps.games.analysis.util.Worker;
import gps.games.util.GameTree;
import gps.games.wrapper.Game;
import gps.games.wrapper.Player;
import gps.util.Tuple;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * This class will analyse a given game object. The game object contains a
 * problem of type T which was specified by the user.
 * 
 * @author Fabian
 * @author haker@uni-bremen.de
 *
 * @param <T>
 *            The type of the problem in the game object
 */
public class GameAnalyser<T> extends AbstractGameAlgorithm<T> {

    /**
     * The amount of random game-paths to be calculated per thread
     */
    private int pathCount = 250;

    /**
     * The number of cores available to the java-vm.
     */
    private final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * The game object to analyse
     */
    private final Game<T> game;

    /**
     * The maximum depth after which the analysis will stop to calculate a path
     */
    private final Integer maxDepth;

    /**
     * When this boolean is set {@code true} the analysis will try and come to 
     * an end as soon as possible. This means that it will skil all remaining 
     * analysis steps and immediately start to calculate results based on the 
     * data already analysed. The only exception to this is the memory analysis,
     * which is time intensive to calculate and thus also interrupted.
     */
    boolean shutDownAsap = false;

    /**
     * Default - constructor for the GameAnalyser. For custom values use the
     * other constructor.
     * 
     * @param pModule
     *            The module that instantiated the algorithm.
     */
    public GameAnalyser(final GamesModule<T> pModule) {
        super(pModule);
        int pMaxDepth = 1000;
        if (pModule == null) {
            throw new NullPointerException();
        }
        if (pMaxDepth < 0) {
            throw new IllegalArgumentException("Depth Limit cant be < 0");
        }
        game = pModule.getGame();
        maxDepth = pMaxDepth;
    }

    /**
     * Generated a tree object by randomly choosing actions to perform on the
     * game states. Analyses the tree to gain informations about the problem.
     * 
     * @return The IGameAnalysisResult<T> with T being the type of the problem.
     *         Contains all gained informations about the problem and the tree
     *         object itself.
     */
    public IGameAnalysisResult analyze() {
        // Measures how much memory is in use before the analysis
        MemoryAnalysisUtility.startMeasurement();

        @SuppressWarnings("unchecked")
        final GameTree<T>[] trees = new GameTree[THREAD_COUNT];
        @SuppressWarnings("unchecked")
        final Worker<T>[] workers = new Worker[THREAD_COUNT];

        // Creating workers
        for (int i = 0; i < THREAD_COUNT; i++) {
            trees[i] = new GameTree<T>();
            workers[i] = new Worker<T>(game.copy(), pathCount, trees[i],
                    maxDepth, i);
            workers[i].start();
        }

        // Waiting for workers to join
        for (int i = 0; i < THREAD_COUNT; i++) {
            try {
                workers[i].join();
            } catch (InterruptedException e) {
                // In this case the thread was interrupted, meaning the analysis
                // should stop
                for (Worker<T> w : workers) {
                    w.interrupt();
                }
                shutDownAsap = true;
                Thread.currentThread().interrupt();
            }
        }
        if (Thread.currentThread().isInterrupted()) {
            // interrupted() changes interrupt state
            // Workers should be interrupted now
            for (int i = 0; i < THREAD_COUNT; i++) {
                while (workers[i].isAlive()) {
                    try {
                        workers[i].join();
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }
            Thread.currentThread().interrupt();
        }
        Double avgBranchingFactor = 0.0;
        Double avgDepth;
        Tuple<Integer, Integer> depthRange = new Tuple<Integer, Integer>(
                Integer.MAX_VALUE, Integer.MIN_VALUE);
        Integer playerCount;
        Double terminationRate = 0.0;
        Tuple<Number, Number> utilityRange = new Tuple<Number, Number>(
                Double.MAX_VALUE, Double.MIN_VALUE);
        GameTree<T> tree = new GameTree<T>();
        Double avgTime;
        tree.insertRoot(game.asRoot());

        // Tmp variables
        double addedBranchingFactor = 0;
        double addedDepth = 0;
        double addedTerminal = 0;
        double addedPaths = pathCount * THREAD_COUNT;
        Integer addedNodeCount = 0;
        double addedTime = 0;
        Set<Player> players = new HashSet<Player>();

        // Collecting data from worker threads
        for (int i = 0; i < workers.length; i++) {
            players.addAll(workers[i].getPlayers());
            tree.insert(workers[i].getTree());

            addedBranchingFactor += workers[i].getAddedBranching();
            addedDepth += workers[i].getAddedDepth();
            addedTerminal += workers[i].getAddedTerminations();
            addedNodeCount += workers[i].getVisitedNodeCount();
            addedTime += workers[i].getRunTime();

            // Depth Range
            if (workers[i].getDepthRange().getX().doubleValue() < depthRange
                    .getX().doubleValue()) {
                depthRange = new Tuple<Integer, Integer>(
                        workers[i].getDepthRange().getX(), depthRange.getY());
            }
            if (workers[i].getDepthRange().getY().doubleValue() > depthRange
                    .getY().doubleValue()) {
                depthRange = new Tuple<Integer, Integer>(depthRange.getX(),
                        workers[i].getDepthRange().getY());
            }

            // Utility Range
            if (workers[i].getUtilityRange().getX().doubleValue() < utilityRange
                    .getX().doubleValue()) {
                utilityRange = new Tuple<Number, Number>(
                        workers[i].getUtilityRange().getX(),
                        utilityRange.getY());
            }
            if (workers[i].getUtilityRange().getY().doubleValue() > utilityRange
                    .getY().doubleValue()) {
                utilityRange = new Tuple<Number, Number>(utilityRange.getX(),
                        workers[i].getUtilityRange().getY());
            }
        }

        // Would result in divide by 0. Should not happen anyways
        if (addedNodeCount != 0) {
            avgBranchingFactor = addedBranchingFactor / addedNodeCount;
        }
        avgDepth = addedDepth / addedPaths;
        playerCount = players.size() > 0 ? players.size() : -1;
        terminationRate = addedTerminal / addedPaths;
        avgTime = addedTime / THREAD_COUNT;

        Optional<Integer> oPlayerCount = Optional.empty();
        Optional<Tuple<Number, Number>> oUtilityRange = Optional.empty();

        // Optional player
        if (playerCount > -1) {
            oPlayerCount = Optional.of(playerCount);
        }

        // Optional utility range, only if values changed
        if (utilityRange.getX().doubleValue() != Double.MAX_VALUE
                && utilityRange.getY().doubleValue() != Double.MIN_VALUE) {
            oUtilityRange = Optional.of(utilityRange);
        }

        Optional<Double> memoryPerNode = Optional.empty();

        // Analysing memory usage of tree object (after analysis)
        if (!shutDownAsap) {
            // This, esp. hardClean(), takes quite some time. 
            // So we wont do it in case of thread.interrupt!
            int discreteNodeCount = tree.entrySet().size();
            long memoryIncrease = MemoryAnalysisUtility.getMemoryIncrease();
            double calc = memoryIncrease / discreteNodeCount;
            calc = calc / 1024 / 1024; // convert to mb
            memoryPerNode = Optional.of(calc);
        }
        // No perfect way to measure memory. In case of "error", Optional.empty is set
        if (memoryPerNode.isPresent()
                && memoryPerNode.get().doubleValue() < 0) {
            memoryPerNode = Optional.empty();
        }

        GameAnalysisResult gra = new GameAnalysisResult(avgBranchingFactor,
                avgDepth, avgTime, addedNodeCount, terminationRate, depthRange,
                tree, oPlayerCount, oUtilityRange, memoryPerNode);

        return gra;
    }

    /**
     * Sets the amount of paths to be calculated per thread. A high value will
     * result in a statistically more meaningful result, but at the same time
     * decrease the amount of time available to each path. This will decrease
     * the average depth on complex problems and thus might lead to less paths
     * terminating.
     * 
     * @param pPathCount
     *            The new amount of paths to be calculated per thread. Can not
     *            be <= 0.
     */
    public void setPathsPerThread(final int pPathCount) {
        if (pPathCount <= 0) {
            throw new IllegalArgumentException("Path count cant be <= 0");
        }
        pathCount = pPathCount;
    }

    @Override
    public String getName() {
        return "Game Analyser";
    }

    @Override
    public boolean isApplicable(ResultEnum type) {
        return type.equals(ResultEnum.GAME_ANALYSIS);
    }

    @Override
    public Optional<IGameAnalysisResult> gameAnalysis() {
        return Optional.ofNullable(analyze());
    }

    /**
     * Returns a boolean which, if set {@code true}, indicates that the 
     * analysis will come to an end as soon as possible. This means that 
     * the remaining analysis steps, as well as the memory calculations (!) 
     * will be ignored.
     * 
     * @return If this boolean is set {@code true} the analysis will 
     *		   come to an end as soon as possible. 
     */
    public boolean isShutDown() {
        return shutDownAsap;
    }
}
