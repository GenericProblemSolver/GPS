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
package gps.games.wrapper.successor;

import gps.games.MemorySavingMode;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Note class that implements the INode interface.
 *
 * @param <T>
 *            The type of the problem class.
 *
 * @author haker@uni-bremen.de, alueck@uni-bremen.de
 */
public class Node<T> implements INode<T> {

    /**
     * The depth of the game node. This game state can be reached by this number
     * of moves.
     */
    protected final int depth;

    /**
     * The game state of this node.
     */
    protected final Game<T> game;

    /**
     * The action which lead to this game state.
     */
    protected final Action action;

    /**
     * A boolean that signals whether {@link #game} is a terminal state.
     */
    // Jens: I added this because calling node.getGame().isTerminal() caused concurrency problems
    protected final boolean isTerminal;

    /**
     * Construct a Root node from the given game. The root node is considered of
     * a depth 0 node. The direct successors are of depth 1 etc. The
     * {@link #action} field of a root node is {@code null}. A deep copy of the
     * given game state will be created.
     *
     * @param pGame
     *            The game state that is considered the root node.
     */
    public Node(final Game<T> pGame) {
        game = pGame.copy();
        depth = 0;
        action = null;
        isTerminal = game.isTerminal();
    }

    /**
     * Constructs a node where the given action is applied to the given game
     * state to create the successor state.
     *
     * @param pAction
     *            action to be applied to create the game state of this node
     * @param pGame
     *            predecessor game state of this node
     * @param pDepth
     *            depth of this node
     * @param memorySavingMode
     *            The memory saving mode that is used when a copy of the game
     *            state is created.
     */
    protected Node(final Action pAction, final Game<T> pGame, final int pDepth,
            final MemorySavingMode memorySavingMode) {
        game = pGame.getNewGame(pAction, memorySavingMode);
        depth = pDepth;
        action = pAction;
        isTerminal = game.isTerminal();
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public Game<T> getGame() {
        return game;
    }

    @Override
    public List<INode<T>> getSuccessors(
            final MemorySavingMode memorySavingMode) {
        return getSuccessors(depth + 1, game, memorySavingMode);
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    // Jens: I added this because calling node.getGame().isTerminal() caused concurrency problems
    public boolean isTerminal() {
        return isTerminal;
    }

    /**
     * Get the direct successors of the given game state.
     *
     * @param pSuccessorDepth
     *            The depth of the successors.
     * @param pGame
     *            The predecessor game state of the successor.
     * @param memorySavingMode
     *            The memory saving mode that is used when a copy of the game
     *            state is created.
     *
     * @return The successors.
     */
    protected static <T> List<INode<T>> getSuccessors(final int pSuccessorDepth,
            final Game<T> pGame, final MemorySavingMode memorySavingMode) {
        return pGame
                .getActions().stream().map(m -> new Node<>(m, pGame,
                        pSuccessorDepth, memorySavingMode))
                .collect(Collectors.toList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGame(), getDepth(), getAction());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof INode)) {
            return false;
        }
        INode<?> g = (INode<?>) obj;
        return getGame().equals(g.getGame()) && getDepth() == g.getDepth()
                && getAction() == null ? g.getAction() == null
                        : getAction().equals(g.getAction());
    }

    @Override
    public String toString() {
        return "Node:\n" + getGame().toString() + " by action "
                + (action != null ? action.toString() : "null");
    }

    @Override
    public boolean isRoot() {
        return depth == 0;
    }

    @Override
    public List<Action> getAvailableActions() {
        return game.getActions();
    }

    @Override
    public INode<T> getSuccessor(Action pAction,
            final MemorySavingMode memorySavingMode) {
        return new Node<>(pAction, game, depth + 1, memorySavingMode);
    }
}
