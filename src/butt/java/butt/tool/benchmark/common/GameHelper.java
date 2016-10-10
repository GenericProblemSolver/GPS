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
package butt.tool.benchmark.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import butt.tool.Arguments;
import butt.tool.benchmark.IRunnableBenchmark;
import gps.ISolverModule;
import gps.ResultEnum;
import gps.common.AbstractAlgorithm;
import gps.common.IBenchmark;
import gps.games.GamesModule;
import gps.games.algorithm.AbstractGameAlgorithm;
import gps.games.algorithm.analysis.IGameAnalysisResult;
import gps.games.wrapper.Action;

/**
 * Game Helper class for benchmarking. This class provides functions that can be
 * commonly used by games to run their benchmark.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class GameHelper {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger
            .getLogger(GameHelper.class.getCanonicalName());

    static {
        LOGGER.setLevel(Level.WARNING);
    }

    /**
     * Cannot instantiate class.
     */
    private GameHelper() {

    }

    /**
     * The String that is placed into the result column if no result can be
     * found.
     */
    private static final String NO_RESULT = "NO RESULT";

    /**
     * Return Benchmarks for a game. Processes a specific algorithms with a
     * specific result types.
     * 
     * @param problem
     *            The problem module to benchmark.
     * @param gameName
     *            The name of the game.
     * @param maxMilliseconds
     *            max milliseconds the algorithms are allowed to do their
     *            calculations until the interrupted flag is set.
     * @param minMilliseconds
     *            minimum amount of milliseconds the algorithm must do it's
     *            calculations.
     * @param e
     *            The desired result type
     * @param algo
     *            the algorithm to use
     */
    private static <T> IRunnableBenchmark<T> singleBenchmark(
            final GamesModule<T> problem, final String gameName,
            long maxMilliseconds, long minMilliseconds, final ResultEnum e,
            final AbstractGameAlgorithm<T> algo) {
        LOGGER.info("call to singleBenchmark( problem, " + gameName + ","
                + maxMilliseconds + "," + minMilliseconds + "," + e.toString()
                + "," + algo.getName());
        return new IRunnableBenchmark<T>() {

            /**
             * The analysis vector
             */
            Double[] vector;

            @Override
            public boolean isApplicable(String pRegExpAlgorithm,
                    String pRegExpResult, String pRegExpProblem) {
                return gameName.matches(pRegExpProblem)
                        && e.name().matches(pRegExpResult) && algo.getClass()
                                .getCanonicalName().matches(pRegExpAlgorithm);
            }

            @Override
            public IBenchmark getBenchmark() {
                return algo.getBenchmark();
            }

            @Override
            public void run(Optional<CSV> out) {
                LOGGER.info("call to singleBenchmark.run( problem, " + gameName
                        + "," + maxMilliseconds + "," + minMilliseconds + ","
                        + e.toString() + "," + algo.getName());
                // getApplicableAlgorithms kann gameanylsisresult nutzen.
                final boolean bUsedAnalysisResults = false;

                final TimeInterval time = new TimeInterval();

                vector = new Double[IGameAnalysisResult.VECTOR_SIZE];

                // out.ifPresent(csv -> csv.append1(gameName, algo.getName(),
                // algo.getClass().getCanonicalName(), e));

                final String[] result = new String[] { NO_RESULT };

                final Runnable runner = () -> {
                    time.start();
                    // always do the analysis

                    final TimeInterval analysisTime = new TimeInterval();
                    analysisTime.start();
                    vector = problem.gameAnalysis()
                            .map(IGameAnalysisResult::getClassificationVector)
                            .orElse(new Double[IGameAnalysisResult.VECTOR_SIZE]);
                    analysisTime.end();
                    LOGGER.info("Analysis took " + analysisTime.deltaMillis()
                            + "ms (" + algo.getName() + ")");
                    problem.setAlgorithm(algo);

                    do {
                        try {
                            switch (e) {
                            case BEST_MOVE: {
                                final Optional<Action> res = problem.bestMove();
                                result[0] = res.isPresent() ? res.get()
                                        .toString().replaceAll(",", ";")
                                        : NO_RESULT;
                                break;
                            }
                            case MOVES: {
                                final Optional<List<Action>> res = problem
                                        .moves();

                                result[0] = res.isPresent()
                                        ? res.get().size() + " actions"
                                        : NO_RESULT;
                                break;
                            }
                            case STATE_SEQ: {
                                final Optional<List<T>> res = problem
                                        .stateSequence();
                                result[0] = res.isPresent()
                                        ? res.get().size() + " states"
                                        : NO_RESULT;
                                break;
                            }
                            case TERMINAL: {
                                final Optional<T> res = problem.terminalState();
                                result[0] = res.isPresent() ? res.get()
                                        .toString().replace(',', ';')
                                        .replace('\n', ';').replace('\r', ';')
                                        : NO_RESULT;
                                break;
                            }
                            case WINNABLE: {
                                final Optional<Boolean> res = problem
                                        .isWinnable();
                                result[0] = res.isPresent()
                                        ? res.get().toString() : NO_RESULT;
                                if (res.isPresent()) {
                                    time.end();
                                    return; // no more operation is necessary
                                }
                                break;
                            }
                            case GAME_ANALYSIS: {
                                Optional<? extends IGameAnalysisResult> res = problem
                                        .gameAnalysis();
                                result[0] = res.isPresent()
                                        ? res.get().toString() : NO_RESULT;
                                break;
                            }
                            default:
                                throw new RuntimeException(
                                        "Non implemented result enum for the given game object");
                            }
                        } catch (final RuntimeException exception) {
                            time.end();
                            result[0] = "EXCEPTION: " + exception.getMessage()
                                    .replace(',', ';').replace('\n', ';');
                            return;
                        } catch (final OutOfMemoryError exception) {
                            time.end();
                            result[0] = "EXCEPTION: Out of Memory";
                            /*
                             * To recover from an out of memory exception is
                             * eventually not possible because this thread might
                             * not be the only thread causing the problem. Even
                             * if it is, the scheduler might switch to another
                             * thread that tries to allocate memory and
                             * therefore thus the other threads would lack of
                             * memory too.
                             * 
                             * Best handling would be to let the JVM die.
                             * 
                             * However we can continue calculations but must
                             * keep in mind, that the JVM might be corrupted
                             * after receiving the out of memory exception. So
                             * treat following results with caution.
                             */
                            out.ifPresent(
                                    csv -> csv.append(gameName, algo.getName(),
                                            algo.getClass().getCanonicalName(),
                                            e, result[0], algo.getBenchmark(),
                                            time.deltaMillis(), false,
                                            bUsedAnalysisResults, vector));
                            out.ifPresent(csv -> csv.close());
                            System.exit(14); // out of memory error code (linux
                                             // +
                                             // windows)
                            return;
                        }
                        time.end();
                    } while (minMilliseconds > 0 // resume
                            && time.deltaMillis() <= minMilliseconds
                            && !Thread.currentThread().isInterrupted());
                };

                // run the test in a separate thread
                final Thread thread = new Thread(runner);

                thread.start();
                try {
                    if (maxMilliseconds > 0) {
                        thread.join(maxMilliseconds);
                    } else {
                        thread.join();
                    }
                } catch (InterruptedException ex) {
                    thread.interrupt();
                }
                final boolean bInterrupted = thread.isAlive();
                if (bInterrupted) {
                    thread.interrupt();
                    try {
                        thread.join(3000); // give the algorithm 3s to shut down
                    } catch (InterruptedException e1) {
                    }
                }
                out.ifPresent(csv -> {
                    csv.append(gameName, algo.getName(),
                            algo.getClass().getCanonicalName(), e, result[0],
                            algo.getBenchmark(), time.deltaMillis(),
                            bInterrupted, bUsedAnalysisResults, vector);
                    csv.close();
                });
                if (thread.isAlive()) {
                    LOGGER.severe(
                            "Algorithm " + algo.getClass().getCanonicalName()
                                    + " did not terminate after interrupt bit has been set: GameName:"
                                    + gameName + " AlgoName:" + algo.getName()
                                    + " ResultType:" + e.toString());
                    // The algorithm may end later and then corrupt the csv
                    // file. So we stop execution here
                    System.exit(258);
                }
            }

            @Override
            public AbstractAlgorithm getAlgorithm() {
                return algo;
            }

            @Override
            public ResultEnum getResultType() {
                return e;
            }

            @Override
            public String getBenchmarkName() {
                return gameName;
            }

            @Override
            public T getProblem() {
                return problem.getGame().getProblem();
            }

            @Override
            public ISolverModule<T> getModule() {
                return problem;
            }
        };

    }

    /**
     * Return Benchmarks for a game. Processes all algorithms with all result
     * types.
     * 
     * @param problem
     *            The problem to benchmark.
     * @param gameName
     *            The name of the game.
     * @param maxMilliseconds
     *            max milliseconds the algorithms are allowed to do their
     *            calculations until the interrupted flag is set.
     * @param minMilliseconds
     *            minimum amount of milliseconds the algorithm must do it's
     *            calculations.
     */
    @SuppressWarnings("unchecked")
    private static <T> Collection<IRunnableBenchmark<T>> gameBenchmarks(
            final GamesModule<T> problem, final String gameName,
            long maxMilliseconds, long minMilliseconds) {
        ArrayList<IRunnableBenchmark<T>> list = new ArrayList<>();
        GamesModule<T> mod = problem;
        for (final ResultEnum e : ResultEnum.values()) {
            for (final AbstractGameAlgorithm<T> algo : mod
                    .getApplicableAlgorithms(e)) {
                list.add(singleBenchmark(mod, gameName, maxMilliseconds,
                        minMilliseconds, e, algo));
            }
        }
        return list;
    }

    /**
     * Return Benchmarks for a game. Processes all algorithms with all result
     * types. Abstracts from time settings.
     * 
     * @param problem
     *            The problem to benchmark.
     * @param gameName
     *            The name of the game.
     */
    public static <T> Collection<IRunnableBenchmark<T>> gameBenchmarks(
            final GamesModule<T> problem, final String gameName) {
        return gameBenchmarks(problem, gameName,
                Arguments.getBenchmarkTime().orElse(-1l),
                Arguments.getBenchmarkTime().orElse(-1l));
    }
}
