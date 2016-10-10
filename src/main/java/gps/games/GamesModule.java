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
package gps.games;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.neuroph.core.NeuralNetwork;

import gps.ISolverModule;
import gps.IWrappedProblem;
import gps.ResultEnum;
import gps.common.AlgorithmUtility;
import gps.games.algorithm.AbstractGameAlgorithm;
import gps.games.algorithm.analysis.GameAnalyser;
import gps.games.algorithm.analysis.GameAnalysisResult;
import gps.games.algorithm.analysis.IGameAnalysisResult;
import gps.games.util.AlgorithmComparator;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;
import gps.games.wrapper.Player;
import gps.util.AnalysisCache;
import gps.util.Tuple;

/**
 * The module for solving game problems.
 * 
 * @author haker@uni-bremen.de
 *
 * @param <T>
 *            The type of the problem class.
 */
public class GamesModule<T> implements ISolverModule<T>, IGameResult<T> {

    /**
     * The logger for this class.
     */
    private static Logger LOGGER = Logger
            .getLogger(GamesModule.class.getCanonicalName());

    /**
     * The game constructed of the problem class that has been provided to the
     * module. Might be {@code null}.
     */
    private final Game<T> game;

    /**
     * Caches the return of the GameAnalyser. Analysis will only be done once.
     */
    private Optional<? extends IGameAnalysisResult> analyserData = Optional
            .empty();

    /**
     * Construct a GamesModule from a wrapped problem that is a game.
     * 
     * @param pProblem
     *            The game problem.
     */
    public GamesModule(IWrappedProblem<T> pProblem) {
        game = new Game<>(pProblem);
    }

    /**
     * Construct a GamesModule from a game instance.
     * 
     * @param pGame
     *            The game.
     */
    public GamesModule(Game<T> pGame) {
        game = pGame.copy();
    }

    /**
     * Construct an empty dummy module. No method may be called for this module.
     * Use {@link #isDummy()} to check whether this module is a dummy.
     * 
     * TOOD tobi think of something better at this point. We need this so we can
     * sort the algorithms for the neuronal network output vector. Every
     * algorithm must accept this dummy.
     */
    public static GamesModule<?> createDummy() {
        return new GamesModule<>(new IWrappedProblem<Object>() {

            @Override
            public void applyAction(Action pAction) {
            }

            @Override
            public boolean hasApplyActionMethod() {
                return true;
            }

            @Override
            public List<Runnable> getRunnableMoves() {
                return new ArrayList<>();
            }

            @Override
            public List<Action> getActions() {
                return new ArrayList<>();
            }

            @Override
            public boolean hasActionMethod() {
                return true;
            }

            @Override
            public boolean isTerminal() {
                return false;
            }

            @Override
            public boolean hasTerminalMethod() {
                return true;
            }

            @Override
            public Number heuristic() {
                return 0;
            }

            @Override
            public Number heuristic(Player pPlayer) {
                return 0;
            }

            @Override
            public boolean hasHeuristicMethod() {
                return true;
            }

            @Override
            public boolean hasHeuristicPlayerMethod() {
                return true;
            }

            @Override
            public Player getPlayer() {
                return new Player("Dummy");
            }

            @Override
            public boolean hasPlayerMethod() {
                return true;
            }

            @Override
            public Number getUtility() {
                return 0;
            }

            @Override
            public Number getUtility(Player pPlayer) {
                return 0;
            }

            @Override
            public boolean hasUtilityMethod() {
                return true;
            }

            @Override
            public boolean hasUtilityPlayerMethod() {
                return true;
            }

            @Override
            public boolean hasObjectiveFunction() {
                return true;
            }

            @Override
            public double objectiveFunction(Object[] params) {
                return 0;
            }

            @Override
            public Object[] getDefaultParams() {
                return new Object[] {};
            }

            @Override
            public List<Object[]> neighbor(Object[] params) {
                return new ArrayList<>();
            }

            @Override
            public boolean hasNeighborFunction() {
                return true;
            }

            @Override
            public Object getSource() {
                return new Object();
            }

            @Override
            public Object[] getAttributes() {
                return new Object[] {};
            }

            @Override
            public void setAttribute(int index, Object val) {

            }

            @Override
            public boolean isAttributeFinal(int index) {
                return false;
            }

            @Override
            public void setThresholdForObjectiveFunction(double pThresh) {

            }

            @Override
            public boolean canBeGreaterThan() {
                return false;
            }

            @Override
            public void setMaximize(byte pMax) {

            }
        });
    }

