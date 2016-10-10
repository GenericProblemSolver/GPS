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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import gps.ResultEnum;
import gps.common.BenchmarkField;
import gps.games.GamesModule;
import gps.games.IGameResult;
import gps.games.algorithm.AbstractGameAlgorithm;
import gps.games.algorithm.recycling.AbstractRecycler;
import gps.games.algorithm.recycling.SearchRecycler;
import gps.games.algorithm.heuristic.HeuristicUtility;
import gps.games.algorithm.heuristic.NoHeuristic;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;
import gps.games.wrapper.IHeuristicPlayer;
import gps.games.wrapper.ISingleplayerHeuristic;
import gps.games.wrapper.successor.INode;
import gps.util.Tuple;

/**
 * Implementation of iterative nested monte carlo search. Uses {@link NMCS} to
 * execute nested monte carlo search iterations until a given time limit is
 * exceeded. Additional provides {@link AbstractGameAlgorithm} and consequently
 * {@link IGameResult} functionalities.
 *
 * @param <T>
 *         The class of the problem that is to be solved by this algorithm
 *
 * @author jschloet@tzi.de
 */
public class IterativeNMCS<T> extends AbstractGameAlgorithm<T> {

    /**
     * The time that is available to solve the problem.
     */
    private long timeLimit;

    /**
     * The starting time of this algorithm. Set to the current time when {@link
     * #startAlgorithm()} is called.
     */
    private long startTime;

    /**
     * The starting level of the nested monte carlo search iterations. See
     * {@link NMCS#} for further information
     */
    private int startingLevel;

    /**
     * The current result is stored in this attribute. If a better solution is
     * found, this attribute is set with set solution.
     */
    private Sample result;

    /**
     * This flag is set when one iteration of {@link NMCS} is executed. Used in
     * {@link IterativeNMCS#isFinished()}
     */
    private boolean terminatedOnce;

    /**
     * A flag that signals whether circles should be avoided during the sampling
     * phase. If set {@link NMCS#sampleRandomDepthFirstSearch(INode, HashSet,
     * int)} is used instead of {@link NMCS#sample(INode)}.
     */
    private boolean circleAvoidance;

    /**
     * Boolean that indicates whether the given {@link #module} specifies a
     * problem that aims to find an as short as possible path.
     */
    private boolean shortestPathProblem;

    /**
     * Lock to prevent concurrency problems. The usage of this locks handles that only the
     * wanted number of threads is running.
     */
    private ReentrantLock startedLock = new ReentrantLock();

    /**
     * A boolean that signals if the heuristic version of {@link NMCS} should be
     * used. Per default this attribute is {@code true} if the given game has an
     * heuristic. The usage of the heuristic version can be disabled by calling
     * {@link #disableHeuristicUsage()}
     */
    private boolean useHeuristic;

    /**
     * Heuristic to be used in {@link HeuristicNMCS}. Or {@code null} if no
     * heuristic should be used.
     */
    private ISingleplayerHeuristic heuristic;

    /**
     * Instance of {@link NMCS} used to solve the given problem
     */
    private NMCS<T> nmcs;

    /**
     * Data structure used by {@link NMCS}
     */
    private INMCSData<T> data;

    /**
     * If set, the algorithm terminates when a better solution than the current {@link #result} is
     * found.
     */
    private boolean terminateOnFoundSolution;

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
     * Recycler that gets passed to {@link NMCS} if not {@code null}
     */
    private AbstractRecycler<T> recycler;

    /**
     * If set, {@link DynamicHeuristicNMCS} is used instead of {@link HeuristicNMCS} (If a heuristic is given).
     */
    private boolean dynamic;

