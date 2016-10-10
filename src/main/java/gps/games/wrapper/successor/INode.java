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

/**
 * Interface that represents a node.
 *
 * @param <T>
 *            The type of the problem class.
 *
 * @author haker@uni-bremen.de, alueck@uni-bremen.de
 */
public interface INode<T> {
    /**
     * The depth of this node.
     */
    public int getDepth();

    /**
     * Returns the game object of this node.
     *
     * @return the game object of this node
     */
    public Game<T> getGame();

    /**
     * Get all successors that are directly reachable by a single move from this
     * node.
     * 
     * @param memorySavingMode
     *            The memory saving mode that is used when a clone of the game
     *            state is created.
     */
    public List<INode<T>> getSuccessors(MemorySavingMode memorySavingMode);

    /**
     * Get all actions that are available for applying onto this node.
     */
    public List<Action> getAvailableActions();

    /**
     * Get a successor of the node by applying an action.
     * 
     * @param pAction
     *            The action to apply.
     * @param memorySavingMode
     *            The memory saving mode that is used when a copy of the game
     *            state is created.
     * @return The successor node.
     */
    public INode<T> getSuccessor(Action pAction,
            MemorySavingMode memorySavingMode);

    /**
     * Get the action that is performed on the predecessor to get to this node.
     *
     * @return The action.
     */
    public Action getAction();

    /**
     * Returns {@code true} if the game of this node is a terminal state.
     *
     * @return {@code true} if the game is a terminal state. {@code false} otherwise.
     */
    // Jens: I added this because calling node.getGame().isTerminal() caused concurrency problems
    public boolean isTerminal();

    /**
     * @return {@code true} if this node is a root node and {@code false}
     *         otherwise
     */
    public boolean isRoot();
}
