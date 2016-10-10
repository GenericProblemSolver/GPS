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
package gps.games.algorithm.nestedMonteCarloSearch;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import gps.games.algorithm.recycling.AbstractRecycler;
import gps.games.MemorySavingMode;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;
import gps.games.wrapper.successor.INode;
import gps.util.Tuple;

/**
 * Implementation of nested monte carlo search.
 *
 * @author jschloet@tzi.de
 *
 * @param <T>
 *            Class of the game to be solved with this algorithm
 */
public class NMCS<T> {

    /**
     * The starting level specifies how deep the nested calls are executed. For
     * a higher level a lot more nested calls and a lot more simulations are
     * executed. In general a higher level leads to better results (Exceptions
     * exist. For example a higher level does not significantly improve the
     * result for problems that can be solved easily with a lower level) but
     * takes exponentially more time to terminate.
     */
    private int startingLevel;

    /**
     * This attribute is used to store the result of the algorithm. As the
     * algorithm produces results during its execution and before it terminates
     * this attribute can be used to access intermediate results.
     */
    private volatile Sample result;

    /**
     * A flag that signals whether circles should be avoided during the sampling
     * phase. If set {@link #sampleRandomDepthFirstSearch(INode, HashSet, int)}
     * is used instead of {@link NMCS#sample(INode)}.
     */
    private boolean circleAvoidance;

    /**
     * The game state for which a solution is wanted.
     */
    private Game<T> game;

    /**
     * Boolean that indicates whether the given {@link #game} specifies a
     * problem that aims to find an as short as possible path.
     */
    private boolean shortestPathProblem;

    /**
     * Boolean that signals whether the algorithm should stop. If set, the
     * recursive calls get dissolved and the algorithm terminates.
     */
    private boolean stop;

    /**
     * If set, the algorithm terminates when a better solution than the current {@link #result} is
     * found.
     */
    private boolean terminateOnFoundSolution;

    /**
     * Stores the number of executed actions on the starting level. This is done to be able to resume the algorithm
     * at that point. The number of executed actions on lower level calls is not taken into account in the context
     * of continuing the algorithm.
     */
    private int executedActionsStartingLevel = -1;

    /**
     * Counter used to count the number of samples that were executed. Is increased after calling {@link #sample(INode)}
     * respectively {@link #sampleRandomDepthFirstSearch(INode, HashSet, int)}. Includes samples that were canceled because
     * of {@link #shortestPathProblem} cut offs and calls of the {@link #stop()} method.
     */
    private int simulationCounter;

    /**
     * Counts the nodes that were seen during the course of the algorithm.
     */
    int seenNodes = 1;

    /**
     * If not {@code null}, the samples created in {@link #sample(INode)} or {@link #sampleRandomDepthFirstSearch(INode, HashSet, int)}
     * are inserted in the recycler. The recycler must already be started.
     */
    private AbstractRecycler<T> recycler;

    /**
     * the Logger of this class
     */
    private static Logger LOGGER = Logger.getAnonymousLogger();

    /**
     * The memory saving mode that is used for this object.
     */
    final MemorySavingMode memorySavingMode;

    /**
     * To be able to refresh the {@link #result} by using {@link INMCSData#getPathTo(INode, int)}
     * a terminal ist stored. If a {@link #sampleRandomDepthFirstSearch(INode, HashSet, int)} gets
     * cut off no new terminal is determined. In that case this {@link INode} can be used instead.
    
     * Remember a terminal state. Doing this because it is needed to call {@link INMCSData#getPathTo(INode, int)} after
     * resuming the algorithm.*
     */
    private INode<T> globalTerminal;

    /**
     *Instantiates {@link #data} and {@link #result} .
     *
     * @param pGame
     *            Used to call the super constructor
     * @param pData
     *            data structure to be used during the algorithm
     * @param pMemorySavingMode
     *            The memory saving mode that is used when a copy of the game
     *            state is created.
     * 
     *            TODO sollte hier nicht statt game auch ein gamesmodule
     *            übergeben werden?
     */
    public NMCS(Game<T> pGame, INMCSData<T> pData,
            MemorySavingMode pMemorySavingMode) {
        game = Game.copy(pGame);
        result = new Sample(Double.NEGATIVE_INFINITY);
        LOGGER.setLevel(Level.OFF);
        data = pData;
        memorySavingMode = pMemorySavingMode;
    }

