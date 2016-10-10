package gps.bytecode.iatfs.java.lang;

import java.math.BigInteger;
import java.lang.Double;
import java.lang.Float;

public class Math {

    public static final double E = 2.718281828459045;

    public static final double PI = 3.141592653589793;

    public static int abs(int i) {
        return (i < 0) ? -i : i;
    }

    public static long abs(long l) {
        return (l < 0) ? -l : l;
    }

    public static float abs(float f) {
        return (f <= 0) ? 0 - f : f;
    }

    public static double abs(double x) {
        return x < 0 ? -x : x;
    }

    public static int min(int a, int b) {
        return (a < b) ? a : b;
    }

    public static long min(long a, long b) {
        return (a < b) ? a : b;
    }

    public static float min(float a, float b) {
        // this check for NaN, from JLS 15.21.1, saves a method call
        if (a != a) {
            return a;
        }
        // no need to check if b is NaN; < will work correctly
        // recall that -0.0 == 0.0, but [+-]0.0 - [+-]0.0 behaves special
        if (a == 0 && b == 0) {
            return -(-a - b);
        }
        return (a < b) ? a : b;
    }

    public static double min(double a, double b) {
        // this check for NaN, from JLS 15.21.1, saves a method call
        if (a != a) {
            return a;
        }
        // no need to check if b is NaN; < will work correctly
        // recall that -0.0 == 0.0, but [+-]0.0 - [+-]0.0 behaves special
        if (a == 0 && b == 0) {
            return -(-a - b);
        }
        return (a < b) ? a : b;
    }

    public static int max(int a, int b) {
        return (a > b) ? a : b;
    }

    public static long max(long a, long b) {
        return (a > b) ? a : b;
    }

    public static float max(float a, float b) {
        // this check for NaN, from JLS 15.21.1, saves a method call
        if (a != a) {
            return a;
        }
        // no need to check if b is NaN; > will work correctly
        // recall that -0.0 == 0.0, but [+-]0.0 - [+-]0.0 behaves special
        if (a == 0 && b == 0) {
            return a - -b;
        }

        return (a > b) ? a : b;
    }

    public static double max(double a, double b) {
        // this check for NaN, from JLS 15.21.1, saves a method call
        if (a != a) {
            return a;
        }
        // no need to check if b is NaN; > will work correctly
        // recall that -0.0 == 0.0, but [+-]0.0 - [+-]0.0 behaves special
        if (a == 0 && b == 0) {
            return a - -b;
        }
        return (a > b) ? a : b;
    }

    //High precision is mainly required for calculating the tangens
    private final static double sinPrecision = 16;

    //http://stackoverflow.com/questions/6242369/how-to-re-implement-sin-method-in-java-to-have-results-close-to-math-sin
    private static double sinWithinTheInterval(double rad) {
        // the first element of the taylor series
        double sum = rad;
        // add them up until a certain precision (eg. 10)
        for (int i = 1; i <= sinPrecision; i++) {
            if (i % 2 == 0) {
                sum += pow(rad, 2 * i + 1) / factorial(2 * i + 1).doubleValue(); //does not work for large numbers in Java, but would probably be translated correcly to the backends
            } else {
                sum -= pow(rad, 2 * i + 1) / factorial(2 * i + 1).doubleValue();
            }
        }
        return sum;
    }

    public static double sin(double rad) {
        double newRad = rad % (2 * PI);
        if (newRad > PI) {
            newRad -= 2 * PI;
        }
        return sinWithinTheInterval(newRad);
    }

    public static BigInteger factorial(int n) {
        BigInteger prod = BigInteger.valueOf(1);
        for (int i = 2; i <= n; i++) {
            prod = prod.multiply(BigInteger.valueOf(i));
        }
        return prod;
    }

    //This can also be calculated in a smart way that requires fewer iterations but this would be more complicated.
    public static double pow(double b, double exp) {
        if (exp < 0) {
            return 1 / (posPow(b, -exp));
        }
        return posPow(b, exp);
    }

    //Public for tests
    public static int powerPrecision = 5;

