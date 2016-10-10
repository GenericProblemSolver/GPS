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
import java.util.logging.Logger;
import java.util.stream.Collectors;

import gps.ResultEnum;
import gps.games.GamesModule;
import gps.games.algorithm.singleplayer.interfaces.IToEvaluate;
import gps.games.util.GameTree;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;
import gps.games.wrapper.successor.INode;

/**
 * Implementation of singleplayer search algorithms.
 *
 * @author haker@uni-bremen.de
 *
 * @param <T>
 *            The type of the problem class
 */
public abstract class AbstractSingleplayerPathAlgorithm<T>
        extends AbstractSingleplayerSearch<T> {

    /**
     * The logger for this class
     */
    private static Logger logger = Logger.getLogger(
            AbstractSingleplayerPathAlgorithm.class.getCanonicalName());

    /**
     * GameTree that models the tree of all expanded game states
     */
    private GameTree<T> gameTree = new GameTree<>();

    public AbstractSingleplayerPathAlgorithm(final GamesModule<T> pModule,
            IToEvaluate<T> pToEval) {
        super(pModule, pToEval);
        gameTree.insertRoot(root);
    }

    public AbstractSingleplayerPathAlgorithm(final GamesModule<T> pModule) {
        super(pModule);
    }

    @Override
    public boolean isApplicable(ResultEnum type) {
        Game<T> game = module.getGame();
        return (type.equals(ResultEnum.STATE_SEQ)
                || type.equals(ResultEnum.MOVES)
                || type.equals(ResultEnum.TERMINAL)
                || type.equals(ResultEnum.WINNABLE))
                && game.hasSuccessorsMethod() && game.hasTerminalMethod();
    }

    @Override
    void handle(final INode pNode, final INode pSuccessor) {
        if (!gameTree.contains(pSuccessor)) {
            benchmark.seenNodes++;
            gameTree.insert(pNode, pSuccessor);
            if (module.getDepthlimit() <= 0
                    || pSuccessor.getDepth() <= module.getDepthlimit()) {
                toEvaluate.add(pSuccessor);
            }
        }
    }

    @Override
    public Optional<List<T>> stateSequence() {
        search();
        if (terminalNode == null) {
            return Optional.empty();
        }
        return Optional.of(gameTree.getPathTo(terminalNode).stream()
                .map(m -> m.getGame().getProblem())
                .collect(Collectors.toList()));
    }

    @Override
    public Optional<List<Action>> moves() {
        search();
        if (terminalNode == null) {
            return Optional.empty();
        }
        return Optional.of(gameTree.getPathToAsActions(terminalNode));
    }

    @Override
    public boolean isFinished() {
        return !toEvaluate.hasNext();
    }
}
