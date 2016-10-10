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
package gps.games.algorithm.recycling;

/**
 * Enum used to tell the {@link SearchRecycler} what {@link gps.games.algorithm.singleplayer.common.AbstractSingleplayerSearch} to use.
 *
 * @author jschloet@tzi.de
 */
public enum SearchRecyclerAlgorithm {

    /**
     * If this gets past to {@link SearchRecycler}, it uses {@link gps.games.algorithm.singleplayer.DepthFirst}
     */
    DEPTH_FIRST_SEARCH,

    /**
     * If this gets past to {@link SearchRecycler}, it uses {@link gps.games.algorithm.singleplayer.AStar}
     */
    ASTAR,

    /**
     * If this gets past to {@link SearchRecycler}, it uses {@link gps.games.algorithm.singleplayer.BreadthFirst}
     */
    BREADTH_FIRST_SEARCH,

    /**
     * If this gets past to {@link SearchRecycler}, it uses all the above algorithm alternating
     */
    ALL
}