    private static double posPow(double b, double exp) {
        double approx = posIntPow(b, (int) exp);
        //System.out.println("start with "+ approx);
        double rem = exp - (int) exp;
        double subExp;
        for (int i = 1; i < powerPrecision; i++) {
            int k = posPow(2, i);
            subExp = 1.0 / (double) k;
            if (subExp <= rem) {
                //System.out.println("multiply with" +k+"-th root of "+b + "="+root(k, b));
                approx *= root(k, b);
                //System.out.println("Resulting in "+approx);
                rem -= subExp;
            }
        }
        return approx;
    }

    private static double posIntPow(double a, int n) {
        if (n < 0) {
            return 1 / posPow(a, -n);
        }
        return posPow(a, n);

    }

    private static double posPow(double a, int n) {
        double power = 1;
        for (int i = 0; i < n; i++) {
            power *= a;
        }
        return power;
    }

    private static int posPow(int a, int n) {
        int power = 1;
        for (int i = 0; i < n; i++) {
            power *= a;
        }
        return power;
    }

    public static int rootPrecision = 3;

    //http://www.math.uni-sb.de/ag/wittstock/lehre/WS00/analysis1/Vorlesung/node38.html
    public static double root(int order, double x) {
        //System.out.println("Order "+ order + " of "+x);
        if (x < 0) {
            if (order % 2 == 0) {
                return Double.NaN;
            }
            return -root(order, -x);
        }
        double guess = 1 + (x - 1) / order;
        for (int i = 0; i < rootPrecision; i++) {
            //System.out.println("Current root guess: "+guess);
            guess = guess * (1 - (posPow(guess, order) - x)
                    / (order * posPow(guess, order)));
        }
        return guess;
    }

    public static int gcd(int a, int b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }

    public static double cos(double x) {
        return sin(x + PI / 2);
    }

    public static double tan(double x) {
        /* System.out.println(sin(x));
        System.out.println(java.lang.Math.sin(x));
        System.out.println();
        System.out.println(cos(x));
        System.out.println(java.lang.Math.cos(x));*/

        return sin(x) / cos(x);
    }

    //Unimplemented: asin, acos, atan, atan2

    public static double exp(double a) {
        return pow(E, a);
    }

    //Needs to be public for testing
    public static int logPrecision = 3;

    //https://en.wikipedia.org/wiki/Natural_logarithm#Definitions
    //Formally, ln(a) may be defined as the area under the hyperbola 1/x.
    public static double log(double a) {
        double integral = 0.0;
        //System.out.println("First sum: "+integral);
        double increment = (a - 1.0) / logPrecision;
        //System.out.println("Increment: " + increment);
        //for(double x = 1.0; a>1.0 ? x < a : x>a; x+= increment){
        for (double x = 1.0; (x - a) * (a - 1.0) < 0; x += increment) {
            integral += increment * (1.0 / x);
            //System.out.println("position "+x+" new integral sum: "+integral);            
        }
        return integral;
    }

    public static double sqrt(double x) {
        return root(2, x);
    }

    //unimplemented: IEEEremainder

    public static double ceil(double a) {
        return -Math.floor(-a);
    }

    public static double floor(double a) {
        if ((double) ((int) a) == a) {
            return a;
        }
        if (a < 0) {
            return (int) a - 1;
        }
        return (int) a;
    }

    public static double rint(double x) {
        //System.out.println("original: " + x);
        int nearestInt = (int) x;
        //System.out.println("cast result: " + nearestInt);
        double diff = x - (double) nearestInt;
        //System.out.println("diff: " + diff);
        if (diff > 0.5) {
            nearestInt++;
        }
        if (diff < -0.5) {
            nearestInt--;
        }
        if (abs(diff) == 0.5) {
            nearestInt += nearestInt % 2;
        }
        return nearestInt;
    }

    public static int round(float a) {
        // this check for NaN, from JLS 15.21.1, saves a method call
        if (a != a) {
            return 0;
        }
        return (int) floor(a + 0.5f);
    }

    public static long round(double a) {
        // this check for NaN, from JLS 15.21.1, saves a method call
        if (a != a) {
            return 0;
        }
        return (long) floor(a + 0.5d);
    }

