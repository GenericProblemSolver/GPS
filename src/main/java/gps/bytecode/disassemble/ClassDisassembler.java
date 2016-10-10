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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sun.tools.classfile.Attribute;
import com.sun.tools.classfile.ClassFile;
import com.sun.tools.classfile.Code_attribute;
import com.sun.tools.classfile.ConstantPoolException;
import com.sun.tools.classfile.Instruction;
import com.sun.tools.classfile.Method;
import com.sun.tools.classfile.Opcode;

/**
 * Contains static methods to disassemble classfiles into DMethods
 *
 * @author mfunk@tzi.de
 *
 */

public class ClassDisassembler {

    /**
     * get the corresponding ClassFile for a given classname Uses the same
     * Method as the JVM for class resolution
     *
     * @param classname
     *            fully qualified name of a class
     * @return ClassFile representation of a classfile or null if file could not
     *         be loaded
     */
    public static ClassFile getClassFile(final String classname) {
        try {
            final Class<?> c = ClassLoader.getSystemClassLoader()
                    .loadClass(classname);
            return getClassFile(c);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static ClassFile getClassFile(final Class<?> c) {
        try {
            final String filename = c.getName().replaceAll("\\.", "/")
                    + ".class";
            if (c.getProtectionDomain().getCodeSource() == null) {
                final InputStream is = ClassLoader.getSystemClassLoader()
                        .getResourceAsStream(filename);
                return ClassFile.read(is);
            } else {
                //Use URI.getPath to decode escape sequences for spaces, etc
                final URI uri = c.getProtectionDomain().getCodeSource()
                        .getLocation().toURI();
                final String path = uri.getPath()
                        + c.getName().replaceAll("\\.", "/") + ".class";
                return ClassFile.read(new File(path));
            }
        } catch (IOException | ConstantPoolException | URISyntaxException e) {
            // We don't throw a RuntimeException here, but in
            // disassembleMethod()
            return null;
        }
    }

    /**
     * Given a classfile, creates a list of all defined Methods
     *
     * @param cf
     *            ClassFile
     * @return List of DMethods
     */
    public static List<DMethod> disassembleMethods(final ClassFile cf) {
        final List<DMethod> result = new ArrayList<>();
        for (final Method m : cf.methods) {
            final Attribute c_attr = m.attributes.get(Attribute.Code);
            if (c_attr instanceof Code_attribute) {
                final Code_attribute code = (Code_attribute) c_attr;
                result.add(createDMethod(code.getInstructions(), m, cf));
            }
        }
        return result;
    }

    /**
     * Loads a classfile containing a class with a certain name
     * 
     * If there exists an iatfs override for the class, it is returned instead
     * 
     * @param name name of the class
     * @return
     */
    public static ClassFile loadClass(String name) {
        //First try: look if there is a replacement class for it
        ClassFile cf = getClassFile("gps.bytecode.iatfs." + name);
        //Second try: look for the real class
        if (cf == null) {
            cf = getClassFile(name);
        }
        // Else: fail
        if (cf == null) {
            throw new RuntimeException("Could not load class " + name);
        }
        return cf;
    }

    /**
     * Creates a disassembled DMethod given a MethodRef
     * @param mref
     * @return
     */
    public static DMethod disassembleMethod(final MethodRef mref) {
        ClassFile cf = loadClass(mref.getClassname());

        Method m = mref.getMatchingMethod(cf);
        if (m == null) {
            throw new RuntimeException("Error loading method " + mref);
        }
        final Code_attribute code = (Code_attribute) m.attributes
                .get(Attribute.Code);
        return createDMethod(code.getInstructions(), m, cf);
    }

    /**
     * Tries to find a matching method in a class or a superclass of it
     * @param mref Reference to a virtual method
     * @param classname classname to start in looking for the method
     * @return
     */
    public static DMethod disassembleVirtualMethod(final MethodRef mref,
            String classname) {
        if (classname == null) {
            classname = mref.getClassname();
        }
        ClassFile cf = loadClass(classname);
        Method m = null;
        try {
            while (m == null && cf.super_class != 0 && !cf.getName()
                    .equals("gps/bytecode/iatfs/java/lang/Object")) {
                m = mref.getMatchingMethod(cf);
                if (m == null) {
                    try {
                        cf = loadClass(
                                cf.getSuperclassName().replace('/', '.'));
                    } catch (ConstantPoolException e) {

                    }
                }
            }
        } catch (ConstantPoolException e) {
            e.printStackTrace();
        }
        if (m == null) {
            return null;
        }
        final Code_attribute code = (Code_attribute) m.attributes
                .get(Attribute.Code);
        return createDMethod(code.getInstructions(), m, cf);
    }

    /**
     * Creates a DMethod from Intructions
     * 
     * @param instrs
     *            Iterable of the Bytecode instructions in this Method
     * @param classfileMethod
     *            the reference to the Method in the classfile
     * @param cf
     *            the classfile
     * @return
     */
    public static DMethod createDMethod(final Iterable<Instruction> instrs,
            final Method classfileMethod, final ClassFile cf) {
        final DMethod result = new DMethod(classfileMethod, cf);

        // Create a Set of blockStarts by getting the jump destinations of all
        // jump instructions
        // Use a Set because we want to avoid duplicates in the result
        // First start should be 0 since it is sorted and the lowest possible
        // address
        final Set<Integer> blockStarts = new HashSet<>();
        blockStarts.add(0);

        for (Instruction i : instrs) {
            if (isBranch(i)) {
                blockStarts.addAll(getJmps(i));
            }
        }

        final ArrayList<Integer> sortedBlockStarts = new ArrayList<>(
                blockStarts);
        Collections.sort(sortedBlockStarts);

        // Put all instructions into a DBlock until we reach the next block
        // start
        // Put the old DBlock into the DMethod and wait for the next block start
        DBlock b = null;
        int blockstart = 0;
        for (Instruction i : instrs) {
            if (blockstart < sortedBlockStarts.size()
                    && i.getPC() == sortedBlockStarts.get(blockstart)) {
                if (b != null) {
                    result.addBlock(b);
                }
                blockstart += 1;

                b = new DBlock(result);
            }
            b.instructions.add(i);
        }
        if (b.instructions.size() > 0) {
            result.addBlock(b);
        }

        return result;
    }

    /**
     * Is the Instruction a branch instruction (includes uncoditional jumps and
     * returns)
     *
     * @param i
     * @return
     */
    static boolean isBranch(final Instruction i) {
        switch (i.getKind()) {
        case BRANCH:
            return true;
        case BRANCH_W:
            return true;
        case DYNAMIC:
            return (i.getOpcode().equals(Opcode.TABLESWITCH)
                    || i.getOpcode().equals(Opcode.LOOKUPSWITCH));
        case NO_OPERANDS:
            return isReturn(i.getOpcode());
        case CPREF_W:
            return isInvoke(i);
        case CPREF_W_UBYTE_ZERO:
            return isInvoke(i);
        default:
            return false;
        }
    }

    /**
     * Is the instruction a method call (invoke)
     *
     * @param i
     * @return
     */
    static boolean isInvoke(final Instruction i) {
        switch (i.getOpcode()) {
        case INVOKEDYNAMIC:
        case INVOKEVIRTUAL:
        case INVOKESTATIC:
        case INVOKESPECIAL:
        case INVOKEINTERFACE:
            return true;
        default:
            return false;
        }
    }

    /**
     * Is the Opcode one of the Return Opcodes
     * 
     * @param o
     * @return
     */
    static boolean isReturn(final Opcode o) {
        switch (o) {
        case RETURN:
            return true;
        case IRETURN:
            return true;
        case FRETURN:
            return true;
        case DRETURN:
            return true;
        case ARETURN:
            return true;
        default:
            return false;
        }
    }

    /**
     * Returns a List of all possible jumps from an Instruction
     *
     * List contains one element if the jump is uncoditional or a return List
     * contains two elements if jump is conditional
     *
     * @param i
     * @return
     */
    static List<Integer> getJmps(final Instruction i) {
        final List<Integer> jmps = new ArrayList<>();
        jmps.add(getJmp(i));
        if (isReturn(i.getOpcode()) || isUnconditional(i.getOpcode())) {
            return jmps;
        }
        // add non-default jump destinations for lookupswitch
        // default in getJmp
        if (i.getOpcode().equals(Opcode.LOOKUPSWITCH)) {
            int offset = 4 - i.getPC() % 4;
            int pairs = i.getInt(4 + offset);
            for (int x = 0; x < pairs; x++) {
                jmps.add(i.getInt(12 + offset + 8 * x) + i.getPC());
            }
        }
        // add non-default jump destinations for tableswitch
        // default in getJmp
        if (i.getOpcode().equals(Opcode.TABLESWITCH)) {
            int offset = 4 - i.getPC() % 4;
            int lowInt = i.getInt(4 + offset);
            int highInt = i.getInt(8 + offset);
            int entries = highInt - lowInt + 1;
            for (int x = 0; x < entries; x++) {
                jmps.add(i.getInt(12 + offset + 4 * x) + i.getPC());
            }
        }
        jmps.add(i.getPC() + i.length());
        return jmps;
    }

    /**
     * Get the jump destination address of a jump instruction
     * 
     * @param i
     * @return -1 if it is not a jump instruction, Integer.MAX_VALUE if it is a
     *         return
     */
    static int getJmp(final Instruction i) {
        switch (i.getKind()) {
        case BRANCH:
            return i.getShort(1) + i.getPC();
        case BRANCH_W:
            return i.getInt(1) + i.getPC();
        case NO_OPERANDS:
            return Integer.MAX_VALUE;
        case DYNAMIC:
            // Returns default jump target for lookupswitch and tableswitch. The
            // other possible targets are added in getJmps.
            if (i.getOpcode().equals(Opcode.LOOKUPSWITCH)
                    || i.getOpcode().equals(Opcode.TABLESWITCH)) {
                int num = i.getInt(4 - i.getPC() % 4) + i.getPC();
                return num;
            } else {
                return -1;
            }
        case CPREF_W:
            return 0;
        case CPREF_W_UBYTE_ZERO:
            return 0;
        default:
            return -1;
        }
    }

    /**
     * Is the Opcode an unconditional Jump opcode?
     * 
     * @param o
     * @return
     */
    static boolean isUnconditional(final Opcode o) {
        switch (o) {
        case GOTO:
            return true;
        case JSR:
            return true;
        case GOTO_W:
            return true;
        case JSR_W:
            return true;
        default:
            return false;
        }
    }
}
