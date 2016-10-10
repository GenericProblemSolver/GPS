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
package gps.optimization.algorithm;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import gps.optimization.flattening.ArrayFlatWrapper;
import gps.optimization.flattening.FlatObjects;
import gps.optimization.flattening.FlatWrapper;
import gps.optimization.flattening.Flattener;
import gps.optimization.flattening.ObjectArrayFlatWrapper;
import gps.optimization.wrapper.Optimizable;

/**
 * Provides operations needed by typical {@link AbstractOptimizer}s to search
 * the space of solutions.
 */
public class SearchOperatorProvider {

    private Optimizable<?> toOpt;

    private final double DEFAULT_BYTE_STEP = 1;
    private final double DEFAULT_CHAR_STEP = 1;
    private final double DEFAULT_DOUBLE_STEP = 0.1;
    private final double DEFAULT_FLOAT_STEP = 0.1;
    private final double DEFAULT_INT_STEP = 1;
    private final double DEFAULT_LONG_STEP = 1;
    private final double DEFAULT_SHORT_STEP = 1;
    private final Random random = new Random();

    public SearchOperatorProvider(final Optimizable<?> pToOpt) {
        toOpt = pToOpt;
    }

    public SearchOperatorProvider() {

    }

    /**
     * Returns the modified form of the given Object. Only works for primitives
     * and enums.
     *
     * @param pValue
     *            Object to be modified
     * @return the "modified" Object
     */
    @SuppressWarnings("unused")
    private Object modify(final Object pValue) {

        if (pValue instanceof Byte) {
            return modify(pValue, DEFAULT_BYTE_STEP);
        }
        if (pValue instanceof Short) {
            return modify(pValue, DEFAULT_SHORT_STEP);
        }
        if (pValue instanceof Integer) {
            return modify(pValue, DEFAULT_INT_STEP);
        }
        if (pValue instanceof Long) {
            return modify(pValue, DEFAULT_LONG_STEP);
        }
        if (pValue instanceof Character) {
            return modify(pValue, DEFAULT_CHAR_STEP);
        }
        if (pValue instanceof Boolean) {
            return modifyBoolean((boolean) pValue);
        }
        if (pValue instanceof Float) {
            return modify(pValue, DEFAULT_FLOAT_STEP);
        }
        if (pValue instanceof Double) {
            return modify(pValue, DEFAULT_DOUBLE_STEP);
        }
        return null;
    }

    /**
     * Returns the modified (by the given step-Mod) form of the given Object.
     * Only works for primitives and enums.
     *
     * @param pValue
     *            Object to be modifyied
     * @param pStedMod
     *            modifier of the modification
     * @return the modified Object
     */
    private Object modify(final Object pValue, final double pStedMod) {
        if (pValue instanceof Byte) {
            return modifyByte((Byte) pValue, (int) pStedMod);
        }
        if (pValue instanceof Short) {
            return modifyShort((Short) pValue, (int) pStedMod);
        }
        if (pValue instanceof Integer) {
            return modifyInteger((Integer) pValue, (int) pStedMod);
        }
        if (pValue instanceof Long) {
            return modifyLong((Long) pValue, (int) pStedMod);
        }
        if (pValue instanceof Character) {
            return modifyCharacter((Character) pValue, (int) pStedMod);
        }
        if (pValue instanceof Boolean) {
            return modifyBoolean((Boolean) pValue);
        }
        if (pValue instanceof Float) {
            return modifyFloat((Float) pValue, pStedMod);
        }
        if (pValue instanceof Double) {
            return modifyDouble((Double) pValue, pStedMod);
        }
        return null;
    }

    /**
     * Modifies the given value by the given step-modifier
     *
     * @param pValue
     *            value to be modified
     * @param pStepMod
     *            modification-modifier
     * @return the modified value
     */
    private Boolean modifyBoolean(final Boolean pValue) {
        return pValue ? false : true;
    }

