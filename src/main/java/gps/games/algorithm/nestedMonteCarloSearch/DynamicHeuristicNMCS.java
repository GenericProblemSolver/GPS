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
import gps.games.wrapper.Game;
import gps.games.wrapper.ISingleplayerHeuristic;
import gps.games.wrapper.successor.INode;

import java.util.HashSet;

/**
 * {@inheritDoc}
 *
 * Extends {@link HeuristicNMCS} by decreasing the {@link #heuristicScoreCap} after a number
 * of samplings.
 *
 * @author jschloet@tzi.de
 * @param <T>
 *
 */
public class DynamicHeuristicNMCS<T> extends HeuristicNMCS<T> {

    /**
     * The lower bound for the {@link #heuristicScoreCap}. The {@link #heuristicScoreCap} can not have
     * a value lower than this.
     */
    private int minimumHeuristicCap;

    /**
     * This value determines how often the {@link #heuristicScoreCap} is decreased. It is decreased every
     * decreaseRate samplings.
     */
    private int decreaseRate;

    /**
     * Counter that is increased after every sampling.
     */
    private int counter;

    /**
     * After this percentage of the current path is simulated, the decrease of
     * the {@link #heuristicScoreCap} starts.
     */
    private double startingPointPercentage = 0.004;

    /**
     * Calls the @{@link HeuristicNMCS} constructor with the given {@link Game}.
     *
     * @param pGame Instance of {@link Game} that gets passed to the super constructor.
     */
    public DynamicHeuristicNMCS(Game<T> pGame, INMCSData<T> data,
            ISingleplayerHeuristic heuristic,
            MemorySavingMode pMemorySavingMode) {
        super(pGame, data, heuristic, pMemorySavingMode);
        minimumHeuristicCap = 0;
        decreaseRate = 1;
        counter = 1;
        heuristicScoreCap = 175;
    }

    /**
     * {@inheritDoc}
     *
     * Decreases {@link #heuristicScoreCap} by calling {@link #refreshHeuristicCap()}.
     *
     * @param currentElement The starting point of this sampling.
     * @param alreadyCalled Game states that have already been starting points of samplings or higher level calls
     * @param depthLimit The depth at which a sampling is aborted if a shortest path is wanted.
     * @return The terminal of the sample
     */
    @Override
    INode<T> sampleRandomDepthFirstSearch(INode<T> currentElement,
            HashSet<Game<T>> alreadyCalled, int depthLimit) {
        INode<T> terminal = super.sampleRandomDepthFirstSearch(currentElement,
                alreadyCalled, depthLimit);
        refreshHeuristicCap();
        return terminal;
    }

    /**
     * {@inheritDoc}
     *
     * Decreases {@link #heuristicScoreCap} by calling {@link #refreshHeuristicCap()}.
     *
     * @param currentNode the starting point of this sample
     *
     * @return The terminal state of this sample
     */
    @Override
    INode<T> sample(INode<T> currentNode) {
        INode<T> terminal = super.sample(currentNode);
        refreshHeuristicCap();
        return terminal;
    }

    /**
     * Decreases {@link #heuristicScoreCap} by one at which the lowest possible value of {@link #heuristicScoreCap}
     * is {@link #minimumHeuristicCap}. Increases {@link #counter} and only decreases {@link #heuristicScoreCap}
     * every {@link #decreaseRate} samplings.
     */
    private void refreshHeuristicCap() {
        counter++;
        if (getResult().getValue() != Double.NEGATIVE_INFINITY
                && counter > getResult().getSequence().size()
                        * startingPointPercentage
                && counter % decreaseRate == 0
                && heuristicScoreCap >= minimumHeuristicCap) {
            heuristicScoreCap--;
        }
    }
}