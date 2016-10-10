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
public class Long extends java.lang.Number {

    public static final long MIN_VALUE = 0x8000000000000000L;

    public static final long MAX_VALUE = 0x7fffffffffffffffL;

    // public static final Class<Long> TYPE = (Class<Long>)
    // Class.getPrimitiveClass("long");

    public Long(java.lang.Long oldObject) {
        value = oldObject.longValue();
    }

    public static java.lang.String toString(long i, int radix) {
        if (radix < java.lang.Character.MIN_RADIX
                || radix > java.lang.Character.MAX_RADIX) {
            radix = 10;
        }
        if (radix == 10) {
            return toString(i);
        }
        char[] buf = new char[65];
        int charPos = 64;
        boolean negative = (i < 0);
        if (!negative) {
            i = -i;
        }
        while (i <= -radix) {
            buf[charPos--] = Integer.digits[(int) (-(i % radix))];
            i = i / radix;
        }
        buf[charPos] = Integer.digits[(int) (-i)];
        if (negative) {
            buf[--charPos] = '-';
        }
        return new java.lang.String(buf, charPos, (65 - charPos));
    }

    public static java.lang.String toHexString(long i) {
        return toUnsignedString(i, 4);
    }

    public static java.lang.String toOctalString(long i) {
        return toUnsignedString(i, 3);

    }

    public static java.lang.String toBinaryString(long i) {
        return toUnsignedString(i, 1);
    }

    private static java.lang.String toUnsignedString(long i, int shift) {
        char[] buf = new char[64];
        int charPos = 64;
        int radix = 1 << shift;
        long mask = radix - 1;
        do {
            buf[--charPos] = Integer.digits[(int) (i & mask)];
            i >>>= shift;
        } while (i != 0);
        return new java.lang.String(buf, charPos, (64 - charPos));
    }

