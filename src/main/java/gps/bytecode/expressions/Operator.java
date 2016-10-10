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
package gps.bytecode.expressions;

/**
 * Enum administrating all the different operators that might be used in the
 * expressions
 * 
 * @author marlonfl
 *
 */
public enum Operator {
    /**
     * An operator consists of a symbolic string representation, the number of
     * parameters used by the operator and the output/input types
     * 
     * TODO: Add missing operators
     */
    ADD("+", 2, Type.NUMBERS, Type.NUMBERS),

    SUB("-", 2, Type.NUMBERS, Type.NUMBERS),

    MUL("*", 2, Type.NUMBERS, Type.NUMBERS),

    DIV("/", 2, Type.NUMBERS, Type.NUMBERS),

    REM("rem", 2, Type.NUMBERS, Type.NUMBERS),

    ITE("ite", 3, Type.ALL_TYPES, Type.ALL_TYPES),

    GREATER(">", 2, Type.NUMBERS, Type.BOOLEAN),

    GREATER_OR_EQUAL(">=", 2, Type.NUMBERS, Type.BOOLEAN),

    LESS("<", 2, Type.NUMBERS, Type.BOOLEAN),

    LESS_OR_EQUAL("<=", 2, Type.NUMBERS, Type.BOOLEAN),

    EQUAL("=", 2, Type.ALL_TYPES, Type.BOOLEAN),

    NOT_EQUAL("!=", 2, Type.ALL_TYPES, Type.BOOLEAN),

    COMPARE("cmp", 2, Type.NUMBERS, Type.INT),

    // bitwise or on 2 numbers
    OR("bvor", 2, Type.NUMBERS, Type.NUMBERS),

    // bitwise and on 2 numbers
    AND("bvand", 2, Type.NUMBERS, Type.NUMBERS),

    // bitwise xor
    XOR("bvxor", 2, Type.NUMBERS, Type.NUMBERS),

    // bitwise shift left
    SHL("bvshl", 1, Type.INTLONG, Type.INTLONG),

    // bitwise shift right
    SHR("bvshr", 1, Type.INTLONG, Type.INTLONG),

    NEG("neg", 1, Type.NUMBERS, Type.NUMBERS),

    NOT("not", 1, Type.BOOLEAN, Type.BOOLEAN),

    BAND("and", 2, Type.BOOLEAN, Type.BOOLEAN),

    BOR("or", 2, Type.BOOLEAN, Type.BOOLEAN),

    TO_BYTE("toByte", 1, Type.INT, Type.BYTE),

    TO_DOUBLE("toDouble", 1, Type.NUMBERS, Type.DOUBLE),

    TO_CHAR("toChar", 1, Type.INT, Type.CHAR),

    TO_FLOAT("toFloat", 1, Type.NUMBERS, Type.FLOAT),

    TO_INT("toInt", 1, Type.NUMBERSBOOL, Type.INT),

    TO_LONG("toLong", 1, Type.NUMBERS, Type.LONG),

    TO_SHORT("toShort", 1, Type.NUMBERS, Type.SHORT),

    TO_BOOL("toBool", 1, Type.INT, Type.BOOLEAN),

    PUT("put", 3, Type.ALL_TYPES, Type.ALL_TYPES),

    GET("get", 2, Type.ALL_TYPES, Type.ALL_TYPES);

    private final String opSymbol;

    private final int parameters;

    private final Class<?>[] inputTypes;

    private final Class<?>[] outputTypes;

    private Operator(String opSymbol, int parameters, Class<?>[] inputTypes,
            Class<?>[] outputTypes) {
        this.opSymbol = opSymbol;
        this.parameters = parameters;
        this.inputTypes = inputTypes;
        this.outputTypes = outputTypes;
    }

    @Override
    public String toString() {
        return opSymbol;
    }

    /**
     * returns the number of parameters required for the operator
     */
    public int getParameterCount() {
        return parameters;
    }

    /**
     * returns the output type of the operator
     */
    public Class<?>[] getOutputTypes() {
        return outputTypes;
    }

    /**
     * returns the input type of the operator
     */
    public Class<?>[] getInputTypes() {
        return inputTypes;
    }

    /**
     * Returns true if this operator is a type cast.
     */
    public boolean isTypeCast() {
        return this == Operator.TO_BYTE || this == Operator.TO_CHAR
                || this == Operator.TO_DOUBLE || this == Operator.TO_FLOAT
                || this == Operator.TO_INT || this == Operator.TO_LONG
                || this == Operator.TO_SHORT;
    }

    /**
     * If for some reason one doesn't want to use the name of an operator, he
     * can access the operator via its string representation using this method
     * 
     * @param string
     *            representation
     * @return Operator
     * 
     * @throws InvalidOperationException
     *             if symbol doesn't belong to any operator
     */
    public static Operator getOpBySymbol(String symbol) {
        Operator operator = null;
        Operator[] values = Operator.values();
        for (int i = 0; i < values.length; i++) {
            if (values[i].opSymbol.equals(symbol)) {
                operator = values[i];
            }
        }
        if (operator == null) {
            throw new InvalidOperatorException(
                    symbol + " does not match any operator");
        }
        return operator;
    }

    public static class InvalidOperatorException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public InvalidOperatorException(String desc) {
            super("\n\nInvalid Operator: " + desc + "\n");
        }
    }

    /**
     * Inner class that has the different groups of input/output types as
     * variables because I couldn't figure out how to otherwise pass the lists
     * as parameters for the enum values
     * 
     * @author marlonfl
     *
     */
    static class Type {
        /**
         * these lists are used as constructor inputs for the enum elements'
         * input/output types. they are all possible combinations of the used
         * types
         */
        final static Class<?>[] NUMBERS = { byte.class, char.class, int.class,
                short.class, long.class, float.class, double.class,
                Reference.class, Undef.class };
        final static Class<?>[] NUMBERSBOOL = { boolean.class, byte.class,
                char.class, int.class, short.class, long.class, float.class,
                double.class, Reference.class, Undef.class };
        final static Class<?>[] INTLONG = { int.class, long.class,
                Undef.class };
        final static Class<?>[] INT = { int.class, Undef.class };
        final static Class<?>[] BYTE = { byte.class, Undef.class };
        final static Class<?>[] DOUBLE = { double.class, Undef.class };
        final static Class<?>[] CHAR = { char.class, Undef.class };
        final static Class<?>[] FLOAT = { float.class, Undef.class };
        final static Class<?>[] LONG = { long.class, Undef.class };
        final static Class<?>[] SHORT = { short.class, Undef.class };
        final static Class<?>[] BOOLEAN = { boolean.class, Undef.class };
        final static Class<?>[] ALL_TYPES = { byte.class, int.class,
                short.class, long.class, float.class, double.class,
                boolean.class, char.class, Reference.class, Heap.class,
                RuntimeType.class, Undef.class };
    }

    /**
     * Most important class in the whole program. The program will fail and your
     * hard drive will be wiped if you remove this class.
     */
    public class Undef {
    }

    /**
     * A reference to something on the simulated heap
     * Value is an int
     */
    public class Reference {
    }

    /**
     * Object representing a heap. Value is a List<Expression>
     */
    public class Heap {
    }

    /**
     * Represents a runtime type, i.e. that what Class<?> is for the JVM
     * Value is an int
     */
    public class RuntimeType {

    }

}