    public static double toRadians(double degrees) {
        return (degrees * PI) / 180;
    }

    public static double toDegrees(double rads) {
        return (rads * 180) / PI;
    }

    public static double cbrt(double a) {
        return root(3, a);
    }

    public static double cosh(double x) {
        return (exp(x) + exp(-x)) / 2.0;
    }

    //In java this is just a (sometimes) more precise way of computing e^x -1
    public static double expm1(double x) {
        return exp(x) - 1;
    }

    public static double hypot(double x, double y) {
        return root(2, x * x + y * y);
    }

    public static double log10(double x) {
        return log(x) / log(10);
    }

    public static double log1p(double a) {
        return log(a + 1);
    }

    public static double signum(double a) {
        if (Double.isNaN(a)) {
            return Double.NaN;
        }
        if (a > 0) {
            return 1.0;
        }
        if (a < 0) {
            return -1.0;
        }
        return a;
    }

    public static float signum(float a) {
        if (Float.isNaN(a)) {
            return Float.NaN;
        }
        if (a > 0) {
            return 1.0f;
        }
        if (a < 0) {
            return -1.0f;
        }
        return a;
    }

    public static double sinh(double a) {
        return (exp(a) - exp(-a)) / 2.0;
    }

    public static double tanh(double x) {
        double y1 = Math.exp(x);
        double y2 = Math.exp(-x);
        return (y1 - y2) / (y1 + y2);
    }

    //Copied from: http://developer.classpath.org/doc/java/lang/Math-source.html
    public static double ulp(double d) {
        if (Double.isNaN(d)) {
            return d;
        }
        if (Double.isInfinite(d)) {
            return Double.POSITIVE_INFINITY;
        }
        // This handles both +0.0 and -0.0.
        if (d == 0.0) {
            return Double.MIN_VALUE;
        }
        long bits = Double.doubleToLongBits(d);
        final int mantissaBits = 52;
        final int exponentBits = 11;
        final long expMask = (1L << exponentBits) - 1;
        long exponent = (bits >>> mantissaBits) & expMask;

        // Denormal number, so the answer is easy.
        if (exponent == 0) {
            long result = (exponent << mantissaBits) | 1L;
            return Double.longBitsToDouble(result);
        }

        // Conceptually we want to have '1' as the mantissa.  Then we would
        // shift the mantissa over to make a normal number.  If this underflows
        // the exponent, we will make a denormal result.
        long newExponent = exponent - mantissaBits;
        long newMantissa;
        if (newExponent > 0) {
            newMantissa = 0;
        } else {
            newMantissa = 1L << -(newExponent - 1);
            newExponent = 0;
        }
        return Double
                .longBitsToDouble((newExponent << mantissaBits) | newMantissa);
    }

    //From http://developer.classpath.org/doc/java/lang/Math-source.html
    public static float ulp(float f) {
        if (Float.isNaN(f)) {
            return f;
        }
        if (Float.isInfinite(f)) {
            return Float.POSITIVE_INFINITY;
        }
        // This handles both +0.0 and -0.0.
        if (f == 0.0) {
            return Float.MIN_VALUE;
        }
        int bits = Float.floatToIntBits(f);
        final int mantissaBits = 23;
        final int exponentBits = 8;
        final int expMask = (1 << exponentBits) - 1;
        int exponent = (bits >>> mantissaBits) & expMask;

        // Denormal number, so the answer is easy.
        if (exponent == 0) {
            int result = (exponent << mantissaBits) | 1;
            return Float.intBitsToFloat(result);
        }

        // Conceptually we want to have '1' as the mantissa.  Then we would
        // shift the mantissa over to make a normal number.  If this underflows
        // the exponent, we will make a denormal result.
        int newExponent = exponent - mantissaBits;
        int newMantissa;
        if (newExponent > 0) {
            newMantissa = 0;
        } else {
            newMantissa = 1 << -(newExponent - 1);
            newExponent = 0;
        }
        return Float
                .intBitsToFloat((newExponent << mantissaBits) | newMantissa);
    }

}