    /**
     * Starts the algorithm using {@link #nestedSearch(int, INode)} with
     * {@link #startingLevel} and the given game.
     */
    public void start() {
        stop = false;
        data.setStartingLevel(startingLevel);
        //TODO:: verify that this is not needed anymore
        //data.clearExec();
        INode<T> terminal = nestedSearch(startingLevel, game.copy().asRoot());
        //Get new results from the recycler:
        if (terminal != null) {
            List<Action> path = data.getPathToAsActions(terminal,
                    startingLevel);
            Sample potentialResult = new Sample();
            potentialResult.setValue(utilityAbstraction(path.size(), terminal));
            potentialResult.setSequence(path);
            if (!stop) {
                result = potentialResult;
            }
        }
    }

    /**
     *
     * Sets {@link #stop} with {@code true}. Caused by that, all recursive calls
     * get dissolved and the algorithm terminates.
     */
    void stop() {
        stop = true;
    }

    /**
     * Sets {@link #startingLevel} with the given value.
     *
     * @param level
     *            Used to set {@link #startingLevel}.
     */
    public void setStartingLevel(final int level) {
        startingLevel = level;
    }

    /**
     * Sets {@link #circleAvoidance} with the given value. If {@code true} the
     * circle avoiding method
     * {@link #sampleRandomDepthFirstSearch(INode, HashSet, int)} will be used
     * instead of {@link #sample(INode)} in the sampling phase.
     *
     * @param value
     *            Used to set {@link #circleAvoidance}.
     */
    public void setCircleAvoidance(final boolean value) {
        circleAvoidance = value;
    }

    /**
     * Sets {@link #shortestPathProblem} with the given value. If {@code true}
     * the algorithm uses some optimizations to improve the performance for
     * problems that aim to find a shortest path.
     *
     * @param value
     *            Boolean used to set {@link #shortestPathProblem}
     */
    public void setShortestPath(final boolean value) {
        shortestPathProblem = value;
    }

    /**
     * Setter for the {@link #terminateOnFoundSolution} attribute. If the attribute is set, the algorithm
     * terminates when a better solution than the current {@link #result} is found.
     *
     * @param value The new value for {@link #terminateOnFoundSolution}
     */
    public void terminateOnFoundSolution(final boolean value) {
        terminateOnFoundSolution = value;
    }

    /**
     * Returns the value of {@link #simulationCounter}
     *
     * @return The value of {@link #simulationCounter}
     */
    int getSimulationCounter() {
        return simulationCounter;
    }

    /**
     * Returns the value of {@link #seenNodes}
     *
     * @return The value of {@link #seenNodes}
     */
    int getSeenNodes() {
        return seenNodes;
    }

    /**
    * Sets the {@link #recycler} that is feeded with the samplings created
     * during this algorithm.
     *
     * @param recycler New value for {@link #recycler}
     */
    void setRecycler(final AbstractRecycler<T> recycler) {
        this.recycler = recycler;
    }

    /**
     * Datastructure used to store solutions
     */
    private INMCSData<T> data;

