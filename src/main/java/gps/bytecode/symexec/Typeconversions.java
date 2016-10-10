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

import gps.bytecode.expressions.Constant;
import gps.bytecode.expressions.Expression;
import gps.bytecode.expressions.OperationExpression;
import gps.bytecode.expressions.Operator;
import gps.bytecode.expressions.Operator.Reference;
import gps.bytecode.expressions.Operator.Undef;

/**
 * Provides some methods to handle type conversion of expressions
 * @author mfunk@tzi.de
 *
 */
public class Typeconversions {

    /**
     * Returns the type a given type is promoted to if placed on the stack
     * @param t
     * @return
     */
    public static Class<?> getStackType(Class<?> t) {
        if (t == byte.class || t == short.class || t == boolean.class
                || t == char.class) {
            return int.class;
        }
        return t;
    }

    /**
     * Converts an expression to the type it has on the simulated stack
     *
     * (byte, short, bool are promoted to int)
     * 
     * @param e
     * @return
     */
    public static Expression toStackType(Expression e) {
        return castToType(e, getStackType(e.getType()));
    }

    /**
     * Wraps an expression in a cast operation to a type
     * @param e
     * @param c
     * @return
     */
    public static Expression castToType(Expression e, Class<?> c) {
        Class<?> type = e.getType();
        if (type == c) {
            return e;
        }
        if (c == boolean.class) {
            return new OperationExpression(Operator.TO_BOOL, e);
        }
        if (c == float.class) {
            return new OperationExpression(Operator.TO_FLOAT, e);
        }
        if (c == double.class) {
            return new OperationExpression(Operator.TO_DOUBLE, e);
        }
        if (c == int.class) {
            return new OperationExpression(Operator.TO_INT, e);
        }
        if (c == short.class) {
            return new OperationExpression(Operator.TO_SHORT, e);
        }
        if (c == byte.class) {
            return new OperationExpression(Operator.TO_BYTE, e);
        }
        if (c == long.class) {
            return new OperationExpression(Operator.TO_LONG, e);
        }
        if (c == char.class) {
            return new OperationExpression(Operator.TO_CHAR, e);
        }
        throw new RuntimeException(
                "No known conversion from " + type + " to " + c);
    }

    /**
     * Returns the value that is read from invalid heap locations for a given type
     * @param c
     * @return
     */
    public static Expression getDefaultInvalidValue(Class<?> c) {
        if (c == boolean.class) {
            return new Constant(false);
        } else if (c == char.class) {
            return new Constant('y');
        } else if (c == double.class) {
            return new Constant(-42.23452d);
        } else if (c == float.class) {
            return new Constant(-42.32234f);
        } else {
            return new Constant(Undef.class, 0xdeadbeef);
        }
    }

    /**
     * Returns the value that is read from uninitialized heap locations for a given type
     * @param c
     * @return
     */
    public static Expression getDefaultValue(Class<?> c) {
        if (c == boolean.class) {
            return new Constant(false);
        }
        if (c == Reference.class) {
            return new Constant(Reference.class, -1);
        }
        return castToType(new Constant(0), c);
    }

    /**
     * Returns the type of an array of elements of type c
     * @param c
     * @return
     */
    public static Class<?> toArrayType(Class<?> c) {
        return java.lang.reflect.Array.newInstance(c, 0).getClass();
    }

    /**
     * Returns the type of an array of elements of type c
     * @param c
     * @return
     */
    public static Class<?> toMultiArrayType(Class<?> c, int dimensions) {
        Class<?> arrayType = c;
        for (int i = 0; i < dimensions; ++i) {
            arrayType = toArrayType(arrayType);
        }
        return arrayType;
    }
}
