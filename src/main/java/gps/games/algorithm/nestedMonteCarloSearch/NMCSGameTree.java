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

import gps.games.util.GameTree;
import gps.games.wrapper.Action;
import gps.games.wrapper.successor.INode;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link INMCSData} that uses a {@link GameTree} to
 * store the inserted {@link INode}.
 *
 * @author jschloet@tzi.de
 *
 * @param <T> The type of game that is to be stored
 */
public class NMCSGameTree<T> implements INMCSData<T> {

    private GameTree<T> gameTree;

    /**
     * Remember the number of executed actions for every level.
     *
     */
    private HashMap<Integer, Integer> levelToExecuted = new HashMap<>();

    public NMCSGameTree(INode<T> root) {
        gameTree = new GameTree<>();
        gameTree.insertRoot(root);
    }

    @Override
    public void insert(INode<T> node, INode<T> pred, int level) {
        gameTree.insert(node, pred);
    }

    @Override
    public void insert(List<INode<T>> path, int level) {
        if (path != null && !path.isEmpty()) {
            INode<T> prev = path.get(0);
            path.remove(0);
            for (INode<T> succ : path) {
                gameTree.insert(prev, succ);
                prev = succ;
            }
        }
    }

    @Override
    public void finishedSample() {
        //Do Nothing
    }

    @Override
    public void finishedSearch(int level) {
        levelToExecuted.remove(level);
    }

    @Override
    public List<INode<T>> getPathTo(INode<T> goal, int level) {
        return gameTree.getPathTo(goal);
    }

    @Override
    public List<Action> getPathToAsActions(INode<T> goal, int level) {
        return gameTree.getPathToAsActions(goal);
    }

    @Override
    public void addExecuted(INode<T> node, int level) {
        if (levelToExecuted.containsKey(level)) {
            int prev = levelToExecuted.get(level);
            levelToExecuted.put(level, prev + 1);
        } else {
            levelToExecuted.put(level, 1);
        }
    }

    @Override
    public int numberExec(int level, int startingLevel) {
        final int[] counter = { 0 };
        levelToExecuted.keySet().stream().filter(a -> a > level)
                .map(a -> levelToExecuted.get(a)).collect(Collectors.toList())
                .forEach(a -> counter[0] = counter[0] + a);
        return counter[0];
    }

    @Override
    public void clearSample() {
        //Do Nothing
    }

    @Override
    public void setStartingLevel(int level) {
        //Do Nothing
    }

    @Override
    public void clearExec() {
        //Do Nothing
    }
}