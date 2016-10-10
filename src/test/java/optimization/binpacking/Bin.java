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

import java.util.ArrayList;
import java.util.List;

/**
 * An bin to put objects into
 * @author bargen
 *
 */
public class Bin {

    /**
     * The size of the bin
     */
    private int size;

    /**
     * All the objects
     */
    private List<BinObject> objects = new ArrayList<>();

    @SuppressWarnings("unused")
    public Bin() {
        if (BinPackingProblem.BINSIZE <= 0) {
            throw new IllegalArgumentException(
                    "The size should not be smaller or equal to zero");
        }
        size = BinPackingProblem.BINSIZE;
    }

    public void addObject(final BinObject pObject) {
        objects.add(pObject);
    }

    public void removeObject(final BinObject pObject) {
        objects.remove(pObject);
    }

    /**
     * Returns the empty space in the bin
     */
    public int getEmptySpaceSize() {
        int takenSpace = 0;
        for (BinObject b : objects) {
            takenSpace += b.getSize();
        }
        return size - takenSpace;
    }

    public boolean isOverFull() {
        return getEmptySpaceSize() < 0;
    }

}
