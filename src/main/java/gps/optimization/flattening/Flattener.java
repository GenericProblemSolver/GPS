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

import java.awt.Point;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Privides Methods for "Flattening" Objects into FlatWrappers
 *
 * @author Steffen
 */
public class Flattener {

    /**
     * Wrapper holding the root-Objects to be flattened.
     */
    class SuperRoot {
        /**
         * Array of the root-Objects DO NOT CHANGE THE FUCKING NAME.
         */
        protected Object[] rootArray;

        /**
         * Creates a new SuperRoot wrapping/holing the given root-Objects
         *
         * @param pRootArray
         *            root-Array to be wrappd
         */
        protected SuperRoot(final Object[] pRootArray) {
            rootArray = pRootArray;
        }
    }

    /**
     * Set of all primitive- (or primitive-wrapper-) classes. Used to check for
     * leaves in the "Flattening"-process
     */
    private static final Set<Class<?>> primitiveLeaves = new HashSet<>();

    static {
        Flattener.primitiveLeaves.add(boolean.class);
        Flattener.primitiveLeaves.add(Boolean.class);
        Flattener.primitiveLeaves.add(char.class);
        Flattener.primitiveLeaves.add(Character.class);
        Flattener.primitiveLeaves.add(byte.class);
        Flattener.primitiveLeaves.add(Byte.class);
        Flattener.primitiveLeaves.add(short.class);
        Flattener.primitiveLeaves.add(Short.class);
        Flattener.primitiveLeaves.add(int.class);
        Flattener.primitiveLeaves.add(Integer.class);
        Flattener.primitiveLeaves.add(long.class);
        Flattener.primitiveLeaves.add(Long.class);
        Flattener.primitiveLeaves.add(float.class);
        Flattener.primitiveLeaves.add(Float.class);
        Flattener.primitiveLeaves.add(double.class);
        Flattener.primitiveLeaves.add(Double.class);
    }

    /**
     * Returns a List of all non-static Fields of the given Object
     *
     * @param pObject
     *            the Object
     * @return List of all non-static Fields of the given Object
     */
    private static List<Field> getAllFields(final Object pObject) {

        List<Field> result = new ArrayList<>();
        Class<?> current = pObject.getClass();
        while (current.getSuperclass() != null) {
            for (Field f : current.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    result.add(f);
                }
            }
            current = current.getSuperclass();
        }
        return result;
    }

    /**
     * Constructor, pretty much does nothing
     */
    public Flattener() {

    }

    /**
     * "Flattens" the given Objects into FlatWrappers. Leaves are primitives
     * (and their wrappers), enums and Arrays. Works on Objects with referential
     * loops.
     *
     * @param pObjects
     *            the Objects to be flattened
     * @return
     */
    public FlatObjects flattenAndWrap(final Object[] pObjects) {
        return flattenAndWrap(pObjects, new HashSet<Class<?>>());
    }

    /**
     * "Flattens" the given Objects into FlatWrappers. Leaves are primitives
     * (and their wrappers), enums, specified additional Classes and Arrays.
     * Works on Objects with referential loops.
     *
     * @param pObjects
     *            the Objects to be flattened
     * @param pLeaves
     *            additional leaves for the "Flattening"-Process
     * @return
     */
    public FlatObjects flattenAndWrap(final Object[] pObjects,
            final Set<Class<?>> pLeaves) {
        SuperRoot sR = this.new SuperRoot(pObjects);
        return flattenAndWrap(sR, new HashSet<Object>(), pLeaves);
    }

    /**
     * "Flattens" the given Objects into FlatWrappers. Leaves are primitives
     * (and their wrappers), enums, specified additional Classes and Arrays.
     * Works on Objects with referential loops.
     *
     * @param pObjects
     *            the Object to be flattened
     * @param pVisited
     *            Set of all visited Objects, prevents loops
     * @param pLeaves
     *            additional leaves for the "Flattening"-Process
     * @return List of FlatWrappers representing the "flattened" Objects
     */
    private FlatObjects flattenAndWrap(final SuperRoot pSuperRoot,
            final Set<Object> pVisited, final Set<Class<?>> pLeaves) {
        FlatObjects result = new FlatObjects(pSuperRoot.rootArray);
        Field rootArray = null;
        try {
            rootArray = SuperRoot.class.getDeclaredField("rootArray");
        } catch (NoSuchFieldException e1) {
            e1.printStackTrace();
        } catch (SecurityException e1) {
            e1.printStackTrace();
        }
        List<Object> objects = new ArrayList<>();
        int rootIndex = 0;
        for (Object o : pSuperRoot.rootArray) {
            Class<?> oClass = o.getClass();
            if (oClass.isArray()) {
                if (oClass.getComponentType().isPrimitive()) {
                    result.add(new ArrayFlatWrapper(rootArray, pSuperRoot,
                            oClass, rootIndex++));
                } else {
                    result.add(new ObjectArrayFlatWrapper(rootArray, pSuperRoot,
                            oClass, rootIndex++, pLeaves));
                }
            }

            else if (Flattener.primitiveLeaves.contains(oClass)
                    || oClass.isEnum() || pLeaves.contains(oClass)) {
                result.add(new FlatWrapper(rootArray, pSuperRoot, oClass,
                        rootIndex++));
            } else {
                objects.add(o);
            }
        }
        while (!objects.isEmpty()) {
            Object o = objects.remove(0);
            if (!pVisited.contains(o)) {
                pVisited.add(o);
                List<Field> fields = Flattener.getAllFields(o);
                for (Field f : fields) {
                    f.setAccessible(true);
                    Class<?> fieldClass = f.getType();
                    if (fieldClass.isArray()) {
                        if (fieldClass.getComponentType().isPrimitive()) {
                            result.add(new ArrayFlatWrapper(f, o, fieldClass));
                        } else {
                            result.add(new ObjectArrayFlatWrapper(f, o,
                                    fieldClass, pLeaves));
                        }

                    } else if (Flattener.primitiveLeaves.contains(fieldClass)
                            || fieldClass.isEnum()
                            || pLeaves.contains(fieldClass)) {

                        result.add(new FlatWrapper(f, o, fieldClass));

                    } else {

                        try {
                            objects.add(0, f.get(o));
                        } catch (IllegalArgumentException
                                | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return result;
    }

    public FlatObjects[] flattenAndWrapObjectArray(final Object pObjectArray,
            final Set<Class<?>> pLeaves) {
        if (!pObjectArray.getClass().isArray()
                || pObjectArray.getClass().getComponentType().isPrimitive()) {
            throw new IllegalArgumentException(
                    "The Object has to be an Object-array.");
        }
        FlatObjects[] result = new FlatObjects[Array.getLength(pObjectArray)];
        for (int x = 0; x < result.length; x++) {
            Object[] temp = new Object[1];
            if (Array.get(pObjectArray, x) == null) {
                continue;
            }
            temp[0] = Array.get(pObjectArray, x);
            result[x] = flattenAndWrap(temp, pLeaves);
        }
        return result;
    }
}
