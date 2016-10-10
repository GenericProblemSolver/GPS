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

import gps.games.wrapper.Game;
import gps.games.wrapper.IHeuristicPlayer;
import gps.games.wrapper.ISingleplayerHeuristic;
import gps.games.wrapper.Player;

/**
 * A heuristic that evaluates nodes always with 0. Thus implementing no
 * heuristic at all.
 * 
 * @author haker@uni-bremen.de
 */
public class NoHeuristic implements ISingleplayerHeuristic, IHeuristicPlayer {

    private static NoHeuristic heur = new NoHeuristic();

    /**
     * Construct a {@link NoHeuristic}.
     * 
     * Singleton Class does not provide a constructor.
     * 
     */
    private NoHeuristic() {

    }

    @Override
    public double eval(Game<?> pGame) {
        return 0;
    }

    @Override
    public double eval(Game<?> pGame, Player pPlayer) {
        return 0;
    }

    /**
     * Construct a {@link #NoHeuristic()}.
     * 
     * @return The NoHeuristic.
     */
    public static NoHeuristic instance() {
        return heur;
    }
}
