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

import gps.IWrappedProblem;
import gps.games.MemorySavingMode;
import gps.games.wrapper.Action;
import gps.games.wrapper.Player;
import gps.games.wrapper.successor.INode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Formulates a search problem that can be used by a {@link AbstractRecycler} to improve
 * existing solutions for a problem of the given type.
 *
 * Gets two {@link INode}s via the constructor and formulates a search problem
 * which solution is a path from one {@link INode} to the other.
 *
 * @author jschloet@tzi.de
 * @param <T> Type of the search problem
 */
// I implement IWrappedProblem because the recycler creates a lot of search problems and I
// do not need to call GPS.wrap this way.
class RecyclingSearchProblem<T>
        implements IWrappedProblem<RecyclingSearchProblem<T>> {

    /**
     * The current state of the search problem. Gets refreshed by
     * {@link #applyAction(Action)}.
     */
    private INode<T> current;

    /**
     * The goal state of this search problem. Used in the {@link #isTerminal()}
     * method to be compared with the {@link #current} state. If those states are
     * equal, the search problem ended.
     */
    private INode<T> goal;

    /**
     * Constructor. Sets {@link #current} and goal with the given values.
     *
     * @param start The starting point of this search problem
     * @param pGoal The goal state of this search problem
     */
    RecyclingSearchProblem(INode<T> start, INode<T> pGoal) {
        current = start;
        goal = pGoal;
    }

    /**
     * Returns the {@link #current} node.
     *
     * @return The {@link #current} node.
      */
    INode<T> getCurrent() {
        return current;
    }

    @Override
    public void applyAction(Action pAction) {
        current = current.getSuccessor((Action) pAction.get(),
                MemorySavingMode.NONE);
    }

    @Override
    public boolean hasApplyActionMethod() {
        return true;
    }

    @Override
    public List<Runnable> getRunnableMoves() {
        return new ArrayList<>();
    }

    @Override
    public List<Action> getActions() {
        return current.getAvailableActions().stream().map(Action::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasActionMethod() {
        return true;
    }

    @Override
    public boolean isTerminal() {
        return current.getGame().equals(goal.getGame());
    }

    @Override
    public boolean hasTerminalMethod() {
        return true;
    }

    @Override
    public Number heuristic() {
        return null;
    }

    @Override
    public Number heuristic(Player pPlayer) {
        return null;
    }

    @Override
    public boolean hasHeuristicMethod() {
        return false;
    }

    @Override
    public boolean hasHeuristicPlayerMethod() {
        return false;
    }

    @Override
    public Player getPlayer() {
        return null;
    }

    @Override
    public boolean hasPlayerMethod() {
        return false;
    }

    @Override
    public Number getUtility() {
        return null;
    }

    @Override
    public Number getUtility(Player pPlayer) {
        return null;
    }

    @Override
    public boolean hasUtilityMethod() {
        return false;
    }

    @Override
    public boolean hasUtilityPlayerMethod() {
        return false;
    }

    @Override
    public RecyclingSearchProblem<T> getSource() {
        return this;
    }

    @Override
    public Object[] getAttributes() {
        return new Object[] { current };
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setAttribute(int index, Object val) {
        if (index == 0) {
            current = (INode) val;
        }
    }

    @Override
    public boolean isAttributeFinal(int index) {
        return false;
    }

    @Override
    public boolean hasObjectiveFunction() {
        return false;
    }

    @Override
    public double objectiveFunction(Object[] params) {
        return 0;
    }

    @Override
    public Object[] getDefaultParams() {
        return new Object[0];
    }

    @Override
    public List<Object[]> neighbor(Object[] params) {
        return null;
    }

    @Override
    public boolean hasNeighborFunction() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RecyclingSearchProblem
                && ((RecyclingSearchProblem<?>) other).getCurrent().getGame()
                        .equals(current.getGame());
    }

    @Override
    public int hashCode() {
        return current.getGame().hashCode();
    }

    @Override
    public void setThresholdForObjectiveFunction(double pThresh) {

    }

    @Override
    public boolean canBeGreaterThan() {
        return false;
    }

    @Override
    public void setMaximize(byte pMax) {

    }
}
