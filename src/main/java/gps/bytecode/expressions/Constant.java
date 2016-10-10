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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import gps.bytecode.exceptions.InternalErrorException;
import gps.bytecode.expressions.Operator.Heap;

/**
 * Class representing an atomic constant inside an Expression
 * 
 * @author marlonfl
 *
 */
public class Constant extends Expression {

    /**
     * type of the constant
     */
    private Class<?> type;

    /**
     * concrete value of this constant, depends on the type of the constant
     */
    private Object value;

    /**
     * constructors for every type we use in our internal expressions
     * 
     * TODO: there's gotta be a nicer way to do this
     */
    public Constant(int value) {
        this.value = value;
        type = int.class;
    }

    public Constant(long value) {
        this.value = value;
        type = long.class;
    }

    public Constant(short value) {
        this.value = value;
        type = short.class;
    }

    public Constant(float value) {
        this.value = value;
        type = float.class;
    }

    public Constant(double value) {
        this.value = value;
        type = double.class;
    }

    public Constant(byte value) {
        this.value = value;
        type = byte.class;
    }

    public Constant(boolean value) {
        this.value = value;
        type = boolean.class;
    }

    public Constant(char value) {
        this.value = value;
        type = char.class;
    }

    public Constant(Class<?> c, Object value) {
        this.value = value;
        type = c;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Constant)) {
            return false;
        }
        Constant e = (Constant) o;
        if (e.hashCode() != this.hashCode()) {
            return false;
        }
        if (!e.value.equals(this.value)) {
            return false;
        }
        if (!e.type.equals(this.type)) {
            return false;
        }
        return true;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public void setType(Class<?> type) {
        this.type = type;
    }

    @Override
    public void accept(IExpressionVisitor visitor) {
        visitor.visitConstant(this);
    }

    @Override
    public List<FunctionCall> getFunctionCalls() {
        return new ArrayList<>();
    }

    public Object getValue() {
        return value;
    }

    public Number getNumber() {
        if (type == boolean.class) {
            return ((Boolean) value) ? 1 : 0;
        } else if (type == char.class) {
            return Character.getNumericValue((Character) value);
        }
        return (Number) value;
    }

    @SuppressWarnings("unchecked")
    public static List<Expression> getHeap(Constant c) {
        if (c.getType() != Heap.class) {
            throw new InternalErrorException(
                    "Constant not a Heap when it should be");
        }
        return (List<Expression>) c.getValue();
    }
}
