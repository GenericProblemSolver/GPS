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

import gps.games.MemorySavingMode;
import gps.games.wrapper.Action;
import gps.games.wrapper.Game;
import gps.games.wrapper.ISingleplayerHeuristic;
import gps.games.wrapper.successor.GameHeuristicComparator;
import gps.games.wrapper.successor.INode;
import gps.games.wrapper.successor.LinkedNode;
import gps.util.Tuple;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * This class is an extension of {@link NMCS}. It overrides
 * {@link #constructRandomSampleElements(INode, HashSet)} and
 * {@link #selectSamplingAction(Game)}. The methods now use weighted
 * probabilities to prioritize game states with a higher heuristic value.
 * 
 * @author jschloet@tzi.de
 *
 * @param <T>
 */
public class HeuristicNMCS<T> extends NMCS<T> {

    /**
     * This variable is used to select an action during the sampling phase. Each
     * action gets a score that consists of a random value and a heuristic
     * value. This variable is the maximum random value an action can score.
     */
    static int randomScoreCap = 100;

    /**
     * This variable is used to select an action during the sampling phase. Each
     * Action gets a score that consists of a random value and a heuristic
     * value. This variable is the maximum heuristic value an action can score.
     */
    static int heuristicScoreCap = 150;

    /**
     * Heuristic to be used in
     * {@link #constructRandomSampleElements(INode, HashSet)}.
     */
    private ISingleplayerHeuristic heuristic;

    /**
     * Calls the super constructor with the given arguments.
     *
     * @param pGame
     *            Game to be solved
     * @param data
     *            Data structure to be used
     * @param pHeuristic
     *            Heuristic to be used
     * @param pMemorySavingMode
     *            The memory saving mode that is used when a copy of the game
     *            state is created.
     * 
     *            TODO: sollte statt game hier nicht das gamesmodule übergeben
     *            werden?
     */
    public HeuristicNMCS(Game<T> pGame, INMCSData<T> data,
            ISingleplayerHeuristic pHeuristic,
            MemorySavingMode pMemorySavingMode) {
        super(pGame, data, pMemorySavingMode);
        heuristic = pHeuristic;
    }

    /**
     * Determines in which order the actions that are executable in the game
     * state of the given {@link INode} are handled during the sampling phase.
     * Orders the successors according to the values calculated by
     * {@link #calculateProbabilityValue(INode, List)}.
     * 
     */
    @Override
    List<Tuple<INode<T>, INode<T>>> constructRandomSampleElements(
            INode<T> currentElement, HashSet<Game<T>> visited) {
        if (heuristicScoreCap == 0) {
            return super.constructRandomSampleElements(currentElement, visited);
        }
        List<INode<T>> successors = currentElement
                .getSuccessors(super.memorySavingMode).stream()
                .filter(s -> !visited.contains(s.getGame()))
                .collect(Collectors.toList());
        successors.sort(new GameHeuristicComparator(heuristic));
        List<INode<T>> randomSampleElements = new ArrayList<>(successors);
        randomSampleElements.sort((o1, o2) -> Double.compare(
                calculateProbabilityValue(o1, successors),
                calculateProbabilityValue(o2, successors)));
        seenNodes += randomSampleElements.size();
        return randomSampleElements.stream()
                .map(s -> new Tuple<>(s, currentElement))
                .collect(Collectors.toList());
    }

    /**
     * Selects the next action to execute during the sampling phase. Uses
     * {@link #constructRandomSampleElements(INode, HashSet)} to determine an
     * order for all possible actions and returns the first of them.
     */
    @Override
    Action selectSamplingAction(Game<T> currentGameState) {
        return constructRandomSampleElements(new LinkedNode<>(currentGameState),
                new HashSet<>()).get(0).getX().getAction();
    }

    /**
     * Calculates the score of the given successor. The score is used in
     * {@link #constructRandomSampleElements(INode, HashSet)} to determine the
     * order in which the successors are handled during the sampling phase.
     * 
     * The successor gets a random score capped by {@link #randomScoreCap} and
     * an heuristic score capped by {@link #heuristicScoreCap} that is dependent
     * of the successors position in the heuristic order (The successor with the
     * highest position {@link INode} the heuristic order gets the full
     * {@link #heuristicScoreCap}, the second highest gets half of
     * {@link #heuristicScoreCap}, the third gets a third of
     * {@link #heuristicScoreCap} ...).
     * 
     * @param successor
     *            The successor which score is to be determined
     * @param heuristicOrder
     *            A List of successors ordered by their heuristic value
     * @return The score for the given successor
     */
    private int calculateProbabilityValue(INode<T> successor,
            List<INode<T>> heuristicOrder) {
        Random random = new Random();
        int randomScore = random.nextInt(randomScoreCap);
        int heuristicScore = heuristicScoreCap
                / (heuristicOrder.indexOf(successor) + 1);
        return randomScore + heuristicScore;
    }
}
