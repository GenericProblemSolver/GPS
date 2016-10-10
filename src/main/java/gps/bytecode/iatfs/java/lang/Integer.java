/*
 * Copyright 1994-2006 Sun Microsystems, Inc.  All Rights Reserved.
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
 * .
 *
 * @author scheetz
 */
public final class Integer extends java.lang.Number {

    private static final long serialVersionUID = 1L;

    public static final int MIN_VALUE = 0x80000000;

    public static final int MAX_VALUE = 0x7fffffff;

    final static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
            'z' };

    public static java.lang.String toString(int i, int radix) {

        if (radix < java.lang.Character.MIN_RADIX
                || radix > java.lang.Character.MAX_RADIX) {
            radix = 10;
        }
        /* Use the faster version */
        if (radix == 10) {
            return toString(i);
        }

        char buf[] = new char[33];
        boolean negative = (i < 0);
        int charPos = 32;

        if (!negative) {
            i = -i;
        }

        while (i <= -radix) {
            buf[charPos--] = digits[-(i % radix)];
            i = i / radix;
        }
        buf[charPos] = digits[-i];

        if (negative) {
            buf[--charPos] = '-';
        }

        return new java.lang.String(buf, charPos, (33 - charPos));
    }

    public static java.lang.String toHexString(int i) {
        return toUnsignedString(i, 4);
    }

    public static java.lang.String toOctalString(int i) {
        return toUnsignedString(i, 3);
    }

    public static java.lang.String toBinaryString(int i) {
        return toUnsignedString(i, 1);
    }

    private static java.lang.String toUnsignedString(int i, int shift) {
        char[] buf = new char[32];
        int charPos = 32;
        int radix = 1 << shift;
        int mask = radix - 1;
        do {
            buf[--charPos] = digits[i & mask];
            i >>>= shift;
        } while (i != 0);

        return new java.lang.String(buf, charPos, (32 - charPos));
    }

    final static char[] DigitTens = { '0', '0', '0', '0', '0', '0', '0', '0',
            '0', '0', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '2',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3',
            '3', '3', '3', '3', '3', '3', '4', '4', '4', '4', '4', '4', '4',
            '4', '4', '4', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
            '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '7', '7', '7',
            '7', '7', '7', '7', '7', '7', '7', '8', '8', '8', '8', '8', '8',
            '8', '8', '8', '8', '9', '9', '9', '9', '9', '9', '9', '9', '9',
            '9', };

    final static char[] DigitOnes = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2',
            '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', };

    public static java.lang.String toString(int i) {
        if (i == java.lang.Integer.MIN_VALUE) {
            return "-2147483648";
        }
        int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);
        char[] buf = new char[size];
        getChars(i, size, buf);
        return new String();
    }

    static void getChars(int i, int index, char[] buf) {
        int q, r;
        int charPos = index;
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Generate two digits per iteration
        while (i >= 65536) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i <= 65536, i);
        for (;;) {
            q = (i * 52429) >>> (16 + 3);
            r = i - ((q << 3) + (q << 1)); // r = i-(q*10) ...
            buf[--charPos] = digits[r];
            i = q;
            if (i == 0) {
                break;
            }
        }
        if (sign != 0) {
            buf[--charPos] = sign;
        }
    }

    final static int[] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999,
            99999999, 999999999, java.lang.Integer.MAX_VALUE };

    // Requires positive x
    static int stringSize(int x) {
        for (int i = 0;; i++) {
            if (x <= sizeTable[i]) {
                return i + 1;
            }
        }
    }

    public static int parseInt(java.lang.String s, int radix) {
        int result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        int digit;

        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // Possible leading "-"
                if (firstChar == '-') {
                    negative = true;
                }
                i++;
            }
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++), radix);

                result *= radix;

                result -= digit;
            }
        }
        return negative ? result : -result;
    }

    public static int parseInt(java.lang.String s) {
        return parseInt(s, 10);
    }

    public static java.lang.Integer valueOf(java.lang.String s, int radix) {
        return new java.lang.Integer(parseInt(s, radix));
    }

    public static java.lang.Integer valueOf(java.lang.String s) {
        return new java.lang.Integer(parseInt(s, 10));
    }

    public static java.lang.Integer valueOf(int i) {
        return new java.lang.Integer(i);
    }

    private final int value;

    public Integer(int value) {
        this.value = value;
    }

    public Integer(java.lang.Integer oldObject) {
        value = oldObject.intValue();
    }

    public Integer(java.lang.String s) {
        this.value = parseInt(s, 10);
    }

    /**
     * Returns the value of this {@code Integer} as a {@code byte}.
     */
    public byte byteValue() {
        return (byte) value;
    }

    /**
     * Returns the value of this {@code Integer} as a {@code short}.
     */
    public short shortValue() {
        return (short) value;
    }

    /**
     * Returns the value of this {@code Integer} as an {@code int}.
     */
    public int intValue() {
        return value;
    }

    /**
     * Returns the value of this {@code Integer} as a {@code long}.
     */
    public long longValue() {
        return (long) value;
    }

    /**
     * Returns the value of this {@code Integer} as a {@code float}.
     */
    public float floatValue() {
        return (float) value;
    }

    /**
     * Returns the value of this {@code Integer} as a {@code double}.
     */
    public double doubleValue() {
        return (double) value;
    }

    public String toString() {
        return java.lang.String.valueOf(value);
    }

    public int hashCode() {
        return value;
    }

    /**
     * Compares this object to the specified object. The result is {@code true}
     * if and only if the argument is not {@code null} and is an {@code Integer}
     * object that contains the same {@code int} value as this object.
     *
     * @param obj
     *            the object to compare with.
     * @return {@code true} if the objects are the same; {@code false}
     *         otherwise.
     */
    public boolean equals(java.lang.Object obj) {
        if (obj instanceof java.lang.Integer) {
            return value == ((java.lang.Integer) obj).intValue();
        } else if (obj instanceof Integer) {
            int eqVal = ((Integer) obj).intValue();
            return value == eqVal;
        }
        return false;
    }

    public static java.lang.Integer getInteger(java.lang.String nm) {
        return getInteger(nm, null);
    }

    public static java.lang.Integer getInteger(java.lang.String nm, int val) {
        java.lang.Integer result = java.lang.Integer.getInteger(nm, null);
        return (result == null) ? new java.lang.Integer(val) : result;
    }

    public static java.lang.Integer getInteger(java.lang.String nm,
            java.lang.Integer val) {
        java.lang.String v = null;
        v = System.getProperty(nm);
        if (v != null) {
            return java.lang.Integer.decode(v);
        }
        return val;
    }

    public static java.lang.Integer decode(java.lang.String nm) {
        int radix = 10;
        int index = 0;
        boolean negative = false;
        java.lang.Integer result;
        char firstChar = nm.charAt(0);
        // Handle sign, if present
        if (firstChar == '-')

        {
            negative = true;
            index++;
        }

        // Handle radix specifier, if present
        if (nm.startsWith("0x", index) || nm.startsWith("0X", index))

        {
            index += 2;
            radix = 16;
        } else if (nm.startsWith("#", index))

        {
            index++;
            radix = 16;
        } else if (nm.startsWith("0", index) && nm.length() > 1 + index)

        {
            index++;
            radix = 8;
        }

        result = java.lang.Integer.valueOf(nm.substring(index), radix);
        result = negative ? new java.lang.Integer(-result.intValue()) : result;

        // If number is Integer.MIN_VALUE, we'll end up here. The next line
        // handles this case, and causes any genuine format error to be
        // rethrown.
        String constant = negative ? ("-" + nm.substring(index))
                : nm.substring(index);
        result = java.lang.Integer.valueOf(constant, radix);

        return result;

    }

    public int compareTo(java.lang.Integer anotherInteger) {
        int thisVal = this.value;
        int anotherVal = anotherInteger.intValue();
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
    }

    public static final int SIZE = 32;

    public static int highestOneBit(int i) {
        // HD, Figure 3-1
        i |= (i >> 1);
        i |= (i >> 2);
        i |= (i >> 4);
        i |= (i >> 8);
        i |= (i >> 16);
        return i - (i >>> 1);
    }

    public static int lowestOneBit(int i) {
        // HD, Section 2-1
        return i & -i;
    }

    public static int numberOfLeadingZeros(int i) {
        // HD, Figure 5-6
        if (i == 0) {
            return 32;
        }
        int n = 1;
        if (i >>> 16 == 0) {
            n += 16;
            i <<= 16;
        }
        if (i >>> 24 == 0) {
            n += 8;
            i <<= 8;
        }
        if (i >>> 28 == 0) {
            n += 4;
            i <<= 4;
        }
        if (i >>> 30 == 0) {
            n += 2;
            i <<= 2;
        } else {
            n -= i >>> 31;
        }
        return n;
    }

    public static int numberOfTrailingZeros(int i) {
        // HD, Figure 5-14
        int y;
        if (i == 0) {
            return 32;
        }
        int n = 31;
        y = i << 16;
        if (y != 0) {
            n = n - 16;
            i = y;
        }
        y = i << 8;
        if (y != 0) {
            n = n - 8;
            i = y;
        }
        y = i << 4;
        if (y != 0) {
            n = n - 4;
            i = y;
        }
        y = i << 2;
        if (y != 0) {
            n = n - 2;
            i = y;
        }
        return n - ((i << 1) >>> 31);
    }

    public static int bitCount(int i) {
        // HD, Figure 5-2
        i = i - ((i >>> 1) & 0x55555555);
        i = (i & 0x33333333) + ((i >>> 2) & 0x33333333);
        i = (i + (i >>> 4)) & 0x0f0f0f0f;
        i = i + (i >>> 8);
        i = i + (i >>> 16);
        return i & 0x3f;
    }

    public static int rotateLeft(int i, int distance) {
        return (i << distance) | (i >>> -distance);
    }

    public static int rotateRight(int i, int distance) {
        return (i >>> distance) | (i << -distance);
    }

    public static int reverse(int i) {
        // HD, Figure 7-1
        i = (i & 0x55555555) << 1 | (i >>> 1) & 0x55555555;
        i = (i & 0x33333333) << 2 | (i >>> 2) & 0x33333333;
        i = (i & 0x0f0f0f0f) << 4 | (i >>> 4) & 0x0f0f0f0f;
        i = (i << 24) | ((i & 0xff00) << 8) | ((i >>> 8) & 0xff00) | (i >>> 24);
        return i;
    }

    public static int signum(int i) {
        // HD, Section 2-7
        return (i >> 31) | (-i >>> 31);
    }

    public static int reverseBytes(int i) {
        return ((i >>> 24)) | ((i >> 8) & 0xFF00) | ((i << 8) & 0xFF0000)
                | ((i << 24));
    }

}
