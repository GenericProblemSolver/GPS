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
package gps.games.algorithm.monteCarloTreeSearch;

/**
 * Enumerates the different termination Modes usable for Monte-Carlo-Tree-Search. 
 * The termination mode specifies according to what termination condition the Monte-Carlo-
 * Tree-Search algorithm terminates.
 * 
 * @author jschloet@tzi.de
 */
public enum MCTSTerminationMode {

    /**
     * This termination mode signals that the corresponding  
     * algorithm has to stop after a specified amount of time.
     */
    TIMELIMIT,

    /**
     * This termination mode signals that the corresponding
     * algorithm has to stop after a specified amount of repetitions.
     */
    REPETITIONLIMIT,

    /**
     * This termination mode signals that the corresponding algorithm
     * has to stop after {@link Thread#interrupt()} is called.
     */
    INTERRUPT
}