    /**
     * Last algorithm that has been used.
     */
    private Optional<AbstractGameAlgorithm<T>> lastUsedAlgorithm = Optional
            .empty();
    /**
     * Map used by the classifier. TODO tobi move to a separate class?
     */
    private Map<ResultEnum, AbstractGameAlgorithm<T>> classifierResult = new HashMap<>();

    /**
     * Get the best algorithm for the given problem and result type.
     * 
     * @param resType
     *            The type of the desired result.
     * 
     * @throws RuntimeException
     *             if none can be found.
     * 
     */
    @SuppressWarnings("unchecked")
    public AbstractGameAlgorithm<T> classify(final ResultEnum resType) {
        if (forceAlgorithm != null) {
            return forceAlgorithm;
        }

        AbstractGameAlgorithm<T> a = classifierResult.getOrDefault(resType,
                null);
        try {
            if (a != null) {
                return a;
            }

            // get input vector
            final Optional<? extends IGameAnalysisResult> ad = gameAnalysis();

            if (!ad.isPresent()) {
                throw new RuntimeException(
                        "Cannot classify without analysis data");
            }

            final double[] vector = Arrays
                    .stream(ad.get().getClassificationVector())
                    .mapToDouble(m -> m).toArray();

            // load neuronal network
            URL s = ClassLoader.getSystemClassLoader()
                    .getResource("nn/" + resType.toString() + ".nnet");
            if (s == null) {
                throw new RuntimeException("getResourceAsStream returned null");
            }
            final InputStream is;
            try {
                is = s.openStream();
            } catch (IOException e) {
                throw new RuntimeException(
                        "Classification failed due to IOException", e);
            }

            @SuppressWarnings("rawtypes")
            NeuralNetwork nn = NeuralNetwork.load(is);

            // set input vector
            nn.setInput(vector);

            // let the nn do it's calculations
            nn.calculate();

            // get the output
            final double[] scores = nn.getOutput();

            // the output is formatted as the algorithm names are sorted in
            // ascending order. Therefore we must find all algorithms that may
            // solve the given result type

            // get all algorithms (any result type)
            @SuppressWarnings("rawtypes")
            List<? extends AbstractGameAlgorithm> list = AlgorithmUtility
                    .instantiateAllAlgorithms(AbstractGameAlgorithm.class,
                            resType, this);

            // get the algorithm in order of the output layer of the neuronal
            // network
            AbstractGameAlgorithm<T> algos[] = list.stream()
                    .sorted(new AlgorithmComparator())
                    .toArray(i -> new AbstractGameAlgorithm[i]);

            // in case the dimension is not equal, we throw an exception. This
            // may happen if a new algorithm is added but no neuronal network
            // has been build for this new algorithm yet.
            if (scores.length != algos.length) {
                LOGGER.info("scores.length=" + scores.length + " algos.length="
                        + algos.length);
                throw new RuntimeException(
                        "The output vector of the neuronal network is not compatible with the algorithms list. Use BUTT to rebuild the neuronal network.");
            }

            // an ordered list with algorithms. Is ordered by score. Highest
            // score is first.
            final List<AbstractGameAlgorithm<T>> mappedScores = IntStream
                    .range(0, algos.length)
                    .mapToObj(i -> new Tuple<>(scores[i], algos[i]))
                    // only keep algorithms that solve the given result type
                    .filter(p -> p.getY().isApplicable(resType))
                    // order by score highest first
                    .sorted((o1, o2) -> Double.compare(o2.getX(), o1.getX()))
                    .map(m -> m.getY()).collect(Collectors.toList());

            // get the best algorithm
            final Optional<AbstractGameAlgorithm<T>> best = mappedScores
                    .isEmpty() ? Optional.empty()
                            : Optional.of(mappedScores.get(0));

            if (!best.isPresent()) {
                throw new RuntimeException(
                        "no algorithm can solve the given result type.");
            }

            // add the algorithm to the map so that we can remember out decision
            classifierResult.put(resType, best.get());

            return best.get();
        } finally {
            lastUsedAlgorithm = Optional.ofNullable(a);
        }
    }

    @Override
    public boolean canSolve(ResultEnum type) {
        return game.hasSuccessorsMethod() && game.hasTerminalMethod();
    }

    @Override
    public Optional<List<Action>> moves() {
        return classify(ResultEnum.MOVES).moves();
    }

    @Override
    public Optional<List<T>> stateSequence() {
        return classify(ResultEnum.STATE_SEQ).stateSequence();
    }

    @Override
    public Optional<T> terminalState() {
        return classify(ResultEnum.TERMINAL).terminalState();
    }

    @Override
    public Optional<Action> bestMove() {
        return classify(ResultEnum.BEST_MOVE).bestMove();
    }

