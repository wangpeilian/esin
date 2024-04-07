package com.esin.base.utility;

public class Number64 {

    private static final String DIGITS64 = "0123456789-abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String toString(int value) {
        char[] buf = new char[6];
        for (int i = buf.length - 1; i >= 0; i--) {
            buf[i] = DIGITS64.charAt(value & 0x3F);
            value >>>= 6;
            if (value == 0) {
                break;
            }
        }
        return String.valueOf(buf).trim();
    }

    public static String toString(float value) {
        return toString(Float.floatToIntBits(value));
    }

    public static String toString(long value) {
        char[] buf = new char[11];
        for (int i = buf.length - 1; i >= 0; i--) {
            buf[i] = DIGITS64.charAt((int) (value & 0x3F));
            value >>>= 6;
            if (value == 0L) {
                break;
            }
        }
        return String.valueOf(buf).trim();
    }

    public static String toString(double value) {
        return toString(Double.doubleToLongBits(value));
    }

    public static int parseInt(String value) {
        int v = 0;
        for (int i = 0; i < value.length(); i++) {
            if (i != 0) {
                v <<= 6;
            }
            v |= DIGITS64.indexOf(value.charAt(i));
        }
        return v;
    }

    public static float parseFloat(String value) {
        return Float.intBitsToFloat(parseInt(value));
    }

    public static long parseLong(String value) {
        long v = 0;
        for (int i = 0; i < value.length(); i++) {
            if (i != 0) {
                v <<= 6;
            }
            v |= DIGITS64.indexOf(value.charAt(i));
        }
        return v;
    }

    public static double parseDouble(String value) {
        return Double.longBitsToDouble(parseLong(value));
    }

}
