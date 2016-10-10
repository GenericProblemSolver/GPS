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
package gps.games.wrapper;

import java.util.Objects;

/**
 * Wrapper class for actions.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class Action extends SimpleWrapper<Object> {

    /**
     * wrap an object into Action.
     * 
     * @param pAction
     *            The action.
     */
    public Action(Object pAction) {
        super(pAction);
        move = -1;
    }

    /**
     * The index of the move.
     */
    private final int move;

    /**
     * Construct an Action as a runnable action. Runnable actions are processed
     * separately from the other actions in the {@link gps.games.wrapper.Game}
     * class.
     * 
     * @param pMove
     *            The index of the move.
     * @param pMethodName
     *            An optional name of the move.
     */
    public Action(int pMove, String pMethodName) {
        super(null);
        move = pMove;
    }

    /**
     * Checks whether this is a direct move. A direct move is applied directly
     * by calling a method of the problem class.
     * 
     * @return {@code true} if it is a direct move {@code false} otherwise.
     */
    public boolean isDirectMove() {
        return move >= 0;
    }

    /**
     * Gets the index of the move that has been specified in the
     * {@link #Action(int, String)} constructor.
     * 
     * @return the move index.
     */
    public int getMoveMethodIdx() {
        return move;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }

        final Action o;
        if (!(other instanceof Action)) {
            return false;
        }
        o = (Action) other;

        return move == o.move && super.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), move);
    }

    @Override
    public String toString() {
        return isDirectMove() ? ("Direct move method " + move)
                : get().toString();
    }
}
