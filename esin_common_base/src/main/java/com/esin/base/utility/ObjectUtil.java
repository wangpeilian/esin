package com.esin.base.utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ObjectUtil {
    public static String toString(Object value) {
        StringBuilder sb = new StringBuilder();
        if (value == null) {
            sb.append("N");
        } else if (value instanceof List) {
            fill(sb, (List) value);
        } else if (value instanceof Map) {
            fill(sb, (Map) value);
        } else if (value instanceof String) {
            fill(sb, (String) value);
        } else if (value instanceof Long) {
            fill(sb, (Long) value);
        } else if (value instanceof Double) {
            fill(sb, (Double) value);
        } else if (value instanceof Integer) {
            fill(sb, (Integer) value);
        } else if (value instanceof Float) {
            fill(sb, (Float) value);
        } else if (value instanceof Boolean) {
            fill(sb, (Boolean) value);
        } else if (value instanceof Date) {
            fill(sb, (Date) value);
        } else if (value instanceof Calendar) {
            fill(sb, (Calendar) value);
        } else if (value instanceof Enum) {
            fill(sb, (Enum) value);
        } else if (value instanceof UUID) {
            fill(sb, (UUID) value);
        } else {
            AssertUtil.check(null, "UnsupportedDataType : " + value);
        }
        return sb.toString().replace("\r", CR1).replace("\n", CR2);
    }

    public static <T> T toObject(String value) {
        if (Utility.isEmpty(value)) {
            return null;
        }
        return (T) toObject(new StringBuilder(value.replace(CR1, "\r").replace(CR2, "\n")));
    }

    private static final String CR1 = "_;=CR1=;_";
    private static final String CR2 = "_;=CR2=;_";

    private static Object toObject(StringBuilder sb) {
        char type = sb.charAt(0);
        if (type == 'N') {
            sb.deleteCharAt(0);
            return null;
        }
        if (type == 'B') {
            boolean value = sb.charAt(1) == 'T';
            sb.delete(0, 2);
            return value;
        }
        int index = sb.indexOf(":");
        String value = sb.substring(1, index);
        if (type == 'S' || type == 'A' || type == 'M') {
            int length = convertInteger(value);
            value = sb.substring(index + 1, index + 1 + length);
            sb.delete(0, index + 1 + length);
            if (type == 'A') {
                return parseList(new StringBuilder(value));
            } else if (type == 'M') {
                return parseMap(new StringBuilder(value), new LinkedHashMap<>());
            } else {
                return revertCR(value);
            }
        } else {
            sb.delete(0, index + 1);
            if (type == 'L') {
                return Long.valueOf(value, Character.MAX_RADIX);
            } else if (type == 'D') {
                return Double.valueOf(value);
            } else if (type == 'I') {
                return convertInteger(value);
            } else if (type == 'F') {
                return Float.valueOf(value);
            } else if (type == 'T') {
                return new Date(Long.valueOf(value, Character.MAX_RADIX));
            } else if (type == 'C') {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(Long.valueOf(value, Character.MAX_RADIX));
                return c;
            } else if (type == 'U') {
                String[] values = value.split("-");
                return new UUID(Long.valueOf(values[0], Character.MAX_RADIX), Long.valueOf(values[1], Character.MAX_RADIX));
            } else if (type == 'E') {
                String[] values = value.split("-");
                try {
                    return Enum.valueOf((Class<? extends Enum>) Class.forName(values[0]), values[1]);
                } catch (Exception e) {
                    // 有可能保存好数据后，后期把代码改了，无法还原，所以直接返回null
                    Logger.getLogger(ObjectUtil.class).warn(values[0] + " - " + values[1]);
                    return null;
                }
            } else {
                AssertUtil.check(false, "UnsupportedDataType : " + sb);
                return null;
            }
        }
    }

    private static Object parseList(StringBuilder sb) {
        List<Object> list = new ArrayList<>();
        while (sb.length() > 0) {
            list.add(toObject(sb));
        }
        return list;
    }

    private static Object parseMap(StringBuilder sb, Map<Object, Object> map) {
        while (sb.length() > 0) {
            map.put(toObject(sb), toObject(sb));
        }
        return map;
    }

    private static void fill(StringBuilder sb, List list) {
        StringBuilder builder = new StringBuilder();
        for (Object value : list) {
            builder.append(toString(value));
        }
        sb.append("A");
        sb.append(convertInteger(builder.length()));
        sb.append(":");
        sb.append(builder);
    }

    private static void fill(StringBuilder sb, Map map) {
        StringBuilder builder = new StringBuilder();
        for (Object value : map.entrySet()) {
            Map.Entry entry = (Map.Entry) value;
            builder.append(toString(entry.getKey()));
            builder.append(toString(entry.getValue()));
        }
        sb.append("M");
        sb.append(convertInteger(builder.length()));
        sb.append(":");
        sb.append(builder);
    }

    private static void fill(StringBuilder sb, String value) {
        value = convertCR(value);
        sb.append("S");
        sb.append(convertInteger(value.length()));
        sb.append(":");
        sb.append(value);
    }

    private static void fill(StringBuilder sb, Boolean value) {
        sb.append("B");
        sb.append(value ? "T" : "F");
    }

    private static void fill(StringBuilder sb, Date value) {
        sb.append("T");
        sb.append(Long.toString(value.getTime(), Character.MAX_RADIX));
        sb.append(":");
    }

    private static void fill(StringBuilder sb, Calendar value) {
        sb.append("C");
        sb.append(Long.toString(value.getTimeInMillis(), Character.MAX_RADIX));
        sb.append(":");
    }

    private static void fill(StringBuilder sb, Enum value) {
        sb.append("E");
        sb.append(value.getDeclaringClass().getName());
        sb.append("-");
        sb.append(value.name());
        sb.append(":");
    }

    private static void fill(StringBuilder sb, Integer value) {
        sb.append("I");
        sb.append(convertInteger(value));
        sb.append(":");
    }

    private static void fill(StringBuilder sb, Float value) {
        sb.append("F");
        sb.append(value);
        sb.append(":");
    }

    private static void fill(StringBuilder sb, Long value) {
        sb.append("L");
        sb.append(Long.toString(value, Character.MAX_RADIX));
        sb.append(":");
    }

    private static void fill(StringBuilder sb, Double value) {
        sb.append("D");
        sb.append(value);
        sb.append(":");
    }

    private static void fill(StringBuilder sb, UUID value) {
        sb.append("U");
        sb.append(Long.toString(value.getMostSignificantBits(), Character.MAX_RADIX));
        sb.append("-");
        sb.append(Long.toString(value.getLeastSignificantBits(), Character.MAX_RADIX));
        sb.append(":");
    }

    private static String convertInteger(int value) {
        return Integer.toString(value, Character.MAX_RADIX);
    }

    private static Integer convertInteger(String value) {
        return Integer.valueOf(value, Character.MAX_RADIX);
    }

    //    private static final String EscapeCR = CipherUtil.digest22(CipherUtil.md5_16("ESIN")).substring(10, 18);
    private static final String EscapeCR = "1rZi45gK";
    private static final String EscapeCR_2 = EscapeCR + EscapeCR;
    private static final String EscapeCR_R = EscapeCR + "r";
    private static final String EscapeCR_N = EscapeCR + "n";

    private static String convertCR(String value) {
        if (value.contains(EscapeCR)) {
            value = value.replace(EscapeCR, EscapeCR_2);
        } else {
            if (value.contains("\r")) {
                value = value.replace("\r", EscapeCR_R);
            }
            if (value.contains("\n")) {
                value = value.replace("\n", EscapeCR_N);
            }
        }
        return value;
    }

    private static String revertCR(String value) {
        if (value.contains(EscapeCR_2)) {
            value = value.replace(EscapeCR_2, EscapeCR);
        } else {
            if (value.contains(EscapeCR_R)) {
                value = value.replace(EscapeCR_R, "\r");
            }
            if (value.contains(EscapeCR_N)) {
                value = value.replace(EscapeCR_N, "\n");
            }
        }
        return value;
    }
}
