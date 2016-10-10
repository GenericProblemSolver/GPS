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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import gps.ResultEnum;
import gps.common.BenchmarkField;
import gps.games.GamesModule;
import gps.games.algorithm.AbstractGameAlgorithm;
import gps.games.algorithm.analysis.MCTSNode;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;
import gps.games.wrapper.Player;

/**
 * Abstract implementation of the monte-carlo-tree-search algorithm. Implements
 * the termination conditions of the algorithm. Provides default implementations
 * for the selection,backpropagation,playout and expansion phase. Also provides
 * abstract methods used to implement the formula used in the selection phase
 * and to update game tree nodes in the backpropagation phase.
 *
 * @author jschloet@tzi.de
 */
public abstract class AbstractMCTS<T> extends AbstractGameAlgorithm<T> {

    /**
     * A game tree node that represents the starting game state. Used as the
     * starting point for the search.
     */
    MCTSNode root;

    /**
     * The current game state. For memory purposes the game state corresponding
     * to a MCTSNode is not stored within the node. Therefore this attribute is
     * used to have access to the corresponding game state while traversing the
     * game tree. Is set with the {@link GamesModule#game} and kept
     * up-to-date during the course of the algorithm.
     */
    Game<T> currentState;

    /**
     * List of all known players, that participate in the given game
     */
    List<Player> participatingPlayers;

    /**
     * The maximum value returned by {@link Game#getUtility(Player)}. Can be
     * used to normalize the return values, for example in
     * {@link AbstractMCTS#calculateNodeValue(MCTSNode, Player)}.
     */
    double maxUtilityReturn;

    /**
     * Maximum amount of time in milliseconds, that is available for this
     * search.
     */
    private long timeLimit;

    /**
     * Maximum number of repetitions, the algorithm is allowed to make.
     */
    private int repetitionLimit;

    /**
     * The currently used termination mode.
     */
    private MCTSTerminationMode terminationMode;

    /**
     * Constant with the default value for the termination mode.
     */
    private final MCTSTerminationMode DEFAULT_TERMINATION_MODE = MCTSTerminationMode.INTERRUPT;

    /**
     * Constant with the default value for the time limit.
     */
    private static final long DEFAULT_TIME_LIMIT = 10000;

    /**
     * Counter for the number of repetitions. Used to maintain the repetition
     * limitation if the corresponding termination mode is selected.
     */
    private int repetitionCounter;

    /**
     * Starting time of the algorithm. Used to maintain the time limitation if
     * the corresponding termination mode is selected.
     */
    long startTime;

    /**
     * Initializes a new instance of Monte-Carlo-Tree-Search for the given game.
     * Sets termination mode and time limit with default values.
     *
     * @param pModule
     *            The module that instantiated the algorithm.
     */
    AbstractMCTS(final GamesModule<T> pModule) {
        super(pModule, BenchmarkField.SEEN_NODES,
                BenchmarkField.NUMBER_OF_SIMULATIONS,
                BenchmarkField.GAME_TREE_DEPTH,
                BenchmarkField.DEEPEST_DISCOVERED_NODE,
                BenchmarkField.PROCESSED_NODES,
                BenchmarkField.BEST_MOVE_HEURISTIC);
        maxUtilityReturn = 1;
        terminationMode = DEFAULT_TERMINATION_MODE;
        timeLimit = DEFAULT_TIME_LIMIT;
        participatingPlayers = new ArrayList<>();
    }

    /**
     * Starts the algorithm and executes it until a termination condition
     * occurs.
     */
    public void start() {
        if (root == null) {
            initGameTree(module.getGame());
        }
        initTerminationCheck();
        while (!checkTermination()) {
            executeIterartion();
        }
        //The number of simulations equals the number of visits in the root nodes
        benchmark.numberOfSimulations = root.getVisitCount();
        benchmarkClearnessOfBestMove();
        benchmarkBestMoveHeuristicValue();
    }

    /**
     * Calculates and sets the attribute of {@link #benchmark} refering to
     * {@link BenchmarkField#BEST_MOVE_HEURISTIC}.
     */
    private void benchmarkBestMoveHeuristicValue() {
        Action bestMove = getMove();
        if (bestMove.get() != null) {
            benchmark.bestMoveHeuristic = module.getGame()
                    .multiplayerScore(bestMove);
        }
    }

