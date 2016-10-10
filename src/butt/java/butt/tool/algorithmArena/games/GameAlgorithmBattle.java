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
package butt.tool.algorithmArena.games;

import gps.games.GamesModule;
import gps.games.algorithm.AbstractGameAlgorithm;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;
import gps.games.wrapper.Player;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * This class allows let multiple game algorithms play against each other.
 *
 * @author alueck@uni-bremen.de
 * @author haker@uni-bremen.de
 */
public class GameAlgorithmBattle<T> {

    private final static Logger LOGGER = Logger
            .getLogger(GameAlgorithmBattle.class.getName());

    /**
     * The game which is played.
     */
    private Game<T> game;

    /**
     * A map of {@link Player} to {@link GameAlgorithmWithInfo}.
     */
    private final Map<Player, GameAlgorithmWithInfo> algorithms;

    /**
     * Creates a new {@link GameAlgorithmBattle} instance.
     *
     * @param pGame
     *         the game to be played
     * @param pAlgorithms
     *         a map of players to the used algorithm with additional info
     */
    public GameAlgorithmBattle(final Game<T> pGame,
            final Map<Player, GameAlgorithmWithInfo> pAlgorithms) {
        if (pGame == null || pAlgorithms == null) {
            throw new IllegalArgumentException();
        }
        game = pGame;
        algorithms = pAlgorithms;
    }

    /**
     * Starts the battle between the algorithms.
     */
    public void battle() {
        while (!game.isTerminal()) {
            final GameAlgorithmWithInfo algWithInfo = algorithms
                    .get(game.getPlayer());
            final GamesModule<T> mod = algWithInfo.getInstance(game);
            if (algWithInfo.getTimeLimit() > 0) {
                AlgorithmRunner<T> algRunner = new AlgorithmRunner<>(mod);
                Thread thread = new Thread(algRunner);
                long start;
                long end;
                try {
                    start = System.nanoTime();
                    thread.start();
                    int time = 0;
                    while (time < algWithInfo.getTimeLimit()
                            && !algRunner.finished) {
                        Thread.sleep(10);
                        time += 10;
                    }
                    thread.interrupt();
                    thread.join();
                    end = System.nanoTime();
                } catch (InterruptedException e) {
                    LOGGER.warning(
                            "InterruptedException has been thrown. Stopping "
                                    + "execution...");
                    break;
                }
                if (algRunner.bestMove == null) {
                    LOGGER.severe(String.format(
                            "Runner for %s returned null as best move.",
                            algRunner.alg.getClass().getSimpleName()));
                    break;
                }
                algWithInfo.getTimesPerRun().add(end - start);
                game.applyAction(algRunner.bestMove);
            } else { // algWithInfo.getTimeLimit() <= 0
                long start = System.nanoTime();
                Optional<Action> bestMove = mod.bestMove();
                long end = System.nanoTime();
                if (!bestMove.isPresent()) {
                    LOGGER.severe(String.format(
                            "Algorithm %s returned null as best move.",
                            algWithInfo.getAlgorithmClass().getSimpleName()));
                    break;
                }
                algWithInfo.getTimesPerRun().add(end - start);
                game.applyAction(bestMove.get());
            }
        }
        logResults();
    }

    private void logResults() {
        LOGGER.info(game.toString());
        LOGGER.info("Average time usages of the algorithms:");
        for (Entry<Player, GameAlgorithmWithInfo> entry : algorithms
                .entrySet()) {
            int result = 0;
            List<Long> timesPerRun = entry.getValue().getTimesPerRun();
            if (timesPerRun.size() == 0) {
                LOGGER.info(String.format(
                        "No time usages have been recorded for algorithm %s.",
                        entry.getValue().getAlgorithmClass().getSimpleName()));
                continue;
            }
            for (long l : timesPerRun) {
                result += l / 1000000;
            }
            result = result / timesPerRun.size();
            LOGGER.info(String.format(
                    "Algorithm %s used an average time of %d ms per run.",
                    entry.getValue().getAlgorithmClass().getSimpleName(),
                    result));
        }
    }

    /**
     * Runnable class for algorithms which should be time terminated.
     */
    private class AlgorithmRunner<E> implements Runnable {

        /**
         * Algorithm to be run.
         */
        private final GamesModule<E> alg;

        /**
         * Best move the execution of the algorithm returned.
         */
        private Action bestMove;

        /**
         * True if the run method was completely executed, false otherwise.
         */
        private boolean finished;

        /**
         * Constructs a new AlgorithmRunner with the given algorithm.
         *
         * @param pAlg
         *         the algorithm for which to create this runnner
         */
        AlgorithmRunner(final GamesModule<E> pAlg) {
            alg = pAlg;
        }

        /**
         * Executes {@link AbstractGameAlgorithm#bestMove()} and saves the
         * result in {@link #bestMove}.
         */
        @Override
        public void run() {
            Optional<Action> best = alg.bestMove();
            finished = true;
            bestMove = best.isPresent() ? best.get() : null;
        }
    }

}
