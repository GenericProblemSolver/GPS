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

import java.util.List;

/**
 * Interface for the attribute representation of objects.
 * 
 * @author haker@uni-bremen.de
 * @author wahler@tzi.de
 */
public interface IObject {

    /**
     * Retrieve all fields that are contained within this object.
     * 
     * @return A list of fields.
     */
    public List<IObject> getFields();

    /**
     * Checks whether this object is a primitive type.
     * 
     * @return {@code true} if primitive, {@code false} otherwise.
     */
    public boolean isPrimitive();

    /**
     * Returns the identifier of this <code>IObject</code>.
     * 
     * @return a unique identifier
     */
    public String getIdentifier();

    /**
     * Get the value or reference of the current field.
     * 
     * @return The value or reference. Will never be <code>null</code>.
     */
    public Object getValue();

    /**
     * Set the value or reference of the current field.
     * 
     * @param o
     *            the new value; is not allowed to be <code>null</code>
     */
    public void setValue(Object o);

}
