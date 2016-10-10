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

import java.util.Arrays;
import java.util.List;

import gps.games.wrapper.Game;
import gps.games.wrapper.IHeuristicPlayer;
import gps.games.wrapper.ISingleplayerHeuristic;

/**
 * Heuristic utility. Provides access to all heuristics.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class HeuristicUtility {
    /**
     * Instantiate all available singleplayer heuristics
     * 
     * @param pStartState
     *            The start state.
     * @return The heuristic.
     * 
     */
    public static List<? extends ISingleplayerHeuristic> getAllSingleplayerHeuristics(
            final Game<?> pStartState) {
        // TODO tobi load with reflections
        return Arrays.asList(pStartState.getUserHeuristic(),
                DeltaHeuristic.createUsingDepthFirstSearch(pStartState),
                NoHeuristic.instance());
    }

    /**
     * Instantiate all available multiplayer heuristics
     * 
     * @param pStartState
     *            The start state.
     * @return The heuristic.
     * 
     */
    public static List<? extends IHeuristicPlayer> getAllMultiplayerHeuristics(
            final Game<?> pStartState) {
        // TODO tobi load with reflections
        return Arrays.asList(pStartState.getUserHeuristicMultiplayer());
    }

}
