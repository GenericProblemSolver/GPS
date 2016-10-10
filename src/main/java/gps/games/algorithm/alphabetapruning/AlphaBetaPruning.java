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
package gps.games.algorithm.alphabetapruning;

import gps.ResultEnum;
import gps.common.BenchmarkField;
import gps.games.GamesModule;
import gps.games.algorithm.AbstractGameAlgorithm;
import gps.games.util.transpositionTable.TranspositionTable;
import gps.games.wrapper.Action;
import gps.games.wrapper.IHeuristicPlayer;
import gps.games.wrapper.Player;
import gps.games.wrapper.successor.GameHeuristicComparator;
import gps.games.wrapper.successor.INode;
import gps.games.wrapper.successor.Node;
import gps.util.Tuple;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of an alpha beta pruning algorithm. This algorithm can only be
 * used for 2 player games and returns the next best move for the maximizing
 * player. It can be depth or time limited, although a time limit does not make
 * much sense for a depth-first algorithm, so if a time limit should be used,
 * {@link gps.games.algorithm.mtdf.MTDf} is the better choice.
 * <p>
 * If a limit is used, the game problem should offer a heuristic method for
 * reliable results.
 *
 * @author alueck@uni-bremen.de
 */
public class AlphaBetaPruning<T> extends AbstractGameAlgorithm<T> {

    /**
     * Maximizing player.
     */
    private Player maxPlayer;

    /**
     * The comparator, which is used to apply move ordering, if the game offers
     * a heuristic.
     */
    private final GameHeuristicComparator comparator;

    /**
     * The action representing the best move found.
     */
    private Action bestMove;

    /**
     * Transposition table used for {@link #withMemory(INode, double, double,
     * int)}.
     */
    private TranspositionTable<T> transpositionTable;

    /**
     * Heuristic used by this algorithm.
     */
    private IHeuristicPlayer heuristic = module.getGame()
            .getUserHeuristicMultiplayer();

    /**
     * Depth limit used in {@link #withMemory(INode, double, double, int)},
     * which is used by {@link gps.games.algorithm.mtdf.MTDf}.
     */
    private int mtdfDepthLimit;

    /**
     * Creates a new alpha-beta-pruning algorithm instance with the given {@link
     * GamesModule}. Player MAX is the current player of {@link
     * GamesModule#game}.
     *
     * @param pModule
     *         the current state of the game
     */
    public AlphaBetaPruning(final GamesModule<T> pModule) {
        super(pModule, BenchmarkField.DEEPEST_DISCOVERED_NODE,
                BenchmarkField.PROCESSED_NODES,
                BenchmarkField.BEST_MOVE_HEURISTIC);
        maxPlayer = module.getGame().hasPlayerMethod()
                ? module.getGame().getPlayer() : null;
        comparator = new GameHeuristicComparator(heuristic, maxPlayer);
    }

    @Override
    public boolean isApplicable(ResultEnum type) {
        return type == ResultEnum.BEST_MOVE
                && module.getGame().hasPlayerMethod()
                && module.getGame().hasUtilityPlayerMethod()
                && module.gameAnalysis().isPresent()
                && module.gameAnalysis().get().getPlayerNumber().isPresent()
                && module.gameAnalysis().get().getPlayerNumber().get() == 2;
    }

    @Override
    public Optional<Action> bestMove() {
        bestMove = null;
        if (module.getGame().hasUserHeuristicPlayerMethod()) {
            maxWithHeuristic(new Node<>(module.getGame()), -Double.MAX_VALUE,
                    Double.MAX_VALUE);
        } else {
            maxWithoutHeuristic(new Node<>(module.getGame()), -Double.MAX_VALUE,
                    Double.MAX_VALUE);
        }
        if (bestMove != null) {
            benchmark.bestMoveHeuristic = module.getGame()
                    .multiplayerScore(bestMove);
        }
        return Optional.ofNullable(bestMove);
    }

