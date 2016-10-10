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

/**
 * Some useful functions for the INode interface.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class NodeUtil {

    /**
     * Hidden constructor. This class is not instantiable.
     */
    private NodeUtil() {
    }

    /**
     * Compares the given nodes by using their utility method. The node that is
     * considered better is returned. The depth of the node is ignored by this
     * method. If one of the nodes is not a terminal stare 0 is returned.
     * 
     * @param a
     *            the first node
     * @param b
     *            the second node
     * @return Positive value if a is better than b. 0 if they are both equally
     *         good or a value less than 0 if b is better.
     */
    public static <T> int compareNodeUtilityWithoutDepth(INode<T> a,
            INode<T> b) {
        if (a == null) {
            return b == null ? 0 : -1;
        }
        if (b == null) {
            return 1;
        }
        if (a.getGame().hasUtilityMethod() && b.getGame().hasUtilityMethod()
                && a.getGame().isTerminal() && b.getGame().isTerminal()) {
            final double da = a.getGame().getUtility().doubleValue();
            final double db = b.getGame().getUtility().doubleValue();
            return Double.compare(da, db);
        }
        return 0;
    }

    /**
     * Compares the given nodes by using their utility method if applicable or
     * by using the depth of the nodes if the utility method is not available.
     * 
     * @param a
     *            the first node
     * @param b
     *            the second node
     * @return Positive value if a is better than b. 0 if they are both equally
     *         good or a value less than 0 if b is better.
     */
    public static <T> int compareNodeUtility(INode<T> a, INode<T> b) {
        final int cmp = compareNodeUtilityWithoutDepth(a, b);
        if (cmp != 0) {
            return cmp;
        }
        return Integer.compare(b.getDepth(), a.getDepth());
    }

    /**
     * Compares the given nodes by using their utility method or by using the
     * depth of the nodes if the utility method is not available. The node that
     * is considered better is returned.
     * 
     * @param a
     *            the first node
     * @param b
     *            the second node
     * @return The better Node.
     */
    public static <T> INode<T> selectBetterNodeOf(INode<T> a, INode<T> b) {
        return compareNodeUtility(a, b) >= 0 ? a : b;
    }
}
