package com.esin.base.utility;

public class NumberUtil {
    public static Integer convertCost(Float cost) {
        return Utility.isZero(cost) ? 0 : Math.round(cost * 10000f);
    }

    public static Float convertCost(Integer cost) {
        return cost == null ? 0f : cost / 10000f;
    }

    public static Integer convertPrice(Float price) {
        return Utility.isZero(price) ? 0 : Math.round(price * 1000f);
    }

    public static Float convertPrice(Integer price) {
        return price == null ? 0f : price / 1000f;
    }

    public static Integer convertMoney(Float money) {
        return Utility.isZero(money) ? 0 : Math.round(money * 100f);
    }

    public static Float convertMoney(Integer money) {
        return money == null ? 0f : money / 100f;
    }

    public static Long convertMoney(Double money) {
        return Utility.isZero(money) ? 0L : Math.round(money * 100d);
    }

    public static Double convertMoney(Long money) {
        return money == null ? 0d : money / 100d;
    }

    public static Integer convertPercent(Number v) {
        return (int) Math.round(v.doubleValue() * 10000);
    }

    public static byte[] convertIntToByte4(int value) {
        return new byte[]{
                (byte) ((value >>> 24) & 0xFF),
                (byte) ((value >>> 16) & 0xFF),
                (byte) ((value >>> 8) & 0xFF),
                (byte) ((value) & 0xFF)
        };
    }

    public static byte[] convertIntToByte3(int value) {
        return new byte[]{
                (byte) ((value >>> 16) & 0xFF),
                (byte) ((value >>> 8) & 0xFF),
                (byte) ((value) & 0xFF)
        };
    }

    public static byte[] convertInt4ToByte12(int... values) {
        return new byte[]{
                (byte) ((values[0] >>> 16) & 0xFF),
                (byte) ((values[0] >>> 8) & 0xFF),
                (byte) ((values[0]) & 0xFF),
                (byte) ((values[1] >>> 16) & 0xFF),
                (byte) ((values[1] >>> 8) & 0xFF),
                (byte) ((values[1]) & 0xFF),
                (byte) ((values[2] >>> 16) & 0xFF),
                (byte) ((values[2] >>> 8) & 0xFF),
                (byte) ((values[2]) & 0xFF),
                (byte) ((values[3] >>> 16) & 0xFF),
                (byte) ((values[3] >>> 8) & 0xFF),
                (byte) ((values[3]) & 0xFF)
        };
    }

    public static int convertByte4ToInt(byte... values) {
        return ((values[0] & 0xFF) << 24)
                + ((values[1] & 0xFF) << 16)
                + ((values[2] & 0xFF) << 8)
                + (values[3] & 0xFF);
    }

    public static int convertByte3ToInt(byte... values) {
        return convertByte4ToInt((byte) 0, values[0], values[1], values[2]);
    }

    public static int[] convertByte12ToInt4(byte... values) {
        return new int[]{
                convertByte3ToInt(values[0], values[1], values[2]),
                convertByte3ToInt(values[3], values[4], values[5]),
                convertByte3ToInt(values[6], values[7], values[8]),
                convertByte3ToInt(values[9], values[10], values[11])
        };
    }
}
