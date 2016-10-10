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
public final class Short extends java.lang.Number {

    public static final short MIN_VALUE = -32768;

    public static final short MAX_VALUE = 32767;

    // public static final Class<Short> TYPE = (Class<Short>)
    // Class.getPrimitiveClass("short");

    public Short(java.lang.Short oldObject) {
        value = oldObject.shortValue();
    }

    public static java.lang.String toString(short s) {
        return java.lang.Integer.toString((int) s, 10);
    }

    public static short parseShort(java.lang.String s, int radix) {
        int i = java.lang.Integer.parseInt(s, radix);
        if (i < MIN_VALUE || i > MAX_VALUE) {
            return -1;// Exception
        }
        return (short) i;
    }

    public static short parseShort(java.lang.String s) {
        return parseShort(s, 10);
    }

    public static java.lang.Short valueOf(java.lang.String s, int radix) {
        return new java.lang.Short(parseShort(s, radix));
    }

    public static java.lang.Short valueOf(java.lang.String s) {
        return valueOf(s, 10);
    }

    public static java.lang.Short valueOf(short s) {
        return new java.lang.Short(s);
    }

    public static java.lang.Short decode(java.lang.String nm) {
        int i = java.lang.Integer.decode(nm).intValue();
        if (i < MIN_VALUE || i > MAX_VALUE) {
            return null;// Exception
        }
        return valueOf((short) i);
    }

    private final short value;

    public Short(short value) {
        this.value = value;
    }

    public Short(java.lang.String s) {
        this.value = parseShort(s, 10);
    }

    public byte byteValue() {
        return (byte) value;
    }

    public short shortValue() {
        return value;
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
        return java.lang.String.valueOf((int) value);
    }

    public int hashCode() {
        return (int) value;
    }

    public boolean equals(java.lang.Object obj) {
        if (obj instanceof java.lang.Short) {
            return value == ((java.lang.Short) obj).shortValue();
        } else if (obj instanceof Short) {
            short eqVal = ((Short) obj).shortValue();
            return value == eqVal;
        }
        return false;
    }

    public int compareTo(java.lang.Short anotherShort) {
        short value2 = anotherShort.shortValue();
        return compare(value, value2);
    }

    public int compare(short short1, short short2) {
        return short1 - short2;
    }

    public static final int SIZE = 16;

    public static short reverseBytes(short i) {
        return (short) (((i & 0xFF00) >> 8) | (i << 8));
    }

    private static final long serialVersionUID = 7515723908773894738L;
}
