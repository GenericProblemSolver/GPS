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
package gps.bytecode.disassemble;

import java.util.Objects;

import com.sun.tools.classfile.ConstantPool.CONSTANT_NameAndType_info;
import com.sun.tools.classfile.ConstantPool.CPRefInfo;
import com.sun.tools.classfile.ConstantPoolException;
import com.sun.tools.classfile.Descriptor;
import com.sun.tools.classfile.Descriptor.InvalidDescriptor;
import com.sun.tools.classfile.Method;
import com.sun.tools.classfile.ClassFile;
import com.sun.tools.classfile.ConstantPool;

/**
 * Wraps around a Methodref and provides utility functions and hashcode + equals
 * 
 * @author mfunk@tzi.de
 *
 */
public class MethodRef {
    public final CPRefInfo ref;

    public final ConstantPool constants;

    /**
     * Construct a MethodRef from a constantpool reference
     * 
     * @param ref
     */
    public MethodRef(CPRefInfo ref, ConstantPool constants) {
        this.ref = ref;
        this.constants = constants;
    }

    /**
     * Returns the Name of the class of the referenced method Separator is .
     * 
     * @return
     */
    public String getClassname() {
        try {
            return ref.getClassName().replace('/', '.');
        } catch (ConstantPoolException e) {
            throw new RuntimeException(
                    "Error getting class name for a method: " + e.getMessage());
        }
    }

    /**
     * Finds a method in a classfile that matches the methodref
     * 
     * @param cf
     * @return null if there is no matching method
     */
    public Method getMatchingMethod(ClassFile cf) {
        try {
            CONSTANT_NameAndType_info nt = ref.getNameAndTypeInfo();
            for (Method m : cf.methods) {
                boolean nameMatch = m.getName(cf.constant_pool)
                        .equals(nt.getName());
                boolean typeMatch = m.descriptor.getValue(cf.constant_pool)
                        .equals(nt.getType());
                if (nameMatch && typeMatch) {
                    return m;
                }
            }
        } catch (ConstantPoolException e) {
            throw new RuntimeException(
                    "Error getting matching method: " + e.getMessage());
        }
        // We don't throw an Exception here, but in the disassembleMethod method
        return null;
    }

    /**
     * Returns the number of declared parameters of this method reference This
     * is _without_ the implicit this parameter for invokevirtual
     * 
     * @return
     */
    public int getNumberOfParameters() {
        try {
            CONSTANT_NameAndType_info nt = ref.getNameAndTypeInfo();
            Descriptor d = new Descriptor(nt.type_index);
            return d.getParameterCount(constants);
        } catch (ConstantPoolException | InvalidDescriptor e) {
            throw new RuntimeException(
                    "Error getting number of parameters: " + e.getMessage());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MethodRef)) {
            return false;
        }
        MethodRef o = (MethodRef) obj;
        try {
            boolean classEq = Objects.equals(getClassname(), o.getClassname());

            CONSTANT_NameAndType_info nt = ref.getNameAndTypeInfo();
            CONSTANT_NameAndType_info ont = o.ref.getNameAndTypeInfo();

            boolean nameEq = Objects.equals(nt.getName(), ont.getName());
            boolean typeEq = Objects.equals(nt.getType(), ont.getType());
            return classEq && nameEq && typeEq;
        } catch (ConstantPoolException e) {
            throw new RuntimeException(
                    "Error getting information of MethodRef: "
                            + e.getMessage());
        }
    }

    @Override
    public int hashCode() {
        try {
            CONSTANT_NameAndType_info nt = ref.getNameAndTypeInfo();
            return Objects.hash(getClassname(), nt.getName(), nt.getType());
        } catch (ConstantPoolException e) {
            throw new RuntimeException(
                    "Error getting hashCode of MethodRef: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        try {
            CONSTANT_NameAndType_info nt = ref.getNameAndTypeInfo();
            return getClassname() + ":" + nt.getName() + nt.getType();
        } catch (ConstantPoolException e) {
            throw new RuntimeException(
                    "Error creating String for MethodRef: " + e.getMessage());
        }
    }

    /**
     * Is the method referenced by this method refernce a constructor?
     * @return
     */
    public boolean isConstructor() {
        try {
            return ref.getNameAndTypeInfo().getName().equals("<init>");
        } catch (ConstantPoolException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * MethodRef that overrides the original classname
     * @author mfunk@tzi.de
     *
     */
    public static class ConstructedMethodRef extends MethodRef {
        String classname;

        public ConstructedMethodRef(MethodRef real, String classname) {
            super(real.ref, real.constants);
            this.classname = classname.replace('/', '.');
        }

        @Override
        public String getClassname() {
            return classname;
        }
    }

}