    private double minWithHeuristic(final INode<T> state, final double alpha,
            final double beta) {
        benchmark.processedNodes++;
        if (state.getDepth() > benchmark.deepestDiscoveredNode) {
            benchmark.deepestDiscoveredNode = state.getDepth();
        }
        if (state.getGame().isTerminal()) {
            return state.getGame().getUtility(maxPlayer).doubleValue();
        }
        if (module.getDepthlimit() > 0
                && state.getDepth() == module.getDepthlimit()
                || Thread.currentThread().isInterrupted()) {
            return heuristic.eval(state.getGame(), maxPlayer);
        }
        double minScore = beta;
        List<INode<T>> successors = state.getSuccessors(getMemorySavingMode());
        Collections.sort(successors, comparator);
        for (INode<T> successor : successors) {
            double score = maxWithHeuristic(successor, alpha, minScore);
            if (score < minScore) {
                minScore = score;
                if (minScore <= alpha) {
                    break;
                }
            }
        }
        return minScore;
    }

    private double maxWithHeuristic(final INode<T> state, final double alpha,
            final double beta) {
        benchmark.processedNodes++;
        if (state.getDepth() > benchmark.deepestDiscoveredNode) {
            benchmark.deepestDiscoveredNode = state.getDepth();
        }
        if (state.getGame().isTerminal()) {
            return state.getGame().getUtility(maxPlayer).doubleValue();
        }
        if (module.getDepthlimit() > 0
                && state.getDepth() == module.getDepthlimit()
                || Thread.currentThread().isInterrupted()) {
            return heuristic.eval(state.getGame(), maxPlayer);
        }
        double maxScore = alpha;
        List<INode<T>> successors = state.getSuccessors(getMemorySavingMode());
        Collections.sort(successors, Collections.reverseOrder(comparator));
        for (INode<T> successor : successors) {
            double score = minWithHeuristic(successor, maxScore, beta);
            if (score > maxScore) {
                maxScore = score;
                if (maxScore >= beta) {
                    break;
                }
                if (state.getDepth() == 0) {
                    bestMove = successor.getAction();
                }
            }
        }
        return maxScore;
    }

    private double minWithoutHeuristic(final INode<T> state, final double alpha,
            final double beta) {
        benchmark.processedNodes++;
        if (state.getDepth() > benchmark.deepestDiscoveredNode) {
            benchmark.deepestDiscoveredNode = state.getDepth();
        }
        if (state.getGame().isTerminal()) {
            return state.getGame().getUtility(maxPlayer).doubleValue();
        }
        if (module.getDepthlimit() > 0
                && state.getDepth() == module.getDepthlimit()
                || Thread.currentThread().isInterrupted()) {
            return -Double.MAX_VALUE;
        }
        double minScore = beta;
        List<INode<T>> successors = state.getSuccessors(getMemorySavingMode());
        for (INode<T> successor : successors) {
            double score = maxWithoutHeuristic(successor, alpha, minScore);
            if (score < minScore) {
                minScore = score;
                if (minScore <= alpha) {
                    break;
                }
            }
        }
        return minScore;
    }

    private double maxWithoutHeuristic(final INode<T> state, final double alpha,
            final double beta) {
        benchmark.processedNodes++;
        if (state.getDepth() > benchmark.deepestDiscoveredNode) {
            benchmark.deepestDiscoveredNode = state.getDepth();
        }
        if (state.getGame().isTerminal()) {
            return state.getGame().getUtility(maxPlayer).doubleValue();
        }
        if (module.getDepthlimit() > 0
                && state.getDepth() == module.getDepthlimit()
                || Thread.currentThread().isInterrupted()) {
            return Double.MAX_VALUE;
        }
        double maxScore = alpha;
        List<INode<T>> successors = state.getSuccessors(getMemorySavingMode());
        for (INode<T> successor : successors) {
            double score = minWithoutHeuristic(successor, maxScore, beta);
            if (score > maxScore) {
                maxScore = score;
                if (maxScore >= beta) {
                    break;
                }
                if (state.getDepth() == 0) {
                    bestMove = successor.getAction();
                }
            }
        }
        return maxScore;
    }

