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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gps.games.MemorySavingMode;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;

/**
 * Implementation if {@link INode} that differs from {@link Node} by storing the
 * parent of this {@link LinkedNode}.
 *
 * @param <T>
 *
 * @author jschloet@tzi.de, alueck@uni-bremen.de
 */
public class LinkedNode<T> extends Node<T> implements ILinkedNode<T> {

    /**
     * The predecessor of this node.
     */
    private LinkedNode<T> predecessor;

    /**
     * Constructs a root LinkedNode. The parent of this node is null.
     *
     * @param pGame
     *            game state of the root node
     */
    public LinkedNode(final Game<T> pGame) {
        super(pGame);
    }

    /**
     * Constructor. Sets this classes attributes with the given values.
     *
     * @param pGame
     *            The previous game state.
     * @param pDepth
     *            the depth of this node
     * @param pAction
     *            the action unsed to reach this node
     * @param pPredecessor
     *            the predecessor of this node or {@code null} if no predecessor
     *            exists
     * @param memorySavingMode
     *            The memory saving mode that is used when a copy of the game
     *            state is created.
     */
    public LinkedNode(final Action pAction, final Game<T> pGame,
            final int pDepth, final LinkedNode<T> pPredecessor,
            final MemorySavingMode memorySavingMode) {
        super(pAction, pGame, pDepth, memorySavingMode);
        predecessor = pPredecessor;
    }

    @Override
    public List<LinkedNode<T>> getLinkedSuccessors(
            final MemorySavingMode memorySavingMode) {
        return game
                .getActions().stream().map(a -> new LinkedNode<>(a, game,
                        depth + 1, this, memorySavingMode))
                .collect(Collectors.toList());
    }

    @Override
    public LinkedNode<T> getPredecessor() {
        return predecessor;
    }

    @Override
    public List<Action> constructActionSequence() {
        List<Action> actions = new ArrayList<>();
        LinkedNode<T> current = this;
        while (current.getPredecessor() != null
                && current.getAction() != null) {
            actions.add(0, current.getAction());
            current = current.getPredecessor();
        }
        return actions;
    }
}