    /**
     *Calculates and sets the attribute of {@link #benchmark} refering to
     * {@link BenchmarkField#CLEARNESS_OF_BEST_MOVE}.
     */
    private void benchmarkClearnessOfBestMove() {
        if (root.getChildren().size() == 1) {
            benchmark.clearnessOfBestMove = Optional.of(root.getChildren()
                    .get(0).getValue(root.getCurrentPlayer()));
        } else if (root.getChildren().size() >= 2) {
            List<MCTSNode> children = root.getChildren();
            children.sort((o1, o2) -> Double.compare(
                    (-1) * o1.getValue(root.getCurrentPlayer()),
                    (-1) * o2.getValue(root.getCurrentPlayer())));
            benchmark.clearnessOfBestMove = Optional.of(
                    children.get(0).getValue(root.getCurrentPlayer()) - children
                            .get(1).getValue(root.getCurrentPlayer()));
        }
    }

    /**
     * Executes one MCTS iteration. Traverses the game tree until the fringe is
     * reached. Expands the game tree at the location where the fringe was
     * reached and starts a playout at the expansion node. Updates the values of
     * the game tree in the backpropagation phase.
     */
    private void executeIterartion() {
        currentState = Game.copy(module.getGame());
        MCTSNode currentNode = root;
        currentNode = select(currentNode);
        currentNode = expand(currentNode);
        Map<Player, Number> result = playout(currentNode.getDepth());
        refreshMaxUtilityReturn(result);
        backpropagate(currentNode, result);
    }

    /**
     * Recursively traverses the game tree until the fringe of the game tree , a
     * not fully expanded node or a terminal node is reached. The path of the
     * traversing is selected according to the highest value calculated by
     * {@link #calculateNodeValue(MCTSNode, Player)}.
     *
     * @param currentNode
     *            The next node is selected among the children of this node.
     * @return The node at which the fringe of the game tree is reached ,the
     *         first not fully expanded node or the first met terminal node.
     */
    MCTSNode select(MCTSNode currentNode) {
        // Add newly seen node to benchmarks
        benchmark.seenNodes++;
        if (currentNode.getChildren().size() < currentNode.getMaxChildren()
                || currentState.isTerminal()) {
            return currentNode;
        } else {
            double maxValue = Double.MIN_VALUE;
            MCTSNode maxChild = null;
            for (MCTSNode node : currentNode.getChildren()) {
                double nodeValue = calculateNodeValue(node,
                        currentNode.getCurrentPlayer());
                if (maxChild == null) {
                    maxChild = node;
                    maxValue = nodeValue;
                } else if (nodeValue > maxValue) {
                    maxChild = node;
                    maxValue = nodeValue;
                }
            }
            currentState.applyAction(maxChild.getAction());
            currentNode = maxChild;
            return select(currentNode);
        }
    }

    /**
     * Recursively walks through the game tree until the root is reached. Starts
     * at the given node and updates all visited nodes with the given result
     * using the
     * {@code updateNode(MCTSNode currentNode ,Map<Object, Number> result)}
     * method.
     *
     * @param currentNode
     *            Node at which the backpropagation is started.
     * @param result
     *            Playout result that is used to update the values of all
     *            visited Nodes.
     */
    void backpropagate(MCTSNode currentNode, Map<Player, Number> result) {
        if (currentNode.getParent() != null) {
            updateNode(currentNode, result);
            backpropagate(currentNode.getParent(), result);
        } else {
            // Only the visit count is relevant for the root node.
            currentNode.setVisitCount(currentNode.getVisitCount() + 1);
        }
    }

    /**
     * Executes random moves on {@link #currentState} until a terminal node is
     * reached. Uses {@link #getNextPlayoutMove()} to select the random moves.
     *
     * @param depth
     *            Depth at which the random playout starts.
     * @return The result of the {@code getUtility(Object pPlayer)} method from
     *         each players point of view.
     */
    Map<Player, Number> playout(int depth) {
        // Add newly seen node to benchmarks
        benchmark.seenNodes++;
        if (currentState.isTerminal()) {
            Map<Player, Number> result = new HashMap<>();
            for (Player player : participatingPlayers) {
                result.put(player, currentState.getUtility(player));
            }
            //benchmark deepest discovered node
            if (benchmark.deepestDiscoveredNode < depth) {
                benchmark.deepestDiscoveredNode = depth;
            }
            return result;
        } else {
            currentState.applyAction(getNextPlayoutMove());
            return playout(depth + 1);
        }
    }