    /**
     * Constructor. Calls the super constructor with the given game. Initializes
     * {@link #result}.
     *
     * @param pModule
     *            The module that instantiated the algorithm.
     * @param heuristicTuple Contains the {@link ISingleplayerHeuristic} to be used in this algorithm as well as the {@link HeuristicUsage} that
     *                       determines how the given heuristic is used (By using {@link HeuristicNMCS} or {@link DynamicHeuristicNMCS}). If {@code null}, no {@link ISingleplayerHeuristic} is given or {@link HeuristicUsage#NONE}
     *                       is given, no {@link ISingleplayerHeuristic} is used (The standard {@link NMCS} is used).
     * @param pData Data structure to be used.
     * @param pCircleAvoidance If {@code true}, the circle avoidance of {@link NMCS} is used. Gets passed to {@link NMCS#setCircleAvoidance(boolean)}
     * @param pShortestPathCutOff If {@code true}, the shortest path cut offs of {@link NMCS} are used. Gets passed to {@link NMCS#setShortestPath(boolean)}
     * @param  level Starting level of the nested monte carlo search. Gets passed to {@link NMCS#setStartingLevel(int)}
     * @param pRecycler The {@link AbstractRecycler} to be used parallel to this thread. If {@code null}, no {@link AbstractRecycler} is used. The given {@link AbstractRecycler}
     *                  must not be started yet.
     */
    public IterativeNMCS(final GamesModule<T> pModule,
            final Tuple<ISingleplayerHeuristic, HeuristicUsage> heuristicTuple,
            final INMCSData<T> pData, final Boolean pCircleAvoidance,
            final Boolean pShortestPathCutOff, final Integer level,
            final AbstractRecycler<T> pRecycler) {
        super(pModule, BenchmarkField.BEST_MOVE_DEPTH,
                BenchmarkField.NUMBER_OF_SIMULATIONS,
                BenchmarkField.SEEN_NODES);
        recycler = pRecycler;
        result = new Sample(Double.NEGATIVE_INFINITY);
        useHeuristic = heuristicTuple != null && heuristicTuple.getX() != null;
        heuristic = (heuristicTuple != null) ? heuristicTuple.getX() : null;
        heuristic = (module.getGame().hasPlayerMethod() && heuristic != null
                && !(heuristic instanceof IHeuristicPlayer)) ? null : heuristic;
        useHeuristic = heuristic != null && heuristicTuple != null
                && heuristicTuple.getX() != null
                && !heuristicTuple.getY().equals(HeuristicUsage.NONE);
        timeLimit = -1;
        data = pData;
        dynamic = (heuristicTuple != null)
                && heuristicTuple.getY().equals(HeuristicUsage.DYNAMIC);
        setShortestPath(pShortestPathCutOff);
        setCircleAvoidance(pCircleAvoidance);
        setStartingLevel(level);
        // Default terminateOnFounndSolution to true, as this is necessary when running the benchmarks
        terminateOnFoundSolution(true);
    }

    /**
     * Constructor without options. Can be used by the invoker to instantiate
     * this class and be able to call {@link #getOptions()}
     *
     * @param pModule
     *         The module that instantiated the algorithm.
     */
    public IterativeNMCS(final GamesModule<T> pModule) {
        super(pModule);
    }

    /**
     * Sets the {@link #startingLevel} with the given value.
     *
     * @param startingLevel
     *         Used to set {@link IterativeNMCS#startingLevel}
     */
    public void setStartingLevel(final int startingLevel) {
        this.startingLevel = startingLevel;
    }

    /**
     * Sets {@link #useHeuristic} with {@code false}. That disables the usage of
     * the heuristic version of {@link NMCS}.
     */
    public void disableHeuristicUsage() {
        useHeuristic = false;
    }

    /**
     * Sets {@link #circleAvoidance} with the given value. If {@code true} the
     * circle avoiding method {@link NMCS#sampleRandomDepthFirstSearch(INode,
     * HashSet, int)} will be used instead of {@link NMCS#sample(INode)} in the
     * sampling phase.
     *
     * @param value
     *         Used to set {@link #circleAvoidance}.
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
     *         Boolean used to set {@link #shortestPathProblem}
     */
    public void setShortestPath(final boolean value) {
        shortestPathProblem = value;
    }

