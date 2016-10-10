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
package gps.common;

/**
 * The fields an algorithm can fill with values during its runtime.
 * 
 * @author haker@uni-bremen.de
 * @author jschloet@tzi.de
 *
 */
public enum BenchmarkField {
    //////////////////////////////////
    // Games
    //////////////////////////////////
    /**
     * Nodes that have been discovered by an algorithm.
     */
    SEEN_NODES,

    /**
     * Nodes that have been processed by an algorithm.
     */
    PROCESSED_NODES,

    /**
     * The depth of the best node that has been found yet. If the algorithm
     * would end this node would be the terminal node.
     */
    BEST_MOVE_DEPTH,

    /**
     * the largest depth of a discovered node.
     */
    DEEPEST_DISCOVERED_NODE,

    /**
     * The number of simulations that were executed during the course of an algorithm
     * This value can be filled by simulation based approaches (i.e. {@link gps.games.algorithm.nestedMonteCarloSearch.IterativeNMCS} and {@link gps.games.algorithm.monteCarloTreeSearch.AbstractMCTS} variants).
     */
    NUMBER_OF_SIMULATIONS,

    /**
     * This value is a measure for the clearness of a best move in multi player games. If this value is high,
     * the determined best move is unambiguous.
     *
     * {@link gps.games.algorithm.monteCarloTreeSearch.AbstractMCTS} based approaches can calculate this value by
     * subtracting the score of the second best move from the score of the best move.
     */
    CLEARNESS_OF_BEST_MOVE,

    /**
     * Most (?) multi player algorithms create some kind of game tree. This value contains the depth of the deepest node
     * in the game tree.
     *
     * In some value may be equivalent to  {@link #DEEPEST_DISCOVERED_NODE}, but in some algorithms not every discovered
     * node is added to the game tree and consequently the values differ.
     */
    GAME_TREE_DEPTH,

    /**
     * The heuristic value of the game state that results from executing the determined best move on the starting game state.
     * Has to be used carefully as the significance of this value depends on the quality of the heuristic function.
     */
    // Jens: I am not sure if this will work well. To maximize this value we would not need one of our algorithms, because all we
    // had to do is order the legal actions according to their heuristic value. The heuristic values , especially the ones on a
    // low depth, can be misleading. Because of that we let our algorithms , even those that use the heuristic, run to an as high
    // depth as possible.
    BEST_MOVE_HEURISTIC,

    //////////////////////////////////
    // Optimization
    //////////////////////////////////
    /**
     * The temperature for simulated annealing algorithm.
     */
    TEMPERATURE,

    /**
     * The best solution that has been found yet.
     */
    BEST_SOLUTION,
}
