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

import gps.games.GamesModule;
import gps.games.algorithm.analysis.MCTSNode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * Extends the {@link UCTMCTS} algorithm with pruning conditions similar to
 * http://dl.acm.org/citation.cfm?id=1935550.
 *
 * After a specified time ({@link #startPruningAt}) pruning is executed during
 * the selection phase.
 * 
 * @author jschloet@tzi.de
 *
 * @param <T>
 */
public class UCTMCTSPruning<T> extends UCTMCTS<T> {

    /**
     * Attribute that specifies after how many milliseconds the pruning should get
     * started for problems of a size similar to {@link #CONNECT_FOUR_SIZE}. For problems
     * of other sizes the starting time is calculated using a linear dependency, this value
     * and {@link #CONNECT_FOUR_SIZE} in {@link #startPruning()}.
     *
     * Can be set using {@link #setStartPruningAt(int)}
     */
    private int startPruningAt = 200;

    /**
     * The value of this constant is the product of the avg. branching factor and the
     * avg. depth  of the game ConnectFour according to the GameAnalyser.
     *
     * The default value of {@link #startPruningAt} (200) is good for problems of a similar
     * size to ConnectFour. To determine the value of {@link #startPruningAt} for problems
     * of a different size, this constant can be used.
     */
    private static final double CONNECT_FOUR_SIZE = 147;

    /**
     * Flag that signals whether at least one node was pruned.
     */
    private boolean pruningSuccessful;

    /**
     * Constructor without options. Can be instantiated by the invoker to
     * be able to call {@link #getOptions()}.
     *
     * Can also be used to run the algorithm with a default {@link #startPruningAt} value.
     *
     * @param gameMod The game to be solved
     */
    public UCTMCTSPruning(GamesModule<T> gameMod) {
        super(gameMod);
    }

    /**
     * Constructor with options. Sets the {@link #startPruningAt} value
     * with the given value.
     *
     * @param gameMod The game to be solved
     * @param value The value for {@link #startPruningAt}
     */
    public UCTMCTSPruning(GamesModule<T> gameMod, Integer value) {
        super(gameMod);
        setStartPruningAt(value);
    }

    /**
     * Sets the basic pruning starting  time. The actual starting time of the pruning is determined
     * dependent of the problem size (Product of avg. branching factor and avg. depth )
     * and {@link #startPruningAt} in {@link #startPruning()}.
     *
     * @param value The new basic pruning starting time.
     */
    private void setStartPruningAt(final int value) {
        startPruningAt = value;
    }

    /**
     * Provides different basic starting time values as options.
     *
     * @return The different options for this algorithm.
     */
    @Override
    public List<?>[] getOptions() {
        return new List<?>[] { Arrays.asList( // starting time
                200, 100, 300) };
    }

    /**
     * {@inheritDoc}
     *
     * Calls {@link #executePruning(MCTSNode)} before the selection is continued.
     *
     * @param currentNode
     *            The next node is selected among the children of this node.
     * @return The node at which the fringe of the game tree is reached ,the
     *         first not fully expanded node or the first met terminal node
     */
    @Override
    MCTSNode select(MCTSNode currentNode) {
        // Execute Pruning in the selection phase to cut off paths from the game tree
        // Should get us a speedup as the calculations of select should take less time
        // Also promising paths (i.e. the not pruned pahts) are more focused
        executePruning(currentNode);
        return super.select(currentNode);
    }

    /**
     * Checks whether children of the given node meet the pruning condition.
     *
     * If a node exists that was visited more than all other children together,
     * all other children are pruned.
     *
     * @param node Node which children may be pruned.
     */
    private void executePruning(final MCTSNode node) {
        // Only start pruning if enough time has past and
        // children exist which could be pruned.
        if (startPruning() && node.getChildren().size() > 1) {
            pruningSuccessful = true;
            // Determine a child that was visited as often as all other
            // children together.
            List<MCTSNode> dominantNodes = node.getChildren().stream()
                    .filter(n -> n.getVisitCount() > node.getVisitCount() / 2)
                    .collect(Collectors.toList());
            //If such a child exists, cut off all the other children
            if (!dominantNodes.isEmpty()) {
                node.getChildren().clear();
                node.getChildren().addAll(dominantNodes);
                pruningSuccessful = true;
            }
            //To prevent the expansion phase from expanding the node agein
            //after the pruning, refresh the maximum children value.
            node.setMaxChildren(node.getChildren().size());
        }
    }

    /**
     * Controls whether pruning should be executed. Pruning should only be executed after a
     * significant number of simulation is executed. This method checks whether the specified time
     * is reached.
     *
     * Because the time to start the pruning depends on the problem size, the pruning can only
     * start if {@link #module#gameAnalysis()} is present. This is the case because the values of {@link #module#gameAnalysis()}
     * are our only hint to estimate the actual problem size. If the {@link #module#gameAnalysis()} is not present
     * the steps of the algorithm are the same as in {@link UCTMCTS}.
     *
     * @return {@code true} if enough time past. {@code false} otherwise.
     */
    private boolean startPruning() {
        return module.gameAnalysis().isPresent() && (System.currentTimeMillis()
                - startTime) > (startPruningAt * getProblemSizeFactor());
    }

    /**
     * Calculates the division of the problem size and the {@link #CONNECT_FOUR_SIZE}, where the
     * problem size refers to the product of the avg. branching factor and the avg. depth
     * according to {@link #module#gameAnalysis()}
     *
     * @return The problem size factor
     */
    private double getProblemSizeFactor() {
        //The isPresent() check should actually not be necessary, because it already happened in startPruning.
        if (module.gameAnalysis().isPresent()) {
            return (module.gameAnalysis().get().getAvgBranchingFactor()
                    * module.gameAnalysis().get().getAvgDepth())
                    / CONNECT_FOUR_SIZE;
        } else {
            return 1;
        }
    }

    /**
     * Returns whether at least one node has been pruned.
     *
     * @return Boolean that signals whether at least one node has been pruned.
     */
    public boolean isPruningSuccessful() {
        return pruningSuccessful;
    }

    @Override
    public String getName() {
        return "UCTMCTSPruning_" + startPruningAt;
    }
}