    /**
     * Alpha beta pruning with transposition table. Only depth terminated, time
     * termination is implemented by {@link gps.games.algorithm.mtdf.MTDf} which
     * uses this method.
     *
     * @param state
     *         game state whose successors should be analyzed
     * @param alpha
     *         alpha value
     * @param beta
     *         beta vlaue
     * @param transpositionTableSize
     *         size of the transposition table (only matters if the table has
     *         not yet been created)
     *
     * @return minimax value
     */
    public double withMemory(final INode<T> state, double alpha, double beta,
            final int transpositionTableSize) {
        benchmark.processedNodes++;
        if (state.getDepth() > benchmark.deepestDiscoveredNode) {
            benchmark.deepestDiscoveredNode = state.getDepth();
        }
        //create transposition table, if it does not already exist
        if (transpositionTable == null) {
            transpositionTable = new TranspositionTable<>(
                    transpositionTableSize, false);
        }
        //transposition table lookup
        Tuple<Number, Number> bounds = transpositionTable.get(state.getGame());
        double lowerBound;
        double upperBound;
        if (bounds != null) {
            lowerBound = bounds.getX().doubleValue();
            upperBound = bounds.getY().doubleValue();
            if (lowerBound >= beta) {
                return lowerBound;
            }
            if (upperBound <= alpha) {
                return upperBound;
            }
            alpha = Math.max(alpha, lowerBound);
            beta = Math.min(beta, upperBound);
        }
        double bestScore;
        if (state.getGame().isTerminal()) {
            bestScore = state.getGame().getUtility(maxPlayer).doubleValue();
        } else if (Thread.currentThread().isInterrupted()
                || state.getDepth() == mtdfDepthLimit) {
            bestScore = heuristic.eval(state.getGame(), maxPlayer);
        } else {
            List<INode<T>> successors = state
                    .getSuccessors(getMemorySavingMode());
            //max node
            if (state.getGame().getPlayer().equals(maxPlayer)) {
                successors.sort(Collections.reverseOrder(comparator));
                bestScore = -Double.MAX_VALUE;
                double a = alpha;
                for (INode<T> successor : successors) {
                    double score = withMemory(successor, a, beta, 0);
                    if (score > bestScore) {
                        bestScore = score;
                        a = Math.max(a, bestScore);
                        if (state.getDepth() == 0) {
                            bestMove = successor.getAction();
                        }
                        if (bestScore >= beta) {
                            break;
                        }
                    }
                }
                // min node
            } else {
                successors.sort(comparator);
                bestScore = Double.MAX_VALUE;
                double b = beta;
                for (INode<T> successor : successors) {
                    double score = withMemory(successor, alpha, b, 0);
                    if (score < bestScore) {
                        bestScore = score;
                        b = Math.min(b, bestScore);
                        if (bestScore <= alpha) {
                            break;
                        }
                    }
                }
            }
        }
        // store bounds in transposition table
        if (bestScore <= alpha) {
            double lowerBoundEntry = (bounds == null) ? -Double.MAX_VALUE
                    : bounds.getX().doubleValue();
            transpositionTable.put(state.getGame(), lowerBoundEntry, bestScore,
                    state.getDepth());

        }
        if (bestScore > alpha && bestScore < beta) {
            transpositionTable.put(state.getGame(), bestScore, bestScore,
                    state.getDepth());
        }
        if (bestScore >= beta) {
            double upperBoundEntry = (bounds == null) ? Double.MAX_VALUE
                    : bounds.getY().doubleValue();
            transpositionTable.put(state.getGame(), bestScore, upperBoundEntry,
                    state.getDepth());
        }
        return bestScore;
    }

    public Action getBestMove() {
        return bestMove;
    }

    public void clearTranspositionTable() {
        if (transpositionTable != null) {
            transpositionTable.clear();
        }
    }

    public void setMtdfDepthLimit(int mtdfDepthLimit) {
        this.mtdfDepthLimit = mtdfDepthLimit;
    }

    @Override
    public String getName() {
        return "A-B-Pruning";
    }

}
