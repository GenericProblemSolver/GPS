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
package gps.attribute;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gps.util.Tuple;

/**
 * Represents a Node for a tree of Attributes of a given Object.
 *
 * @author wahler@tzi.de
 */
public class AttributeValue implements IObject {

    /**
     * the fields of this node; can be empty
     */
    private final List<IObject> fields;

    /**
     * the primitive fields of this node; can be empty
     */
    private final List<IObject> primitives;

    /**
     * the value of this Node
     */
    private Object value;

    /**
     * the identifier of this node
     *
     * Contains the class of the value <code>Object</code>, its
     * <code>hash code</code>, its parent's <code>hash code</code> and, if
     * applicable, the index in its parental collection
     */
    private final String identifier;

    /**
     * the index of this node if the parental node is a list/array; is -1 if
     * parent is no list/array
     */
    private final int index;

    /**
     * Creates a new <code>AttributeValue</code> with the given Object as value.
     *
     * Alternate constructor which also takes the list index if applicable.
     *
     * @param pValue
     *            the object of this node
     * 
     * @param fieldName
     *            the name of the field
     *
     * @param parentField
     *            the {@link AttributeValue#identifier} of the parent
     * 
     * @param index
     *            the index of the given object in the parental list/array
     * 
     * @param fromRoot
     *            the path from the root to this node (in
     *            <code>hash codes</code>)
     */
    public AttributeValue(final Object pValue, final String fieldName,
            final String parentField, final int index) {
        value = pValue;
        fields = new ArrayList<>();
        primitives = new ArrayList<>();
        if (parentField == null) {
            identifier = fieldName;
        } else {
            identifier = parentField + "." + fieldName;
        }
        this.index = index;
    }

    /**
     * Returns this <code>Object</code>'s children. Can be empty.
     */
    @Override
    public List<IObject> getFields() {
        return fields;
    }

    /**
     * Returns this <code>Object</code>'s primitive children. Can be empty.
     */
    public List<IObject> getPrimitives() {
        return primitives;
    }

    /**
     * Checks whether this <code>Object</code> is primitive.
     *
     * @return <code>true</code>, when {@link TreeBuilder#PRIMITIVES} contains
     *         this class, <code>false</code> otherwise
     */
    @Override
    public boolean isPrimitive() {
        return AttributeGraph.PRIMITIVES.contains(value.getClass());
    }

    /**
     * Returns this <code>Object</code>'s value.
     */
    @Override
    public Object getValue() {
        return value;
    }

    /**
     * Changes the {@link AttributeValue#value} of this
     * <code>AttributeValue</code>.
     * 
     * Throws an {@link IllegalArgumentException} if the new value is
     * <code>null</code> or its class is different from the current one.
     * 
     * @param o
     *            the new <code>value</code>
     */
    @Override
    public void setValue(final Object o) {
        if (o == null) {
            throw new IllegalArgumentException("The new Object is null.");
        }
        if (!o.getClass().equals(value.getClass())) {
            throw new IllegalArgumentException(
                    "The new Object has a different type than the old one.");
        }
        List<Tuple<Object, List<Field>>> fs = new ArrayList<>();
        for (IObject io : fields) {
            fs.add(new Tuple<Object, List<Field>>(io.getValue(), Arrays
                    .asList(io.getValue().getClass().getDeclaredFields())));
        }
        for (Tuple<Object, List<Field>> t : fs) {
            for (Field f : t.getY()) {
                if (Modifier.isStatic(f.getModifiers())
                        || Modifier.isFinal(f.getModifiers())) {
                    continue;
                }
                f.setAccessible(true);
                try {
                    if (f.get(t.getX()) == value) {
                        f.set(t.getX(), o);
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        value = o;
    }

    /**
     * Returns this <code>AttributeValue</code>'s identifier.
     *
     * @return the identifier
     */
    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the index of this node in the parental collection/array.
     *
     * @return the index or -1 if parent is no collection/array
     */
    public int getIndex() {
        return index;
    }

    /**
     * Checks if two <code>AttributeValue</code> are equal.
     *
     * Two nodes are equal when their {@link AttributeValue#identifier}s are.
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null || !(o instanceof AttributeValue)) {
            return false;
        }
        return identifier.equals(((AttributeValue) o).identifier);
    }

    /**
     * Returns a hash code based on the {@link AttributeValue#identifier} of
     * this {@link AttributeValue}. As the contract for
     * {@link Object#hashCode()} states the value should not change unless a
     * field used in {@link Object#equals(Object)} changed.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(identifier.getBytes());
    }

    /**
     * Returns a <code>String</code> containing all connections of this node.
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(identifier + " connects to: \n");
        for (IObject io : fields) {
            sb.append("\t\t" + ((AttributeValue) io).identifier + "\n");
        }
        return sb.toString();
    }

}
