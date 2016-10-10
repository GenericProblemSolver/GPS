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
package gps.games.util.transpositionTable;

/**
 * A abstract transposition table entry.
 *
 * @author alueck@uni-bremen.de
 */
abstract class AbstractTTEntry {

    /**
     * Hash code of the game state object to which this HashEntry belongs.
     */
    final int hashCode;

    /**
     * Depth at which the game state with {@link #hashCode} was evaluated.
     */
    int depth;

    AbstractTTEntry(final int pHashCode, final int pDepth) {
        if (pDepth < 0) {
            throw new IllegalArgumentException();
        }
        depth = pDepth;
        hashCode = pHashCode;
    }

    /**
     * Overrides all field values (but {@link #hashCode}) of this {@link
     * AbstractTTEntry} with the values of the given {@link AbstractTTEntry}.
     *
     * @param pEntry
     *         entry used to override field values
     */
    void update(final AbstractTTEntry pEntry) {
        depth = pEntry.depth;
    }
}
