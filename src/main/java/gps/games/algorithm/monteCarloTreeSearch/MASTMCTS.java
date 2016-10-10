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

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import gps.games.GamesModule;
import gps.games.algorithm.analysis.MCTSNode;
import gps.games.wrapper.Action;
import gps.games.wrapper.Player;

/**
 * This extension of {@link UCTMCTS} uses the state independent average score of
 * an {@link Action} to bias the probability of an {@link Action} to be executed
 * during the {@link #playout(int)} phase.
 * <p>
 * To do so, this class keeps track of the average score of an {@link Action}
 * from every players point of view and includes the average score in the
 * {@link #getNextPlayoutMove()} to select the next action to be executed during
 * {@link #playout(int)}.
 * <p>
 * This is similar to "Choosing among Unexplored Actions" section in
 * https://www.aaai.org/Papers/AAAI/2008/AAAI08-041.pdf.
 *
 * @author igor@uni-bremen.de
 */
public class MASTMCTS<T> extends UCTMCTS<T> {

    /**
     * Stores globally the action including the average score and how often it
     * has been used.
     */
    private static final Map<Action, Map.Entry<Number, Integer>> cachedActions = new ConcurrentHashMap<>();

    /**
     * The logger used by this class.
     */
    private final static Logger LOGGER = Logger
            .getLogger(MASTMCTS.class.getCanonicalName());

    /**
     * Initializes a new instance of the Monte Carlo Tree Search extension with
     * the Move-Average Sampling Technique.
     *
     * @param module
     *            Module on which the algorithm will be applied. It is also used
     *            to initialize the game tree.
     */
    public MASTMCTS(final GamesModule<T> module) {
        super(module);
    }

    @Override
    void backpropagate(final MCTSNode currentNode,
            final Map<Player, Number> result) {
        if (currentNode.getParent() != null) {
            Action usedActionInNode = currentNode.getAction();
            Player currentPlayer = currentNode.getCurrentPlayer();
            Double value = 100.0;
            if (currentNode.getMap().containsKey(currentPlayer)) {
                value = currentNode.getValue(currentPlayer);
            }

            // is it possible to have a node without an action or player?
            // Jens: Yes, that is possible. The root node of the created game tree
            // for example has no action as it was not reached by executing an action.
            // The current player should not be null.
            if (usedActionInNode != null && currentPlayer != null) {
                System.out.println(currentPlayer);
                Map.Entry<Number, Integer> entry;

                if (!getCachedActions().containsKey(usedActionInNode)) {
                    entry = new AbstractMap.SimpleEntry<>(value, 1);
                } else {
                    Integer actionCount = getCachedActions()
                            .get(usedActionInNode).getValue();
                    entry = new AbstractMap.SimpleEntry<>(
                            value + getCachedActions().get(usedActionInNode)
                                    .getKey().doubleValue(),
                            actionCount++);
                }
                getCachedActions().put(usedActionInNode, entry);
            }
            updateNode(currentNode, result);
            backpropagate(currentNode.getParent(), result);
        } else {
            // Only the visit count is relevant for the root node.
            currentNode.setVisitCount(currentNode.getVisitCount() + 1);
        }
    }

    @Override
    Action getNextPlayoutMove() {
        List<Action> possbleActions = new CopyOnWriteArrayList<Action>();
        possbleActions.addAll(currentState.getActions());
        Action nextAction = null;
        Map.Entry<Number, Integer> entry;
        boolean newActionFound = false;

        if (possbleActions.isEmpty()) {
            LOGGER.warning("List with possible actions is empty.");
            return null;
        }

        for (Action action : possbleActions) {
            // use unexplored actions first
            if (!getCachedActions().containsKey(action)) {
                nextAction = action;

                entry = new AbstractMap.SimpleEntry<>(getScore(action), 1);
                getCachedActions().put(action, entry);
                newActionFound = true;
                break;
            }
            nextAction = getNextPossibleAction(possbleActions);
        }

        if (!newActionFound) {
            entry = new AbstractMap.SimpleEntry<>(
                    currentState.getUtility(currentState.getPlayer()),
                    getCachedActions().get(nextAction).getValue() + 1);

            getCachedActions().put(nextAction, entry);
        }

        return nextAction;
    }

    /**
     * Return a {@link Map} with already used actions. If no map is available, a
     * new one will be created.
     *
     * @return the map with saved actions
     */
    public static Map<Action, Map.Entry<Number, Integer>> getCachedActions() {
        return cachedActions;
    }

    /**
     * Determine the score of an action. The default score for unknown action is
     * 100.
     *
     * @param action
     *            the action for determine the score
     *
     * @return a score for the action
     */
    public Double getScore(final Action action) {
        Double score = 100.0;
        if (getCachedActions().containsKey(action)) {
            score = getCachedActions().get(action).getKey().doubleValue()
                    / getCachedActions().get(action).getValue();
        }

        return score;
    }

    /**
     * Determines which action should be used. At the beginning the list will be
     * sorted from the highest to the lowest score. While iterate over the new
     * list a random boolean will decide if the given action will be returned.
     *
     * @param actions
     *            the possible actions
     *
     * @return a possible action
     */
    public Action getNextPossibleAction(final List<Action> actions) {
        if (actions.isEmpty()) {
            LOGGER.warning("List with possible actions is empty.");
            return null;
        }

        Action selectedAction = null;
        actions.sort((o1, o2) -> getScore(o2).compareTo(getScore(o1)));

        Random random = new Random();

        for (Action action : actions) {
            if (random.nextBoolean()) {
                selectedAction = action;
                break;
            }
        }

        // if no action selected, just use the first one
        if (selectedAction == null) {
            selectedAction = actions.get(0);
        }

        return selectedAction;
    }

    /**
     * This method exist only for testing reasons.
     *
     * @param action
     *            the action which score has to be changed
     * @param score
     *            a score for the action
     * @param used
     *            number of how often the action has been used
     */
    public void setScore(final Action action, final Double score,
            final Integer used) {
        if (action != null) {
            getCachedActions().put(action,
                    new AbstractMap.SimpleEntry<Number, Integer>(score, used));
        }
    }

}