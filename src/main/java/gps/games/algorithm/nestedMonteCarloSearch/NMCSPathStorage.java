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

import gps.games.wrapper.Action;
import gps.games.wrapper.successor.INode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link INMCSData} that stores single paths.
 * So not every inserted node is stored. If used, basically the
 * standard variant of {@link NMCS} is executed.
 *
 * @author jschloet@tzi.de
 *
 * @param <T> The type of game that is to be stored
 */
public class NMCSPathStorage<T> implements INMCSData<T> {

    /**
     * Stores a path to a terminal state for every level
     * the associated {@link NMCS} visits. The stored paths
     * are propagated from lower levels to higher levels.
     * If a lower level finds a better path the higher level
     * paths are replaced.
     */
    private HashMap<Integer, List<INode<T>>> levelToPath;

    /**
     * Stores a list of already visited {@link INode}s for every
     * level the associated {@link NMCS} visits.
     */
    private HashMap<Integer, List<INode<T>>> levelToExecuted;

    /**
     * List used to store the {@link INode}s of a sampling.
     */
    private List<INode<T>> currentList;

    /**
     * the starting level of the corresponding {@link NMCS}
     */
    private int startingLevel;

    /**
     * Constructor. Initialises {@link #levelToExecuted},{@link #levelToPath}
     * and {@link #currentList}.
     */
    public NMCSPathStorage() {
        levelToPath = new HashMap<>();
        levelToExecuted = new HashMap<>();
        currentList = new ArrayList<>();
    }

    /**
     * Stores the given node in the currentList. If the depth of
     * the given node is inconsistent with the depth of the nodes
     * in {@link #currentList} the currentList, inconsistent nodes
     * are removed from {@link #currentList}. Only handles node
     * inserted in level 0. Ignores other levels as other levels nodes
     * are adressed in {@link #finishedSample()}.
     *
     * @param node Node to be inserted
     * @param pred Predecessor of the given node
     * @param level Current level in {@link NMCS}
     */
    @Override
    public void insert(INode<T> pred, INode<T> node, int level) {
        if (level == 0) {
            if (!currentList.isEmpty()
                    && currentList.get(currentList.size() - 1)
                            .getDepth() >= node.getDepth()) {
                currentList = currentList.stream()
                        .filter(a -> a.getDepth() < node.getDepth())
                        .collect(Collectors.toList());
            }
            currentList.add(node);
        }
    }

    /**
     *
     *
     * @param path Path to be inserted
     * @param level Current level in {@link NMCS}
     */
    @Override
    public void insert(List<INode<T>> path, int level) {
        List<INode<T>> startingLevelPath = levelToPath.get(startingLevel);
        if (path != null && !path.isEmpty()) {
            if (startingLevelPath == null || startingLevelPath.isEmpty()) {
                levelToPath.put(startingLevel, path);
            } else if (NMCS.utilityAbstraction(path.size(),
                    path.get(path.size() - 1)) > NMCS.utilityAbstraction(
                            startingLevelPath.size(), startingLevelPath
                                    .get(startingLevelPath.size() - 1))) {
                levelToPath.put(startingLevel, path);
            }
        }
    }

    /**
     * Replaces the level 0 path in {@link #levelToExecuted} with the
     * {@link #currentList} if the {@link #currentList} contains a better
     * path. Adds higher levels executed {@link INode}s to the {@link #currentList}
     * to build legal paths. Clears the {@link #currentList}.
     */
    @Override
    public void finishedSample() {
        List<INode<T>> path = levelToPath.get(0);
        currentList.addAll(0, levelToExecuted.get(0));
        int i = 1;
        while (levelToExecuted.containsKey(i)) {
            currentList.addAll(0, levelToExecuted.get(i));
            i++;
        }
        if (path == null || NMCS.utilityAbstraction(currentList.size(),
                currentList.get(currentList.size() - 1)) > NMCS
                        .utilityAbstraction(path.size(),
                                path.get(path.size() - 1))) {
            levelToPath.put(0, new ArrayList<>(currentList));
        }
        for (Integer j : levelToExecuted.keySet()) {
            if (levelToPath.containsKey(j)) {
                if (NMCS.utilityAbstraction(currentList.size(), currentList
                        .get(currentList.size() - 1)) > NMCS.utilityAbstraction(
                                levelToPath.get(j).size(), levelToPath.get(j)
                                        .get(levelToPath.get(j).size() - 1))) {
                    levelToPath.put(j, new ArrayList<>(currentList));
                }
            } else {
                levelToPath.put(j, new ArrayList<>(currentList));
            }
        }
        currentList.clear();
    }