    /**
     * Starts the algorithm. Executes iterations of {@link NMCS}
     * until the {@link #timeLimit} is exceeded or the thread is interrupted. Keeps the {@link #result} attribute
     * up to date with the best current result.
     */
    public void startAlgorithm() {
        //Two thread: one for the recycler and one for NMCS
        ExecutorService threadPool = Executors.newFixedThreadPool(
                (recycler == null) ? 1 : 2,
                new ThreadFactoryBuilder().setDaemon(true).build());
        startTime = System.currentTimeMillis();
        if (nmcs == null) {
            if (useHeuristic && heuristic != null) {
                if (dynamic) {
                    nmcs = new DynamicHeuristicNMCS<>(
                            Game.copy(module.getGame()), data, heuristic,
                            getMemorySavingMode());
                } else {
                    nmcs = new HeuristicNMCS<>(module.getGame(), data,
                            heuristic, getMemorySavingMode());
                }
            } else {
                nmcs = new NMCS<>(module.getGame(), data,
                        getMemorySavingMode());
            }
        }
        if (recycler != null) {
            threadPool.execute(() -> recycler.run());
        }
        while (timeLeft() && !Thread.currentThread().isInterrupted()) {
            if (!startedLock.isLocked() && !threadPool.isTerminated()) {
                threadPool.execute(() -> {
                    startedLock.lock();
                    nmcs.setCircleAvoidance(circleAvoidance);
                    nmcs.setShortestPath(shortestPathProblem);
                    nmcs.setStartingLevel(startingLevel);
                    //If terminateOnFoundSolution is set, set NMCSs terminateOnFoundSolution
                    //flag
                    nmcs.terminateOnFoundSolution(terminateOnFoundSolution);
                    if (recycler != null) {
                        nmcs.setRecycler(recycler);
                    }
                    nmcs.start();
                    terminatedOnce = true;
                    if (result.getValue() < nmcs.getResult().getValue()) {
                        result = nmcs.getResult();
                    }
                    startedLock.unlock();
                });
            }
            // Do not iterate through the loop if termination on a found solution is activated.
            if (terminatedOnce && terminateOnFoundSolution) {
                // If that is the case, terminatedOnce does not really mean that the algorithm finished.
                // So I set it with false
                terminatedOnce = false;
                break;
            }
        }
        nmcs.stop();
        if (result.getValue() < nmcs.getResult().getValue()) {
            result = nmcs.getResult();
        }
        threadPool.shutdownNow();
        try {
            threadPool.awaitTermination(500, TimeUnit.MILLISECONDS);
            if (recycler != null && recycler.getBestElem() != null) {
                Sample sample = new Sample();
                sample.setSequence(recycler.getBestElem().stream()
                        .filter(a -> !a.isRoot()).map(INode::getAction)
                        .collect(Collectors.toList()));
                sample.setValue(NMCS.utilityAbstraction(
                        recycler.getBestElem().size(), recycler.getBestElem()
                                .get(recycler.getBestElem().size() - 1)));
                if (sample.getValue() > result.getValue()) {
                    result = sample;
                }
            }
        } catch (InterruptedException e) {
            //Restore the interrupt status
            Thread.currentThread().interrupt();
        }
        //Keep the benchmarks up to date:
        benchmark.seenNodes = nmcs.getSeenNodes();
        benchmark.bestMoveDepth = Optional.of(result.getSequence().size());
        benchmark.numberOfSimulations = nmcs.getSimulationCounter();
    }

    /**
     * Checks whether the time limit is exceeded.
     *
     * @return {@code true} if the time limit is not exceeded. Returns {@code
     * false} otherwise.
     */
    private boolean timeLeft() {
        return timeLimit < 0
                || System.currentTimeMillis() - startTime < timeLimit;
    }

    /**
     * Uses the current {@link #result} to create a sequence of instances of game states.
     * 
     * @return A List of {@code T} that starts at the {@code T} that corresponds to {@link IterativeNMCS#module}
     * and ends in an terminal state.  
     */
    private List<T> constructStateSeqence() {
        List<T> stateSequence = new ArrayList<>();
        Game<T> currentGameState = module.getGame();
        if (result.getValue() > Double.NEGATIVE_INFINITY) {
            stateSequence.add(Game.copy(currentGameState).getProblem());
            for (Action action : result.getSequence()) {
                currentGameState.applyAction(action);
                stateSequence.add(Game.copy(currentGameState).getProblem());
            }
        }
        return stateSequence;
    }

    /**
     * Checks whether this algorithm is applicable for the given {@link Game}
     * and the give {@link ResultEnum}.
     *
     * @param type
     *         The type of solution that is requested.
     *
     * @return {@code true} if the algorithm is applicable. {@code false}
     * otherwise.
     */
    @Override
    public boolean isApplicable(final gps.ResultEnum type) {
        return (type.equals(ResultEnum.BEST_MOVE)
                || type.equals(ResultEnum.MOVES)
                || type.equals(ResultEnum.TERMINAL)
                || type.equals(ResultEnum.STATE_SEQ))
                && module.getGame().hasTerminalMethod()
                && module.getGame().hasActionMethod()
                && (!module.gameAnalysis().get().getPlayerNumber().isPresent()
                        || module.gameAnalysis().get().getPlayerNumber()
                                .get() == 1);
    }

