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
package gps.games.algorithm.singleplayer.common.datastruct;

import java.util.LinkedList;

import gps.games.algorithm.singleplayer.interfaces.IToEvaluate;
import gps.games.wrapper.successor.INode;

/**
 * Store the nodes that are to be examined in a linked list and use the order of the
 * insertion of the nodes for traversal.
 * 
 * @author haker@uni-bremen.de
 *
 * @param <T>
 *            The type of the problem class.
 */
public abstract class ToEvalLinkedList<T> implements IToEvaluate<T> {

    /**
     * A queue for the nodes that are to be examined.
     */
    protected final LinkedList<INode<T>> toEvaluate = new LinkedList<INode<T>>();

    @Override
    public void add(final INode<T> pNode) {
        toEvaluate.add(pNode);
    }

    @Override
    public boolean hasNext() {
        return !toEvaluate.isEmpty();
    }

}