    /**
     * Removes the executed INodes and the current path of the given level.
     *
     * @param level Level of the finished {@link NMCS#nestedSearch(int, INode)}
     */
    @Override
    public void finishedSearch(int level) {
        levelToExecuted.remove(level);
        levelToPath.remove(level);
    }

    /**
     * Returns the path that is mapped to the given level in {@link #levelToPath}.
     *
     * @param level The level which path is wanted
     * @return Returns the path for the given level
     */
    private List<INode<T>> getPath(int level) {
        return levelToPath.getOrDefault(level, new ArrayList<>());
    }

    /**
     * If the given node contains a terminal state, the path
     * for the given level is returned. Otherwise the subpath
     * that is mapped to the given level in {@link #levelToExecuted}
     * is returned.
     *
     * @param goal The terminal of the wanted path
     * @param level The current level of {@link NMCS}
     * @return A path or subpath
     */
    @Override
    public List<INode<T>> getPathTo(INode<T> goal, int level) {
        if (goal.getGame().isTerminal()) {
            return new ArrayList<>(getPath(level));
        } else {
            int counter = level;
            List<INode<T>> path = new ArrayList<>();
            while (levelToExecuted.containsKey(counter)) {
                path.addAll(levelToExecuted.getOrDefault(counter,
                        new ArrayList<>()));
                counter++;
            }
            return path;
        }
    }

    /**
     * Return the path returned by {@link #getPathTo(INode, int)} as a {@link Action}
     * sequence.
     *
     * @param goal Terminal of the wanted {@link Action} sequence.
     * @param level The current level of {@link NMCS}
     * @return {@link Action} sequence
     */
    @Override
    public List<Action> getPathToAsActions(INode<T> goal, int level) {
        List<INode<T>> path = getPathTo(goal, level);
        return path.stream().filter(a -> a.getAction() != null)
                .map(INode::getAction).collect(Collectors.toList());
    }

    /**
     * Adds the given node to the list of {@link INode}s
     * stored in {@link #levelToExecuted} for the
     * given level.
     *
     * @param node The node to be inserted
     * @param level The current level of {@link NMCS}
     */
    @Override
    public void addExecuted(INode<T> node, int level) {
        if (!levelToExecuted.containsKey(level)) {
            levelToExecuted.put(level, new ArrayList<>());
        }
        levelToExecuted.get(level).add(node);
    }

    /**
     * Returns the cumulative number of Elements
     * stored in {@link #levelToExecuted} for all levels
     * higher then the given level.
     *
     * @param level The current level of {@link NMCS}
     * @param startingLevel The starting Level of {@link NMCS}
     * @return The calculated number of Elements
     */
    @Override
    public int numberExec(int level, int startingLevel) {
        int number = 0;
        int counter = level + 1;
        while (counter <= startingLevel) {
            number += levelToExecuted.getOrDefault(counter, new ArrayList<>())
                    .size();
            counter++;
        }
        return number;
    }

    /**
     * Clears the {@link #currentList}. Called if samples do not reach
     * a terminal state.
     */
    @Override
    public void clearSample() {
        currentList.clear();
    }

    @Override
    public void setStartingLevel(int level) {
        startingLevel = level;
    }

    @Override
    public void clearExec() {
        levelToExecuted.clear();
    }
}