    /**
     * Modifies the given value by the given step-modifier
     *
     * @param pValue
     *            value to be modified
     * @param pStepMod
     *            modification-modifier
     * @return the modified value
     */
    private Byte modifyByte(final Byte pValue, final int pStepMod) {
        return (byte) (pValue + pStepMod);
    }

    /**
     * Modifies the given value by the given step-modifier
     *
     * @param pValue
     *            value to be modified
     * @param pStepMod
     *            modification-modifier
     * @return the modified value
     */
    private Character modifyCharacter(final Character pValue,
            final int pStepMod) {
        return (char) ((((int) pValue) + pStepMod) % 256);
    }

    /**
     * Modifies the given value by the given step-modifier
     *
     * @param pValue
     *            value to be modified
     * @param pStepMod
     *            modification-modifier
     * @return the modified value
     */
    private Double modifyDouble(final Double pValue, final double pStepMod) {
        return pValue + pStepMod;
    }

    /**
     * Modifies the given value by the given step modifier
     * 
     * @param pEnum
     *            value to be modified
     * @param pStedMod
     *            modification modifier
     * @return the modified value
     */
    @SuppressWarnings("unused")
    private Enum<?> modifyEnum(final Enum<?> pEnum, final int pStedMod) {
        Enum<?>[] enumValues = pEnum.getClass().getEnumConstants();
        int prevPos = pEnum.ordinal();
        int size = enumValues.length;
        return enumValues[(((prevPos + pStedMod) % size) + size) % size];
    }

    /**
     * Modifies the given value by the given step-modifier
     *
     * @param pValue
     *            value to be modified
     * @param pStepMod
     *            modification-modifier
     * @return the modified value
     */
    private Float modifyFloat(final Float pValue, final double pStepMod) {
        return pValue + (float) pStepMod;
    }

    /**
     * Modifies the given value by the given step-modifier
     *
     * @param pValue
     *            value to be modified
     * @param pStepMod
     *            modification-modifier
     * @return the modified value
     */
    private Integer modifyInteger(final Integer pValue, final int pStepMod) {
        return (pValue + pStepMod);
    }

    /**
     * Modifies the given value by the given step-modifier
     *
     * @param pValue
     *            value to be modified
     * @param pStepMod
     *            modification-modifier
     * @return the modified value
     */
    private Long modifyLong(final Long pValue, final int pStepMod) {
        return pValue + pStepMod;
    }

    /**
     * Modifies the given value by the given step-modifier
     *
     * @param pValue
     *            value to be modified
     * @param pStepMod
     *            modification-modifier
     * @return the modified value
     */
    private Short modifyShort(final Short pValue, final int pStepMod) {
        return (short) (pValue + pStepMod);
    }

