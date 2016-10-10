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
package optimization.binpacking;

/**
 * An object to put into the bin
 * @author bargen
 *
 */
public class BinObject {

    /**
     * The size of the object
     */
    private int size;

    public BinObject(final int pSize) {
        if (BinPackingProblem.BINSIZE < pSize) {
            throw new IllegalArgumentException(
                    "This BinObject will not fit inside any bin.");
        }
        size = pSize;
    }

    public int getSize() {
        return size;
    }

}
