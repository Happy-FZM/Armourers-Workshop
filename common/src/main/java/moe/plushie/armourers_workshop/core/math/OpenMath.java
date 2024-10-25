package moe.plushie.armourers_workshop.core.math;

public class OpenMath {

    public static final float PI_f = (float) Math.PI;
    public static final float PIHalf_f = (float) Math.PI * 0.5f;
    public static final float PI2_f = (float) Math.PI * 2.0f;

    public static final boolean HAS_Math_fma = hasMathFma();

    public static int floori(float value) {
        int i = (int) value;
        return value < (float) i ? i - 1 : i;
    }

    public static int floori(double value) {
        int i = (int) value;
        return value < (double) i ? i - 1 : i;
    }


    public static int ceili(float value) {
        int i = (int) value;
        return value > (float) i ? i + 1 : i;
    }

    public static int ceili(double value) {
        int i = (int) value;
        return value > (double) i ? i + 1 : i;
    }


    public static float sqrt(float r) {
        return (float) Math.sqrt(r);
    }

    public static double sqrt(double r) {
        return Math.sqrt(r);
    }


    public static float invsqrt(float r) {
        return 1.0f / (float) Math.sqrt(r);
    }

    public static double invsqrt(double r) {
        return 1.0 / Math.sqrt(r);
    }

    public static float sin(float f) {
        return (float) Math.sin(f);
    }

    public static double sin(double f) {
        return Math.sin(f);
    }


    public static float cos(float f) {
        return (float) Math.cos(f);
    }

    public static double cos(double f) {
        return Math.cos(f);
    }


    public static float atan2(float d, float e) {
        return (float) Math.atan2(d, e);
    }

    public static double atan2(double d, double e) {
        return Math.atan2(d, e);
    }

    public static float cosFromSin(float sin, float angle) {
//        if (Options.FASTMATH)
//            return sin(angle + PIHalf_f);
        // sin(x)^2 + cos(x)^2 = 1
        float cos = sqrt(1.0f - sin * sin);
        float a = angle + PIHalf_f;
        float b = a - (int) (a / PI2_f) * PI2_f;
        if (b < 0.0) {
            b = PI2_f + b;
        }
        if (b >= PI_f) {
            return -cos;
        }
        return cos;
    }


    public static float fma(float a, float b, float c) {
        if (HAS_Math_fma) {
            return java.lang.Math.fma(a, b, c);
        }
        return a * b + c;
    }

    public static double fma(double a, double b, double c) {
        if (HAS_Math_fma) {
            return java.lang.Math.fma(a, b, c);
        }
        return a * b + c;
    }

    public static int clamp(int value, int minValue, int maxValue) {
        if (value < minValue) {
            return minValue;
        }
        if (value > maxValue) {
            return maxValue;
        }
        return value;
    }

    public static float clamp(float value, float minValue, float maxValue) {
        if (value < minValue) {
            return minValue;
        }
        if (value > maxValue) {
            return maxValue;
        }
        return value;
    }

    public static double clamp(double value, double minValue, double maxValue) {
        if (value < minValue) {
            return minValue;
        }
        if (value > maxValue) {
            return maxValue;
        }
        return value;
    }


    public static float lerp(float position, float a, float b) {
        return fma(position, b - a, a);
    }

    public static double lerp(double position, double a, double b) {
        return fma(position, b - a, a);
    }

    public static float rotLerp(float position, float a, float n) {
        return fma(position, wrapDegrees(n - a), a);
    }

    public static double rotLerp(double position, double a, double n) {
        return fma(position, wrapDegrees(n - a), a);
    }


    public static float toDegrees(float a) {
        return (float) Math.toDegrees(a);
    }

    public static double toDegrees(double a) {
        return Math.toDegrees(a);
    }


    public static float toRadians(float value) {
        return (float) Math.toRadians((value + 360) % 360);
    }

    public static double toRadians(double value) {
        return Math.toRadians((value + 360) % 360);
    }


    public static int wrapDegrees(int r) {
        int i = r % 360;
        if (i >= 180) {
            i -= 360;
        }
        if (i < -180) {
            i += 360;
        }
        return i;
    }

    public static float wrapDegrees(float r) {
        float f = r % 360.0F;
        if (f >= 180.0F) {
            f -= 360.0F;
        }
        if (f < -180.0F) {
            f += 360.0F;
        }
        return f;
    }

    public static double wrapDegrees(double r) {
        double d0 = r % 360.0D;
        if (d0 >= 180.0D) {
            d0 -= 360.0D;
        }
        if (d0 < -180.0D) {
            d0 += 360.0D;
        }
        return d0;
    }


    public static float fastInvSqrt(float v) {
        float f = 0.5f * v;
        int i = Float.floatToIntBits(v);
        i = 1597463007 - (i >> 1);
        v = Float.intBitsToFloat(i);
        return v * (1.5f - f * v * v);
    }

    public static double fastInvSqrt(double d) {
        double e = 0.5 * d;
        long l = Double.doubleToRawLongBits(d);
        l = 6910469410427058090L - (l >> 1);
        d = Double.longBitsToDouble(l);
        d *= 1.5 - e * d * d;
        return d;
    }

    public static float fastInvCubeRoot(float f) {
        int i = Float.floatToIntBits(f);
        i = 1419967116 - i / 3;
        float g = Float.intBitsToFloat(i);
        g = 0.6666667F * g + 1.0F / (3.0F * g * g * f);
        g = 0.6666667F * g + 1.0F / (3.0F * g * g * f);
        return g;
    }


    public static int roundToward(int i, int j) {
        return positiveCeilDiv(i, j) * j;
    }

    public static int positiveCeilDiv(int i, int j) {
        return -Math.floorDiv(-i, j);
    }


    public static void normalize(float[] values) {
        float f = fma(values[0], values[0], fma(values[1], values[1], values[2] * values[2]));
        float g = fastInvCubeRoot(f);
        values[0] *= g;
        values[1] *= g;
        values[2] *= g;
    }


    private static boolean hasMathFma() {
        try {
            Math.class.getDeclaredMethod("fma", float.class, float.class, float.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
