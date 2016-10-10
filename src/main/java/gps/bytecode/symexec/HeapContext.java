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
package gps.bytecode.symexec;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import gps.bytecode.expressions.Expression;
import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.OperationExpression;
import gps.bytecode.expressions.Operator;
import gps.bytecode.expressions.Operator.Reference;
import javassist.Modifier;
import gps.bytecode.expressions.Operator.RuntimeType;
import gps.bytecode.expressions.Variable;
import gps.annotations.Variable.VariableDepth;

/**
 * Adds objects from the real JVM heap into the symbolically executed heap
 * 
 * @author mfunk@tzi.de
 *
 */
public class HeapContext {
    // Already mapped objects and their addresses
    private final Map<Object, Expression> addressMap = new HashMap<>();

    private final Map<Object, VariableDepth> variableObjects = new HashMap<>();

    private final Queue<Object> objectQueue = new ArrayDeque<>();
    // Counter to uniquely name created parameters
    private int paramCounter = 0;

    Map<Variable, InsertPoint> insertVarMap = new HashMap<>();

    /**
     * Returns an ArrayList of all Fields in a Class. Includes Inherited and
     * static Fields
     *
     * Ordering is arbitrary but consistent
     * 
     * @param c
     * @return
     */
    public static ArrayList<Field> getAllFields(Class<?> c) {
        ArrayList<Field> list = new ArrayList<>();
        do {
            list.addAll(Arrays.asList(c.getDeclaredFields()));
            for (Class<?> interfaze : c.getInterfaces()) {
                list.addAll(Arrays.asList(interfaze.getDeclaredFields()));
            }
            c = c.getSuperclass();
        } while (c != null);
        Collections.reverse(list);
        return list;
    }

    /**
     * Returns an Array of all non static Fields in a Class. Includes inherited
     * Fields.
     * 
     * @param c
     * @return
     */
    public static Field[] getAllNonStaticFields(Class<?> c) {
        ArrayList<Field> list = getAllFields(c);
        ArrayList<Field> returnList = new ArrayList<>();
        for (Field f : list) {
            int modifiers = f.getModifiers();
            if (!Modifier.isStatic(modifiers)) {
                returnList.add(f);
            }
        }
        return returnList.toArray(new Field[returnList.size()]);
    }

    /**
     * Returns the size of an object on the symbolic Heap
     * 
     * @param o
     * @return
     */
    private static int getObjectSize(Object o) {
        if (o.getClass().isArray()) {
            //Add one field for length and one for the type
            return Array.getLength(o) + 2;
        }
        //Add one for runtimetype information
        return getAllNonStaticFields(replaceWithIatfs(o).getClass()).length + 1;
    }

    /**
     * Inserts an Array into the heap at a specific address
     * 
     * @param ctx
     * @param array
     * @param heapAddress
     */
    private void putArrayIntoHeap(Context ctx, Object array,
            Expression heapAddress) {
        ctx.heap = new OperationExpression(Operator.PUT, ctx.heap, heapAddress,
                new Constant(RuntimeType.class,
                        ctx.getTypeId(array.getClass())));
        Expression lengthAddr = new OperationExpression(Operator.ADD,
                heapAddress, new Constant(Reference.class, 1));
        ctx.heap = new OperationExpression(Operator.PUT, ctx.heap, lengthAddr,
                new Constant(int.class, Array.getLength(array)));
        for (int i = 0; i < Array.getLength(array); ++i) {
            Expression fieldAddress = new OperationExpression(Operator.ADD,
                    heapAddress, new Constant(Reference.class, i + 2));
            ctx.heap = new OperationExpression(Operator.PUT, ctx.heap,
                    fieldAddress, getArrayValue(ctx, array, i));
        }
    }

    /**
     * Converts an Arraytype into its primitive corresponding type
     * 
     * @param c
     * @return
     */
    public static Class<?> getElementClass(Class<?> c) {
        if (c == int[].class) {
            return int.class;
        }
        if (c == short[].class) {
            return short.class;
        }
        if (c == long[].class) {
            return long.class;
        }
        if (c == byte[].class) {
            return byte.class;
        }
        if (c == boolean[].class) {
            return boolean.class;
        }
        if (c == float[].class) {
            return float.class;
        }
        if (c == double[].class) {
            return double.class;
        }
        if (c == char[].class) {
            return char.class;
        }
        throw new RuntimeException(
                "Cannot get primitive type for array of type " + c);
    }