    /**
     * Determines the next move to make in the playout phase. The move must be
     * legit for the {@link #currentState}. Uses a uniform random strategy to
     * determine the move.
     *
     * Can be overridden to change the playout strategy.
     *
     * @return Next move to make in the playout phase
     */
    Action getNextPlayoutMove() {
        Random random = new Random();
        int selectedMove = random.nextInt(currentState.getActions().size());
        return currentState.getActions().get(selectedMove);
    }

    /**
     * Adds one child of the given Node to the game tree. Updates the current
     * game state to correspond with the added node. Uses
     * {@link #createExpansionChild(MCTSNode, Action)} to create a new game tree
     * node.
     *
     * @param currentNode
     *            A child of this node is added to the game tree
     * @return The newly added game tree node
     */
    MCTSNode expand(MCTSNode currentNode) {
        if (currentState.isTerminal()) {
            return currentNode;
        }
        Action expandingAction = null;
        for (Action action : currentState.getActions()) {
            if (!currentNode.getExpandedActions().contains(action)) {
                expandingAction = action;
                break;
            }
        }
        if (expandingAction == null) {
            // Normally expanding action should never be null.
            throw new RuntimeException("expandingAction is null");
        }
        currentState.applyAction(expandingAction);
        MCTSNode newChild = createExpansionChild(currentNode, expandingAction);
        currentNode.addExpandingAction(expandingAction);
        currentNode.addChild(newChild);
        if (!participatingPlayers.contains(newChild.getCurrentPlayer())
                && !currentState.isTerminal()) {
            participatingPlayers.add(newChild.getCurrentPlayer());
        }
        return newChild;
    }

    /**
     * Creates a new node and sets its attributes.
     *
     * @param currentNode
     *            Parent of the new node
     * @param expandingAction
     *            Action used to get from the parent to the new node.
     * @return The new created node.
     */
    MCTSNode createExpansionChild(MCTSNode currentNode,
            Action expandingAction) {
        // Count newly added nodes as processed Nodes
        benchmark.processedNodes++;
        MCTSNode newChild = new MCTSNode();
        newChild.setAction(expandingAction);
        newChild.setCurrentPlayer(currentState.getPlayer());
        newChild.setDepth(currentNode.getDepth() + 1);
        newChild.setParent(currentNode);
        newChild.setMaxChildren(currentState.getActions().size());
        //Refresh the game tree depth in the benchmarks
        if (benchmark.gameTreeDepth < newChild.getDepth()) {
            benchmark.gameTreeDepth = newChild.getDepth();
        }
        return newChild;
    }

    /**
     * Updates the attributes of the given node with the given result. Used in
     * the backpropagation phase to update the game tree.
     *
     *
     * @param currentNode
     *            Node which attributes are to be updated
     * @param result
     *            Playout result that is used for the update
     */
    abstract void updateNode(MCTSNode currentNode, Map<Player, Number> result);

    /**
     * Calculates the value of the given node that is used in the selection
     * phase to traverse the game tree.
     *
     * Can be overridden to adjust define the selection strategy.
     *
     * @param currentNode
     *            The value of this node is calculated
     * @param player
     *            The value is calculated from this players point of view
     * @return The value of the given node
     */
    abstract double calculateNodeValue(MCTSNode currentNode, Player player);

    /**
     * Checks whether the algorithm should terminate or execute the next
     * iteration.
     *
     * @return true, if the selected termination condition is fulfilled. false,
     *         else.
     *
     */
    private boolean checkTermination() {
        if (terminationMode.equals(MCTSTerminationMode.TIMELIMIT)) {
            return System.currentTimeMillis() - startTime > timeLimit;
        } else if (terminationMode
                .equals(MCTSTerminationMode.REPETITIONLIMIT)) {
            repetitionCounter++;
            return repetitionCounter == repetitionLimit;
        } else if (terminationMode.equals(MCTSTerminationMode.INTERRUPT)) {
            return Thread.currentThread().isInterrupted();
        }
        return false;
    }

    /**
     * Initializes the termination check. Resets the repetition counter and sets
     * the start time to the current time. Must be called before the algorithm
     * is started or resumed.
     *
     */
    private void initTerminationCheck() {
        // Set to -1 as the counter is increased before the termination
        // contition is checked
        repetitionCounter = -1;
        startTime = System.currentTimeMillis();
    }

