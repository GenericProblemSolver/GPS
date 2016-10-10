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
 * A transposition table entry which stores an exact minimax value.
 *
 * @author alueck@uni-bremen.de
 */
class ExactTTEntry extends AbstractTTEntry {

    Number exactValue;

    /**
     * Creates a new HashEntry with an exact minimax value.
     *
     * @param pHashCode
     *         hash code of the evaluated game state
     * @param pDepth
     *         depth at which the game state was evaluated
     * @param pExactValue
     *         exact minimax value
     */
    ExactTTEntry(final int pHashCode, final int pDepth,
            final Number pExactValue) {
        super(pHashCode, pDepth);
        if (pExactValue == null) {
            throw new IllegalArgumentException();
        }
        exactValue = pExactValue;
    }

    /**
     * {@inheritDoc}
     *
     * @param pEntry
     *         entry used to override field values, must be of class {@link
     *         ExactTTEntry}
     */
    @Override
    void update(final AbstractTTEntry pEntry) {
        if (!(pEntry instanceof ExactTTEntry)) {
            throw new IllegalArgumentException(
                    "pEntry must be of class ExactTTEntry");
        }
        super.update(pEntry);
        exactValue = ((ExactTTEntry) pEntry).exactValue;
    }
}
