/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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

/**
 * 
 * @author akueck@uni-bremen.de, Joseph D. Darcy
 *
 */
public class FpUtils {

    public static float scalb(float f, int scale_factor) {
        final int MAX_SCALE = FloatConsts.MAX_EXPONENT
                + -FloatConsts.MIN_EXPONENT + FloatConsts.SIGNIFICAND_WIDTH + 1;
        scale_factor = Math.max(Math.min(scale_factor, MAX_SCALE), -MAX_SCALE);
        return (float) ((double) f * powerOfTwoD(scale_factor));
    }

    public static double scalb(double d, int scale_factor) {
        final int MAX_SCALE = DoubleConsts.MAX_EXPONENT
                + -DoubleConsts.MIN_EXPONENT + DoubleConsts.SIGNIFICAND_WIDTH
                + 1;
        int exp_adjust = 0;
        int scale_increment = 0;
        double exp_delta = Double.NaN;
        if (scale_factor < 0) {
            scale_factor = Math.max(scale_factor, -MAX_SCALE);
            scale_increment = -512;
            exp_delta = twoToTheDoubleScaleDown;
        } else {
            scale_factor = Math.min(scale_factor, MAX_SCALE);
            scale_increment = 512;
            exp_delta = twoToTheDoubleScaleUp;
        }
        int t = (scale_factor >> 9 - 1) >>> 32 - 9;
        exp_adjust = ((scale_factor + t) & (512 - 1)) - t;
        d *= powerOfTwoD(exp_adjust);
        scale_factor -= exp_adjust;
        while (scale_factor != 0) {
            d *= exp_delta;
            scale_factor -= scale_increment;
        }
        return d;
    }

    static double powerOfTwoD(int n) {
        assert (n >= DoubleConsts.MIN_EXPONENT
                && n <= DoubleConsts.MAX_EXPONENT);
        return Double.longBitsToDouble((((long) n
                + (long) DoubleConsts.EXP_BIAS) << (DoubleConsts.SIGNIFICAND_WIDTH
                        - 1))
                & DoubleConsts.EXP_BIT_MASK);
    }

    static double twoToTheDoubleScaleUp = powerOfTwoD(512);
    static double twoToTheDoubleScaleDown = powerOfTwoD(-512);
}
