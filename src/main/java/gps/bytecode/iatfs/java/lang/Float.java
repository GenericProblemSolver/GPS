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

import gps.bytecode.iatfs.sun.misc.FloatingDecimal;

/**
 * 
 * @author akueck@uni-bremen.de, Lee Boynton, Arthur van Hoff, Joseph D. Darcy
 *         for documentation see original java class
 */
public class Float extends java.lang.Number {

    public static final float POSITIVE_INFINITY = 1.0f / 0.0f;
    public static final float NEGATIVE_INFINITY = -1.0f / 0.0f;
    public static final float NaN = 0.0f / 0.0f;
    public static final float MAX_VALUE = 0x1.fffffeP+127f;
    public static final float MIN_NORMAL = 0x1.0p-126f;
    public static final float MIN_VALUE = 0x0.000002P-126f;
    public static final int MAX_EXPONENT = 127;
    public static final int MIN_EXPONENT = -126;
    public static final int SIZE = 32;

    // public static final Class<Float> TYPE = Class.getPrimitiveClass("float");

    public Float(java.lang.Float oldObject) {
        value = oldObject.floatValue();
    }

    public static java.lang.String toString(float f) {
        return new FloatingDecimal(f).toJavaFormatString();
    }

    // toHexString not supported
    /*
     * public static String toHexString(float f) { if (Math.abs(f) <
     * FloatConsts.MIN_NORMAL && f != 0.0f) { String s =
     * Double.toHexString(FpUtils.scalb((double) f, DoubleConsts.MIN_EXPONENT -
     * FloatConsts.MIN_EXPONENT)); return s.replaceFirst("p-1022$", "p-126"); }
     * else { return Double.toHexString(f); } }
     */

    public static java.lang.Float valueOf(java.lang.String s) {
        return new java.lang.Float(
                FloatingDecimal.readJavaFormatString(s).floatValue());
    }

    public static java.lang.Float valueOf(float f) {
        return new java.lang.Float(f);
    }

    public static float parseFloat(java.lang.String s) {
        return FloatingDecimal.readJavaFormatString(s).floatValue();
    }

    static public boolean isNaN(float v) {
        return (v != v);
    }

    static public boolean isInfinite(float v) {
        return (v == POSITIVE_INFINITY) || (v == NEGATIVE_INFINITY);
    }

    private final float value;

    public Float(float value) {
        this.value = value;
    }

    public Float(double value) {
        this.value = (float) value;
    }

    public Float(java.lang.String s) {
        // REMIND: this is inefficient
        this(valueOf(s).floatValue());
    }

    public boolean isNaN() {
        return isNaN(value);
    }

    public boolean isInfinite() {
        return isInfinite(value);
    }

    public java.lang.String toString() {
        return java.lang.String.valueOf(value);
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

    public float floatValue() {
        return value;
    }

    public double doubleValue() {
        return (double) value;
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

    // not in the original way cause native floatToRawIntBits
    public boolean equals(java.lang.Object obj) {
        if (obj instanceof java.lang.Float) {
            return value == ((java.lang.Float) obj).floatValue();
        } else if (obj instanceof Float) {
            float eqVal = ((Float) obj).floatValue();
            return value == eqVal;
        }
        return false;
    }

    // not implemented cause native floatToRawIntBits
    /*
     * public static int floatToIntBits(float value) { int result =
     * floatToRawIntBits(value); // Check for NaN based on values of bit fields,
     * maximum // exponent and nonzero significand. if (((result &
     * FloatConsts.EXP_BIT_MASK) == FloatConsts.EXP_BIT_MASK) && (result &
     * FloatConsts.SIGNIF_BIT_MASK) != 0) { result = 0x7fc00000; } return
     * result; }
     */

    // natives
    // public static native int floatToRawIntBits(float value);
    // public static native float intBitsToFloat(int bits);

    public int compareTo(java.lang.Float anotherFloat) {
        float value2 = anotherFloat.floatValue();
        return java.lang.Float.compare(value, value2);
    }

    // our representation of compare cause of native used methods ind original
    public static int compare(float f1, float f2) {
        if (f1 < f2) {
            return -1;
        }
        if (f1 > f2) {
            return 1;
        }
        return 0;
    }

    private static final long serialVersionUID = -2671257302660747028L;

}
