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
package gps.games.algorithm.singleplayer;

import gps.games.algorithm.singleplayer.common.AbstractSingleplayerPathAlgorithm;
import gps.games.GamesModule;
import gps.games.algorithm.singleplayer.common.datastruct.ToEvalLIFO;

/**
 * Implements the depth first search algorithm.
 * 
 * @author haker@uni-bremen.de
 *
 * @param <T>
 *            The type of the problem class
 */
public class DepthFirst<T> extends AbstractSingleplayerPathAlgorithm<T> {

    /**
     * Construct a depth first algorithm for a given GamesModule. This
     * algorithm does not feature {@link #getOptions()} so it can run
     * immediately.
     * 
     * @param pModule
     *            The module.
     */
    public DepthFirst(GamesModule<T> pModule) {
        super(pModule, new ToEvalLIFO<>());
    }

    @Override
    public String getName() {
        return "Depth-First";
    }

}
