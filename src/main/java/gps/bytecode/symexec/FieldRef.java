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

import com.sun.tools.classfile.ConstantPool.CONSTANT_Fieldref_info;
import com.sun.tools.classfile.ConstantPool.CONSTANT_NameAndType_info;
import com.sun.tools.classfile.ConstantPool.CPInfo;
import com.sun.tools.classfile.ConstantPoolException;

/**
 * Represents a reference to a (static) field
 * 
 * @author ihritil
 *
 */
public class FieldRef {

    final String name;

    final String type;

    /**
     * Name of the class the field is in. Notably the name of the specific
     * class, when the static field might be in a superclass of that class.
     */
    final String className;

    /**
     * @param con
     *            Fieldref_info from a Classfile
     */
    public FieldRef(final CPInfo con) {
        CONSTANT_Fieldref_info cfield = (CONSTANT_Fieldref_info) con;
        try {
            CONSTANT_NameAndType_info ntInfo = cfield.getNameAndTypeInfo();
            name = ntInfo.getName();
            type = ntInfo.getType();
            className = cfield.getClassName().replaceAll("/", ".");
        } catch (ConstantPoolException e) {
            throw new RuntimeException(
                    "Error retrieving values for field reference: "
                            + e.getMessage());
        }
    }

    @Override
    public boolean equals(final Object ob) {
        final FieldRef ref = (FieldRef) ob;
        boolean equal = ref.name == name;
        equal = equal && ref.type == type;
        return equal;
    }

    @Override
    public int hashCode() {
        String identifier = name + type;
        return identifier.hashCode();
    }

}
