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
package gps.games.algorithm.nestedMonteCarloSearch;

import java.util.Arrays;
import java.util.List;

/**
 * Enum representing the different ways of including a give {@link gps.games.wrapper.ISingleplayerHeuristic}
 * in {@link IterativeNMCS}.
 *
 * @author jschloet@tzi.de
 */
public enum HeuristicUsage {

    /**
     * This value represents a dynamic way of including a given {@link gps.games.wrapper.ISingleplayerHeuristic}
     * in {@link IterativeNMCS}. If this value is passed to {@link IterativeNMCS}s constructor {@link DynamicHeuristicNMCS}
     * is used to include the {@link gps.games.wrapper.ISingleplayerHeuristic}.
     */
    DYNAMIC,

    /**
     * This value represents a constant way of including a given {@link gps.games.wrapper.ISingleplayerHeuristic} in
     * {@link IterativeNMCS}. If this value is passed to {@link IterativeNMCS}s constructor {@link HeuristicNMCS} is
     * used to include the {@link gps.games.wrapper.ISingleplayerHeuristic}.
     */
    CONSTANT,

    /**
     * This value represents, that no {@link gps.games.wrapper.ISingleplayerHeuristic} is used in {@link IterativeNMCS}
     *  If this value is passed to {@link IterativeNMCS}s constructor {@link NMCS} is used.
     */
    NONE;

    /**
     * Returns a list containing all values of {@link HeuristicUsage}
     *
     * @return A list containing all values of {@link HeuristicUsage}
     */
    public static List<HeuristicUsage> valuesAsList() {
        return Arrays.asList(values());
    }
}
