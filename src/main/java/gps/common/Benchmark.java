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
package gps.common;

import java.util.Optional;

/**
 * A simple data class for holding benchmark counters. These counters are
 * updated during the runtime of an algorithm. Only primitve types are allowed
 * because this class may be accessed by other threads and concurrency problems
 * could occur.
 * 
 * There is no need for synchronization if only primitve types are used. The
 * fields may not be set by other threads or algorithms. Only reading operations
 * are allowed from other threads.
 * 
 * This class is the internal class for algorithms. The interface IBenchmark
 * does not provide any setters to prevent writing from other threads.
 * 
 * Other threads read concurrently over time the values and render them as a
 * graph.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class Benchmark implements IBenchmark {

    /**
     * THis fields are used by the algorithm
     */
    private final BenchmarkField usedFields[];

    /**
     * Construct a Benchmark Object.
     * 
     * @param usedFields
     *            The fields that are used by the algorithm.
     */
    public Benchmark(BenchmarkField... usedFields) {
        this.usedFields = usedFields;
    }

    @Override
    public BenchmarkField[] getUsedFields() {
        return usedFields;
    }

    @Override
    public Optional<? extends Number> getField(BenchmarkField field) {
        if (field == null) {
            throw new IllegalArgumentException("field may not null");
        }
        switch (field) {
        case BEST_MOVE_DEPTH:
            return bestMoveDepth;
        case DEEPEST_DISCOVERED_NODE:
            return Optional.of(deepestDiscoveredNode);
        case PROCESSED_NODES:
            return Optional.of(processedNodes);
        case SEEN_NODES:
            return Optional.of(seenNodes);
        case BEST_SOLUTION:
            return Optional.of(bestSolution);
        case TEMPERATURE:
            return Optional.of(temperature);
        case CLEARNESS_OF_BEST_MOVE:
            return clearnessOfBestMove;
        case GAME_TREE_DEPTH:
            return Optional.of(gameTreeDepth);
        case NUMBER_OF_SIMULATIONS:
            return Optional.of(numberOfSimulations);
        case BEST_MOVE_HEURISTIC:
            return bestMoveHeuristic;
        }
        throw new UnsupportedOperationException("the field " + field.toString()
                + " is not covered by the getField() method.");
    }

    /**
     * Amount of nodes that has been seen
     */
    public int seenNodes = 0;

    /**
     * Amount of nodes that has been processed
     *
     */
    public int processedNodes = 0;

    /**
     * The number of simulations that were executed during the
     * course of the algorithm
     *
     * Refers to {@link BenchmarkField#NUMBER_OF_SIMULATIONS}
     */
    public int numberOfSimulations = 0;

    /**
     * A measure for the clearness of the solution or an
     * empty {@link Optional} if no solution was determined.
     *
     * Refers to {@link BenchmarkField#CLEARNESS_OF_BEST_MOVE}
     */
    public Optional<Double> clearnessOfBestMove = Optional.empty();

    /**
     * A measure for the quality of a determined best move according
     * to the heuristic value.
     *
     * Empty, if no heuristic method exists or no best move was determined.
     *
     * Refers to {@link BenchmarkField#BEST_MOVE_HEURISTIC}
     *
     */
    public Optional<? extends Number> bestMoveHeuristic = Optional.empty();

    /**
     * The depth of the game tree at it deepest point.
     *
     * Refers to {@link BenchmarkField#GAME_TREE_DEPTH}
     */
    public int gameTreeDepth = 0;

    /**
     * The move depth of the best terminal found
     */
    public Optional<Integer> bestMoveDepth = Optional.empty();

    /**
     * The deepest expanded move sequence
     */
    public int deepestDiscoveredNode = 0;

    /**
     * Best solution that has been found so far.
     */
    public Number bestSolution = 0;

    /**
     * Temperature of simulated annealing.
     */
    public int temperature;

}