    /**
     * Returns a symbolic expression for the array value at the given index
     *
     * Inserts new Variables into the context if the array is anotated
     * with @Variable
     * 
     * @param ctx
     * @param array
     * @param index
     * @return
     */
    private Expression getArrayValue(Context ctx, Object array, int index) {
        Class<?> arrayClass = array.getClass();
        // Is it a reference array?
        if (Object[].class.isAssignableFrom(arrayClass)) {
            // Array elements have the same variabledepth as their containers
            variableObjects.put(Array.get(array, index),
                    variableObjects.get(array));
            return requestAddress(ctx, Array.get(array, index));
        }
        Class<?> elementClass = getElementClass(arrayClass);
        //If it is a variable, generate a new variable for it
        if (variableObjects.get(array) == VariableDepth.Flat) {
            Variable v = new Variable("h" + "_" + paramCounter, elementClass);
            ctx.invoker.heapParams.put(paramCounter, v);
            insertVarMap.put(v, new ArrayInsertPoint(array, index));
            paramCounter += 1;
            return v;
        }
        if (elementClass == int.class) {
            return new Constant(Array.getInt(array, index));
        } else if (elementClass == long.class) {
            return new Constant(Array.getLong(array, index));
        } else if (elementClass == short.class) {
            return new Constant(Array.getShort(array, index));
        } else if (elementClass == byte.class) {
            return new Constant(Array.getByte(array, index));
        } else if (elementClass == boolean.class) {
            return new Constant(Array.getBoolean(array, index));
        } else if (elementClass == float.class) {
            return new Constant(Array.getFloat(array, index));
        } else if (elementClass == double.class) {
            return new Constant(Array.getDouble(array, index));
        } else if (elementClass == char.class) {
            return new Constant(Array.getChar(array, index));
        }
        throw new RuntimeException(
                "Access to unknown array type " + arrayClass);
    }

    /**
     * "Allocates" a a slot for the object on the symbolic heap and returns it
     * address Returns the address of an object if it already is allocated by
     * this HeapInserter
     * 
     * @param ctx
     * @param o
     * @return
     */
    private Expression requestAddress(Context ctx, Object o) {
        if (o == null) {
            return new Constant(Reference.class, -1);
        }
        if (!addressMap.containsKey(o)) {
            addressMap.put(o, ctx.heapsize);
            // Reserve space
            ctx.heapsize = new OperationExpression(Operator.ADD, ctx.heapsize,
                    new Constant(Reference.class, getObjectSize(o)));
            // Queue object for insertion
            objectQueue.add(o);
        }
        return addressMap.get(o);
    }

    /**
     * Returns a Fields VariableDepth in combination with its parent VariableDepth
     * @param f
     * @param parentDepth
     * @return
     */
    private static VariableDepth getCombinedVariableDepth(Field f,
            VariableDepth parentDepth) {
        gps.annotations.Variable v = f
                .getAnnotation(gps.annotations.Variable.class);

        VariableDepth depth;
        if (v == null) {
            // If there is no depth annotation it defaults to None
            depth = VariableDepth.None;
        } else {
            depth = v.value();
        }

        if (parentDepth == VariableDepth.Deep) {
            // Deep variablity can be stopped by an explicit @Variable(NONE)
            if (v != null && depth == VariableDepth.None) {
                return VariableDepth.None;
            } else {
                return VariableDepth.Deep;
            }
        }
        return depth;
    }