    /**
     * Executes the nested monte carlo search (NMCS) algorithm to determinate an
     * as good as possible solution for the given game. NMCS selects the next
     * move to execute by using nested calls in the higher levels and by using
     * random simulations in level 0.
     *
     * @param level
     *            Specifies the level of the NMCS. If zero random simulations
     *            are used to determine the next move to execute. Otherwise
     *            nested calls are used to determine the next move.
     * @param currentNode
     *            Starting point of this search
     * @return A terminal state as {@link INode}.
     */
    private INode<T> nestedSearch(final int level, INode<T> currentNode) {
        LOGGER.info("Starting level " + level + " search");
        data.addExecuted(currentNode, level);
        INode<T> terminal = null;
        boolean terminalReached = false;
        int executedActions = 1 + data.numberExec(level, startingLevel);
        List<INode<T>> path = new ArrayList<>();
        if (level == startingLevel && executedActionsStartingLevel != -1
                && globalTerminal != null) {
            executedActions = executedActionsStartingLevel;
            path = data.getPathTo(globalTerminal, startingLevel);
            currentNode = path.get(executedActions - 1);
        }
        while (!terminalReached) {
            LOGGER.warning("Executed Actions: " + executedActions
                    + " Path size: " + path.size());
            INode<T> potentialTerminal;
            if (stop) {
                return terminal;
            }
            if (level == 0) {
                potentialTerminal = level0Call(currentNode, path.size());
            } else {
                potentialTerminal = higherLevelCall(level, currentNode);
            }
            if (potentialTerminal != null) {
                terminal = potentialTerminal;
            }
            if (recycler != null && recycler.getBestElem() != null) {
                data.insert(recycler.getBestElem(), level);
            }
            if (terminal != null) {
                path = data.getPathTo(terminal, level);
            } else if (globalTerminal != null) {
                path = data.getPathTo(globalTerminal, level);
            }
            if (path.size() <= executedActions) {
                terminalReached = true;
            } else {
                executedActions++;
                currentNode = path.get(executedActions - 1);
                data.addExecuted(currentNode, level);
                if (level == startingLevel) {
                    executedActionsStartingLevel = executedActions;
                }
            }
        }
        LOGGER.info("Ending level " + level + " search");
        LOGGER.warning("Returning: " + terminal);
        if (level != startingLevel) {
            data.finishedSearch(level);
        } else if (!stop) {
            //If the algorithm is finished because it naturally terminated, the executed actions should
            // be cleared to enable a restart of the algorithm. The solutions can be reused.
            data.clearExec();
            executedActionsStartingLevel = 1;
        }
        return terminal;
    }

    /**
     * Executes a level 0 call. Executes a {@link #sample(INode)} or
     * {@link #sampleRandomDepthFirstSearch(INode, HashSet, int)} for every
     * successor of the given game state depending on the value of
     * {@link #circleAvoidance}. Stores the result with the highest value in
     * {@link #data}. Returns a terminal state.
     *
     * @param currentNode
     *            The Starting point of this level 0 call.
     * @param depthLimit
     *            The maximum depth a sampling is allowed to reach
     * @return A terminal state as {@link INode}.
     */
    private INode<T> level0Call(INode<T> currentNode, final int depthLimit) {
        LOGGER.info("Starting level 0 call");
        HashSet<Game<T>> alreadyCalled = new HashSet<>();
        alreadyCalled.addAll(data.getPathTo(currentNode, 0).stream()
                .map(INode::getGame).collect(Collectors.toList()));
        alreadyCalled.add(currentNode.getGame());
        alreadyCalled.add(game);
        INode<T> terminal = null;
        for (INode<T> node : currentNode.getSuccessors(memorySavingMode)) {
            seenNodes++;
            if (stop) {
                return terminal;
            }
            if (!alreadyCalled.contains(node.getGame())) {
                data.insert(currentNode, node, 0);
                if (circleAvoidance) {
                    terminal = sampleRandomDepthFirstSearch(node, alreadyCalled,
                            depthLimit);
                    if (terminal == null) {
                        data.clearSample();
                    } else {
                        data.finishedSample();
                    }
                } else {
                    terminal = sample(node);
                    data.finishedSample();
                }
                simulationCounter++;
                // Use the globalTerminal workaround, as the
                // path can improve even if no better path was
                // found, when the game tree is used.
                if (terminal == null) {
                    terminal = globalTerminal;
                }
                if (terminal != null) {
                    globalTerminal = terminal;
                    LOGGER.info("Getting Path");
                    if (recycler != null && recycler.getBestElem() != null) {
                        data.insert(recycler.getBestElem(), 0);
                    }
                    List<Action> path = data.getPathToAsActions(terminal, 0);
                    LOGGER.info("Got Path");
                    //Feed the recycler with solutions:
                    if (recycler != null) {
                        recycler.insert(data.getPathTo(terminal, 0));
                    }
                    Sample sample = new Sample();
                    sample.setSequence(path);
                    sample.setValue(utilityAbstraction(path.size(), terminal));
                    if (!path.isEmpty()
                            && result.getValue() < sample.getValue()) {
                        result = sample;
                        // If terminateOnFoundSolution is set, dissolve the recursion.
                        if (terminateOnFoundSolution) {
                            stop();
                            //Clear the data for lower level calls, as these are ignored when resuming the
                            //algorithm later.
                            for (int i = startingLevel - 1; i >= 0; i--) {
                                data.finishedSearch(i);
                            }
                            return terminal;
                        }
                    }
                }
            }
        }
        LOGGER.info("Ending level 0 call");
        return terminal;
    }

