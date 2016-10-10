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
 * A transposition table entry which stores lower and upper bound minimax
 * values.
 *
 * @author alueck@uni-bremen.de
 */
class BoundsTTEntry extends AbstractTTEntry {

    /**
     * The lower bound minimax value.
     */
    Number lowerBound;

    /**
     * The upper bound minimax value.
     */
    Number upperBound;

    /**
     * Creates a new HashEntry containing an upper and lower bound of the
     * minimax value.
     *
     * @param pHashCode
     *         hashcode of the evaluated game state
     * @param pDepth
     *         depth at which the game state was evaluated, must not be less
     *         than 0
     * @param pLowerBound
     *         lower bound of the minimax value of the game state, must not be
     *         {@code null} and must be of the same class as pUpperBound
     * @param pUpperBound
     *         upper Bound of the minimax value of the game state, must not be
     *         {@code null} and must  be of the same class as pLowerBound
     */
    BoundsTTEntry(final int pHashCode, final int pDepth,
            final Number pLowerBound, final Number pUpperBound) {
        super(pHashCode, pDepth);
        if (pLowerBound == null || pUpperBound == null
                || !pLowerBound.getClass().equals(pUpperBound.getClass())) {
            throw new IllegalArgumentException();
        }
        lowerBound = pLowerBound;
        upperBound = pUpperBound;
    }

    /**
     * {@inheritDoc}
     *
     * @param pEntry
     *         entry used to override field values, must be of clas {@link
     *         BoundsTTEntry}
     */
    @Override
    void update(final AbstractTTEntry pEntry) {
        if (!(pEntry instanceof BoundsTTEntry)) {
            throw new IllegalArgumentException(
                    "pEntry must be of class BoundsTTEntry");
        }
        super.update(pEntry);
        lowerBound = ((BoundsTTEntry) pEntry).lowerBound;
        upperBound = ((BoundsTTEntry) pEntry).upperBound;
    }

}
