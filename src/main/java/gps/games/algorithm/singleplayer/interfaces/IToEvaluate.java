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
package gps.games.algorithm.singleplayer.interfaces;

import gps.games.wrapper.successor.INode;

/**
 * Determines how an algorithm is supposed to store the nodes that have not been
 * evaluated yet by the search algorithm.
 * 
 * @author haker@uni-bremen.de
 *
 * @param <T>
 *            The type of the problem class.
 */
public interface IToEvaluate<T> {

    /**
     * Add a node that should be evaluated to the structure.
     * 
     * @param pNode
     *            The node. Must not be {@code null}.
     */
    public void add(INode<T> pNode);

    /**
     * Retrieve the node that should be evaluated next.
     * 
     * @return The node.
     */
    public INode<T> retrieveNext();

    /**
     * Checks whether there is still a node that has to be evaluated.
     * 
     * @return {@code true} if there is another node to evaluate. {@code false}
     *         otherwise.
     */
    public boolean hasNext();
}