    /**
     * Refreshes the value of {@link #maxUtilityReturn} if the given map contains
     * a value higher than the current value.
     *
     * @param result
     *            A player number mapping that represents the return of
     *            {@link Game#getUtility(Player)} calls.
     */
    private void refreshMaxUtilityReturn(final Map<Player, Number> result) {
        double potentialNewValue = Collections.max(
                result.values().stream().map(n -> Math.abs(n.doubleValue()))
                        .collect(Collectors.toList()));
        if (potentialNewValue > maxUtilityReturn) {
            maxUtilityReturn = potentialNewValue;
        }
    }

    /**
     * Initializes a new game tree.
     *
     * @param game
     *            An instance of game whose attributes are used to set the
     *            currentPlayer and maxChildren Attributes of the new game tree
     *            root.
     */
    private void initGameTree(final Game<T> game) {
        root = new MCTSNode();
        root.setCurrentPlayer(game.getPlayer());
        root.setMaxChildren(game.getActions().size());
        root.setDepth(0);
        participatingPlayers.add(root.getCurrentPlayer());
    }

    /**
     * Sets the termination mode of the Monte-Carlo-Tree-Search to use a time
     * limitation as termination condition.
     *
     * @param pMilliseconds
     *            The maximum amount of time the algorithm is allowed to take.
     */
    public void setTimelimit(final int pMilliseconds) {
        timeLimit = pMilliseconds;
        terminationMode = MCTSTerminationMode.TIMELIMIT;
    }

    /**
     * Sets the termination mode of the Monte-Carlo-Tree-Search to use a
     * repetition limitation as termination condition.
     *
     * @param repetitions
     *            The maximum number of repetitions the algorithm is allowed to
     *            make.
     */
    public void useRepetitionLimit(final int repetitions) {
        repetitionLimit = repetitions;
        terminationMode = MCTSTerminationMode.REPETITIONLIMIT;
    }

    /**
     * Resumes the algorithm with the already build game tree. Uses the current
     * termination mode as termination condition.
     */
    public void resume() {
        start();
    }

    /**
     * Resumes the algorithm with the already build game tree. Uses a time
     * limitation as termination mode.
     *
     * @param pMilliseconds
     *            The maximum amount of time the algorithm is allowed to take.
     */
    public void resume(final int pMilliseconds) {
        terminationMode = MCTSTerminationMode.TIMELIMIT;
        timeLimit = pMilliseconds;
        resume();
    }

    /**
     * Checks whether this algorithm is applicable for
     * {@link AbstractGameAlgorithm#module}. The algorithm is applicable if the
     * game has an action method, a getPlayer method and an utility method.
     */
    @Override
    public boolean isApplicable(ResultEnum type) {
        // TODO:: The algorithm can also be applied if a getSuccessors method
        // exists instead of an
        // action method. However another abstraction will be needed that uses
        // the indices of getSuccessor as
        // actions and returns the corresponding successor for an given index in
        // the applyAction method.
        return type.equals(ResultEnum.BEST_MOVE)
                && module.getGame().hasActionMethod()
                && module.getGame().hasPlayerMethod()
                && module.getGame().hasUtilityPlayerMethod();
    }

    /**
     * Finds the best move playable in the origin game state according to the
     * values of the current game tree.
     *
     * @return Move with the highest average return. Or an empty action wrapper
     *         if the algorithm has not been started yet.
     */
    public Action getMove() {
        Action move = null;
        double maxValue = Double.NEGATIVE_INFINITY;
        for (MCTSNode node : root.getChildren()) {
            double value = node.getValue(root.getCurrentPlayer())
                    / node.getVisitCount();
            if (value > maxValue) {
                move = node.getAction();
                maxValue = value;
            }
        }
        if (move == null) {
            return new Action(null);
        } else {
            return move;
        }
    }

    /**
     * Returns an optional containing the determined best move if one was
     * determined. Otherwise starts the algorithm and returns the determined
     * move afterwards.
     *
     * @return The resulting optional.
     */
    @Override
    public java.util.Optional<Action> bestMove() {
        if (root == null || getMove().get() == null) {
            start();
        }
        return Optional.of(getMove());
    }

    /**
     * Returns a flag that signals whether the algorithm is finished.
     *
     * @return A boolean that signals whether the algorithm is finished.
     */
    @Override
    public boolean isFinished() {
        // TODO:: Im not sure how to handle this. As the algorithm can often
        // find a
        // better move when more time is given. I may use something like
        // margin to the second best move as an indicator.
        return false;
    }

    /**
     * Returns the root of the game tree.
     *
     * @return Root of the game tree.
     */
    public MCTSNode getTree() {
        return root;
    }
}
