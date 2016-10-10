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

import java.util.List;
import java.util.Optional;

import gps.games.algorithm.analysis.IGameAnalysisResult;
import gps.games.wrapper.Action;

/**
 * The result types you can request for a given problem class.
 * 
 * @author haker@uni-bremen.de
 *
 * @param <T>
 *            The problem class type
 */
public interface IGameResult<T> {
    /**
     * Retrieves a move sequence to solve the given game.
     * 
     * @return The move sequence.
     */
    public Optional<List<Action>> moves();

    /**
     * Retrieves the state sequence that has been passed to solve the given
     * game.
     * 
     * @return The state sequence
     */
    public Optional<List<T>> stateSequence();

    /**
     * Retrieve the state of the game when it has been won.
     * 
     * @return The terminal state.
     */
    public Optional<T> terminalState();

    /**
     * Retrieve the best move to perform in order to win the game.
     * 
     * @return The best move.
     */
    public Optional<Action> bestMove();

    /**
     * Checks whether the game is actually winnable.
     * 
     * @return {@code true} if winnable {@code false} otherwise.
     */
    public Optional<Boolean> isWinnable();

    /**
     * Checks whether the algorithm that processed the previous requested result
     * type has been finished or whether it can continue to solve in order to
     * improve the result.
     * 
     * @return {@code true} if the result cannot be improved {@code false}
     *         otherwise.
     */
    public boolean isFinished();

    /**
     * Gets the analysis result that has been calculated by the analyzing phase
     * during the game analysis process.
     * 
     * @return A copy of the game analysis results.
     */
    public Optional<? extends IGameAnalysisResult> gameAnalysis();

}