    /**
     * Implementation of a higher level call used by
     * {@link #nestedSearch(int, INode)}. Calls
     * {@link #nestedSearch(int, INode)} with the given level decreased by one
     * for every successor of the given game state. Stores the solution with the
     * highest value in {@link #data}. Returns a terminal state.
     *
     * @param level
     *            The current level.
     * @param currentNode
     *            The current game state.
     * @return A terminal state as {@link INode}.
     */
    private INode<T> higherLevelCall(final int level, INode<T> currentNode) {
        LOGGER.info("Starting higher level call");
        HashSet<Game<T>> alreadyCalled = new HashSet<>();
        LOGGER.info("Getting Path");
        alreadyCalled.addAll(data.getPathTo(currentNode, 0).stream()
                .map(INode::getGame).collect(Collectors.toList()));
        LOGGER.info("Got Path");
        alreadyCalled.add(game);
        alreadyCalled.add(currentNode.getGame());
        INode<T> terminal = null;
        if (currentNode.getGame().isTerminal()) {
            return currentNode;
        }
        for (INode<T> node : currentNode.getSuccessors(memorySavingMode)) {
            seenNodes++;
            if (stop) {
                return terminal;
            }
            if (!alreadyCalled.contains(node.getGame())) {
                data.insert(currentNode, node, level);
                // Another variant would be to just call higherlevelcall
                // recursively and check the exit condition here.
                INode<T> searchResult = nestedSearch(level - 1, node);
                terminal = searchResult == null ? terminal : searchResult;
            }
        }
        LOGGER.info("Ending higher level call");
        // TODO: More than one terminal
        return terminal;
    }

    /**
     * Plays random moves according to the playout strategy implemented in
     * {@link #selectSamplingAction(Game)} until a terminal state is reached.
     * Stores the path in {@link #data}.
     *
     * @param currentNode
     *            the game state used to execute a random playout
     *
     * @return A terminal state as {@link INode}
     */
    INode<T> sample(INode<T> currentNode) {
        LOGGER.info("Starting sample");
        while (!currentNode.getGame().isTerminal()) {
            seenNodes++;
            if (stop) {
                break;
            }
            Action action = selectSamplingAction(currentNode.getGame());
            INode<T> succ = currentNode.getSuccessor(action, memorySavingMode);
            if (!succ.getGame().equals(game)) {
                data.insert(currentNode, succ, 0);
                currentNode = succ;
            }
        }
        LOGGER.info("Ending sample");
        return currentNode;
    }

