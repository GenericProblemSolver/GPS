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
package bytecode.invokes.iatfs;

import static org.junit.Assert.*;

import org.junit.Test;
import gps.bytecode.iatfs.java.lang.Math;

public class MathIATFSTests {

    // assigning the values
    protected void setUp() {

    }

    @Test
    public void rootTest() {
        for (int n = 1; n < 8; n++) {
            for (double b = 1.0; b < 10.0; b += 0.15) {
                double ourResult = Math.root(n, b);
                try {
                    double correctResult = java.lang.Math.pow(b,
                            1.0 / (double) n);
                    if (!Double.isNaN(correctResult)) {
                        assertTrue(
                                Math.abs(ourResult - correctResult) < 0.1337);
                    } else {
                        assertTrue(Double.isNaN(ourResult));
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    @Test
    public void allPositivePowerTest() {
        Math.powerPrecision = 16;
        Math.rootPrecision = 100;
        for (double b = 0.3; b < 5.0; b += 0.7) {
            for (double exp = 0.3; exp < 5.0; exp += 0.13) {
                double ourResult = Math.pow(b, exp);
                try {
                    double correctResult = java.lang.Math.pow(b, exp);
                    if (!Double.isNaN(correctResult)) {
                        assertTrue(
                                Math.abs(ourResult - correctResult) < 0.1337);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    @Test
    public void powerTest() {
        Math.powerPrecision = 16;
        Math.rootPrecision = 100;
        for (double b = -5.0; b < 5.0; b += 0.2) {
            for (double exp = -5.0; exp < 5.0; exp += 0.15) {
                double ourResult = Math.pow(b, exp);
                try {
                    double correctResult = java.lang.Math.pow(b, exp);
                    if (!Double.isNaN(correctResult)) {
                        assertTrue(
                                Math.abs(ourResult / correctResult - 1) < 0.01);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    @Test
    public void sinTest() {
        for (double rad = -30.0; rad < 30.0; rad += 0.00037) {
            double ourResult = Math.sin(rad);
            double javaResult = java.lang.Math.sin(rad);
            assertTrue(Math.abs(ourResult - javaResult) < 0.0001);
        }
    }

    @Test
    public void cosTest() {
        for (double rad = -30.0; rad < 30.0; rad += 0.00037) {
            double ourResult = Math.cos(rad);
            double javaResult = java.lang.Math.cos(rad);
            assertTrue(Math.abs(ourResult - javaResult) < 0.0001);
        }
    }

    @Test
    public void tanTest() {
        for (double rad = -30.0; rad < 30.0; rad += 0.00037) {
            double ourResult = Math.tan(rad);
            double javaResult = java.lang.Math.tan(rad);
            assertTrue(Math.abs(ourResult - javaResult) < 0.01);
        }
    }

    @Test
    public void expTest() {
        Math.powerPrecision = 16;
        for (double exponent = -10.0; exponent < 10.0; exponent += 0.0037) {
            double ourResult = Math.exp(exponent);
            double javaResult = java.lang.Math.exp(exponent);
            assertTrue(Math.abs(ourResult / javaResult - 1) < 0.001);
        }
    }

    /**
     * For small numbers, getting high precision results is more difficult.
     */
    @Test
    public void logTestSmaller1() {
        //Set precision to high value and then be very strict in comparison, to make sure that the algorithm used works in principle
        Math.logPrecision = 100000;
        for (double x = 0.001; x < 1.0; x += 0.0037) {
            double ourResult = Math.log(x);
            double javaResult = java.lang.Math.log(x);
            assertTrue(Math.abs(ourResult / javaResult - 1) < 0.001);
        }
    }

    @Test
    public void logTestGreater1() {
        //Set precision to high value and then be very strict in comparison, to make sure that the algorithm used works in principle
        Math.logPrecision = 100000;
        //Fraction based comparison does not work if results are 0
        assertTrue(Math.abs(Math.log(1.0)) < 0.000000001);
        for (double x = 1.00001; x < 1000.0; x += 0.0037) {
            double ourResult = Math.log(x);
            double javaResult = java.lang.Math.log(x);
            assertTrue(Math.abs(ourResult / javaResult - 1) < 0.001
                    || Math.abs(ourResult - javaResult) < 0.0001);
        }
    }

    @Test
    public void sqrtTest() {
        Math.rootPrecision = 100;
        for (double x = 0.0; x < 10000.0; x += 0.0037) {
            double ourResult = Math.sqrt(x);
            double javaResult = java.lang.Math.sqrt(x);
            assertTrue(Math.abs(ourResult / javaResult - 1) < 0.001
                    || Math.abs(ourResult - javaResult) < 0.0001);
        }
    }

    @Test
    public void sqrtNegativeTest() {
        for (double x = -10000.0; x < 0.0; x += 0.0037) {
            assertTrue(Double.isNaN(Math.sqrt(x)));
        }
    }

    @Test
    public void floorTest() {
        for (double x = -10000.0; x < 10000.0; x += 0.17523572) {
            double ourResult = Math.floor(x);
            double javaResult = java.lang.Math.floor(x);
            assertTrue(ourResult == javaResult);
        }
    }

    @Test
    public void ceilTest() {
        for (double x = -10000.0; x < 10000.0; x += 0.17523572) {
            double ourResult = Math.ceil(x);
            double javaResult = java.lang.Math.ceil(x);
            assertTrue(ourResult == javaResult);
        }
    }

    @Test
    public void rintTest() {
        for (double x = -10000.0; x < 10000.0; x += 0.17523572) {
            double ourResult = Math.rint(x);
            double javaResult = java.lang.Math.rint(x);
            assertTrue(ourResult == javaResult);
        }
    }

    @Test
    public void rintInTheMiddleTest() {
        for (int x = -10000; x < 10000; x++) {
            double middle = ((double) x) + 0.5;
            double ourResult = Math.rint(middle);
            double javaResult = java.lang.Math.rint(middle);
            assertTrue(ourResult == javaResult);
        }
    }

    @Test
    public void cbrtTest() {
        Math.rootPrecision = 100;
        assertTrue(Math.abs(Math.cbrt(0.0)) < 0.0001);
        for (double x = -10000.0; x < 10000.0; x += 0.17523572) {
            double ourResult = Math.cbrt(x);
            double javaResult = java.lang.Math.cbrt(x);
            assertTrue(Math.abs(ourResult / javaResult - 1) < 0.001
                    || Math.abs(ourResult - javaResult) < 0.0001);
        }
    }

    @Test
    public void coshTest() {
        Math.powerPrecision = 16;
        for (double x = -10.0; x < 10.0; x += 0.017523572) {
            double ourResult = Math.cosh(x);
            double javaResult = java.lang.Math.cosh(x);
            assertTrue(Math.abs(ourResult / javaResult - 1) < 0.001
                    || Math.abs(ourResult - javaResult) < 0.0001);
        }
    }

    @Test
    public void sinhTest() {
        Math.powerPrecision = 16;
        for (double x = -10.0; x < 10.0; x += 0.017523572) {
            double ourResult = Math.sinh(x);
            double javaResult = java.lang.Math.sinh(x);
            assertTrue(Math.abs(ourResult / javaResult - 1) < 0.001
                    || Math.abs(ourResult - javaResult) < 0.0001);
        }
    }

    @Test
    public void tanhTest() {
        Math.powerPrecision = 16;
        for (double x = -10.0; x < 10.0; x += 0.017523572) {
            double ourResult = Math.tanh(x);
            double javaResult = java.lang.Math.tanh(x);
            assertTrue(Math.abs(ourResult / javaResult - 1) < 0.001
                    || Math.abs(ourResult - javaResult) < 0.0001);
        }
    }

    @Test
    public void expm1Test() {
        Math.powerPrecision = 16;
        for (double x = -10.0; x < 15.0; x += 0.017523572) {
            double ourResult = Math.expm1(x);
            double javaResult = java.lang.Math.expm1(x);
            assertTrue(Math.abs(ourResult / javaResult - 1) < 0.001
                    || Math.abs(ourResult - javaResult) < 0.0001);
        }
    }

    @Test
    public void hypotTest() {
        Math.rootPrecision = 100;
        for (double x = -100.0; x < 100.0; x += 0.0129348) {
            for (double y = -100.0; y < 100.0; y += 0.092983) {
                double ourResult = Math.hypot(x, y);
                double correctResult = java.lang.Math.hypot(x, y);
                assertTrue(Math.abs(ourResult / correctResult - 1) < 0.01);
            }
        }
    }

    @Test
    public void log10Test() {
        Math.logPrecision = 20000;
        for (double x = 0.001; x < 5000.0; x += 0.017523572) {
            double ourResult = Math.log10(x);
            double javaResult = java.lang.Math.log10(x);
            assertTrue(Math.abs(ourResult / javaResult - 1) < 0.05
                    || Math.abs(ourResult - javaResult) < 0.001);
        }
    }

    @Test
    public void log1pTest() {
        Math.logPrecision = 20000;
        for (double x = 1 - 0.001; x < 5000.0; x += 0.017523572) {
            double ourResult = Math.log1p(x);
            double javaResult = java.lang.Math.log1p(x);
            assertTrue(Math.abs(ourResult / javaResult - 1) < 0.05
                    || Math.abs(ourResult - javaResult) < 0.001);
        }
    }

    public void tearDown() {
    }
}
