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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.sun.tools.classfile.Annotation;
import com.sun.tools.classfile.Attribute;
import com.sun.tools.classfile.ClassFile;
import com.sun.tools.classfile.ConstantPool;
import com.sun.tools.classfile.ConstantPoolException;
import com.sun.tools.classfile.Descriptor;
import com.sun.tools.classfile.Descriptor.InvalidDescriptor;
import com.sun.tools.classfile.Method;
import com.sun.tools.classfile.RuntimeVisibleAnnotations_attribute;
import com.sun.tools.classfile.Signature_attribute;

/**
 * 
 * This class represents a disassembled method.
 * 
 * @author jowlo@uni-bremen.de
 *
 */
public class DMethod {

    @SuppressWarnings("unused")
    final private Method classFileMethod;

    final private ClassFile classFile;

    private String name;

    /**
     * Annotations that were present at runtime for this method.
     */
    private final List<String> annotations = new ArrayList<>();

    private final Map<Integer, DBlock> blockMap = new HashMap<>();

    public DMethod(final Method classFileMethod, final ClassFile classFile) {
        this.classFileMethod = classFileMethod;
        this.classFile = classFile;

        Descriptor desc = classFileMethod.descriptor;

        Signature_attribute sa = (Signature_attribute) classFileMethod.attributes
                .get(Attribute.Signature);
        if (sa != null) {
            desc = sa.getParsedSignature();
        }

        try {
            String clazz = classFile.getName();
            String params = desc.getParameterTypes(classFile.constant_pool);
            int hash = Objects.hash(clazz, params);

            name = classFileMethod.getName(classFile.constant_pool) + "_"
                    + Integer.toHexString(hash);
        } catch (final ConstantPoolException | InvalidDescriptor e) {
            throw new RuntimeException(
                    "Could not load class name, parameters or method name for DMethod.");
        }

        final Attribute annoAttr = classFileMethod.attributes
                .get(Attribute.RuntimeVisibleAnnotations);
        if (annoAttr instanceof RuntimeVisibleAnnotations_attribute) {
            final RuntimeVisibleAnnotations_attribute annotations = (RuntimeVisibleAnnotations_attribute) annoAttr;
            final Annotation[] anns = annotations.annotations;
            for (final Annotation a : anns) {
                try {
                    final ConstantPool constant_pool = classFile.constant_pool;
                    final Descriptor d = new Descriptor(a.type_index);
                    this.annotations.add(d.getFieldType(constant_pool));
                } catch (final ConstantPoolException ignore) {
                    System.err.println(ignore);
                } catch (final InvalidDescriptor ignore) {
                    System.err.println(ignore);
                }
            }
        }
    }

    /**
     * Returns the ConstantPool the Code of this Method acceses
     * 
     * @return
     */
    public ConstantPool getConstantPool() {
        return classFile.constant_pool;
    }

    /**
     * Adds a codeblock to this method
     * @param b
     */
    public void addBlock(final DBlock b) {
        blockMap.put(b.instructions.get(0).getPC(), b);
    }

    /**
     * Is this Method annotated with a specific annotation?
     * @param clazz
     * @return
     */
    public boolean hasAnnotation(
            final Class<? extends java.lang.annotation.Annotation> clazz) {
        final String name = clazz.getCanonicalName();
        return annotations.contains(name);
    }

    /**
     * Return a specific DBlock in this method. Indexing is done by byte offset.
     * This is useful for retrieving the next block that is jumped to after a
     * branching instruction.
     * 
     * @param offset
     *            starting byte of desired block.
     */
    public DBlock getBlock(final int byteOffset) {
        return blockMap.get(byteOffset);
    }

    public Map<Integer, DBlock> getBlockMap() {
        return blockMap;
    }

    public String getName() {
        return name;
    }

    /**
     * 
     * @return the name of the class this Method was defined in
     */
    public String getClassname() {
        try {
            return classFile.getName();
        } catch (ConstantPoolException e) {
            throw new RuntimeException(
                    "Could not get class name of DMethod " + name);
        }
    }

}
