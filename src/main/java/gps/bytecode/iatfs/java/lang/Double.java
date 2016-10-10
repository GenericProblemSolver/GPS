/*
 * Copyright 1994-2006 Sun Microsystems, Inc.  All Rights Resered.
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
 * @author scheetz
 */
public final class Double extends java.lang.Number {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Double(java.lang.Double oldObject) {
        value = oldObject.doubleValue();
    }

    /**
     * A constant holding the positive infinity of type {@code double}. It is
     * equal to the value returned by
     * {@code Double.longBitsToDouble(0x7ff0000000000000L)}.
     */
    public static final double POSITIVE_INFINITY = 1.0 / 0.0;

    /**
     * A constant holding the negative infinity of type {@code double}. It is
     * equal to the value returned by
     * {@code Double.longBitsToDouble(0xfff0000000000000L)}.
     */
    public static final double NEGATIVE_INFINITY = -1.0 / 0.0;

    /**
     * A constant holding a Not-a-Number (NaN) value of type {@code double}. It
     * is equivalent to the value returned by
     * {@code Double.longBitsToDouble(0x7ff8000000000000L)}.
     */
    public static final double NaN = 0.0d / 0.0;

    public static final double MAX_VALUE = 0x1.fffffffffffffP+1023; // 1.7976931348623157e+308

    public static final double MIN_NORMAL = 0x1.0p-1022; // 2.2250738585072014E-308

    public static final double MIN_VALUE = 0x0.0000000000001P-1022; // 4.9e-324

    public static final int MAX_EXPONENT = 1023;

    public static final int MIN_EXPONENT = -1022;

    public static final int SIZE = 64;

    static public boolean isNaN(double v) {
        return (v != v);
    }

    static public boolean isInfinite(double v) {
        return (v == POSITIVE_INFINITY) || (v == NEGATIVE_INFINITY);
    }

    /**
     * The value of the Double.
     *
     * @serial
     */
    private final double value;

    public Double(double value) {
        this.value = value;
    }

    public int hashCode() {
        double hash = value * value;
        while (hash < Integer.MAX_VALUE) {
            hash = hash * 10;
        }
        hash = hash / 10;
        int ret = (int) hash;
        return ret;
    }

    public boolean equals(java.lang.Object obj) {
        if (obj instanceof java.lang.Double) {
            return value == ((java.lang.Double) obj).doubleValue();
        } else if (obj instanceof Double) {
            double eqVal = ((Double) obj).doubleValue();
            return value == eqVal;
        }
        return false;
    }

    public boolean isNaN() {
        return isNaN(value);
    }

    public boolean isInfinite() {
        return isInfinite(value);
    }

    public String toString() {
        return java.lang.String.valueOf(value);
    }

    public static java.lang.Double valueOf(double d) {
        return new java.lang.Double(d);
    }

    public byte byteValue() {
        return (byte) value;
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

    /**
     * Returns the {@code float} value of this {@code Double} object.
     *
     * @return the {@code double} value represented by this object converted to
     *         type {@code float}
     * @since JDK1.0
     */
    public float floatValue() {
        return (float) value;
    }

    public double doubleValue() {
        return (double) value;
    }

    public static int compare(double d1, double d2) {
        if (d1 < d2) {
            return -1;
        } else if (d1 > d2) {
            return 1;
        } else {
            return 0;
        }

    }

    public int compareTo(java.lang.Double anotherDouble) {
        return Double.compare(value, anotherDouble.intValue());
    }

}
