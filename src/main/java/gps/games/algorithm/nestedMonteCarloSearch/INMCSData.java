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
package gps.games.algorithm.nestedMonteCarloSearch;

import gps.games.wrapper.Action;
import gps.games.wrapper.successor.INode;

import java.util.List;

/**
 * Interface for the data structure used by {@link NMCS}.
 *
 * @author jschloet@tzi.de
 *
 * @param <T> The type of game that is to be stored
 */
public interface INMCSData<T> {

    /**
     * Stores the given node and its predecessor. Called during
     * the given level.
     *
     * @param pred Predecessor of the given node
     * @param node Node to be inserted
     * @param level Current level in {@link NMCS}
     */
    void insert(INode<T> pred, INode<T> node, int level);

    /**
     * Stores the given path. Called during the given level
     *
     * @param path Path to be inserted
     * @param level Current level in {@link NMCS}
     */
    void insert(List<INode<T>> path, int level);

    /**
     * This method is called by {@link NMCS} when a sample is finished;
     */
    void finishedSample();

    /**
     * This method is called by {@link NMCS} if a {@link NMCS#nestedSearch(int, INode)}
     * of the given level is finished.
     *
     * @param level Level of the finished {@link NMCS#nestedSearch(int, INode)}
     */
    void finishedSearch(int level);

    /**
     * Returns a path to the given node. If the given node is a terminal,
     * the whole path must be returned. If not, the whole path or a subpath
     * must be returned
     *
     * @param goal The terminal of the wanted path
     * @param level The current level of {@link NMCS}
     * @return A path or subpath to the given node
     */
    List<INode<T>> getPathTo(INode<T> goal, int level);

    /**
     * Returns an {@link Action} sequence that leads to the given
     * node.
     *
     * @param goal Terminal of the wanted {@link Action} sequence.
     * @param level The current level of {@link NMCS}
     * @return A {@link Action} sequence that leads to the given node.
     */
    List<Action> getPathToAsActions(INode<T> goal, int level);

    /**
     * Can be used to store nodes the were reached in the
     * {@link NMCS#nestedSearch(int, INode)} method excluding
     * submethods.
     *
     * @param node The node to be inserted
     * @param level The current level of {@link NMCS}
     */
    void addExecuted(INode<T> node, int level);

    /**
     * Returns the number of executed actions in the
     * given level and higher levels.
     *
     * @param level The current level of {@link NMCS}
     * @param startingLevel The starting Level of {@link NMCS}
     * @return The determined number of executed actions
     */
    int numberExec(int level, int startingLevel);

    /**
     * This method is called by {@link NMCS} if a sample is canceled
     * before a terminal state was reached.
     */
    void clearSample();

    /**
     * Sets the starting level of the corresponding {@link NMCS}
     *
     * @param level The starting level of the corresponding {@link NMCS}
     *
     */
    void setStartingLevel(int level);

    /**
     * Clear the list of executed actions. If the data structure is used more than one time
     * this can be called.
     */
    void clearExec();
}
