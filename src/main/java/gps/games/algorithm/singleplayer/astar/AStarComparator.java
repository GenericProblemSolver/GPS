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
package gps.games.algorithm.singleplayer.astar;

import java.util.Comparator;

import gps.games.wrapper.ISingleplayerHeuristic;
import gps.games.wrapper.successor.INode;
import gps.games.wrapper.successor.NodeUtil;

/**
 * The heuristic comparator for a-star algorithm.
 * 
 * @author haker@uni-bremen.de
 *
 * @param <T>
 *            The type of the problem class
 */
public class AStarComparator<T> implements Comparator<AStarNode<T>> {

    private final ISingleplayerHeuristic heuristic;

    public AStarComparator(final ISingleplayerHeuristic pHeuristic) {
        heuristic = pHeuristic;
    }

    @Override
    public int compare(final AStarNode<T> oo1, final AStarNode<T> oo2) {

        // compare for equality (reference is sufficient)
        if (oo1 == oo2) {
            return 0;
        }

        final INode<T> o1 = oo1.node;
        final INode<T> o2 = oo2.node;

        int cmp = 0;

        // compare heuristic
        cmp = Double.compare(heuristic.eval(o1.getGame()),
                heuristic.eval(o2.getGame()));
        if (cmp != 0) {
            return cmp;
        }

        // compare utility and depth
        cmp = NodeUtil.compareNodeUtility(o1, o2);
        if (cmp != 0) {
            return cmp;
        }

        // use construction id. Each node has an individual construction id
        // which gets higher the more nodes are constructed.
        final long h1 = oo1.constructionId;
        final long h2 = oo2.constructionId;
        if (h1 == h2 || h1 == Long.MIN_VALUE || h2 == Long.MIN_VALUE
                || Long.compare(h1, h2) == 0) {
            throw new RuntimeException("cannot guarantee order");
        }
        return Long.compare(h2, h1);
    }
}