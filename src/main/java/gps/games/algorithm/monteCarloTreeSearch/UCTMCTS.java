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
package gps.games.algorithm.monteCarloTreeSearch;

import java.util.Map;
import java.util.Map.Entry;

import gps.games.GamesModule;
import gps.games.algorithm.analysis.MCTSNode;
import gps.games.wrapper.Player;

/**
 * 
 * Implementation of AbstractMCTS. Implements the 
 * selection phase using the Upper-Confidence-Bounds-for-trees
 * formula. 
 * 
 * @author jschloet@tzi.de
 *
 */
public class UCTMCTS<T> extends AbstractMCTS<T> {

    /**
     * constant to balance the exploration,exploitation tradeoff
     */
    private static final double explorationConstant = 1;

    public UCTMCTS(GamesModule<T> module) {
        super(module);
    }

    /**
     * {@inheritDoc}
     * 	
     * Increases the visit counter by one and adds the result value from all
     * players point of view to the value of the node.
     */
    @Override
    void updateNode(MCTSNode currentNode, Map<Player, Number> result) {
        for (Entry<Player, Number> player : result.entrySet()) {
            currentNode.addValue(player.getKey(), player.getValue());
        }
        currentNode.setVisitCount(currentNode.getVisitCount() + 1);
    }

    /**
     * {@inheritDoc}
     * 
     * Calculates the value of the given node using the upper confidence bounds for trees
     * formula.
     */
    @Override
    double calculateNodeValue(MCTSNode currentNode, Player player) {
        final double exploitation = currentNode.getValue(player)
                / (currentNode.getVisitCount() * maxUtilityReturn);
        final double exploration = Math
                .sqrt(Math.log(currentNode.getParent().getVisitCount())
                        / currentNode.getVisitCount());
        final double value = exploitation
                + 2 * explorationConstant * exploration;
        return value;
    }

    @Override
    public String getName() {
        return "UCTMCTS";
    }
}