    /**
     * Returns the changed value for the given
     * {@link gps.optimization.flattening.FlatWrapper}, if the given boolean is
     * {@code true}. Otherwise, the value of the given FlatWrapper is returned.
     * The value is changed according to the the given step width.
     * 
     * @param pFlatW
     *            the FlatWrapper the neighboring value is to be calculated for
     * @param pShouldBeChanged
     *            indicates, whether or not the value of the given FlatWrapper
     *            is to be changed. {@code true} if yes, {@code false} otherwise
     * @return the changed value
     */
    @SuppressWarnings("unchecked")
    private Object neighbors(final FlatWrapper pFlatW,
            final boolean pShouldBeChanged, final int pStepMod) {
        if (!pShouldBeChanged) {
            return pFlatW.get();
        }
        Class<?> wrappedClass = pFlatW.getWrappedClass();

        if (wrappedClass.isEnum()) {
            Enum<?>[] enumValues = ((Class<Enum<?>>) wrappedClass)
                    .getEnumConstants();
            Enum<?> en = enumValues[random.nextInt(enumValues.length)];
            while (true) {
                if (!en.equals(pFlatW.get())) {
                    break;
                }
                en = enumValues[random.nextInt(enumValues.length)];
            }
            return en;

        }
        if (wrappedClass.isAssignableFrom(Byte.class)
                || wrappedClass.isAssignableFrom(Byte.TYPE)) {

            return (byte) ((byte) pFlatW.get() + pStepMod);
        }
        if (wrappedClass.isAssignableFrom(Short.class)
                || wrappedClass.isAssignableFrom(Short.TYPE)) {

            return (short) ((short) pFlatW.get() + pStepMod);
        }
        if (wrappedClass.isAssignableFrom(Integer.class)
                || wrappedClass.isAssignableFrom(Integer.TYPE)) {

            return (int) ((int) pFlatW.get() + pStepMod);
        }
        if (wrappedClass.isAssignableFrom(Long.class)
                || wrappedClass.isAssignableFrom(Long.TYPE)) {

            return (long) ((long) pFlatW.get() + pStepMod);
        }
        if (wrappedClass.isAssignableFrom(Float.class)
                || wrappedClass.isAssignableFrom(Float.TYPE)) {

            return (float) ((float) pFlatW.get()
                    + random.nextFloat() * pStepMod);
        }
        if (wrappedClass.isAssignableFrom(Double.class)
                || wrappedClass.isAssignableFrom(Double.TYPE)) {

            return (double) ((double) pFlatW.get()
                    + random.nextDouble() * pStepMod);
        }
        if (wrappedClass.isAssignableFrom(Character.class)
                || wrappedClass.isAssignableFrom(Character.TYPE)) {

            return (char) ((char) pFlatW.get() + pStepMod);
        }
        if (wrappedClass.isAssignableFrom(Boolean.class)
                || wrappedClass.isAssignableFrom(Boolean.TYPE)) {
            if (!pShouldBeChanged) {
                return (boolean) pFlatW.get();
            }

            return !(boolean) pFlatW.get();
        }

        throw new RuntimeException(
                "Can't generate a neighboring value for the type of the given FlatWrapper");

    }

