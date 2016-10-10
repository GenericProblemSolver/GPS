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
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps an Array and provides methods for manipulation. Any changes to the
 * ArrayFlatWrapper are propagated to the wrapped Array.
 *
 * @author Steffen
 */
public class ArrayFlatWrapper extends FlatWrapper {

    /**
     * Creates a new ArrayFlatWrapper wrapping the given (Array-)Field, Object
     * and Class. If Field, Object or Class is null or the class is not
     * representing an Array an IllegalArgumentException is thrown.
     *
     * @param pWrappedField
     *            the to be wrapped Field
     * @param pWrappedObject
     *            the Object containing the Field
     * @param pWrappedClass
     *            the Class defining the Field
     */
    public ArrayFlatWrapper(final Field pWrappedField,
            final Object pWrappedObject, final Class<?> pWrappedClass) {
        super(pWrappedField, pWrappedObject, pWrappedClass);
        if (!pWrappedClass.isArray()) {
            throw new IllegalArgumentException(
                    "The wrapped class has to be an array.");
        }
    }

    /**
     * Creates a new ArrayFlatWrapper wrapping the given (Root-)Field, Object
     * and Class. If Field, Object or Class is null or the class is not
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
     */
    public ArrayFlatWrapper(final Field pWrappedField,
            final Object pWrappedObject, final Class<?> pWrappedClass,
            final int pRootIndex) {
        super(pWrappedField, pWrappedObject, pWrappedClass, pRootIndex);
        if (!pWrappedClass.isArray()) {
            throw new IllegalArgumentException(
                    "The wrapped class has to be an array.");
        }
    }

    /**
     * Returns the value of the wrapped Array at the given index
     *
     * @return the value of the wrapped Array at the given index
     */
    public Object get(final int pIndex) {
        return Array.get(super.get(), pIndex);
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
     * Returns the representation of the value held by this FlatWrapper as an
     * Object
     * 
     * @return the representation of the value held by this FlatWrapper as an
     *         Object
     */
    Object getObjectRepresentation() {
        Object toWrap = super.get();
        Object[] result = new Object[Array.getLength(toWrap)];
        for (int x = 0; x < result.length; x++) {
            result[x] = get(x);
        }
        return result;
    }

    /**
     * Sets the values held by this FlatWrapper to the Object representing hold values.,
     * 
     */
    public void setValues(final Object pValue) {
        Object[] values = (Object[]) pValue;
        for (int x = 0; x < values.length; x++) {
            set(values[x], x);
        }
    }
}
