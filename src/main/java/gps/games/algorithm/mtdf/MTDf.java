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
package gps.games.algorithm.mtdf;

import gps.ResultEnum;
import gps.common.BenchmarkField;
import gps.games.GamesModule;
import gps.games.algorithm.AbstractGameAlgorithm;
import gps.games.algorithm.alphabetapruning.AlphaBetaPruning;
import gps.games.util.transpositionTable.TranspositionTable;
import gps.games.wrapper.Action;
import gps.games.wrapper.successor.INode;
import gps.games.wrapper.successor.Node;

import java.util.Optional;

/**
 * Implementation of the MTD(f) algorithm by Aske Plaat
 * (https://askeplaat.wordpress.com/534-2/mtdf-algorithm/).
 * <p>
 * Game problems, that should be solved using this algorithm, have to implement
 * a heuristic method and a good hashCode method. This algorithm also needs a
 * depth limit. If you do not set it, a default value is used.
 *
 * @author alueck@uni-bremen.de
 */
public class MTDf<T> extends AbstractGameAlgorithm<T> {

    /**
     * Alpha beta pruning algorithm used by this algorithm.
     */
    private final AlphaBetaPruning<T> alphaBetaPruning;

    /**
     * Size of the transposition table. Default is 100000. Can be set using
     * {@link #setTranspositionTableSize(int)}. This must be done before
     * starting the algorithm.
     */
    private int transpositionTableSize = 100000;

    /**
     * The best move found by this algorithm.
     */
    private Action bestMove;

    /**
     * Creates a new MTDf algorithm instance. The depth limit of the
     * {@link GamesModule} will be set to 15, if it was not already set before.
     * Assuming that you also use a time limit (by interrupting the thread).
     *
     * @param pModule
     *            The module that instantiated the algorithm.
     */
    public MTDf(final GamesModule<T> pModule) {
        super(pModule, BenchmarkField.DEEPEST_DISCOVERED_NODE,
                BenchmarkField.PROCESSED_NODES,
                BenchmarkField.BEST_MOVE_HEURISTIC);
        alphaBetaPruning = new AlphaBetaPruning<>(pModule);
        if (module.getDepthlimit() <= 0) {
            module.setDepthlimit(15);
        }
    }

    @Override
    public boolean isApplicable(final ResultEnum type) {
        return alphaBetaPruning.isApplicable(type)
                && module.getGame().hasUserHeuristicPlayerMethod();
    }

    @Override
    public Optional<Action> bestMove() {
        bestMove = null;
        iterativeDeepening(new Node<>(module.getGame()));
        return Optional.ofNullable(bestMove);
    }

    private double mtdf(INode<T> root, double f, int depth) {
        double g = f;
        double upperBound = Double.MAX_VALUE;
        double lowerBound = -Double.MAX_VALUE;
        double beta;
        alphaBetaPruning.setMtdfDepthLimit(depth);
        alphaBetaPruning.clearTranspositionTable();
        while (lowerBound < upperBound) {
            beta = (g == lowerBound) ? g + 1 : g;
            g = alphaBetaPruning.withMemory(root, beta - 1, beta,
                    transpositionTableSize);
            if (g < beta) {
                upperBound = g;
            } else {
                lowerBound = g;
            }
        }
        bestMove = alphaBetaPruning.getBestMove();
        benchmark.deepestDiscoveredNode = (Integer) alphaBetaPruning
                .getBenchmark().getField(BenchmarkField.DEEPEST_DISCOVERED_NODE)
                .get();
        benchmark.processedNodes = (Integer) alphaBetaPruning.getBenchmark()
                .getField(BenchmarkField.PROCESSED_NODES).get();
        benchmark.bestMoveHeuristic = alphaBetaPruning.getBenchmark()
                .getField(BenchmarkField.BEST_MOVE_HEURISTIC);
        return g;
    }

    private double iterativeDeepening(INode<T> root) {
        if (module.getDepthlimit() <= 0) {
            throw new IllegalArgumentException(
                    "maxDepth must be 1 or greater.");
        }
        double firstguess = 0;
        for (int d = 1; d <= module.getDepthlimit(); d++) {
            firstguess = mtdf(root, firstguess, d);
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
        }
        return firstguess;
    }

    /**
     * Allows to set the size of the {@link TranspositionTable}. Must be invoked
     * before starting the algorithm.
     *
     * @param transpositionTableSize
     *            size the transposition table should have
     */
    public void setTranspositionTableSize(int transpositionTableSize) {
        this.transpositionTableSize = transpositionTableSize;
    }

    @Override
    public String getName() {
        return "MTDf";
    }
}