    /**
     * Gets neighboring solutions for the given
     * {@link gps.optimization.flatteningFlatObjects}. <br>
     * 
     * Returns an Object-Array containing multiple Object-Arrays each containing
     * possible values for the {@link gps.optimization.flattening.FlatWrapper}s
     * of the given FlatObjects. These values are calculated by modifying the
     * current value in the FlatWrappers according to the given step width.<br>
     * 
     * e.g.: 1, 200 -> [ (0, 200) , (2, 200), (1, 199), (1, 201)] <br>
     * If {@link #toOpt} is not null and the given
     * {@link gps.optimization.wrapping.Optimizable} has a
     * {@link gps.optimization.wrapper.Optimizable#neighbor}-function, the
     * return of that function is returned (converted to an Object[][]).<br>
     * 
     * Throws a {@code RuntimeException} if the Neighbor-annotated method does
     * not return at least one set of parameters for the
     * {@link gps.annotations.Optimize}-annotated method.<br>
     * The {@link gps.optimization.algorithm.AbstractOptimizer}s have to check
     * whether the returned new solution(s) can be passed as arguments to the
     * Optimize-annotated method.
     * 
     * @param pFlatO
     *            the object for which the neighbors are to be calculated for
     * @param pStepMod
     *            the step width
     * @return an Object-Array containing all calculated neighbors
     */
    public Object[][] neighbors(final FlatObjects pFlatO, final int pStepMod) {
        if (toOpt != null && toOpt.hasNeighborFunction()) {
            List<Object[]> neighbors = toOpt.neighbor(pFlatO.getRootObjects());
            if (neighbors.size() == 0) {
                throw new RuntimeException(
                        "Bad implementation of @Neighbor-annotated method. "
                                + "The method must return at least one set of parameters "
                                + "for the @Optimize-annotated method.");
            }
            Object[][] ret = new Object[neighbors.size()][];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = neighbors.get(i);
            }
            return ret;
        } else {
            int index = 0;
            FlatObjects noArrays = breakupArrays(pFlatO);
            int size = noArrays.getFlatWrappers().size();
            Object[][] array = new Object[size * 2][size];
            while (index < size * 2) {
                for (int i = 0; i < size; i++) {
                    array[index][i] = neighbors(
                            noArrays.getFlatWrappers().get(i), i == index / 2,
                            pStepMod);
                    array[index + 1][i] = neighbors(
                            noArrays.getFlatWrappers().get(i), i == index / 2,
                            -pStepMod);
                }
                index += 2;
            }

            return addArraysAgain(array, pFlatO);

        }

    }

    /**
     * Brings the structure of the given array into line with the structure of
     * the given {@link gps.optimization.flattening.FlatObjects}.
     * 
     * @param pInput
     *            the input that is to be restructured
     * @param pFlatO
     *            the FlatObjects which structure is to be applied on the input
     * @return the restructured 2d array
     */
    private Object[][] addArraysAgain(final Object[][] pInput,
            final FlatObjects pFlatO) {
        int element = 0;
        int arrayLength = 1;
        Object[][] returnList = new Object[pInput.length][pFlatO
                .getFlatWrappers().size()];
        for (FlatWrapper fw : pFlatO.getFlatWrappers()) {
            for (int line = 0; line < pInput.length; line++) {
                if (fw.getWrappedClass().isArray()) {
                    arrayLength = Array.getLength(fw.get());
                    Object[] arr = new Object[arrayLength];

                    for (int i = element; i < arrayLength; i++) {
                        arr[i - element] = pInput[line][i];
                    }
                    returnList[line][element] = arr;
                } else {
                    returnList[line][element] = pInput[line][element
                            + arrayLength - 1];
                }
            }
            element++;
        }

        return returnList;

    }

    /**
     * Flattens the Arrays in the given
     * {@link gps.optimization.flattening.FlatObjects}, if there are any.
     * 
     * @param pFlatO
     *            the FlatObjects that is to be "flattened"
     * @return the FlatObjects exclusively containing primitives
     */
    private FlatObjects breakupArrays(final FlatObjects pFlatO) {
        List<Object> oList = new ArrayList<>();
        for (FlatWrapper fw : pFlatO.getFlatWrappers()) {
            if (fw.getWrappedClass().isArray()) {
                if (fw.getWrappedClass().getComponentType().isPrimitive()) {
                    int arrLength = Array.getLength(fw.get());
                    Object[] arr = new Object[arrLength];
                    for (int i = 0; i < arrLength; ++i) {
                        arr[i] = Array.get(fw.get(), i);
                    }
                    for (Object o : arr) {
                        oList.add(o);
                    }
                } else {
                    Object[] arr = (Object[]) fw.get();
                    for (Object ob : arr) {
                        oList.add(ob);
                    }
                }
            } else {
                oList.add(fw.get());
            }
        }
        Object[] o = oList.toArray();
        Flattener fl = new Flattener();
        FlatObjects f = fl.flattenAndWrap(o);
        return f;

    }

    /**
     * Sets the value held by all the FlatWrappers of the given FlatObjects to a
     * random new one or sets the given FlatObjects to a neighboring solution
     * randomly chosen from the neighboring solutions returned by the user
     * defined {@link gps.annotations.Neighbor}- annotated method. Throws a
     * {@code RuntimeException} if the Neighbor-annotated method does not return
     * at least one set of parameters for the {@link gps.annotations.Optimize}
     * -annotated method.<br>
     * The {@link gps.optimization.algorithm.AbstractOptimizer}s have to check
     * whether the returned new solution(s) can be passed as arguments to the
     * Optimize-annotated method.
     * 
     * @param pFlatO
     *            the FlatObjects to be randomized
     */
    public void randomVariation(FlatObjects pFlatO) {
        if (toOpt != null && toOpt.hasNeighborFunction()) {
            List<Object[]> neighbors = toOpt.neighbor(pFlatO.getRootObjects());
            if (neighbors.size() == 0) {
                throw new RuntimeException(
                        "Bad implementation of @Neighbor-annotated method. "
                                + "The method must return at least one set of parameters "
                                + "for the @Optimize-annotated method.");
            }
            Object[] newSolution = neighbors
                    .get(random.nextInt(neighbors.size()));
            for (int i = 0; i < newSolution.length; i++) {
                pFlatO.getFlatWrappers().get(i).set(newSolution[i]);
            }
        } else {
            for (FlatWrapper fW : pFlatO.getFlatWrappers()) {
                randomVariation(fW);
            }
        }
    }

    /**
     * Sets the value held by all the FlatWrappers of the given FlatObjects to a
     * random new one within the given distance to the previous value or sets
     * the given FlatObjects to a neighboring solution randomly chosen from the
     * neighboring solutions returned by the user defined
     * {@link gps.annotations.Neighbor}- annotated method. <br>
     * Throws a {@code RuntimeException} if the Neighbor-annotated method does
     * not return at least one set of parameters for the
     * {@link gps.annotations.Optimize}-annotated method.<br>
     * The {@link gps.optimization.algorithm.AbstractOptimizer}s have to check
     * whether the returned new solution(s) can be passed as arguments to the
     * Optimize-annotated method.
     * 
     * @param pFlatO
     *            the FlatObjects to be randomized within the given range
     * @param pRange
     *            maximum distance of the new values from the old ones
     */
    public void randomVariation(FlatObjects pFlatO, final int pRange) {
        if (toOpt != null && toOpt.hasNeighborFunction()) {
            List<Object[]> neighbors = toOpt.neighbor(pFlatO.getRootObjects());
            if (neighbors.size() == 0) {
                throw new RuntimeException(
                        "Bad implementation of @Neighbor-annotated method. "
                                + "The method must return at least one set of parameters "
                                + "for the @Optimize-annotated method.");
            }
            Object[] newSolution = neighbors
                    .get(random.nextInt(neighbors.size()));
            for (int i = 0; i < newSolution.length; i++) {
                pFlatO.getFlatWrappers().get(i).set(newSolution[i]);
            }
        } else {
            for (FlatWrapper fW : pFlatO.getFlatWrappers()) {
                randomVariation(fW, pRange);
            }
        }
    }

    /**
     * Sets the value held by the FlatWrapper to a random new one.
     * 
     * @param pFlatW
     *            the FlatWrapper to be randomized
     */
    @SuppressWarnings("unchecked")
    public void randomVariation(final FlatWrapper pFlatW) {
        Class<?> wrappedClass = pFlatW.getWrappedClass();
        if (wrappedClass.isEnum()) {
            Enum<?>[] enumValues = ((Class<Enum<?>>) wrappedClass)
                    .getEnumConstants();
            pFlatW.set(enumValues[random.nextInt(enumValues.length)]);
        }
        if (wrappedClass.isAssignableFrom(Byte.TYPE)
                || wrappedClass.isAssignableFrom(Byte.class)) {
            pFlatW.set((byte) random.nextInt(Byte.MAX_VALUE + 1));
        }
        if (wrappedClass.isAssignableFrom(Short.TYPE)
                || wrappedClass.isAssignableFrom(Short.class)) {
            pFlatW.set((short) random.nextInt(Short.MAX_VALUE + 1));
        }
        if (wrappedClass.isAssignableFrom(Integer.TYPE)
                || wrappedClass.isAssignableFrom(Integer.class)) {
            pFlatW.set(random.nextInt());
        }
        if (wrappedClass.isAssignableFrom(Long.TYPE)
                || wrappedClass.isAssignableFrom(Long.class)) {
            pFlatW.set(random.nextLong());
        }
        if (wrappedClass.isAssignableFrom(Float.TYPE)
                || wrappedClass.isAssignableFrom(Float.class)) {
            pFlatW.set(random.nextFloat());
        }
        if (wrappedClass.isAssignableFrom(Double.TYPE)
                || wrappedClass.isAssignableFrom(Double.class)) {
            pFlatW.set(random.nextDouble());
        }
        if (wrappedClass.isAssignableFrom(Character.TYPE)
                || wrappedClass.isAssignableFrom(Character.class)) {
            pFlatW.set((char) random.nextInt(256));
        }
        if (wrappedClass.isAssignableFrom(Boolean.TYPE)
                || wrappedClass.isAssignableFrom(Boolean.class)) {
            pFlatW.set(random.nextBoolean());
        }
    }

    /**
     * Sets the value held by the FlatWrapper to a random new one within the
     * given distance to the previous value.
     * 
     * @param pFlatW
     *            the FlatWrapper to be randomized
     * @param pRange
     *            maximum distance of the new value from the old one
     */
    @SuppressWarnings("unchecked")
    public void randomVariation(final FlatWrapper pFlatW, final int pRange) {
        Class<?> wrappedClass = pFlatW.getWrappedClass();
        Object value = pFlatW.get();
        if (wrappedClass.isEnum()) {
            Enum<?> enumValue = (Enum<?>) value;
            Enum<?>[] enumValues = ((Class<Enum<?>>) wrappedClass)
                    .getEnumConstants();
            int size = enumValues.length;
            pFlatW.set(enumValues[((((random.nextInt(2 * pRange + 1) - pRange)
                    + enumValue.ordinal()) % size) + size) % size]);
        }
        if (wrappedClass.isAssignableFrom(Byte.TYPE)
                || wrappedClass.isAssignableFrom(Byte.class)) {
            pFlatW.set((byte) ((random.nextInt(2 * pRange + 1) - pRange)
                    + (byte) value));
        }
        if (wrappedClass.isAssignableFrom(Short.TYPE)
                || wrappedClass.isAssignableFrom(Short.class)) {
            pFlatW.set((short) ((random.nextInt(2 * pRange + 1) - pRange)
                    + (short) value));
        }
        if (wrappedClass.isAssignableFrom(Integer.TYPE)
                || wrappedClass.isAssignableFrom(Integer.class)) {
            pFlatW.set((random.nextInt(2 * pRange + 1) - pRange) + (int) value);
        }
        if (wrappedClass.isAssignableFrom(Long.TYPE)
                || wrappedClass.isAssignableFrom(Long.class)) {
            pFlatW.set(
                    (random.nextInt(2 * pRange + 1) - pRange) + (long) value);
        }
        if (wrappedClass.isAssignableFrom(Float.TYPE)
                || wrappedClass.isAssignableFrom(Float.class)) {
            pFlatW.set((random.nextBoolean() ? -random.nextFloat()
                    : random.nextFloat()) * pRange + (float) value);
        }
        if (wrappedClass.isAssignableFrom(Double.TYPE)
                || wrappedClass.isAssignableFrom(Double.class)) {
            pFlatW.set((random.nextBoolean() ? -random.nextDouble()
                    : random.nextDouble()) * pRange + (double) value);
        }
        if (wrappedClass.isAssignableFrom(Character.TYPE)
                || wrappedClass.isAssignableFrom(Character.class)) {
            pFlatW.set((char) ((random.nextInt(2 * pRange + 1) - pRange)
                    + (int) (char) value));
        }
        if (wrappedClass.isAssignableFrom(Boolean.TYPE)
                || wrappedClass.isAssignableFrom(Boolean.class)) {
            pFlatW.set(random.nextBoolean());
        }
    }

    /**
     * Returns a randomly (within the given variation) changed Object.
     * The Object must be a wrapper for a primitive type or an enum for this to work.
     * 
     * @param pObject
     *            the Object to be randomized
     * @param pRange
     *            maximum distance of the new value from the old one
     */
    @SuppressWarnings("unchecked")
    private Object randomVariation(final Object pObject, final int pRange) {
        Class<?> wrappedClass = pObject.getClass();
        if (wrappedClass.isEnum()) {
            Enum<?> enumValue = (Enum<?>) pObject;
            Enum<?>[] enumValues = ((Class<Enum<?>>) wrappedClass)
                    .getEnumConstants();
            int size = enumValues.length;
            return enumValues[((((random.nextInt(2 * pRange + 1) - pRange)
                    + enumValue.ordinal()) % size) + size) % size];
        }
        if (wrappedClass.isAssignableFrom(Byte.TYPE)
                || wrappedClass.isAssignableFrom(Byte.class)) {
            return (byte) ((random.nextInt(2 * pRange + 1) - pRange)
                    + (byte) pObject);
        }
        if (wrappedClass.isAssignableFrom(Short.TYPE)
                || wrappedClass.isAssignableFrom(Short.class)) {
            return (short) ((random.nextInt(2 * pRange + 1) - pRange)
                    + (short) pObject);
        }
        if (wrappedClass.isAssignableFrom(Integer.TYPE)
                || wrappedClass.isAssignableFrom(Integer.class)) {
            return (random.nextInt(2 * pRange + 1) - pRange + (int) pObject);
        }
        if (wrappedClass.isAssignableFrom(Long.TYPE)
                || wrappedClass.isAssignableFrom(Long.class)) {
            return (random.nextInt(2 * pRange + 1) - pRange + (long) pObject);
        }
        if (wrappedClass.isAssignableFrom(Float.TYPE)
                || wrappedClass.isAssignableFrom(Float.class)) {
            return (random.nextBoolean() ? -random.nextFloat()
                    : random.nextFloat() * pRange + (float) pObject);
        }
        if (wrappedClass.isAssignableFrom(Double.TYPE)
                || wrappedClass.isAssignableFrom(Double.class)) {
            return (random.nextBoolean() ? -random.nextDouble()
                    : random.nextDouble() * pRange + (double) pObject);
        }
        if (wrappedClass.isAssignableFrom(Character.TYPE)
                || wrappedClass.isAssignableFrom(Character.class)) {
            return (char) ((random.nextInt(2 * pRange + 1) - pRange)
                    + (int) (char) pObject);
        }
        if (wrappedClass.isAssignableFrom(Boolean.TYPE)
                || wrappedClass.isAssignableFrom(Boolean.class)) {
            return random.nextBoolean();
        }
        return null;
    }

    /**
     * Sets the value held by a random Sub-FlatWrapper of the FlatWrappers of
     * the given FlatObjects to a random new one within the given distance to
     * the previous value.
     * 
     * @param pFlatO
     *            the FlatObjects to be randomized within the given range
     * @param pRange
     *            maximum distance of the new values from the old ones
     */
    public void randomRandomVariation(FlatObjects pFlatO, final int pRange) {
        Random random = new Random();
        FlatWrapper chosen = pFlatO.getFlatWrappers()
                .get(random.nextInt(pFlatO.getFlatWrappers().size()));
        if (chosen instanceof ArrayFlatWrapper) {
            ArrayFlatWrapper aFW = (ArrayFlatWrapper) chosen;
            int randPosition = random.nextInt(Array.getLength(aFW.get()));
            aFW.set(randomVariation(aFW.get(randPosition), pRange),
                    randPosition);
        } else if (chosen instanceof ObjectArrayFlatWrapper) {
            ObjectArrayFlatWrapper oAFW = (ObjectArrayFlatWrapper) chosen;
            int randPosition = random.nextInt(Array.getLength(oAFW.get()));
            randomRandomVariation(oAFW.get(randPosition), pRange);
        } else {
            randomVariation(chosen, pRange);
        }
    }
}