    @Override
    public Optional<Boolean> isWinnable() {
        return classify(ResultEnum.WINNABLE).isWinnable();
    }

    @Override
    public boolean isFinished() {
        return lastUsedAlgorithm.isPresent()
                && lastUsedAlgorithm.get().isFinished();
    }

    @Override
    public Optional<? extends IGameAnalysisResult> gameAnalysis() {
        if (!(analyserData.isPresent())) {
            {
                // check if cached result can be found
                Optional<GameAnalysisResult> cachedRes = AnalysisCache
                        .getCachedResult(game);
                if (cachedRes.isPresent()) {
                    LOGGER.fine("cache hit: "
                            + game.getProblem().getClass().getCanonicalName());
                    // yes, so we return this
                    analyserData = cachedRes;
                    return analyserData;
                }
                LOGGER.warning("cache miss: "
                        + game.getProblem().getClass().getCanonicalName()
                        + " run butt analysis cache tool");
            }
            // no cached result has been found, so we run the analysis
            final GameAnalyser<T> ga = new GameAnalyser<>(this);
            analyserData = ga.gameAnalysis();
            if (analyserData.isPresent()) {
                // put it to the cache
                AnalysisCache.putToCache(game,
                        (GameAnalysisResult) analyserData.get());
            }
        }
        return analyserData;
    }

    /**
     * The depth limit algorithms should respect for their algorithms. Is always
     * positive.
     */
    private int depthlimit = -1;

    /**
     * if not {@code null} the classification and anaylsis is skipped and the
     * algorithm stored in this field is used to solve any solution.
     */
    private AbstractGameAlgorithm<T> forceAlgorithm = null;

    /**
     * The depth limit algorithms should respect for their algorithms. Is always
     * positive.
     * 
     * Algorithms may interpret that however they want. This is just a
     * recommendation and not mandatory for algorithms to confess.
     * 
     * @return The depth limit.
     */
    public int getDepthlimit() {
        return depthlimit;
    }

    /**
     * Set or remove the depthlimit for calculations.
     * 
     * @param pMaxdepth
     *            The depthlimit in moves, set non-positive if no depthlimit
     *            should be respected.
     */
    public void setDepthlimit(int pMaxDepth) {
        if (pMaxDepth <= 0) {
            throw new IllegalArgumentException(
                    "pMaxDepth must be greater than 0");
        }
        depthlimit = pMaxDepth;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<? extends AbstractGameAlgorithm> getApplicableAlgorithms(
            ResultEnum pResultEnum) {
        return AlgorithmUtility.getApplicableAlgorithms(
                AbstractGameAlgorithm.class, pResultEnum, this);
    }

    /**
     * Returns {@code true} if this games module should not actually solve
     * anything but rather is used for management of algorithms.
     * 
     * @return {@code true} if this games module is a dummy. {@code false}
     *         otherwise.
     */
    public boolean isDummy() {
        return game == null;
    }

    /**
     * Return a copy from the game that should be solved.
     * 
     * @return A copy of the game to solve. Might be {@code null} if the games
     *         module has been initialized with no game. Which might occur
     *         during the training phase.
     */
    public Game<T> getGame() {
        return game.copy();
    }

    /**
     * Reset all data to default. This is useful if you want to execute an
     * algorithm with a predetermined empty module state.
     * 
     * The game is not affected.
     */
    public void clear() {
        setDepthlimit(-1); // reset depth limit
        analyserData = Optional.empty(); // clear analysis data
        lastUsedAlgorithm = Optional.empty(); // clear last used algorithm
        classifierResult.clear(); // remove data stored by the classifier
    }

    /**
     * Set the algorithm that is used to solve the succeeding result types.
     * 
     * @param algo
     *            The algorithm. Might be {@code null} if the classifier should
     *            be used (this is default behavior).
     */
    public void setAlgorithm(AbstractGameAlgorithm<T> algo) {
        forceAlgorithm = algo;
    }

    /**
     * The memory saving mode.
     */
    private MemorySavingMode memorySavingMode = MemorySavingMode.NONE;

    /**
     * See {@link MemorySavingMode} for more information about the available
     * modes.
     *
     * @param pMemorySavingMode
     *            memory saving mode which should be used
     */
    public void setMemorySavingMode(MemorySavingMode pMemorySavingMode) {
        if (pMemorySavingMode == null) {
            throw new IllegalArgumentException(
                    "pMemorySavingMode may not be null");
        }
        memorySavingMode = pMemorySavingMode;
    }

    /**
     * Return the currently used memory saving mode.
     * 
     * @return The memory saving mode.
     */
    public MemorySavingMode getMemorySavingMode() {
        return memorySavingMode;
    }
}
