/*
 * Copyright 1996-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
*/

package gps.bytecode.iatfs.java.lang;

/**
 * 
 * @author akueck@uni-bremen.de, Nakul Saraiya, Joseph D. Darcy, for
 *         documentation see original java class
 * 
 *         for thrown Exception we return -1 or null
 */
public final class Byte extends java.lang.Number {
    public static final byte MIN_VALUE = -128;
    public static final byte MAX_VALUE = 127;
    // public static final Class<Byte> TYPE = (Class<Byte>)
    // Class.getPrimitiveClass("byte");

    public Byte(java.lang.Byte oldObject) {
        value = oldObject.byteValue();
    }

    public static java.lang.String toString(byte b) {
        return java.lang.Integer.toString((int) b, 10);
    }

    public static byte parseByte(String s, int radix) {
        int i = java.lang.Integer.parseInt(s, radix);
        if (i < MIN_VALUE || i > MAX_VALUE) {
            return -1; // Exception
        }
        return (byte) i;
    }

    public static byte parseByte(String s) {
        return parseByte(s, 10);
    }

    public static java.lang.Byte valueOf(byte b) {
        return new java.lang.Byte(b);
    }

    public static java.lang.Byte valueOf(String s, int radix) {
        return valueOf(parseByte(s, radix));
    }

    public static java.lang.Byte valueOf(String s) {
        return valueOf(s, 10);
    }

    public static java.lang.Byte decode(String nm) {
        int i = java.lang.Integer.decode(nm).intValue();
        if (i < MIN_VALUE || i > MAX_VALUE) {
            return null;// Exception
        }
        return valueOf((byte) i);
    }

    private final byte value;

    public Byte(byte value) {
        this.value = value;
    }

    public Byte(String s) {
        this.value = parseByte(s, 10);
    }

    public byte byteValue() {
        return value;
    }

    public short shortValue() {
        return (short) value;
    }

    public int intValue() {
        return (int) value;
    }

    public long longValue() {
        return (long) value;
    }

    public float floatValue() {
        return (float) value;
    }

    public double doubleValue() {
        return (double) value;
    }

    public java.lang.String toString() {
        return java.lang.Integer.toString((int) value);
    }

    public int hashCode() {
        return (int) value;
    }

    public boolean equals(java.lang.Object obj) {
        if (obj instanceof java.lang.Byte) {
            return value == ((java.lang.Byte) obj).byteValue();
        } else if (obj instanceof Byte) {
            byte eqVal = ((Byte) obj).byteValue();
            return value == eqVal;
        }
        return false;
    }

    public int compareTo(java.lang.Byte anotherByte) {
        // if we use byteValue directly in the compare-call, it does not work.
        byte necessarySplit = anotherByte.byteValue();
        return compare(value, necessarySplit);
    }

    public static int compare(byte f1, byte f2) {
        if (f1 < f2) {
            return -1;
        }
        if (f1 > f2) {
            return 1;
        }
        return 0;
    }

    public static final int SIZE = 8;

    /** use serialVersionUID from JDK 1.1. for interoperability */
    private static final long serialVersionUID = -7183698231559129828L;
}
