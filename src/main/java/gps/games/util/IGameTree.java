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

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import gps.games.wrapper.Action;
import gps.games.wrapper.successor.INode;

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
 *
 */
public interface IGameTree<T> {

    /**
     * Calculate the path from a root the the given game.
     * 
     * @param node
     *            The game the path has to be extracted.
     * @return The List in order from the root to the game.
     * 
     * @throws RuntimeException
     *             If a node has been inserted that has no predecessor in the
     *             game tree.
     */
    public List<INode<T>> getPathTo(INode<T> node);

    /**
     * Calculate the path from a root the the given game.
     * 
     * @param node
     *            The game the path has to be extracted.
     * @return The List in order from the root to the game. Only the actions
     *         that are required to get to the give game state are returned.
     * 
     * @throws RuntimeException
     *             If a node has been inserted that has no predecessor in the
     *             game tree.
     */
    public List<Action> getPathToAsActions(INode<T> node);

    /**
     * Insert a root node. Root nodes do not have a predecessor.
     * 
     * If a root node is inserted after non-root nodes have been inserted it is
     * not guaranteed that the tree will return the shortest paths.
     * 
     * @param root
     *            a root node.
     */
    public void insertRoot(INode<T> root);

    /**
     * Insert a successor.
     * 
     * @param pred
     *            The predecessor of the tree. Must be already known to the
     *            GameTree.
     * @param succ
     *            The successor to insert.
     * 
     */
    public void insert(INode<T> pred, INode<T> succ);

    /**
     * Insert all nodes of a game tree.
     * 
     * Actually this tree can be a forest. However if non-root nodes have been
     * inserted it is not guaranteed that the tree will return the shortest
     * paths.
     * 
     * If a tree is inserted into a tree and the root nodes are equal they are
     * merged. Thus still containing only one root.
     * 
     * @param tree
     *            The tree to insert.
     * 
     */
    public void insert(IGameTree<T> tree);

    /**
     * Allows to change the comparator for the utility of the tree. Default is
     * the utility method provided by the Game class and if the utility value is
     * equal the depth of the inserted nodes are compared.
     * 
     * @param pComp
     *            the comparator or {@code null} if the default comparator
     *            should be used.
     */
    public void setNodeUtilityComparator(Comparator<INode<T>> pComp);

    /**
     * Return the predecessor of a given game.
     * 
     * @param game
     *            the game.
     * @return The predecessor node. Might be an ISuccessor.
     */
    public INode<T> getPredecessorOf(INode<T> game);

    /**
     * Return all Games and it's predecessors in a Set view.
     * 
     * @return the set.
     */
    public Set<Entry<TreeNode<T>, INode<T>>> entrySet();

    /**
     * Checks whether a node exists in the GameTree.
     * 
     * @param pGame
     *            The game state
     * @return {@code true} if exists in the game tree, {@code false} otherwise.
     */
    public boolean contains(INode<T> pGame);
}
