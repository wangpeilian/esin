package com.esin.base.utility;

import com.esin.base.bean.IBean;
import com.esin.base.exception.SystemException;
import com.esin.base.executor.IExecutorAR;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Utility {
    private static final Logger logger = Logger.getLogger(Utility.class);
    public static final String EMPTY = "";
    public static final String SPACE = " ";
    public static final int ZERO = 0;

    public static boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static <T> boolean isEmpty(T[] arr) {
        return arr == null || arr.length == 0;
    }

    public static <T> boolean isEmpty(Collection<T> coll) {
        return coll == null || coll.isEmpty();
    }

    public static <T> boolean isEmpty(Iterable<T> it) {
        return it == null || isEmpty(it.iterator());
    }

    public static <T> boolean isEmpty(Iterator<T> it) {
        return it == null || !it.hasNext();
    }

    public static <T> boolean isEmpty(Enumeration<T> enu) {
        return enu == null || !enu.hasMoreElements();
    }

    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isEmpty(File file) {
        return file == null || !file.exists()
                || file.isFile() && file.length() == 0L
                || file.isDirectory() && file.list().length == 0;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    public static <T> boolean isNotEmpty(T[] arr) {
        return !isEmpty(arr);
    }

    public static <T> boolean isNotEmpty(Collection<T> coll) {
        return !isEmpty(coll);
    }

    public static <T> boolean isNotEmpty(Iterable<T> it) {
        return !isEmpty(it);
    }

    public static <T> boolean isNotEmpty(Iterator<T> it) {
        return !isEmpty(it);
    }

    public static <T> boolean isNotEmpty(Enumeration<T> enu) {
        return !isEmpty(enu);
    }

    public static <K, V> boolean isNotEmpty(Map<K, V> map) {
        return !isEmpty(map);
    }

    public static boolean isNotEmpty(File file) {
        return !isEmpty(file);
    }

    public static String toEmpty(String v) {
        return isEmpty(v) ? Utility.EMPTY : v.trim();
    }

    public static String toNull(String v) {
        return isEmpty(v) ? null : v;
    }

    public static String defaultIfEmpty(String v, String defaultValue) {
        return isEmpty(v) ? defaultValue : v.trim();
    }

    public static <T> T defaultIfNull(T o, T defaultValue) {
        return o == null ? defaultValue : o;
    }

    public static int toZero(Integer v) {
        return v != null ? v : 0;
    }

    public static long toZero(Long v) {
        return v != null ? v : 0L;
    }

    public static float toZero(Float v) {
        return isZero(v) ? 0f : v;
    }

    public static double toZero(Double v) {
        return isZero(v) ? 0d : v;
    }

    public static BigDecimal toZero(BigDecimal v) {
        return isZero(v) ? BigDecimal.ZERO : v;
    }

    public static boolean isPositive(Integer v) {
        return v != null && v > 0;
    }

    public static boolean isZero(Integer v) {
        return v == null || v == 0;
    }

    public static boolean isZero(Long v) {
        return v == null || v == 0L;
    }

    public static boolean isZero(Float v) {
        return v == null || v.isNaN() || v.isInfinite() || Math.abs(v) < 0.0001f;
    }

    public static boolean isZero(Double v) {
        return v == null || v.isNaN() || v.isInfinite() || Math.abs(v) < 0.0001d;
    }

    public static boolean isZero(BigDecimal v) {
        return v == null || BigDecimal.ZERO.equals(v) || Math.abs(v.doubleValue()) < 0.0001d;
    }

    public static boolean isNotZero(Integer v) {
        return !isZero(v);
    }

    public static boolean isNotZero(Long v) {
        return !isZero(v);
    }

    public static boolean isNotZero(Float v) {
        return !isZero(v);
    }

    public static boolean isNotZero(Double v) {
        return !isZero(v);
    }

    public static String getLocalHostInfo() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String info = inetAddress.getHostName() + ":" + inetAddress.getHostAddress();
            logger.info(info);
            return info;
        } catch (UnknownHostException e) {
            return Utility.EMPTY;
        }
    }

    public static void sleep(int seconds) {
        sleepMS(seconds * 1000);
    }

    public static void sleepMS(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            logger.error(String.valueOf(ms), e);
        }
    }

    public static void close(AutoCloseable autoCloseable) {
        if (autoCloseable != null) {
            try {
                autoCloseable.close();
            } catch (Exception ignore) {
            }
        }
    }

    public static String fillSpace(Object value, int width, boolean isHeadOrTail) {
        String text = String.valueOf(value);
        int length = 0;
        for (int i = 0; i < text.length(); i++) {
            length += text.charAt(i) < 256 ? 1 : 2;
        }
        String repeat = StringUtils.repeat(SPACE, width - length);
        return isHeadOrTail ? (repeat + text) : (text + repeat);
    }

    public static boolean equals(Object value1, Object value2) {
        return value1 != null ? value1.equals(value2) : value2 == null;
    }

    public static boolean notEquals(Object value1, Object value2) {
        return !equals(value1, value2);
    }

    public static final List<String> WeekList1 = Arrays.asList(
            "日", "１", "２", "３", "４", "５", "六"
    );
    public static final List<String> WeekList2 = Arrays.asList(
            "日", "一", "二", "三", "四", "五", "六"
    );

    public static Calendar getValidDate(Calendar calendar) {
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            calendar.add(Calendar.DATE, -2);
            return getValidDate(calendar);
        } else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            calendar.add(Calendar.DATE, -1);
            return getValidDate(calendar);
        } else {
            return calendar;
        }
    }

    public static String tidyDate(String date) {
        if (isEmpty(date)) {
            return date;
        }
        if (date.length() == 8 && !date.contains("-")) {
            return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
        } else {
            if (date.contains(Utility.SPACE)) {
                date = date.substring(0, date.indexOf(Utility.SPACE));
            }
            date = date.replace("/", "-");
            String[] values = date.split("-");
            if (values.length == 3) {
                if (values[0].length() == 2) {
                    values[0] = "20" + values[0];
                }
                if (values[1].length() == 1) {
                    values[1] = "0" + values[1];
                }
                if (values[2].length() == 1) {
                    values[2] = "0" + values[2];
                }
                return values[0] + "-" + values[1] + "-" + values[2];
            } else {
                return date;
            }
        }
    }

    public static <T> void println(String title, Iterable<T> iterable, IExecutorAR<T, String> executor) {
        if (executor == null) {
            executor = String::valueOf;
        }
        System.out.println("");
        System.out.println(title);
        Iterator<T> iterator = iterable.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            System.out.println(++index + " : " + executor.doExecute(iterator.next()));
        }
    }

    public static <K, V> void println(String title, Map<K, V> map, IExecutorAR<Map.Entry<K, V>, String> executor) {
        if (executor == null) {
            executor = arg -> arg.getKey() + " = " + arg.getValue();
        }
        System.out.println("");
        System.out.println(title);
        int index = 0;
        if (Utility.isNotEmpty(map)) {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                System.out.println(++index + " : " + executor.doExecute(entry));
            }
        }
    }

    public static String getStackTraceContent(Throwable t) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        t.printStackTrace(printWriter);
        printWriter.flush();
        return writer.toString();
    }

    private static final Map<Class, Map<String, Field>> clazzFieldMap = new HashMap<>();

    public static Map<String, Field> describeFieldMap(Object obj) {
        return describeFieldMap(obj.getClass());
    }

    public static Map<String, Field> describeFieldMap(Class clazz) {
        if (clazz.getSimpleName().contains("_$$_jvst") || clazz.getSimpleName().contains("$$EnhancerByCGLIB$$")) {
            clazz = clazz.getSuperclass();
        }
        Map<String, Field> fieldMap = clazzFieldMap.get(clazz);
        if (fieldMap == null) {
            fieldMap = new LinkedHashMap<>();
            clazzFieldMap.put(clazz, fieldMap);
            List<Class> classList = new ArrayList<>();
            while (!Object.class.equals(clazz)) {
                classList.add(clazz);
                clazz = clazz.getSuperclass();
            }
            Collections.reverse(classList);
            for (Class _clazz : classList) {
                for (Field field : _clazz.getDeclaredFields()) {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isStatic(modifiers)) {
                        if (!Modifier.isPublic(modifiers) || Modifier.isFinal(modifiers)) {
                            field.setAccessible(true);
                        }
                        fieldMap.put(field.getName(), field);
                    }
                }
            }
        }
        return fieldMap;
    }

    public static Object getFieldValue(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            String errorMsg = "get field value error.(" + target.getClass().getSimpleName() + "." + field.getName() + ")";
            logger.error(e.getClass().getSimpleName() + ": " + errorMsg, e);
            return null;
        }
    }

    public static void setFieldValue(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            String errorMsg = "set field value error.(" + target.getClass().getSimpleName() + "." + field.getName() + ") : " + value;
            logger.error(e.getClass().getSimpleName() + ": " + errorMsg, e);
        }
    }

    public static <B extends IBean> B newInstance(Class<B> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new SystemException("newInstance error.(" + clazz.getClass().getSimpleName() + ")", e);
        }
    }

    private static Type[] getGenericTypes(Type type) {
        if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getActualTypeArguments();
        } else {
            return new Type[]{type};
        }
    }

    public static Type[] getClassTypes(Class<?> clazz) {
        return getGenericTypes(clazz.getGenericSuperclass());
    }

    public static Class<?> getFieldType(Field field) {
        return getFieldType(null, field);
    }

    public static Class<?> getFieldType(Class<?> parent, Field field) {
        Class<?> clazz = field.getType();
        if (parent != null && !Collection.class.isAssignableFrom(clazz) && !clazz.isArray()) {
            Type type = getGenericTypes(field.getGenericType())[0];
            if (type instanceof TypeVariable) {
                Class<?> parentType = (Class<?>) ((TypeVariable) type).getBounds()[0];
                for (Type genericType : getClassTypes(parent)) {
                    if (parentType.isAssignableFrom((Class<?>) genericType)) {
                        return (Class<?>) genericType;
                    }
                }
            }
        }
        return clazz;
    }

    public static Class<?> getCollectionType(Class<?> clazz, Field field) {
        Type type = getGenericTypes(field.getGenericType())[0];
        if (type instanceof TypeVariable) {
            Class<?> parentType = (Class<?>) ((TypeVariable) type).getBounds()[0];
            for (Type genericType : getClassTypes(clazz)) {
                if (parentType.isAssignableFrom((Class<?>) genericType)) {
                    return (Class<?>) genericType;
                }
            }
            return parentType;
        } else {
            return (Class<?>) type;
        }
    }

    public static Class<?> getArrayType(Field field) {
        Class<?> clazz = field.getType();
        if (clazz.isArray()) {
            return clazz.getComponentType();
        } else {
            return clazz;
        }
    }

    public static Map<String, Object> object2map(Object value) {
        Map<String, Object> map = new LinkedHashMap<>();
        Map<String, Field> fieldMap = describeFieldMap(value);
        for (String name : fieldMap.keySet()) {
            Field field = fieldMap.get(name);
            map.put(field.getName(), getFieldValue(field, value));
        }
        return map;
    }

    public static String map2url(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value != null) {
                try {
                    value = URLEncoder.encode(String.valueOf(value), FileUtil.UTF8);
                    if (sb.length() != 0) {
                        sb.append("&");
                    }
                    sb.append(key);
                    sb.append("=");
                    sb.append(value);
                } catch (UnsupportedEncodingException e) {
                    logger.error(e.getClass().getSimpleName() + ": " + sb.toString(), e);
                }
            }
        }
        return sb.toString();
    }

    public static String getString(Object object) {
        String str = Utility.EMPTY;
        if (object != null) {
            str = object.toString();
        }
        return str;
    }

    public static int getInt(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        return 0;
    }

    public static void doTaskWithLogCostTime(String taskName, Runnable task) {
        final long startTime = System.nanoTime();
        try {
            task.run();
        } finally {
            long costMS = (System.nanoTime() - startTime) / 1000000L;
            if (costMS >= 200) {
                logger.info("Task cost : " + taskName + " -> " + costMS + "ms");
            }
        }
    }

    public static int calculatePercent(Number v1, Number v2) {
        if (v1 == null || v2 == null || Utility.isZero(v1.doubleValue()) || Utility.isZero(v2.doubleValue())) {
            return 0;
        }
        return (int) Math.round(v1.doubleValue() * 10000d / v2.doubleValue());
    }

    public static <T extends Comparable<T>> T max(T... values) {
        if (Utility.isEmpty(values)) {
            return null;
        }
        return Arrays.stream(values).max(Comparable::compareTo).orElse(null);
    }

    public static <T extends Comparable<T>> T min(T... values) {
        if (Utility.isEmpty(values)) {
            return null;
        }
        return Arrays.stream(values).min(Comparable::compareTo).orElse(null);
    }

    public static <T, V> V get(T obj, IExecutorAR<T, V> executor) {
        return obj == null ? null : executor.doExecute(obj);
    }

}
