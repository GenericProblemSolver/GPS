/* 
 * Copyright 1996-2004 Sun Microsystems, Inc.  All Rights Reserved.
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

package gps.bytecode.iatfs.sun.misc;

import java.util.Arrays;
import java.util.regex.*;

public class FloatingDecimal {
    private static final char zero[] = { '0', '0', '0', '0', '0', '0', '0',
            '0' };
    //
    // Constants of the implementation;
    // most are IEEE-754 related.
    // (There are more really boring constants at the end.)
    //
    static final int MAX_SMALL_BIN_EXP = 62;
    static final int MIN_SMALL_BIN_EXP = -(63 / 3);
    static final int MAX_DECIMAL_DIGITS = 15;
    static final int MAX_DECIMAL_EXPONENT = 308;
    static final int MIN_DECIMAL_EXPONENT = -324;
    static final int BIG_DECIMAL_EXPONENT = 324; // i.e.
                                                 // abs(MIN_DECIMAL_EXPONENT)
    static final int MAX_NDIGITS = 1100;

    static final int SINGLE_MAX_DECIMAL_DIGITS = 7;
    static final int SINGLE_MAX_DECIMAL_EXPONENT = 38;
    static final int SINGLE_MIN_DECIMAL_EXPONENT = -45;
    static final int SINGLE_MAX_NDIGITS = 200;

    static final int INT_DECIMAL_DIGITS = 9;
    static final int bigDecimalExponent = 324;

    static final long signMask = 0x8000000000000000L;
    static final long expMask = 0x7ff0000000000000L;
    static final long fractMask = ~(signMask | expMask);
    static final int expShift = 52;
    static final int expBias = 1023;
    static final long fractHOB = (1L << expShift); // assumed High-Order bit
    static final long expOne = ((long) expBias) << expShift; // exponent of 1.0
    static final int maxSmallBinExp = 62;
    static final int minSmallBinExp = -(63 / 3);
    static final int maxDecimalDigits = 15;
    static final int maxDecimalExponent = 308;
    static final int minDecimalExponent = -324;

    boolean isExceptional;
    boolean isNegative;
    int decExponent;
    char digits[];
    int nDigits;
    int bigIntExp;
    int bigIntNBits;
    boolean mustSetRoundDir = false;
    boolean fromHex = false;
    int roundDir = 0; // set by doubleValue

    private static final char infinity[] = { 'I', 'n', 'f', 'i', 'n', 'i', 't',
            'y' };
    private static final char notANumber[] = { 'N', 'a', 'N' };

    private FloatingDecimal(boolean negSign, int decExponent, char[] digits,
            int n, boolean e) {
        isNegative = negSign;
        isExceptional = e;
        this.decExponent = decExponent;
        this.digits = digits;
        this.nDigits = n;
    }

    private static final double small10pow[] = { 1.0e0, 1.0e1, 1.0e2, 1.0e3,
            1.0e4, 1.0e5, 1.0e6, 1.0e7, 1.0e8, 1.0e9, 1.0e10, 1.0e11, 1.0e12,
            1.0e13, 1.0e14, 1.0e15, 1.0e16, 1.0e17, 1.0e18, 1.0e19, 1.0e20,
            1.0e21, 1.0e22 };

    private static final float singleSmall10pow[] = { 1.0e0f, 1.0e1f, 1.0e2f,
            1.0e3f, 1.0e4f, 1.0e5f, 1.0e6f, 1.0e7f, 1.0e8f, 1.0e9f, 1.0e10f };

    private static final double big10pow[] = { 1e16, 1e32, 1e64, 1e128, 1e256 };
    private static final double tiny10pow[] = { 1e-16, 1e-32, 1e-64, 1e-128,
            1e-256 };

    private static final int maxSmallTen = small10pow.length - 1;
    private static final int singleMaxSmallTen = singleSmall10pow.length - 1;

    public FloatingDecimal(double d) {
        double d1 = d;
    }

    public interface IBinaryToASCIIConverter {

        public java.lang.String toJavaFormatString();

        public void appendTo(Appendable buf);

        public int getDecimalExponent();

        public int getDigits(char[] digits);

        public boolean isNegative();

        public boolean isExceptional();

        public boolean digitsRoundedUp();

        public boolean decimalDigitsExact();
    }

    static final int intDecimalDigits = 9;

    static class BinaryToASCIIBuffer implements IBinaryToASCIIConverter {
        private boolean isNegative;
        private int decExponent;
        private int firstDigitIndex;
        private int nDigits;
        private final char[] digits;
        private final char[] buffer = new char[26];

        private boolean exactDecimalConversion = false;

        private boolean decimalDigitsRoundedUp = false;

        BinaryToASCIIBuffer() {
            this.digits = new char[20];
        }

        BinaryToASCIIBuffer(boolean isNegative, char[] digits) {
            this.isNegative = isNegative;
            this.decExponent = 0;
            this.digits = digits;
            this.firstDigitIndex = 0;
            this.nDigits = digits.length;
        }

        @Override
        public java.lang.String toJavaFormatString() {
            int len = getChars(buffer);
            return new java.lang.String(buffer, 0, len);
        }

        @Override
        public void appendTo(java.lang.Appendable buf) {
            int len = getChars(buffer);
            if (buf instanceof StringBuilder) {
                ((StringBuilder) buf).append(buffer, 0, len);
            } else if (buf instanceof StringBuffer) {
                ((StringBuffer) buf).append(buffer, 0, len);
            } else {
                assert false;
            }
        }

        @Override
        public int getDecimalExponent() {
            return decExponent;
        }

        @Override
        public int getDigits(char[] digits) {
            System.arraycopy(this.digits, firstDigitIndex, digits, 0,
                    this.nDigits);
            return this.nDigits;
        }

        @Override
        public boolean isNegative() {
            return isNegative;
        }

        @Override
        public boolean isExceptional() {
            return false;
        }

        @Override
        public boolean digitsRoundedUp() {
            return decimalDigitsRoundedUp;
        }

        @Override
        public boolean decimalDigitsExact() {
            return exactDecimalConversion;
        }

        private int getChars(char[] result) {
            assert nDigits <= 19 : nDigits; // generous bound on size of nDigits
            int i = 0;
            if (isNegative) {
                result[0] = '-';
                i = 1;
            }
            if (decExponent > 0 && decExponent < 8) {
                // print digits.digits.
                int charLength = Math.min(nDigits, decExponent);
                System.arraycopy(digits, firstDigitIndex, result, i,
                        charLength);
                i += charLength;
                if (charLength < decExponent) {
                    charLength = decExponent - charLength;
                    Arrays.fill(result, i, i + charLength, '0');
                    i += charLength;
                    result[i++] = '.';
                    result[i++] = '0';
                } else {
                    result[i++] = '.';
                    if (charLength < nDigits) {
                        int t = nDigits - charLength;
                        System.arraycopy(digits, firstDigitIndex + charLength,
                                result, i, t);
                        i += t;
                    } else {
                        result[i++] = '0';
                    }
                }
            } else if (decExponent <= 0 && decExponent > -3) {
                result[i++] = '0';
                result[i++] = '.';
                if (decExponent != 0) {
                    Arrays.fill(result, i, i - decExponent, '0');
                    i -= decExponent;
                }
                System.arraycopy(digits, firstDigitIndex, result, i, nDigits);
                i += nDigits;
            } else {
                result[i++] = digits[firstDigitIndex];
                result[i++] = '.';
                if (nDigits > 1) {
                    System.arraycopy(digits, firstDigitIndex + 1, result, i,
                            nDigits - 1);
                    i += nDigits - 1;
                } else {
                    result[i++] = '0';
                }
                result[i++] = 'E';
                int e;
                if (decExponent <= 0) {
                    result[i++] = '-';
                    e = -decExponent + 1;
                } else {
                    e = decExponent - 1;
                }
                // decExponent has 1, 2, or 3, digits
                if (e <= 9) {
                    result[i++] = (char) (e + '0');
                } else if (e <= 99) {
                    result[i++] = (char) (e / 10 + '0');
                    result[i++] = (char) (e % 10 + '0');
                } else {
                    result[i++] = (char) (e / 100 + '0');
                    e %= 100;
                    result[i++] = (char) (e / 10 + '0');
                    result[i++] = (char) (e % 10 + '0');
                }
            }
            return i;
        }

    }

    interface IASCIIToBinaryConverter {

        double doubleValue();

        float floatValue();

    }

    static class PreparedASCIIToBinaryBuffer
            implements IASCIIToBinaryConverter {
        final private double doubleVal;
        final private float floatVal;

        public PreparedASCIIToBinaryBuffer(double doubleVal, float floatVal) {
            this.doubleVal = doubleVal;
            this.floatVal = floatVal;
        }

        @Override
        public double doubleValue() {
            return doubleVal;
        }

        @Override
        public float floatValue() {
            return floatVal;
        }
    }

    static final IASCIIToBinaryConverter A2BC_POSITIVE_INFINITY = new PreparedASCIIToBinaryBuffer(
            java.lang.Double.POSITIVE_INFINITY,
            java.lang.Float.POSITIVE_INFINITY);
    static final IASCIIToBinaryConverter A2BC_NEGATIVE_INFINITY = new PreparedASCIIToBinaryBuffer(
            java.lang.Double.NEGATIVE_INFINITY,
            java.lang.Float.NEGATIVE_INFINITY);
    static final IASCIIToBinaryConverter A2BC_NOT_A_NUMBER = new PreparedASCIIToBinaryBuffer(
            java.lang.Double.NaN, java.lang.Float.NaN);
    static final IASCIIToBinaryConverter A2BC_POSITIVE_ZERO = new PreparedASCIIToBinaryBuffer(
            0.0d, 0.0f);
    static final IASCIIToBinaryConverter A2BC_NEGATIVE_ZERO = new PreparedASCIIToBinaryBuffer(
            -0.0d, -0.0f);

    @SuppressWarnings("unused")
    private static class HexFloatPattern {
        /**
         * Grammar is compatible with hexadecimal floating-point constants
         * described in section 6.4.4.2 of the C99 specification.
         */
        private static final Pattern VALUE = Pattern.compile(
                // 1 234 56 7 8 9
                "([-+])?0[xX](((\\p{XDigit}+)\\.?)|((\\p{XDigit}*)\\.(\\p{XDigit}+)))[pP]([-+])?(\\p{Digit}+)[fFdD]?");
    }

    /**
     * Returns <code>s</code> with any leading zeros removed.
     */
    static java.lang.String stripLeadingZeros(java.lang.String s) {
        // return s.replaceFirst("^0+", "");
        if (!s.isEmpty() && s.charAt(0) == '0') {
            for (int i = 1; i < s.length(); i++) {
                if (s.charAt(i) != '0') {
                    return s.substring(i);
                }
            }
            return "";
        }
        return s;
    }

    /**
     * Extracts a hexadecimal digit from position <code>position</code> of
     * string <code>s</code>.
     */
    static int getHexDigit(java.lang.String s, int position) {
        int value = Character.digit(s.charAt(position), 16);
        if (value <= -1 || value >= 16) {

        }
        return value;
    }

    public java.lang.String toJavaFormatString() {
        char result[] = (char[]) (perThreadBuffer.get());
        int i = getChars(result);
        return new java.lang.String(result, 0, i);
    }

    @SuppressWarnings("rawtypes")
    private static ThreadLocal perThreadBuffer = new ThreadLocal() {
        protected synchronized Object initialValue() {
            return new char[26];
        }
    };

    private int getChars(char[] result) {
        assert nDigits <= 19 : nDigits; // generous bound on size of nDigits
        int i = 0;
        if (isNegative) {
            result[0] = '-';
            i = 1;
        }
        if (isExceptional) {
            System.arraycopy(digits, 0, result, i, nDigits);
            i += nDigits;
        } else {
            if (decExponent > 0 && decExponent < 8) {
                // print digits.digits.
                int charLength = Math.min(nDigits, decExponent);
                System.arraycopy(digits, 0, result, i, charLength);
                i += charLength;
                if (charLength < decExponent) {
                    charLength = decExponent - charLength;
                    System.arraycopy(zero, 0, result, i, charLength);
                    i += charLength;
                    result[i++] = '.';
                    result[i++] = '0';
                } else {
                    result[i++] = '.';
                    if (charLength < nDigits) {
                        int t = nDigits - charLength;
                        System.arraycopy(digits, charLength, result, i, t);
                        i += t;
                    } else {
                        result[i++] = '0';
                    }
                }
            } else if (decExponent <= 0 && decExponent > -3) {
                result[i++] = '0';
                result[i++] = '.';
                if (decExponent != 0) {
                    System.arraycopy(zero, 0, result, i, -decExponent);
                    i -= decExponent;
                }
                System.arraycopy(digits, 0, result, i, nDigits);
                i += nDigits;
            } else {
                result[i++] = digits[0];
                result[i++] = '.';
                if (nDigits > 1) {
                    System.arraycopy(digits, 1, result, i, nDigits - 1);
                    i += nDigits - 1;
                } else {
                    result[i++] = '0';
                }
                result[i++] = 'E';
                int e;
                if (decExponent <= 0) {
                    result[i++] = '-';
                    e = -decExponent + 1;
                } else {
                    e = decExponent - 1;
                }
                // decExponent has 1, 2, or 3, digits
                if (e <= 9) {
                    result[i++] = (char) (e + '0');
                } else if (e <= 99) {
                    result[i++] = (char) (e / 10 + '0');
                    result[i++] = (char) (e % 10 + '0');
                } else {
                    result[i++] = (char) (e / 100 + '0');
                    e %= 100;
                    result[i++] = (char) (e / 10 + '0');
                    result[i++] = (char) (e % 10 + '0');
                }
            }
        }
        return i;
    }

    public static FloatingDecimal readJavaFormatString(String in) {
        boolean isNegative = false;
        boolean signSeen = false;
        int decExp;
        char c;

        parseNumber: try {
            in = in.trim(); // don't fool around with white space.
                            // throws NullPointerException if null
            int l = in.length();
            if (l == 0) {
                return null;
            }
            int i = 0;
            switch (c = in.charAt(i)) {
            case '-':
                isNegative = true;
                // FALLTHROUGH
            case '+':
                i++;
                signSeen = true;
            }

            // Check for NaN and Infinity strings
            c = in.charAt(i);
            if (c == 'N' || c == 'I') { // possible NaN or infinity
                boolean potentialNaN = false;
                char targetChars[] = null; // char array of "NaN" or "Infinity"

                if (c == 'N') {
                    targetChars = notANumber;
                    potentialNaN = true;
                } else {
                    targetChars = infinity;
                }

                // compare Input string to "NaN" or "Infinity"
                int j = 0;
                while (i < l && j < targetChars.length) {
                    if (in.charAt(i) == targetChars[j]) {
                        i++;
                        j++;
                    } else {// something is amiss, throw exception
                        break parseNumber;
                    }
                }

                // For the candidate string to be a NaN or infinity,
                // all characters in input string and target char[]
                // must be matched ==> j must equal targetChars.length
                // and i must equal l
                if ((j == targetChars.length) && (i == l)) { // return NaN or
                                                                 // infinity
                    return (potentialNaN ? new FloatingDecimal(Double.NaN) // NaN
                            // has
                            // no
                            // sign
                            : new FloatingDecimal(
                                    isNegative ? Double.NEGATIVE_INFINITY
                                            : Double.POSITIVE_INFINITY));
                } else { // something went wrong, throw exception
                    break parseNumber;
                }

            } else if (c == '0') { // check for hexadecimal floating-point
                                       // number
                if (l > i + 1) {
                    char ch = in.charAt(i + 1);
                    if (ch == 'x' || ch == 'X') { // possible hex string
                        return null;
                    }
                }
            } // look for and process decimal floating-point string

            char[] digits = new char[l];
            int nDigits = 0;
            boolean decSeen = false;
            int decPt = 0;
            int nLeadZero = 0;
            int nTrailZero = 0;
            digitLoop: while (i < l) {
                switch (c = in.charAt(i)) {
                case '0':
                    if (nDigits > 0) {
                        nTrailZero += 1;
                    } else {
                        nLeadZero += 1;
                    }
                    break; // out of switch.
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    while (nTrailZero > 0) {
                        digits[nDigits++] = '0';
                        nTrailZero -= 1;
                    }
                    digits[nDigits++] = c;
                    break; // out of switch.
                case '.':
                    if (decSeen) {
                        // already saw one ., this is the 2nd.
                        return null;
                    }
                    decPt = i;
                    if (signSeen) {
                        decPt -= 1;
                    }
                    decSeen = true;
                    break; // out of switch.
                default:
                    break digitLoop;
                }
                i++;
            }
            /*
             * At this point, we've scanned all the digits and decimal point
             * we're going to see. Trim off leading and trailing zeros, which
             * will just confuse us later, and adjust our initial decimal
             * exponent accordingly. To review: we have seen i total characters.
             * nLeadZero of them were zeros before any other digits. nTrailZero
             * of them were zeros after any other digits. if ( decSeen ), then a
             * . was seen after decPt characters ( including leading zeros which
             * have been discarded ) nDigits characters were neither lead nor
             * trailing zeros, nor point
             */
            /*
             * special hack: if we saw no non-zero digits, then the answer is
             * zero! Unfortunately, we feel honor-bound to keep parsing!
             */
            if (nDigits == 0) {
                digits = zero;
                nDigits = 1;
                if (nLeadZero == 0) {
                    // we saw NO DIGITS AT ALL,
                    // not even a crummy 0!
                    // this is not allowed.
                    break parseNumber; // go throw exception
                }

            }

            /*
             * Our initial exponent is decPt, adjusted by the number of
             * discarded zeros. Or, if there was no decPt, then its just nDigits
             * adjusted by discarded trailing zeros.
             */
            if (decSeen) {
                decExp = decPt - nLeadZero;
            } else {
                decExp = nDigits + nTrailZero;
            }

            /*
             * Look for 'e' or 'E' and an optionally signed integer.
             */
            if ((i < l) && (((c = in.charAt(i)) == 'e') || (c == 'E'))) {
                int expSign = 1;
                int expVal = 0;
                int reallyBig = java.lang.Integer.MAX_VALUE / 10;
                boolean expOverflow = false;
                switch (in.charAt(++i)) {
                case '-':
                    expSign = -1;
                    // FALLTHROUGH
                case '+':
                    i++;
                }
                int expAt = i;
                expLoop: while (i < l) {
                    if (expVal >= reallyBig) {
                        // the next character will cause integer
                        // overflow.
                        expOverflow = true;
                    }
                    switch (c = in.charAt(i++)) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        expVal = expVal * 10 + ((int) c - (int) '0');
                        continue;
                    default:
                        i--; // back up.
                        break expLoop; // stop parsing exponent.
                    }
                }
                int expLimit = bigDecimalExponent + nDigits + nTrailZero;
                if (expOverflow || (expVal > expLimit)) {
                    //
                    // The intent here is to end up with
                    // infinity or zero, as appropriate.
                    // The reason for yielding such a small decExponent,
                    // rather than something intuitive such as
                    // expSign*Integer.MAX_VALUE, is that this value
                    // is subject to further manipulation in
                    // doubleValue() and floatValue(), and I don't want
                    // it to be able to cause overflow there!
                    // (The only way we can get into trouble here is for
                    // really outrageous nDigits+nTrailZero, such as 2 billion.
                    // )
                    //
                    decExp = expSign * expLimit;
                } else {
                    // this should not overflow, since we tested
                    // for expVal > (MAX+N), where N >= abs(decExp)
                    decExp = decExp + expSign * expVal;
                }

                // if we saw something not a digit ( or end of string )
                // after the [Ee][+-], without seeing any digits at all
                // this is certainly an error. If we saw some digits,
                // but then some trailing garbage, that might be ok.
                // so we just fall through in that case.
                // HUMBUG
                if (i == expAt) {
                    break parseNumber; // certainly bad
                }
            }
            /*
             * We parsed everything we could. If there are leftovers, then this
             * is not good input!
             */
            if (i < l && ((i != l - 1)
                    || (in.charAt(i) != 'f' && in.charAt(i) != 'F'
                            && in.charAt(i) != 'd' && in.charAt(i) != 'D'))) {
                break parseNumber; // go throw exception
            }

            return new FloatingDecimal(isNegative, decExp, digits, nDigits,
                    false);
        } catch (StringIndexOutOfBoundsException e) {

        }
        return null;
    }

    public double doubleValue() {
        int kDigits = Math.min(nDigits, maxDecimalDigits + 1);
        long lValue;
        double dValue;
        double rValue, tValue;

        // First, check for NaN and Infinity values
        if (digits == infinity || digits == notANumber) {
            if (digits == notANumber) {
                return java.lang.Double.NaN;
            } else {
                return (isNegative ? java.lang.Double.NEGATIVE_INFINITY
                        : java.lang.Double.POSITIVE_INFINITY);
            }
        } else {
            if (mustSetRoundDir) {
                roundDir = 0;
            }
            /*
             * convert the lead kDigits to a long integer.
             */
            // (special performance hack: start to do it using int)
            int iValue = (int) digits[0] - (int) '0';
            int iDigits = Math.min(kDigits, intDecimalDigits);
            for (int i = 1; i < iDigits; i++) {
                iValue = iValue * 10 + (int) digits[i] - (int) '0';
            }
            lValue = (long) iValue;
            for (int i = iDigits; i < kDigits; i++) {
                lValue = lValue * 10L + (long) ((int) digits[i] - (int) '0');
            }
            dValue = (double) lValue;
            int exp = decExponent - kDigits;
            /*
             * lValue now contains a long integer with the value of the first
             * kDigits digits of the number. dValue contains the (double) of the
             * same.
             */

            if (nDigits <= maxDecimalDigits) {
                /*
                 * possibly an easy case. We know that the digits can be
                 * represented exactly. And if the exponent isn't too
                 * outrageous, the whole thing can be done with one operation,
                 * thus one rounding error. Note that all our constructors trim
                 * all leading and trailing zeros, so simple values (including
                 * zero) will always end up here
                 */
                if (exp == 0 || dValue == 0.0) {
                    return (isNegative) ? -dValue : dValue; // small floating
                } // integer
                else if (exp >= 0) {
                    if (exp <= maxSmallTen) {
                        /*
                         * Can get the answer with one operation, thus one
                         * roundoff.
                         */
                        rValue = dValue * small10pow[exp];
                        if (mustSetRoundDir) {
                            tValue = rValue / small10pow[exp];
                            roundDir = (tValue == dValue) ? 0
                                    : (tValue < dValue) ? 1 : -1;
                        }
                        return (isNegative) ? -rValue : rValue;
                    }
                    int slop = maxDecimalDigits - kDigits;
                    if (exp <= maxSmallTen + slop) {
                        /*
                         * We can multiply dValue by 10^(slop) and it is still
                         * "small" and exact. Then we can multiply by
                         * 10^(exp-slop) with one rounding.
                         */
                        dValue *= small10pow[slop];
                        rValue = dValue * small10pow[exp - slop];

                        if (mustSetRoundDir) {
                            tValue = rValue / small10pow[exp - slop];
                            roundDir = (tValue == dValue) ? 0
                                    : (tValue < dValue) ? 1 : -1;
                        }
                        return (isNegative) ? -rValue : rValue;
                    }
                    /*
                     * Else we have a hard case with a positive exp.
                     */
                } else {
                    if (exp >= -maxSmallTen) {
                        /*
                         * Can get the answer in one division.
                         */
                        rValue = dValue / small10pow[-exp];
                        tValue = rValue * small10pow[-exp];
                        if (mustSetRoundDir) {
                            roundDir = (tValue == dValue) ? 0
                                    : (tValue < dValue) ? 1 : -1;
                        }
                        return (isNegative) ? -rValue : rValue;
                    }
                    /*
                     * Else we have a hard case with a negative exp.
                     */
                }
            }

            /*
             * Harder cases: The sum of digits plus exponent is greater than
             * what we think we can do with one error.
             * 
             * Start by approximating the right answer by, naively, scaling by
             * powers of 10.
             */
            if (exp > 0) {
                if (decExponent > maxDecimalExponent + 1) {
                    /*
                     * Lets face it. This is going to be Infinity. Cut to the
                     * chase.
                     */
                    return (isNegative) ? java.lang.Double.NEGATIVE_INFINITY
                            : java.lang.Double.POSITIVE_INFINITY;
                }
                if ((exp & 15) != 0) {
                    dValue *= small10pow[exp & 15];
                }
                if ((exp >>= 4) != 0) {
                    int j;
                    for (j = 0; exp > 1; j++, exp >>= 1) {
                        if ((exp & 1) != 0) {
                            dValue *= big10pow[j];
                        }
                    }
                    /*
                     * The reason for the weird exp > 1 condition in the above
                     * loop was so that the last multiply would get unrolled. We
                     * handle it here. It could overflow.
                     */
                    double t = dValue * big10pow[j];
                    if (java.lang.Double.isInfinite(t)) {
                        /*
                         * It did overflow. Look more closely at the result. If
                         * the exponent is just one too large, then use the
                         * maximum finite as our estimate value. Else call the
                         * result infinity and punt it. ( I presume this could
                         * happen because rounding forces the result here to be
                         * an ULP or two larger than Double.MAX_VALUE ).
                         */
                        t = dValue / 2.0;
                        t *= big10pow[j];
                        if (java.lang.Double.isInfinite(t)) {
                            return (isNegative)
                                    ? java.lang.Double.NEGATIVE_INFINITY
                                    : java.lang.Double.POSITIVE_INFINITY;
                        }
                        t = java.lang.Double.MAX_VALUE;
                    }
                    dValue = t;
                }
            } else if (exp < 0) {
                exp = -exp;
                if (decExponent < minDecimalExponent - 1) {
                    /*
                     * Lets face it. This is going to be zero. Cut to the chase.
                     */
                    return (isNegative) ? -0.0 : 0.0;
                }
                if ((exp & 15) != 0) {
                    dValue /= small10pow[exp & 15];
                }
                if ((exp >>= 4) != 0) {
                    int j;
                    for (j = 0; exp > 1; j++, exp >>= 1) {
                        if ((exp & 1) != 0) {
                            dValue *= tiny10pow[j];
                        }
                    }
                    /*
                     * The reason for the weird exp > 1 condition in the above
                     * loop was so that the last multiply would get unrolled. We
                     * handle it here. It could underflow.
                     */
                    double t = dValue * tiny10pow[j];
                    if (t == 0.0) {
                        /*
                         * It did underflow. Look more closely at the result. If
                         * the exponent is just one too small, then use the
                         * minimum finite as our estimate value. Else call the
                         * result 0.0 and punt it. ( I presume this could happen
                         * because rounding forces the result here to be an ULP
                         * or two less than Double.MIN_VALUE ).
                         */
                        t = dValue * 2.0;
                        t *= tiny10pow[j];
                        if (t == 0.0) {
                            return (isNegative) ? -0.0 : 0.0;
                        }
                        t = java.lang.Double.MIN_VALUE;
                    }
                    dValue = t;
                }
            }

            return (isNegative) ? -dValue : dValue;
        }

    }

    static final int singleSignMask = 0x80000000;
    static final int singleExpMask = 0x7f800000;
    static final int singleFractMask = ~(singleSignMask | singleExpMask);
    static final int singleExpShift = 23;
    static final int singleFractHOB = 1 << singleExpShift;
    static final int singleExpBias = 127;
    static final int singleMaxDecimalDigits = 7;
    static final int singleMaxDecimalExponent = 38;
    static final int singleMinDecimalExponent = -45;

    public float floatValue() {
        int kDigits = Math.min(nDigits, singleMaxDecimalDigits + 1);
        int iValue;
        float fValue;

        // First, check for NaN and Infinity values
        if (digits == infinity || digits == notANumber) {
            if (digits == notANumber) {
                return java.lang.Float.NaN;
            } else {
                return (isNegative ? java.lang.Float.NEGATIVE_INFINITY
                        : java.lang.Float.POSITIVE_INFINITY);
            }
        } else {
            /*
             * convert the lead kDigits to an integer.
             */
            iValue = (int) digits[0] - (int) '0';
            for (int i = 1; i < kDigits; i++) {
                iValue = iValue * 10 + (int) digits[i] - (int) '0';
            }
            fValue = (float) iValue;
            int exp = decExponent - kDigits;
            /*
             * iValue now contains an integer with the value of the first
             * kDigits digits of the number. fValue contains the (float) of the
             * same.
             */

            if (nDigits <= singleMaxDecimalDigits) {
                /*
                 * possibly an easy case. We know that the digits can be
                 * represented exactly. And if the exponent isn't too
                 * outrageous, the whole thing can be done with one operation,
                 * thus one rounding error. Note that all our constructors trim
                 * all leading and trailing zeros, so simple values (including
                 * zero) will always end up here.
                 */
                if (exp == 0 || fValue == 0.0f) {
                    return (isNegative) ? -fValue : fValue; // small floating
                } // integer
                else if (exp >= 0) {
                    if (exp <= singleMaxSmallTen) {
                        /*
                         * Can get the answer with one operation, thus one
                         * roundoff.
                         */
                        fValue *= singleSmall10pow[exp];
                        return (isNegative) ? -fValue : fValue;
                    }
                    int slop = singleMaxDecimalDigits - kDigits;
                    if (exp <= singleMaxSmallTen + slop) {

                        fValue *= singleSmall10pow[slop];
                        fValue *= singleSmall10pow[exp - slop];
                        return (isNegative) ? -fValue : fValue;
                    }

                } else {
                    if (exp >= -singleMaxSmallTen) {

                        fValue /= singleSmall10pow[-exp];
                        return (isNegative) ? -fValue : fValue;
                    }

                }
            } else if ((decExponent >= nDigits)
                    && (nDigits + decExponent <= maxDecimalDigits)) {

                long lValue = (long) iValue;
                for (int i = kDigits; i < nDigits; i++) {
                    lValue = lValue * 10L
                            + (long) ((int) digits[i] - (int) '0');
                }
                double dValue = (double) lValue;
                exp = decExponent - nDigits;
                dValue *= small10pow[exp];
                fValue = (float) dValue;
                return (isNegative) ? -fValue : fValue;

            }

            if (decExponent > singleMaxDecimalExponent + 1) {

                return (isNegative) ? Float.NEGATIVE_INFINITY
                        : Float.POSITIVE_INFINITY;
            } else if (decExponent < singleMinDecimalExponent - 1) {

                return (isNegative) ? -0.0f : 0.0f;
            }

            mustSetRoundDir = !fromHex;
            double dValue = doubleValue();
            return stickyRound(dValue);
        }
    }

    float stickyRound(double dval) {

        long binexp = expMask;
        if (binexp == 0L || binexp == expMask) {
            // what we have here is special.
            // don't worry, the right thing will happen.
            return (float) dval;
        }
        // hack-o-matic.
        return (float) binexp;
    }
}
