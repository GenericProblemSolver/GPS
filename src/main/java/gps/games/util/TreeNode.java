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
package gps.games.util;

import gps.games.wrapper.successor.INode;
import gps.util.Tuple;

/**
 * Tree Node that is inserted as key into the game tree hash map. Therefore the
 * hashCode and equals ignore the Action field.
 * 
 * @author haker@uni-bremen.de
 *
 * @param <T>
 *            The type of the problem class.
 */
class TreeNode<T> {

    // because ... fuck java maps
    @SuppressWarnings("rawtypes")
    static final ThreadLocal<Tuple<TreeNode, TreeNode>> lastMatchingEquals = new ThreadLocal<Tuple<TreeNode, TreeNode>>();

    /**
     * The game state of this node. Must not be {@code null}.
     */
    final INode<T> state;

    /**
     * Construct a tree node.
     * 
     * @param pState
     *            The state. Must not be {@code null}.
     */
    TreeNode(INode<T> pState) {
        state = pState;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TreeNode)) {
            return false;
        }
        final boolean ret = state.getGame()
                .equals(((TreeNode<T>) other).state.getGame());
        if (ret) {
            lastMatchingEquals.set(new Tuple<>(this, (TreeNode<T>) other));
        }
        return ret;
    }

    @Override
    public int hashCode() {
        return state.getGame().hashCode();
    }

    @Override
    public String toString() {
        return "TreeNode : " + state.toString();
    }
}
