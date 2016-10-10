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

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import gps.games.GamesModule;
import gps.games.algorithm.heuristic.HeuristicUtility;
import gps.games.algorithm.singleplayer.common.AbstractSingleplayerPathAlgorithm;
import gps.games.algorithm.singleplayer.common.datastruct.ToEvalSortedList;
import gps.games.algorithm.singleplayer.common.datastruct.ToEvalTree;
import gps.games.algorithm.singleplayer.interfaces.IToEvaluate;
import gps.games.wrapper.ISingleplayerHeuristic;

/**
 * Implementation of the A-Star Algorithm. An Algorithm which can utilize a
 * heuristic to improve graph traversal. The data is sorted by a tree or a list
 * in O(log n). The O(log n) for the list is achieved by using binary search.
 *
 * @author haker@uni-bremen.de
 *
 * @param <T>
 *            The type of the problem class
 */
public class AStar<T> extends AbstractSingleplayerPathAlgorithm<T> {

    /**
     * Construct a new A-Star algorithm. This algorithm does feature
     * {@link #getOptions()} so it cannot be run immediately. Use the alternate
     * constructor {@link #AStar(GamesModule, ISingleplayerHeuristic, Class)} to
     * instantiate a runnable algorithm.
     * 
     * @param pModule
     *            The games module that has been used to construct this object.
     */
    public AStar(GamesModule<T> pModule) {
        super(pModule);
        name = "A-Star";
    }

    /**
     * The name of the algorithm.
     */
    private final String name;

    /**
     * Additional constructor for algorithm options.
     *
     * @param pModule
     *            The games module that has been used to construct this object.
     * @param pHeuristic
     *            The heuristic to use.
     * @param pToEvalClass
     *            The class that is used for the open set.
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public AStar(GamesModule<T> pModule, ISingleplayerHeuristic pHeuristic,
            Class<? extends IToEvaluate> pToEvalClass)
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        super(pModule,
                pToEvalClass
                        .getDeclaredConstructor(ISingleplayerHeuristic.class)
                        .newInstance(pHeuristic));
        name = "A-Star (" + pHeuristic.getClass().getSimpleName() + ","
                + pToEvalClass.getSimpleName() + ")";
    }

    /**
     * Provide a heuristic and an open set variant.
     * <br />
     * {@inheritDoc}
     */
    @Override
    public List<?>[] getOptions() {
        return new List<?>[] {
                HeuristicUtility.getAllSingleplayerHeuristics(module.getGame()),
                Arrays.asList(ToEvalTree.class, ToEvalSortedList.class) };

    }

    @Override
    public String getName() {
        return name;
    }
}
