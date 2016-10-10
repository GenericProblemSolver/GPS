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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import gps.games.wrapper.Action;
import gps.games.wrapper.successor.INode;
import gps.games.wrapper.successor.NodeUtil;
import gps.util.Tuple;

/**
 * Game Tree that reduces the tree if necessary by using a hash map. Requires
 * therefore a properly written hashCode and equals method in the Game class.
 * 
 * Actually this is a forest. Multiple roots are possible. However if roots are
 * inserted after successors have been inserted it is not guaranteed that the
 * found paths are best paths.
 * 
 * @author haker@uni-bremen.de
 *
 * @param <T>
 *            The type of the problem class.
 */
public class GameTree<T> implements IGameTree<T> {

    /**
     * the map. maps to each game the best predecessor of the game.
     */
    private final Map<TreeNode<T>, INode<T>> map;

    /**
     * the default comparator.
     */
    private final Comparator<INode<T>> defaultComparator = (pred,
            node) -> NodeUtil.compareNodeUtility(pred, node);

    /**
     * the used comparator.
     */
    private Comparator<INode<T>> comp = defaultComparator;

    /**
     * Construct a new game tree.
     * 
     * @param initialCapacity
     *            The initial capacity of the game tree.
     */
    public GameTree(final int initialCapacity) {
        map = new HashMap<>(initialCapacity);
    }

    /**
     * Construct a new game tree.
     */
    public GameTree() {
        map = new HashMap<>();
    }

    /**
     * Iterate through the path
     * 
     * @param t
     *            Previuos node
     * @param skip
     *            Only simulate retrieval but do not really retrieve (necessary
     *            for first node)
     * 
     * @return Next node (or first node if skip has been {@code true}.
     */
    @SuppressWarnings("unchecked")
    private INode<T> it(INode<T> t, boolean skip) {
        // because ... fuck java maps
        TreeNode.lastMatchingEquals.set(null);
        INode<T> oldPred = map.get(new TreeNode<T>(t));
        if (!skip) {
            map.get(new TreeNode<T>(oldPred));
        }
        @SuppressWarnings("rawtypes")
        Tuple<TreeNode, TreeNode> tuple = TreeNode.lastMatchingEquals.get();
        if (tuple == null) {
            TreeNode.lastMatchingEquals.set(null);
            return t;
        }
        INode<T> ret = tuple.getY().state;
        TreeNode.lastMatchingEquals.set(null);
        return ret;
    }

    /**
     * Calculated the path from the given node to a root node.
     * 
     * @param node
     *            the node. Should be a node that has been inserted into the
     *            tree.
     * @return The list in order from root to node.
     */
    @Override
    public List<INode<T>> getPathTo(INode<T> node) {
        List<INode<T>> reversed = new ArrayList<>();
        for (INode<T> t = it(node, true);; t = it(t, false)) {
            if (t == null) {
                break;
            }
            reversed.add(t);
            if (t.isRoot()) {
                break;
            }
        }
        final List<INode<T>> ret = Lists.reverse(reversed);
        if (!ret.isEmpty() && ret.get(0).getDepth() != 0) {
            // if this gets thrown you may debug by uncommenting the exception
            // in insert(INode<T> pred, Game<T> succ) method
            throw new RuntimeException(
                    "a predecessor of an inserted node has not been inserted in the tree");
        }
        return ret;
    }

    @Override
    public List<Action> getPathToAsActions(INode<T> node) {
        return getPathTo(node).stream().filter(p -> !p.isRoot())
                .map(INode::getAction).collect(Collectors.toList());
    }

    @Override
    public void insertRoot(INode<T> root) {
        if (!root.isRoot()) {
            throw new RuntimeException("given node must be a root node");
        }
        final TreeNode<T> node = new TreeNode<T>(root);
        map.put(node, null);
    }

    /**
     * Insert a node. Only inserts the node if the new node is considered
     * better. See {@link #comp} for the definition of "better".
     * 
     * @param pred
     *            the predecessor that the successor node was generated with. If
     *            {@code null} the node is added as a root node.
     * @param succ
     *            the successor node.
     * @param currentPred
     *            the predecessor of the successor that is currently in the game
     *            tree.
     * 
     */
    private void insert(INode<T> pred, INode<T> succ, INode<T> currentPred) {
        if (currentPred == null || comp.compare(currentPred, pred) < 0) {
            TreeNode<T> tn = new TreeNode<>(succ);
            map.remove(tn);
            map.put(tn, pred);
        }
    }

    @Override
    public void insert(INode<T> pred, INode<T> succ) {
        final INode<T> currentPred = map.get(new TreeNode<>(succ));
        // Do not do this check at this position since it wastes time
        // You may uncomment this for debugging purposes if you experience
        // exception in getPathTo method
        /*
         * if (!map.containsKey(pred.getGame())) { throw new RuntimeException(
         * "the predecessor of the given node cannot be found in the tree"); }
         */
        insert(pred, succ, currentPred);
    }

    @Override
    public void insert(IGameTree<T> tree) {
        tree.entrySet().stream().forEach(e -> insert(e.getValue(),
                e.getKey().state, map.get(e.getKey())));
    }

    @Override
    public void setNodeUtilityComparator(Comparator<INode<T>> pComp) {
        comp = pComp == null ? defaultComparator : pComp;
    }

    @Override
    public INode<T> getPredecessorOf(INode<T> game) {
        return map.get(new TreeNode<>(game));
    }

    @Override
    public Set<Entry<TreeNode<T>, INode<T>>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean contains(INode<T> pGame) {
        return map.containsKey(new TreeNode<>(pGame));
    }

}