    public static java.lang.String toString(long i) {
        if (i == java.lang.Long.MIN_VALUE) {
            return "-9223372036854775808";
        }
        int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);
        char[] buf = new char[size];
        getChars(i, size, buf);
        return new java.lang.String(buf, size, 0);
    }

    static void getChars(long i, int index, char[] buf) {
        long q;
        int r;
        int charPos = index;
        char sign = 0;
        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i > Integer.MAX_VALUE) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            buf[--charPos] = Integer.DigitOnes[r];
            buf[--charPos] = Integer.DigitTens[r];
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int) i;
        while (i2 >= 65536) {
            q2 = i2 / 100;
            // really: r = i2 - (q * 100);
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            buf[--charPos] = Integer.DigitOnes[r];
            buf[--charPos] = Integer.DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i2 <= 65536, i2);
        for (;;) {
            q2 = (i2 * 52429) >>> (16 + 3);
            r = i2 - ((q2 << 3) + (q2 << 1)); // r = i2-(q2*10) ...
            buf[--charPos] = Integer.digits[r];
            i2 = q2;
            if (i2 == 0) {
                break;
            }
        }
        if (sign != 0) {
            buf[--charPos] = sign;
        }
    }

    // Requires positive x
    static int stringSize(long x) {
        long p = 10;
        for (int i = 1; i < 19; i++) {
            if (x < p) {
                return i;
            }
            p = 10 * p;
        }
        return 19;
    }

    public static long parseLong(java.lang.String s, int radix) {
        if (s == null) {
            return -1;// Exception NumberFormat
        }
        if (radix < java.lang.Character.MIN_RADIX) {
            return -1;// Exception NumberFormat
        }
        if (radix > java.lang.Character.MAX_RADIX) {
            return -1;// Exception NumberFormat
        }
        long result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        long limit = -java.lang.Long.MAX_VALUE;
        long multmin;
        int digit;
        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // Possible leading "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = java.lang.Long.MIN_VALUE;
                } else {
                    return -1; // Exception NumberFormat
                }

                if (len == 1) {// Cannot have lone "-"
                    return -1;// Exception NumberFormat
                }
                i++;
            }
            multmin = limit / radix;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = java.lang.Character.digit(s.charAt(i++), radix);
                if (digit < 0) {
                    return -1;// Exception NumberFormat
                }
                if (result < multmin) {
                    return -1;// Exception NumberFormat
                }
                result *= radix;
                if (result < limit + digit) {
                    return -1;// Exception NumberFormat
                }
                result -= digit;
            }
        } else {
            return -1;// Exception NumberFormat
        }
        return negative ? result : -result;
    }

    public static long parseLong(java.lang.String s) {
        return parseLong(s, 10);
    }

    public static java.lang.Long valueOf(java.lang.String s, int radix) {
        return new java.lang.Long(parseLong(s, radix));
    }

    public static java.lang.Long valueOf(java.lang.String s) {
        return new java.lang.Long(parseLong(s, 10));
    }

    public static java.lang.Long valueOf(long l) {
        return new java.lang.Long(l);
    }

    // cant support decode, try,catch block
    /*
     * public static Long decode(String nm) throws NumberFormatException { int
     * radix = 10; int index = 0; boolean negative = false; Long result;
     * 
     * if (nm.length() == 0) throw new NumberFormatException(
     * "Zero length string"); char firstChar = nm.charAt(0); // Handle sign, if
     * present if (firstChar == '-') { negative = true; index++; }
     * 
     * // Handle radix specifier, if present if (nm.startsWith("0x", index) ||
     * nm.startsWith("0X", index)) { index += 2; radix = 16; } else if
     * (nm.startsWith("#", index)) { index ++; radix = 16; } else if
     * (nm.startsWith("0", index) && nm.length() > 1 + index) { index ++; radix
     * = 8; }
     * 
     * if (nm.startsWith("-", index)) throw new NumberFormatException(
     * "Sign character in wrong position");
     * 
     * try { result = Long.valueOf(nm.substring(index), radix); result =
     * negative ? new Long((long)-result.longValue()) : result; } catch
     * (NumberFormatException e) { // If number is Long.MIN_VALUE, we'll end up
     * here. The next line // handles this case, and causes any genuine format
     * error to be // rethrown. String constant = negative ? ("-" +
     * nm.substring(index)) : nm.substring(index); result =
     * Long.valueOf(constant, radix); } return result; } }
     */

    private final long value;

    public Long(long value) {
        this.value = value;
    }

    public Long(java.lang.String s) {
        this.value = parseLong(s, 10);
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
        return (float) value;
    }

    public double doubleValue() {
        return (double) value;
    }

    public java.lang.String toString() {
        return java.lang.String.valueOf(value);
    }

    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }

    public boolean equals(java.lang.Object obj) {
        if (obj instanceof java.lang.Long) {
            return value == ((java.lang.Long) obj).longValue();
        } else if (obj instanceof Long) {
            long eqVal = ((Long) obj).longValue();
            return value == eqVal;
        }
        return false;
    }

    // getLong not supported cause System.getProperty
    /*
     * public static Long getLong(String nm) { return getLong(nm, null); }
     *
     * public static Long getLong(String nm, long val) { Long result =
     * Long.getLong(nm, null); return (result == null) ? new Long(val) : result;
     * }
     *
     * public static Long getLong(String nm, Long val) { String v = null; v =
     * System.getProperty(nm); if (v != null) { return Long.decode(v); } return
     * val; }
     */

    public int compareTo(java.lang.Long anotherLong) {
        long value2 = anotherLong.longValue();
        long thisVal = this.value;
        long anotherVal = value2;
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
    }

    public static int compare(long f1, long f2) {
        if (f1 < f2) {
            return -1;
        }
        if (f1 > f2) {
            return 1;
        }
        return 0;
    }

    public static final int SIZE = 64;

    public static long highestOneBit(long i) {

        // HD, Figure 3-1
        i |= (i >> 1);
        i |= (i >> 2);
        i |= (i >> 4);
        i |= (i >> 8);
        i |= (i >> 16);
        i |= (i >> 32);
        return i - (i >>> 1);
    }

    public static long lowestOneBit(long i) {
        // HD, Section 2-1
        return i & -i;
    }

    public static int numberOfLeadingZeros(long i) {

        // HD, Figure 5-6
        if (i == 0) {
            return 64;
        }
        int n = 1;
        int x = (int) (i >>> 32);
        if (x == 0) {
            n += 32;
            x = (int) i;
        }
        if (x >>> 16 == 0) {
            n += 16;
            x <<= 16;
        }
        if (x >>> 24 == 0) {
            n += 8;
            x <<= 8;
        }
        if (x >>> 28 == 0) {
            n += 4;
            x <<= 4;
        }
        if (x >>> 30 == 0) {
            n += 2;
            x <<= 2;
        }
        n -= x >>> 31;
        return n;
    }

    public static int numberOfTrailingZeros(long i) {

        // HD, Figure 5-14
        int x, y;
        if (i == 0) {
            return 64;
        }
        int n = 63;
        y = (int) i;
        if (y != 0) {
            n = n - 32;
            x = y;
        } else {
            x = (int) (i >>> 32);
        }

        y = x << 16;
        if (y != 0) {
            n = n - 16;
            x = y;
        }

        y = x << 8;
        if (y != 0) {
            n = n - 8;
            x = y;
        }

        y = x << 4;
        if (y != 0) {
            n = n - 4;
            x = y;
        }

        y = x << 2;
        if (y != 0) {
            n = n - 2;
            x = y;
        }
        return n - ((x << 1) >>> 31);
    }

    public static int bitCount(long i) {

        // HD, Figure 5-14
        i = i - ((i >>> 1) & 0x5555555555555555L);
        i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L);
        i = (i + (i >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
        i = i + (i >>> 8);
        i = i + (i >>> 16);
        i = i + (i >>> 32);
        return (int) i & 0x7f;
    }

    public static long rotateLeft(long i, int distance) {
        return (i << distance) | (i >>> -distance);
    }

    public static long rotateRight(long i, int distance) {
        return (i >>> distance) | (i << -distance);
    }

    public static long reverse(long i) {

        // HD, Figure 7-1
        i = (i & 0x5555555555555555L) << 1 | (i >>> 1) & 0x5555555555555555L;
        i = (i & 0x3333333333333333L) << 2 | (i >>> 2) & 0x3333333333333333L;
        i = (i & 0x0f0f0f0f0f0f0f0fL) << 4 | (i >>> 4) & 0x0f0f0f0f0f0f0f0fL;
        i = (i & 0x00ff00ff00ff00ffL) << 8 | (i >>> 8) & 0x00ff00ff00ff00ffL;
        i = (i << 48) | ((i & 0xffff0000L) << 16) | ((i >>> 16) & 0xffff0000L)
                | (i >>> 48);
        return i;
    }

    public static int signum(long i) {
        // HD, Section 2-7
        return (int) ((i >> 63) | (-i >>> 63));
    }

    public static long reverseBytes(long i) {
        i = (i & 0x00ff00ff00ff00ffL) << 8 | (i >>> 8) & 0x00ff00ff00ff00ffL;
        return (i << 48) | ((i & 0xffff0000L) << 16)
                | ((i >>> 16) & 0xffff0000L) | (i >>> 48);
    }

    private static final long serialVersionUID = 4290774380558885855L;

}
