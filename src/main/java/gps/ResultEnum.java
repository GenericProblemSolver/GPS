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
package gps;

/**
 * An Enum of all the result types the GPS can solve.
 *
 */
public enum ResultEnum {
    /**
     * A move sequence that lets you win the game
     */
    MOVES,

    /**
     * A state sequence that ends with the winning state
     */
    STATE_SEQ,

    /**
     * A terminal state
     */
    TERMINAL,

    /**
     * The best move to perform in order to win
     */
    BEST_MOVE,

    /**
     * The game analysis data
     */
    GAME_ANALYSIS,

    /**
     * Checks whether the game is winnable
     */
    WINNABLE,

    /**
     * The best found solution that maximizes the function
     */
    MAXIMIZED,

    /**
     * The best found solution that minimizes the function
     */
    MINIMIZED,

    /**
     * A model that satisfies the constrains
     */
    SATISFYING_MODEL
}