    /**
     * A sampling method as an alternative to {@link #sample(INode)}. Uses some
     * kind of random depth first search to play random moves until a terminal
     * node is reached while avoiding circles. Stores the result in
     * {@link #data}
     *
     * @param currentElement
     *            The starting point of this sampling.
     * @param alreadyCalled
     *            Game states that have already been starting points of
     *            samplings or higher level calls
     * @param depthLimit
     *            The maximum depth this method is allowed to reach.
     * @return A terminal state as {@link INode}
     */
    // So this gets us a speed up over the other sample method.
    // But for problem with a higher depth it seems like the
    // other method is faster. I do not know how the impact on
    // the quality of the solutions is.
    // If those (mostly) collision free hashes, those might give us
    // a speedup here.
    INode<T> sampleRandomDepthFirstSearch(INode<T> currentElement,
            final HashSet<Game<T>> alreadyCalled, final int depthLimit) {
        LOGGER.info("Starting randomDFS sample");
        // To prevent circles, use some type of random depth first search:
        // Therefore this linkedList is needed
        LinkedList<Tuple<INode<T>, INode<T>>> dfsStack = new LinkedList<>();
        // Also keep track of the visited game states to prevent circles
        HashSet<Game<T>> visited = new HashSet<>();
        visited.addAll(alreadyCalled);
        visited.add(game);
        visited.add(currentElement.getGame());
        dfsStack.addAll(constructRandomSampleElements(currentElement, visited));
        while (!currentElement.getGame().isTerminal() && !dfsStack.isEmpty()) {
            Tuple<INode<T>, INode<T>> tuple = dfsStack.pop();
            currentElement = tuple.getX();
            if (stop || (shortestPathProblem && depthLimit > 0
                    && currentElement.getDepth() > depthLimit)) {
                return null;
            }
            data.insert(tuple.getY(), currentElement, 0);
            visited.add(currentElement.getGame());
            dfsStack.addAll(0,
                    constructRandomSampleElements(currentElement, visited));
        }
        LOGGER.info("Ending randomDFS sample");
        if (dfsStack.isEmpty()) {
            return null;
        }
        return currentElement;
    }

    /**
     *
     * Determines a list of {@link Action}s that are legal in the given game
     * state. Filters the {@link Action}s that lead to a game state that was
     * already visited. Shuffles those list. Also stores the predecessor for
     * every game state in the list.
     *
     * @param currentElement
     *            The game state which legal actions are to be executed next.
     * @param visited
     *            A List of the already visited game states.
     * @return A randomly shuffled list of all actions that are legal in the
     *         given game and do not lead to an already visited game state.
     */
    List<Tuple<INode<T>, INode<T>>> constructRandomSampleElements(
            INode<T> currentElement, final HashSet<Game<T>> visited) {
        List<Tuple<INode<T>, INode<T>>> randomSampleElements = currentElement
                .getSuccessors(memorySavingMode).stream()
                .filter(s -> !visited.contains(s.getGame()))
                .map(s -> new Tuple<>(s, currentElement))
                .collect(Collectors.toList());
        Collections.shuffle(randomSampleElements);
        seenNodes += randomSampleElements.size();
        return randomSampleElements;
    }

    /**
     * This method selects an action to be executed in the given game state
     * during the {@link #sample(INode)} phase. Uses a uniform random strategy
     * to select the next action. Can be overridden to use a different strategy.
     *
     * @return The selected action.
     */
    Action selectSamplingAction(Game<T> currentGameState) {
        Random random = new Random();
        int selectedAction = random
                .nextInt(currentGameState.getActions().size());
        return currentGameState.getActions().get(selectedAction);
    }

    /**
     * This method is used to abstract from the utility method of the given
     * game. In case a utility method is given. The given utility method is used
     * ( either the one with parameter or the one without). If there is no
     * utility method, we assume that a as short as possible solution is wanted.
     * Therefore we use a combination of {@link Game#isTerminal()} and the depth
     * of the solution as utility value.
     *
     * @param depth
     *            The depth of the currentGameState
     * @return The abstracted utility value
     */
    public static <T> double utilityAbstraction(final int depth,
            final INode<T> currentGameState) {
        if (currentGameState.getGame().hasUtilityMethod()) {
            if (currentGameState.getGame().hasPlayerMethod()) {
                return currentGameState.getGame()
                        .getUtility(currentGameState.getGame().getPlayer())
                        .doubleValue();
            } else {
                return currentGameState.getGame().getUtility().doubleValue();
            }
        } else {
            if (currentGameState.isTerminal()) {
                return (-1) * depth;
            } else {
                return Double.NEGATIVE_INFINITY;
            }
        }
    }

    /**
     * Getter for the {@link Sample#getSequence()} value of {@link #result}
     *
     * @return Returns the sequence of {@link Action}s that forms a solution for
     *         the given game.
     */
    public List<Action> getSequence() {
        return result.getSequence();
    }

    /**
     * Getter for the {@link #result}
     *
     * @return A {@link Sample} that contains a solution for the given problem
     *         and its value.
     */
    public Sample getResult() {
        return result;
    }
}