    /**
     * Overrides {@link AbstractGameAlgorithm#getOptions()}.
     *
     * Provides possible options for the {@link #heuristic} and its {@link HeuristicUsage},
     * {@link INMCSData} and the starting level of the corresponding
     * {@link NMCS}. Also provides options for the {@link AbstractRecycler}
     * to use,
     *
     * Also provides options for the usage of shortest path cut offs
     * and circle avoidance ({@code true},{@code false}) in the
     * corresponding {@link NMCS}.
     * <p>
     * Provides possible options for the {@link #heuristic}, {@link INMCSData}
     * and the starting level of the corresponding {@link NMCS}.
     * <p>
     * Also provides options for the usage of shortest path cut offs and circle
     * avoidance ({@code true},{@code false}) in the corresponding {@link
     * NMCS}.
     *
     * @return Array of Lists with possible options for {@link IterativeNMCS}.
     */
    @Override
    public List<?>[] getOptions() {
        return new List<?>[] { getHeuristicCombinations(), // heuristics
                Arrays.asList( // Data structure
                        new NMCSGameTree<>(module.getGame().asRoot()),
                        new NMCSPathStorage<T>()),
                Arrays.asList( // circle avoidance
                        true, false),
                Arrays.asList( // Shortest path cutoffs
                        true, false),
                Arrays.asList( // Level
                        0, 1, 2, 3),
                SearchRecycler.getSearchRecyclerVariants() };
    }

    /**
     * Returns all legal, non redundant {@link ISingleplayerHeuristic}, {@link
     * HeuristicUsage} Combinations.
     *
     * @return Returns a list with the combinations
     */
    private List<Tuple<ISingleplayerHeuristic, HeuristicUsage>> getHeuristicCombinations() {
        List<Tuple<ISingleplayerHeuristic, HeuristicUsage>> toReturn = new ArrayList<>();
        for (ISingleplayerHeuristic heuristic : HeuristicUtility
                .getAllSingleplayerHeuristics(module.getGame())) {
            if (!(heuristic instanceof NoHeuristic)) {
                toReturn.addAll(HeuristicUsage.valuesAsList().stream()
                        .filter(a -> !a.equals(HeuristicUsage.NONE))
                        .collect(Collectors.toList()).stream()
                        .map(usage -> new Tuple<>(heuristic, usage))
                        .collect(Collectors.toList()));
            } else {
                // We do not need to combine HeuristicUsage.NONE or
                // NoHeuristic with any other elements.
                toReturn.add(new Tuple<>(NoHeuristic.instance(),
                        HeuristicUsage.NONE));
            }
        }
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFinished() {
        //TODO:: Right now returns true, if one iteration of NMCS is executed.
        //Even if that is the case, the result may be improved given more time.
        //I will eventually change this method if I find a better way to 
        // override this method.
        //TODO:: If termoinateOnFoundSolution is set, I do not know how to determine if
        // the algorithm is finished. Right now, I return false;
        return terminatedOnce && !terminateOnFoundSolution;
    }

    /**
     * If called the algorithm terminates after the given amount of
     * milliseconds past. Can be used to test this algorithm
     *
     * @param timeLimit The time limit
     */
    public void useTimeLimit(final int timeLimit) {
        this.timeLimit = timeLimit;
    }

    @Override
    public Optional<Action> bestMove() {
        if (!isFinished()) {
            startAlgorithm();
        }
        if (result.getValue() > Double.NEGATIVE_INFINITY
                && !result.getSequence().isEmpty()) {
            return Optional.of(result.getSequence().get(0));
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<Action>> moves() {
        if (!isFinished()) {
            startAlgorithm();
        }
        if (result.getValue() > Double.NEGATIVE_INFINITY) {
            return Optional.of(result.getSequence());
        }
        return Optional.empty();
    }

    @Override
    public Optional<T> terminalState() {
        if (!isFinished()) {
            startAlgorithm();
        }
        List<T> stateSequence = constructStateSeqence();
        if (!stateSequence.isEmpty()) {
            return Optional.of(stateSequence.get(stateSequence.size() - 1));
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<T>> stateSequence() {
        if (!isFinished()) {
            startAlgorithm();
        }
        List<T> stateSequence = constructStateSeqence();
        if (!stateSequence.isEmpty()) {
            return Optional.of(stateSequence);
        }
        return Optional.empty();
    }

    //TODO:: Keep this up-to-date when adding new options
    @Override
    public String getName() {
        return "IterativeNMCS_" + (!useHeuristic ? null : heuristic.getClass())
                + "_" + data.getClass().getName() + "_" + circleAvoidance + "_"
                + shortestPathProblem + "_" + startingLevel + "_" + dynamic
                + "_"
                + ((recycler == null) ? null
                        : ((recycler instanceof SearchRecycler)
                                ? recycler.getClass().getName() + "_"
                                        + ((SearchRecycler<?>) recycler)
                                                .getSearchRecyclerAlgorithm()
                                                .toString()
                                : recycler.getClass().getName()));
    }
}