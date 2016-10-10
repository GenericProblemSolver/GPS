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

import gps.attribute.AttributeGraph;
import gps.games.wrapper.Game;
import gps.games.wrapper.ISingleplayerHeuristic;

/**
 * A heuristic that uses the hamming distance as heuristic. The less differences
 * exist the higher is the value returned by the heuristic. The amount of
 * differences is calculated by all primitives that are not equal.
 * 
 * TODO tobi implement this properly with kryo
 * 
 * @author haker@uni-bremen.de
 */
public class HammingHeuristic implements ISingleplayerHeuristic {

    /**
     * This experimental function constructs a heuristic by solving the terminal
     * state with depth first search. Then another algorithm can be used to
     * improve the result using the heuristic.
     * 
     * @return Heuristic.
     */
    public static <T> ISingleplayerHeuristic createUsingDepthFirstSearch(
            Game<T> game) {
        // final DepthFirst<T> dfs = new DepthFirst<T>(new GamesModule<>(game));
        // TODO tobi
        return new HammingHeuristic(null);
    }

    /**
     * Construct a DeltaHeuristic.
     * 
     * @param pGame
     *            The AttrubuteGraph of the game in a terminal state.
     */
    private HammingHeuristic(AttributeGraph pGame) {
        // TODO tobi
    }

    @Override
    public double eval(Game<?> pGame) {
        // TODO tobi 
        return 0;
    }
}
