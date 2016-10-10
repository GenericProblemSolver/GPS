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

import gps.games.algorithm.singleplayer.common.AbstractSingleplayerSearch;
import gps.games.algorithm.singleplayer.common.datastruct.ToEvalLimitedLIFO;
import gps.games.GamesModule;

/**
 * Implements the breadth first search algorithm.
 * 
 * @author haker@uni-bremen.de
 *
 * @param <T>
 *            The type of the problem class
 */
public class IterativeDeepening<T> extends AbstractSingleplayerSearch<T> {

    public IterativeDeepening(GamesModule<T> pModule) {
        super(pModule, new ToEvalLimitedLIFO<>());
    }

    @Override
    public String getName() {
        return "Iterative Deepening Search";
    }

}
