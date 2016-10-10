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

import java.io.Serializable;
import java.util.Comparator;

/**
 * A comparator for INodes. Sorts the Nodes by the natural order of the values
 * provided by the depth of the node.
 *
 * @author haker@uni-bremen.de
 */
public class GameDepthComparator implements Comparator<INode<?>>, Serializable {

    private static final long serialVersionUID = 6041429516379408492L;

    @Override
    public int compare(INode<?> o1, INode<?> o2) {
        return Integer.compare(o1.getDepth(), o2.getDepth());
    }
}
