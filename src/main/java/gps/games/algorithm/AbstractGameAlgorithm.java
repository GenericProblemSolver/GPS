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
package gps.games.algorithm;

import gps.common.AbstractAlgorithm;
import gps.common.BenchmarkField;
import gps.games.GamesModule;
import gps.games.IGameResult;
import gps.games.MemorySavingMode;
import gps.games.algorithm.analysis.IGameAnalysisResult;
import gps.games.wrapper.Action;

import java.util.List;
import java.util.Optional;

/**
 * All game algorithms must extend this class.
 *
 * @param <T>
 *            The type of the original problem class
 *
 * @author haker@uni-bremen.de , jschloet@tzi.de
 */
public abstract class AbstractGameAlgorithm<T> extends AbstractAlgorithm
        implements IGameResult<T> {

    /**
     * The module that instantiated the algorithm.
     */
    protected final GamesModule<T> module;

    /**
     * Construct a new algorithm. Algorithms that are not abstract are
     * considered to be expedient for solving problems.
     *
     * @param pModule
     *            The module that instantiated this algorithm
     * @param usedFields
     *            Benchmark Data that is generated by this algorithm.
     */
    public AbstractGameAlgorithm(final GamesModule<T> pModule,
            BenchmarkField... usedFields) {
        super(usedFields);
        if (pModule == null) {
            throw new NullPointerException("pModule must not be null");
        }
        module = pModule;
    }

    @Override
    public boolean isFinished() {
        return true;
    }

    /**
     * Retrieves a move sequence to solve the given game. Per default returns an
     * empty Optional. Can be overridden by algorithms that can determined the
     * requested result type.
     *
     * @return The move sequence.
     */
    @Override
    public Optional<List<Action>> moves() {
        return Optional.empty();
    }

    /**
     * Retrieves the state sequence that has been passed to solve the given
     * game.Can be overridden by algorithms that can determined the requested
     * result type.
     *
     * @return The state sequence
     */
    @Override
    public Optional<List<T>> stateSequence() {
        return Optional.empty();
    }

    /**
     * Retrieve the state of the game when it has been won. Can be overridden by
     * algorithms that can determined the requested result type.
     *
     * @return The terminal state.
     */
    @Override
    public Optional<T> terminalState() {
        return Optional.empty();
    }

    /**
     * Retrieve the best move to perform in order to win the game. Can be
     * overridden by algorithms that can determined the requested result type.
     *
     * @return The best move.
     */
    @Override
    public Optional<Action> bestMove() {
        return Optional.empty();
    }

    /**
     * Checks whether the game is actually winnable. Can be overridden by
     * algorithms that can determined the requested result type.
     *
     * @return {@code true} if winnable {@code false} otherwise.
     */
    @Override
    public Optional<Boolean> isWinnable() {
        return Optional.empty();
    }

    /**
     * Default implementation of {@link IGameResult#gameAnalysis()}.
     *
     * @return The result of the analysis phase.
     */
    @Override
    public Optional<IGameAnalysisResult> gameAnalysis() {
        return Optional.empty();
    }

    /**
     * Checks which memory saving mode is enabled.
     * 
     * @return The memory saving mode.
     */
    protected MemorySavingMode getMemorySavingMode() {
        return module.getMemorySavingMode();
    }

}
