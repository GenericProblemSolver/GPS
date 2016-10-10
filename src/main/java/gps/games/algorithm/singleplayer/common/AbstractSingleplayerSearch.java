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
package gps.games.algorithm.singleplayer.common;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import gps.ResultEnum;
import gps.common.BenchmarkField;
import gps.games.GamesModule;
import gps.games.algorithm.AbstractGameAlgorithm;
import gps.games.algorithm.singleplayer.interfaces.IToEvaluate;
import gps.games.wrapper.Game;
import gps.games.wrapper.successor.INode;
import gps.games.wrapper.successor.NodeUtil;

/**
 * Implementation of singleplayer search algorithms.
 *
 * @author haker@uni-bremen.de
 *
 * @param <T>
 *            The type of the problem class
 */
public abstract class AbstractSingleplayerSearch<T>
        extends AbstractGameAlgorithm<T> {

    /**
     * The best terminal node that has been found by the algorithm.
     */
    INode<T> terminalNode = null;

    /**
     * Queue of Nodes that have to be evaluated eventually.
     */
    final IToEvaluate<T> toEvaluate;

    /**
     * Stores the root node for subclasses to access
     */
    INode<T> root;

    public AbstractSingleplayerSearch(final GamesModule<T> pModule,
            IToEvaluate<T> pToEval) {
        super(pModule, BenchmarkField.PROCESSED_NODES,
                BenchmarkField.SEEN_NODES,
                BenchmarkField.DEEPEST_DISCOVERED_NODE,
                BenchmarkField.BEST_MOVE_DEPTH);
        toEvaluate = pToEval;

        root = pModule.getGame().asRoot();

        toEvaluate.add(root);
    }

    public AbstractSingleplayerSearch(final GamesModule<T> pModule) {
        super(pModule);
        toEvaluate = null;
    }

    /**
     * The logger for this class
     */
    private static Logger logger = Logger
            .getLogger(AbstractSingleplayerSearch.class.getCanonicalName());

    @Override
    public boolean isApplicable(ResultEnum type) {
        Game<T> game = module.getGame();
        return (type.equals(ResultEnum.WINNABLE)) && game.hasSuccessorsMethod()
                && game.hasTerminalMethod();
    }

    static {
        logger.setLevel(Level.WARNING);
    }

    /**
     * Defines the algorithm specific handling of a node and its successor when traversing the search space
     * @param pNode
     *          the base node
     * @param pSuccessor
     *          its successor
     */
    void handle(final INode pNode, final INode pSuccessor) {
        benchmark.seenNodes++;
        if (module.getDepthlimit() <= 0
                || pSuccessor.getDepth() <= module.getDepthlimit()) {
            toEvaluate.add(pSuccessor);
        }
    }

    /**
     * Perform the search. Stops if the Interrupt Signal of the executing thread
     * is set.
     */
    protected void search() {
        try {
            while (!Thread.currentThread().isInterrupted()
                    && toEvaluate.hasNext()) {
                final INode<T> node = toEvaluate.retrieveNext();
                final Game<T> gameOfNode = node.getGame();

                boolean ret = false;

                if (gameOfNode.isTerminal()) {
                    if (NodeUtil.compareNodeUtility(terminalNode, node) < 0) {
                        benchmark.bestMoveDepth = Optional.of(node.getDepth());
                        terminalNode = node;
                        ret = true;
                    }
                }

                benchmark.processedNodes++;

                List<? extends INode<T>> successors = node
                        .getSuccessors(getMemorySavingMode());
                successors.stream().forEach(p -> {
                    if (p.getDepth() > benchmark.deepestDiscoveredNode) {
                        benchmark.deepestDiscoveredNode = p.getDepth();
                    }
                    handle(node, p);
                });
                if (ret) {
                    return;
                }
            }
        } finally {
            logger.info(benchmark.toString());
        }
    }

    @Override
    public Optional<T> terminalState() {
        search();
        if (terminalNode == null) {
            return Optional.empty();
        }
        return Optional.of(terminalNode.getGame().getProblem());
    }

    @Override
    public Optional<Boolean> isWinnable() {
        while (!isFinished()) {
            if (terminalNode != null) {
                return Optional.of(true);
            }
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            search();
        }
        if (terminalNode == null && isFinished()) {
            Optional.of(false);
        }
        return Optional.empty();
    }

    @Override
    public boolean isFinished() {
        return !toEvaluate.hasNext();
    }
}
