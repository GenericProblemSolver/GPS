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
 * Wraps a Field and provides methods for manipulation. Any changes to the
 * FlatWrapper are propagated to the wrapped Field/Object.
 *
 * @author Steffen
 */
public class FlatWrapper {

    /**
     * The root-Array of this Wrapper, null if not a root-Object.
     */
    protected Object rootArray;

    /**
     * The index of this root-Node in the root-Array. If this is -1 this is not
     * a root-Object.
     */
    protected final int rootIndex;

    /**
     * The class of which a Field is wrapped
     */
    private final Class<?> wrappedClass;

    /**
     * The wrapped Field
     */
    private final Field wrappedField;

    /**
     * The Object containing the wrapped Field
     */
    private final Object wrappedObject;

    /**
     * The current value of the Field
     */
    private Object wrappedValue;

    /**
     * Creates a new FlatWrapper wrapping the given Field, Object and Class. If
     * Field, Object or Class is null an IllegalArgumentException is thrown.
     *
     * @param pWrappedField
     *            the to be wrapped Field
     * @param pWrappedObject
     *            the Object containing the Field
     * @param pWrappedClass
     *            the Class defining the Field
     */
    FlatWrapper(final Field pWrappedField, final Object pWrappedObject,
            final Class<?> pWrappedClass) {
        if (pWrappedField == null) {
            throw new IllegalArgumentException("Field may not be null.");
        }
        if (pWrappedObject == null) {
            throw new IllegalArgumentException("Object may not be null.");
        }
        if (pWrappedClass == null) {
            throw new IllegalArgumentException("Class may not be null.");
        }
        wrappedField = pWrappedField;
        wrappedObject = pWrappedObject;
        wrappedClass = pWrappedClass;
        try {
            wrappedValue = wrappedField.get(wrappedObject);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        rootIndex = -1;
        rootArray = null;
    }

    /**
     * Creates a new FlatWrapper wrapping the given (Root-)Field, Object and
     * Class at the given Index. If Field, Object or Class is null an
     * IllegalArgumentException is thrown.
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
    FlatWrapper(final Field pWrappedField, final Object pWrappedObject,
            final Class<?> pWrappedClass, final int pRootIndex) {
        if (pWrappedField == null) {
            throw new IllegalArgumentException("Field may not be null.");
        }
        if (pWrappedObject == null) {
            throw new IllegalArgumentException("Object may not be null.");
        }
        if (pWrappedClass == null) {
            throw new IllegalArgumentException("Class may not be null.");
        }
        wrappedField = pWrappedField;
        wrappedObject = pWrappedObject;
        wrappedClass = pWrappedClass;
        rootIndex = pRootIndex;
        try {
            rootArray = wrappedField.get(wrappedObject);
            wrappedValue = Array.get(rootArray, pRootIndex);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the value of the wrapped Field
     *
     * @return the value of the wrapped Field
     */
    public Object get() {
        if (rootIndex != -1) {
            return Array.get(rootArray, rootIndex);
        } else {
            return wrappedValue;
        }
    }

    /**
     * Returns the wrapped Class
     *
     * @return the wrapped Class
     */
    public Class<?> getWrappedClass() {
        return wrappedClass;
    }

    /**
     * Sets the wrapped Field of the wrapped Object to the given Value. If the
     * given value is not compatible with the Field an Exception is thrown.
     *
     * @param pValue
     *            the new value
     */
    public void set(final Object pValue) {
        if (rootIndex != -1) {
            Array.set(rootArray, rootIndex, pValue);
            wrappedValue = pValue;
        } else {
            try {
                wrappedField.set(wrappedObject, pValue);
                wrappedValue = pValue;
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the representation of the value held by this FlatWrapper as an
     * Object
     * 
     * @return the representation of the value held by this FlatWrapper as an
     *         Object
     */
    Object getObjectRepresentation() {
        return get();
    }

    /**
     * Sets the values held by this FlatWrapper to the Object representing hold values.,
     * 
     */
    public void setValues(final Object pValue) {
        set(pValue);
    }
}
