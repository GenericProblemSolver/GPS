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
package gps.games.wrapper;

/**
 * Represents a heuristic for singleplayer games. Heuristics may be used by
 * algorithms.
 *
 * @author haker@uni-bremen.de
 */
public interface ISingleplayerHeuristic extends IHeuristic {

    /**
     * Evaluate the state of the game.
     *
     * @param pGame
     *         The state that should be evaluated.
     *
     * @return The higher it is the better is the state.
     */
    double eval(Game<?> pGame);

}
