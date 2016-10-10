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

import gps.common.AlgorithmUtility;
import gps.games.GamesModule;
import gps.games.algorithm.AbstractGameAlgorithm;
import gps.games.wrapper.Game;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains a game algorithm and additional information used by
 * {@link GameAlgorithmBattle}.
 *
 * @author alueck@uni-bremen.de
 * @author haker@uni-bremen.de
 */
public class GameAlgorithmWithInfo {

    /**
     * The game algorithm class
     */
    @SuppressWarnings("rawtypes")
    private final Class<? extends AbstractGameAlgorithm> algorithm;

    /**
     * The options used for the algorithm
     */
    private final Object options[];

    /**
     * Time limit in milliseconds for the algorithm. A non positive number
     * represents no time limit.
     */
    private final int timeLimit;

    /**
     * This list contains the time used for every run of
     * {@link AbstractGameAlgorithm#bestMove()} in nanoseconds.
     */
    private final List<Long> timesPerRun = new ArrayList<>();

    /**
     * The depthlimit for this algorithm. Non positive number represents no
     * limit.
     */
    private final int depthlimit;

    /**
     * Creates a new {@link GameAlgorithmWithInfo} instance.
     *
     * @param pAlg
     *            the algorithm
     * @param pOptions
     *            The options that are passed to the algorithm. May be
     *            {@code null} if no options should be passed.
     */
    @SuppressWarnings("rawtypes")
    public GameAlgorithmWithInfo(
            final Class<? extends AbstractGameAlgorithm> pAlg,
            final Object[] pOptions) {
        this(pAlg, pOptions, -1, -1);
    }

    /**
     * Creates a new {@link GameAlgorithmWithInfo} instance.
     * 
     * @param pAlg
     *            The algorithm.
     * @param pOptions
     *            The options that are passed to the algorithm. May be
     *            {@code null} if no options should be passed.
     * @param pTimeLimit
     *            The timelimit in milliseconds for the algorithm. If non
     *            positive no timelimit is set.
     * @param pDepthlimit
     *            The depthlimit for the algorithm. If non positive no limit is
     *            set.
     */
    @SuppressWarnings("rawtypes")
    public GameAlgorithmWithInfo(
            final Class<? extends AbstractGameAlgorithm> pAlg,
            final Object[] pOptions, final int pTimeLimit,
            final int pDepthlimit) {
        if (pAlg == null) {
            throw new IllegalArgumentException("algorithm must not be null");
        }
        algorithm = pAlg;
        options = pOptions;
        timeLimit = pTimeLimit;
        depthlimit = pDepthlimit;
    }

    public <T> GamesModule<T> getInstance(Game<T> pGame) {
        final GamesModule<T> mod = new GamesModule<T>(pGame);
        @SuppressWarnings("unchecked")
        AbstractGameAlgorithm<T> algo = AlgorithmUtility
                .instantiateAlgorithm(algorithm, new Object[] { mod }, options);
        mod.setAlgorithm(algo);
        if (depthlimit > 0) {
            mod.setDepthlimit(depthlimit);
        }
        return mod;
    }

    @SuppressWarnings("rawtypes")
    Class<? extends AbstractGameAlgorithm> getAlgorithmClass() {
        return algorithm;
    }

    int getTimeLimit() {
        return timeLimit;
    }

    List<Long> getTimesPerRun() {
        return timesPerRun;
    }
}
