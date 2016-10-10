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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

public class ObjectArrayFlatWrapper extends FlatWrapper {

    /**
     * Size of longest FlatWrappers in this ObjectArrayFlatWrapper
     */
    private int maxFlatWrappers;

    /**
     * Array of Flatobjects wrapping the values of the Objects in this
     * ObjectArrayFlatWrapper
     */
    private FlatObjects[] arrayFlatObjects;

    /**
     * Creates a new ObjectArrayFlatWrapper wrapping the given (Array-)Field,
     * Object and Class. If Field, Object or Class is null or the class is not
     * representing an Array an IllegalArgumentException is thrown.
     *
     * @param pWrappedField
     *            the to be wrapped Field
     * @param pWrappedObject
     *            the Object containing the Field
     * @param pWrappedClass
     *            the Class defining the Field
     * @param pLeaves
     *            additional leaves for the "Flattening"-Process of the Object
     *            array
     */
    public ObjectArrayFlatWrapper(final Field pWrappedField,
            final Object pWrappedObject, final Class<?> pWrappedClass,
            final Set<Class<?>> pLeaves) {
        super(pWrappedField, pWrappedObject, pWrappedClass);
        if (!pWrappedClass.isArray()
                || pWrappedClass.getComponentType().isPrimitive()) {
            throw new IllegalArgumentException(
                    "The wrapped class has to be an Object-array.");
        }
        maxFlatWrappers = 0;
        createArrayFlatObjects(pLeaves);
    }

    /**
     * Creates a new ObjectArrayFlatWrapper wrapping the given (Root-)Field,
     * Object and Class. If Field, Object or Class is null or the class is not
     * representing an Array an IllegalArgumentException is thrown.
     *
     * @param pWrappedField
     *            the to be wrapped Field
     * @param pWrappedObject
     *            the Object containing the Field
     * @param pWrappedClass
     *            the Class defining the Field
     * @param pRootIndex
     *            the root-Index of this Wrapper
     * @param pLeaves
     *            additional leaves for the "Flattening"-Process of the Object
     *            array
     */
    public ObjectArrayFlatWrapper(final Field pWrappedField,
            final Object pWrappedObject, final Class<?> pWrappedClass,
            final int pRootIndex, final Set<Class<?>> pLeaves) {
        super(pWrappedField, pWrappedObject, pWrappedClass, pRootIndex);
        if (!pWrappedClass.isArray()
                || pWrappedClass.getComponentType().isPrimitive()) {
            throw new IllegalArgumentException(
                    "The wrapped class has to be an Object-array.");
        }
        maxFlatWrappers = 0;
        createArrayFlatObjects(pLeaves);
    }

    /**
     * Creates the array of Flatobjects wrapping the values of the Objects in
     * this ObjectArrayFlatWrapper
     * 
     * @param pLeaves
     *            additional leaves for the "Flattening"-Process of the Object
     *            array
     */
    private void createArrayFlatObjects(final Set<Class<?>> pLeaves) {
        arrayFlatObjects = new FlatObjects[Array.getLength(super.get())];
        Flattener f = new Flattener();
        arrayFlatObjects = f.flattenAndWrapObjectArray(super.get(), pLeaves);
    }

    /**
     * Returns the value of the wrapped Array at the given index
     *
     * @return the value of the wrapped Array at the given index
     */
    public FlatObjects get(final int pIndex) {
        return arrayFlatObjects[pIndex];
    }

    /**
     * Returns the component-type of the wrapped Array
     *
     * @return the component-type of the wrapped Array
     */
    public Class<?> getCompenentType() {
        return super.getWrappedClass().getComponentType();
    }

    /**
     * Returns the representation of the value held by this FlatWrapper as an
     * Object
     *
     * @return the representation of the value held by this FlatWrapper as an
     *         Object
     */
    @Override
    Object getObjectRepresentation() {
        if (maxFlatWrappers == 0) {
            for (int x = 0; x < arrayFlatObjects.length; x++) {
                int size = arrayFlatObjects[x].getFlatWrappers().size();
                maxFlatWrappers = Math.max(maxFlatWrappers, size);
            }
        }
        Object[][] result = new Object[arrayFlatObjects.length][maxFlatWrappers];
        for (int x = 0; x < result.length; x++) {
            List<FlatWrapper> flatWrappers = arrayFlatObjects[x]
                    .getFlatWrappers();
            int length = flatWrappers.size();
            for (int y = 0; y < length; y++) {
                result[x][y] = flatWrappers.get(y).getObjectRepresentation();
            }
        }
        return result;
    }

    /**
     * Sets the value at the given index of the wrapped Array to the given
     * Value. If the given value is not compatible with the Array an Exception
     * is thrown.
     *
     * @param pValue
     *            the new value
     * @param pIndex
     *            the index to be set
     */
    public void set(final Object pValue, final int pIndex) {
        Array.set(super.get(), pIndex, pValue);
    }

    /**
     * Sets the values held by this FlatWrapper to the Object representing hold
     * values.,
     *
     */
    @Override
    public void setValues(final Object pValue) {
        Object[][] values = (Object[][]) pValue;
        for (int x = 0; x < values.length; x++) {
            Object[] singleValues = values[x];
            for (int y = 0; y < arrayFlatObjects[x].getFlatWrappers()
                    .size(); y++) {
                arrayFlatObjects[x].getFlatWrappers().get(y)
                        .setValues(values[x][y]);
            }
        }
    }
}
