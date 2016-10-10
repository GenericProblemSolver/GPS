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
package game.hanoi.assets;

/**
 * The Action Class for Hanoi. An action is the move of the upper disk of the
 * {@link #from} tower to the top of the {@link #to} tower.
 * 
 * @author haker@uni-bremen.de
 *
 */
public class Action {
    /**
     * The source tower from where to lift the topmost disk.
     */
    public final int from;

    /**
     * The destination tower to where to place the disk onto.
     */
    public final int to;

    /**
     * Construct a new Action Object.
     * 
     * @param pFrom
     *            The source tower from where to lift the topmost disk.
     * 
     * @param pTo
     *            The destination tower to where to place the disk onto.
     */
    public Action(final int pFrom, final int pTo) {
        from = pFrom;
        to = pTo;
    }

    @Override
    public String toString() {
        return "Move disk from " + from + " to " + to;
    }
}
