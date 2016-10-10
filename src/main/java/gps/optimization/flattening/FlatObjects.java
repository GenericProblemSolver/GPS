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
package gps.optimization.flattening;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents "flattened" Objects and provices access to the underlying
 * FlatWrappers
 *
 * @author Steffen
 */
public class FlatObjects {

    /**
     * List of FlatWrappers representing "flattened" Objects
     */
    private final List<FlatWrapper> flatWrappers = new ArrayList<>();

    /**
     * Array of the root-Objects wrapped by this FlatObjects.
     */
    private final Object[] rootObjects;

    /**
     * Constructor, creates a FlatObjects with an empty underlying List of
     * FlatWrappers and the given root-Objects.
     */
    FlatObjects(final Object[] pRootObjects) {
        rootObjects = pRootObjects;
    }

    /**
     * Adds the given FlatWrapper to the List of FlatWrappers
     *
     * @param pFlatWrapper
     *            the Flatwrapper to be added
     */
    void add(final FlatWrapper pFlatWrapper) {
        flatWrappers.add(pFlatWrapper);
    }

    /**
     * Returns the List of FlatWrappers representing the "flattened" Objects
     *
     * @return the List of FlatWrappers representing the "flattened" Objects
     */
    public List<FlatWrapper> getFlatWrappers() {
        return flatWrappers;
    }

    /**
     * Returns the Array of root-Objects
     *
     * @return the Array of root-Objects
     */
    public Object[] getRootObjects() {
        return rootObjects;
    }

    /**
     * Sets the values held by the FlatWrappers to the Objects in the given array, in order,
     * The array must have the same size as the number of FlatWrappers, otherwise an IllegalArgumentException is thrown.
     * @param pValues Array containing the new values
     */
    public void setValues(final Object[] pValues) {
        if (pValues.length != flatWrappers.size()) {
            throw new IllegalArgumentException(
                    "The array size does not match the number of FlatWrappers.");
        }
        for (int x = 0; x < pValues.length; x++) {
            flatWrappers.get(x).setValues(pValues[x]);
        }
    }

    /**
     * Returns an Object-array holding the values held by the FlatWrappers in order.
     * @return an Object-array holding the values held by the FlatWrappers in order.
     */
    public Object[] getObjects() {
        Object[] result = new Object[flatWrappers.size()];
        for (int x = 0; x < result.length; x++) {
            result[x] = flatWrappers.get(x).getObjectRepresentation();
        }
        return result;
    }

}
