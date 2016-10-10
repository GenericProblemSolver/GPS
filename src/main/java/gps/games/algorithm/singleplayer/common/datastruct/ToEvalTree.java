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

import java.util.TreeSet;

import gps.games.algorithm.singleplayer.astar.AStarNode;
import gps.games.algorithm.singleplayer.astar.AStarComparator;
import gps.games.algorithm.singleplayer.interfaces.IToEvaluate;
import gps.games.wrapper.ISingleplayerHeuristic;
import gps.games.wrapper.successor.INode;

/**
 * Store the nodes that are to be examined in a tree set and use the heuristic,
 * utility, depth and identity hashcode for comparison (in that order).
 * 
 * @author haker@uni-bremen.de
 *
 * @param <T>
 *            The type of the problem class.
 */
public class ToEvalTree<T> implements IToEvaluate<T> {

    /**
     * Construct a evaluation tree by using a heuristic for ordering.
     * 
     * @param pHeuristic
     *            The heuristic.
     */
    public ToEvalTree(final ISingleplayerHeuristic pHeuristic) {
        comparator = new AStarComparator<>(pHeuristic);
        toEvaluateTree = new TreeSet<AStarNode<T>>(comparator);
    }

    /**
     * A-Star specific comparator that is passed to {@link #toEvaluateTree}.
     */
    private final AStarComparator<T> comparator;

    /**
     * A tree that is sorted by heuristic and utility
     */
    private final TreeSet<AStarNode<T>> toEvaluateTree;

    private long nodeId = Long.MIN_VALUE;

    @Override
    public void add(final INode<T> pNode) {
        toEvaluateTree.add(new AStarNode<T>(pNode, ++nodeId));
    }

    @Override
    public INode<T> retrieveNext() {
        return toEvaluateTree.pollLast().node;
    }

    @Override
    public boolean hasNext() {
        return !toEvaluateTree.isEmpty();
    }

}
