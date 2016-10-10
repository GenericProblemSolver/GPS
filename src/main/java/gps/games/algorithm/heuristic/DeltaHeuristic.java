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
package gps.games.algorithm.heuristic;

import java.util.function.Supplier;

import gps.games.GamesModule;
import gps.games.algorithm.singleplayer.DepthFirst;
import gps.games.wrapper.Game;
import gps.games.wrapper.ISingleplayerHeuristic;
import gps.util.KryoHelper;

/**
 * A heuristic that sums up all differences of all attributes. The less
 * differences exist the higer is the value returned by the heuristic.
 * 
 * @author haker@uni-bremen.de
 */
public class DeltaHeuristic implements ISingleplayerHeuristic {

    /**
     * The attributes of the terminal state
     */
    private byte[] terminalBytes;

    /**
     * The supplier that produces the terminal state. Is called once on first
     * evaluation.
     */
    private final Supplier<Object> terminalFunc;

    /**
     * This experimental function constructs a heuristic by solving the terminal
     * state with depth first search. Then another algorithm can be used to
     * improve the result using the heuristic.
     * 
     * @param game
     *            The starting state of the game.
     * 
     * @return The Heuristic.
     */
    public static <T> ISingleplayerHeuristic createUsingDepthFirstSearch(
            Game<T> game) {
        final DepthFirst<T> dfs = new DepthFirst<T>(new GamesModule<>(game));
        return new DeltaHeuristic(() -> dfs.terminalState().orElse(null));
    }

    /**
     * Constructs a heuristic using the given terminal state. Another algorithm
     * can be used to improve the result using the heuristic.
     *
     * @param terminal
     *            A terminal state of the problem to solve
     * @param <T>
     *            The type of the problem to Solve
     * @return A delta heuristic for the problem to solve
     */
    public static <T> ISingleplayerHeuristic createUsingGivenTerminal(
            Game<T> terminal) {
        return new DeltaHeuristic(terminal::getProblem);
    }

    /**
     * Construct a DeltaHeuristic.
     * 
     * @param supplier
     *            The function that produces the terminal state (not as Game
     *            <?> but as ?)
     */
    private DeltaHeuristic(Supplier<Object> supplier) {
        terminalBytes = null;
        terminalFunc = supplier;
    }

    @Override
    public double eval(Game<?> pGame) {
        if (terminalBytes == null) {
            final Object terminalState = terminalFunc.get();
            if (terminalState == null) {
                terminalBytes = new byte[0];
            } else {
                terminalBytes = KryoHelper.objectToBytes(terminalState);
            }
        }

        byte[] bytes = KryoHelper.objectToBytes(pGame.getProblem());
        if (bytes.length != terminalBytes.length) {
            // TODO tobi find a nice way of handling this
            return Integer.MIN_VALUE;
        }

        int delta = Integer.MAX_VALUE;
        for (int i = 0; i < bytes.length; i++) {
            delta -= Math.abs(((int) bytes[i]) - ((int) terminalBytes[i]));
        }

        return delta;
    }
}
