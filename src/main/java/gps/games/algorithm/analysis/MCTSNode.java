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
package gps.games.algorithm.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gps.games.wrapper.Action;
import gps.games.wrapper.Player;

/**
 * Representation of a game tree node used by Monte-Carlo- Tree-Search
 * algorithm. A game tree node represents a game state of the game to solve and contains
 * statistics and information that are determined in the course of an Monte-Carlo- Tree-Search
 * algorithm. Those statistics and information can be used by the algorithm to solve the game.
 * 
 * @author jschloet@tzi.de
 */
// TODO:: The use of generics should be adjusted to the use of generics in the
// other
// game classes (when they are implemented).
public class MCTSNode {

    /**
     * The parent node of this node or null if this node is a root node.
     */
    private MCTSNode parent;

    /**
     * Map that stores the value of this node from each players point of view. This value is calculated according
     * to the used implementation of {@link gps.games.algorithm.monteCarloTreeSearch.AbstractMCTS}. As the whole
     * point of using implementations of {@link gps.games.algorithm.monteCarloTreeSearch.AbstractMCTS} is to determine
     * which move has to be made in the game state that corresponds to the root node, this map does necessarily needs
     * to be filled for a root node.
     */
    private Map<Player, Double> value;

    /**
     * List with the children of this node. The children of a node are those {@link MCTSNode}s that
     * represent game states which are reachable from the game state of this node via a single action.
     * A newly instantiated {@link MCTSNode} has an empty list of children which gets filled during the
     * course of an {@link gps.games.algorithm.monteCarloTreeSearch.AbstractMCTS} run.
     */
    private List<MCTSNode> children;

    /**
     * Actions that were used to expand the game tree. This list contains the actions that were used
     * to reach the game states that are represented by the elements of {@link MCTSNode#children}. As the
     * children are added individually this list does not necessarily equals the possible actions of the
     * game state that is represented by the current node.
     */
    private List<Action> expandedActions;

    /**
     * Number of simulations that passed this node.
     */
    private Integer visitCount;

    /**
     * The player, who has to make the next move
     */
    private Player currentPlayer;

    /**
     * The maximum number of game states (children) the current player can reach
     * from the current game state, i.e. the number of moves the current player
     * can make. The number of this variable does not need to match the size of {@link #children} as
     * the children are added separately during the course of an {@link gps.games.algorithm.monteCarloTreeSearch.AbstractMCTS}
     * algorithm.
     */
    private int maxChildren;

    /**
     * The level in the game tree that contains this node. The root node has a depth of zero and the elements of {@link #children}
     * should have the value of this attributed summed up with one.
     */
    private int depth;

    /**
     * The move that was made to reach this node. Can be {@code null} for a root node
     * as no action was used to reach it.
     */
    private Action action;

    /**
     * Create a MCTSNode. VisitCount is set with 0. Instantiates
     * {@link #children}, {@link #expandedActions} and {@link #value}.
     */
    public MCTSNode() {
        visitCount = 0;
        children = new ArrayList<MCTSNode>();
        expandedActions = new ArrayList<Action>();
        value = new HashMap<>();
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public int getMaxChildren() {
        return maxChildren;
    }

    public void setMaxChildren(int maxChildren) {
        this.maxChildren = maxChildren;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int pVisitCount) {
        visitCount = pVisitCount;
    }

    public void setParent(MCTSNode pParent) {
        parent = pParent;
    }

    public MCTSNode getParent() {
        return parent;
    }

    public List<MCTSNode> getChildren() {
        return children;
    }

    /**
     * Adds the given node to the {@link #children} of this node.
     * 
     * @param child
     *            This node is added to the {@link #children} list.
     */
    public void addChild(MCTSNode child) {
        children.add(child);
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public List<Action> getExpandedActions() {
        return expandedActions;
    }

    /**
     * Returns the map which contains the values for each player.
     * 
     * @return the map
     */
    public Map<Player, Double> getMap() {
        return value;
    }

    /**
     * Adds the given action to {@link #expandedActions}
     * 
     * @param action
     *            Action to be added to {@link #expandedActions}
     */
    public void addExpandingAction(Action action) {
        expandedActions.add(action);
    }

    /**
     * Adds the given value to the value of this node from the given players
     * point of view.
     * 
     * @param player
     *            The value from this players point of view gets adjusted.
     * @param pValue
     *            The value from the given players point of view is increased by
     *            this value.
     */
    public void addValue(Player player, Number pValue) {
        if (value.containsKey(player)) {
            Number newValue = value.get(player) + pValue.doubleValue();
            value.put(player, newValue.doubleValue());
        } else {
            value.put(player, pValue.doubleValue());
        }
    }

    /**
     * Returns the value of this node from the point of view of the given
     * player.
     * 
     * @param player
     *            The value of the node is returned from this players point of
     *            view.
     * @return The Value of this node from the given players point of view.
     */
    public double getValue(Player player) {
        return value.get(player);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("player: " + currentPlayer);
        builder.append("\n");
        builder.append("visits: " + visitCount);
        builder.append("\n");
        return builder.toString();
    }
}