    private Expression getFieldValue(Context ctx, Field f, Object parent) {
        VariableDepth parentDepth = variableObjects.get(parent);
        VariableDepth thisDepth = getCombinedVariableDepth(f, parentDepth);
        f.setAccessible(true);

        try {
            if (!f.getType().isPrimitive()) {
                if (thisDepth == VariableDepth.Flat && !f.getType().isArray()) {
                    Variable v = new Variable("h_" + paramCounter,
                            Reference.class);
                    ctx.invoker.heapParams.put(paramCounter, v);
                    insertVarMap.put(v, new ObjectInsertPoint(parent, f));
                    paramCounter += 1;
                    return v;
                } else {
                    variableObjects.put(f.get(parent), thisDepth);
                    return requestAddress(ctx, f.get(parent));
                }
            }
            Class<?> type = f.getType();
            if (thisDepth != VariableDepth.None) {
                Variable v = new Variable("h_" + paramCounter, type);
                ctx.invoker.heapParams.put(paramCounter, v);
                insertVarMap.put(v, new ObjectInsertPoint(parent, f));
                paramCounter += 1;
                return v;
            } else if (type == int.class) {
                return new Constant(f.getInt(parent));
            } else if (type == long.class) {
                return new Constant(f.getLong(parent));
            } else if (type == short.class) {
                return new Constant(f.getShort(parent));
            } else if (type == byte.class) {
                return new Constant(f.getByte(parent));
            } else if (type == boolean.class) {
                return new Constant(f.getBoolean(parent));
            } else if (type == double.class) {
                return new Constant(f.getDouble(parent));
            } else if (type == float.class) {
                return new Constant(f.getFloat(parent));
            } else if (type == char.class) {
                return new Constant(f.getChar(parent));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error accessing field " + f.getName());
        }
        throw new RuntimeException(
                "No idea what to do with field " + f.getName());
    }

    /**
     * Add an object and all of its fields to the symbolic heap
     * 
     * @param ctx
     * @param o
     * @param address
     */
    private void putObjectIntoHeap(Context ctx, Object o, Expression address) {
        o = replaceWithIatfs(o);
        Class<?> c = o.getClass();
        //Put type information onto the heap
        Expression runtimeType = new Constant(RuntimeType.class,
                ctx.getTypeId(c));
        ctx.heap = new OperationExpression(Operator.PUT, ctx.heap, address,
                runtimeType);

        Field[] fields = getAllNonStaticFields(c);
        for (int fieldIndex = 0; fieldIndex < fields.length; ++fieldIndex) {
            Expression value = getFieldValue(ctx, fields[fieldIndex], o);
            Expression fieldAddress = new OperationExpression(Operator.ADD,
                    address, new Constant(Reference.class, fieldIndex + 1));
            ctx.heap = new OperationExpression(Operator.PUT, ctx.heap,
                    fieldAddress, value);
        }
    }

    /**
     * Add an object and everything it references into a symbolic heap
     * 
     * @param ctx
     * @param o
     * @return
     */
    public Expression addObjectToHeap(Context ctx, Object o) {
        final Expression address = requestAddress(ctx, o);
        variableObjects.put(o, VariableDepth.None);
        while (!objectQueue.isEmpty()) {
            Object currentObject = objectQueue.poll();
            Class<?> c = currentObject.getClass();
            final Expression objectAddress = addressMap.get(currentObject);

            if (c.isArray()) {
                putArrayIntoHeap(ctx, currentObject, objectAddress);
            } else {
                putObjectIntoHeap(ctx, currentObject, objectAddress);
            }
        }
        return address;
    }

    /**
     * Insert the value of a variable in a satisfying model into the object
     * 
     * @author mfunk@tzi.de
     */
    static interface InsertPoint {
        public void put(Constant c);
    }

    /**
     * Don't insert, variable was a parameter
     */
    static class NopInsertPoint implements InsertPoint {
        @Override
        public void put(Constant c) {
            // Nop
        }
    }

    /**
     * Insert into an array
     */
    static class ArrayInsertPoint implements InsertPoint {
        private final Object array;
        private final int index;

        public ArrayInsertPoint(Object array, int index) {
            this.array = array;
            this.index = index;
        }

        @Override
        public void put(Constant c) {
            Class<?> type = getElementClass(array.getClass());
            if (!type.isPrimitive()) {
                throw new RuntimeException(
                        "Type " + type + " not reinsertable");
            }
            switch (type.toString()) {
            case "int":
                Array.setInt(array, index, c.getNumber().intValue());
                return;
            case "long":
                Array.setLong(array, index, c.getNumber().longValue());
                return;
            case "short":
                Array.setShort(array, index, c.getNumber().shortValue());
                return;
            case "byte":
                Array.setByte(array, index, c.getNumber().byteValue());
                return;
            case "float":
                Array.setFloat(array, index, c.getNumber().floatValue());
                return;
            case "double":
                Array.setDouble(array, index, c.getNumber().doubleValue());
                return;
            case "char":
                Array.setChar(array, index,
                        Character.toChars(c.getNumber().intValue())[0]);
                return;
            case "boolean":
                Array.setBoolean(array, index, (Boolean) c.getValue());
                return;
            }
        }
    }

    /**
     * Insert into an object attribute
     */
    static class ObjectInsertPoint implements InsertPoint {
        private final Object object;
        private final Field field;

        public ObjectInsertPoint(Object object, Field field) {
            this.object = object;
            this.field = field;
        }

        @Override
        public void put(Constant c) {
            // TODO: handle references here by accesing the heap inserter
            Class<?> type = field.getType();
            if (!type.isPrimitive()) {
                throw new RuntimeException(
                        "Type " + type + " not reinsertable");
            }
            try {
                switch (type.toString()) {
                case "int":
                    field.setInt(object, c.getNumber().intValue());
                    return;
                case "long":
                    field.setLong(object, c.getNumber().longValue());
                    return;
                case "short":
                    field.setShort(object, c.getNumber().shortValue());
                    return;
                case "byte":
                    field.setByte(object, c.getNumber().byteValue());
                    return;
                case "float":
                    field.setFloat(object, c.getNumber().floatValue());
                    return;
                case "double":
                    field.setDouble(object, c.getNumber().doubleValue());
                    return;
                case "char":
                    field.setChar(object,
                            Character.toChars(c.getNumber().intValue())[0]);
                    return;
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Given an Object looks if there exists an replacement in the iatfs package,
     * if that is the case a iatfs object for the given object if created and returned
     * otherwise the original object is returned
     * 
     * @param o the object to be replaced
     * @return original or new object
     */
    private static Object replaceWithIatfs(Object o) {
        Class<?> c = o.getClass();
        String newname = "gps.bytecode.iatfs." + c.getName();
        Class<?> nclass;
        try {
            nclass = ClassLoader.getSystemClassLoader().loadClass(newname);
        } catch (ClassNotFoundException e) {
            return o;
        }
        try {
            return nclass.getConstructor(c).newInstance(o);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("iatfs class " + nclass.getName()
                    + " should have a constructor for " + c.getName());
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | SecurityException
                | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
