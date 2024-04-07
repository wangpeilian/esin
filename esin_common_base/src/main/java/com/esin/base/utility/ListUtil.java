package com.esin.base.utility;

import com.esin.base.executor.IExecutorAA;
import com.esin.base.executor.IExecutorAR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

public final class ListUtil {
    public static <T> T first(Iterable<T> it) {
        if (Utility.isEmpty(it)) {
            return null;
        } else {
            return it.iterator().next();
        }
    }

    public static <T> T first(List<T> list) {
        if (Utility.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public static <T> T last(List<T> list) {
        if (Utility.isEmpty(list)) {
            return null;
        } else {
            return list.get(list.size() - 1);
        }
    }

    public static <T> T unique(List<T> list, String key) {
        if (Utility.isEmpty(list)) {
            return null;
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            AssertUtil.check(new HashSet<>(list).size() == 1,
                    "Found too more records.(" + list.get(0).getClass().getSimpleName() + ":" + key + ")", list.size());
            return list.get(0);
        }
    }

    public static int sql_count(List<? extends Number> list) {
        Number value = first(list);
        return value == null ? 0 : value.intValue();
    }

    public static <T, V> List<V> toList(Collection<T> list, IExecutorAA<List<V>, T> executor) {
        if (Utility.isEmpty(list)) {
            return new ArrayList<>(0);
        }
        List<V> dataList = new ArrayList<>(list.size());
        for (T element : list) {
            executor.doExecute(dataList, element);
        }
        return dataList;
    }

    public static <T, V> List<V> toList(Collection<T> list, IExecutorAR<T, V> executor) {
        return toList(list, (dataList, element) -> {
            dataList.add(executor.doExecute(element));
        });
    }

    public static <T, K, V> LinkedHashMap<K, V> map(Collection<T> list, IExecutorAA<LinkedHashMap<K, V>, T> executor) {
        if (Utility.isEmpty(list)) {
            return new LinkedHashMap<>(0);
        }
        LinkedHashMap<K, V> dataMap = new LinkedHashMap<>(list.size());
        for (T element : list) {
            executor.doExecute(dataMap, element);
        }
        return dataMap;
    }

    public static <T, K, V> LinkedHashMap<K, V> map(Collection<T> list, Convert<T, K, V> convert) {
        return map(list, (dataMap, element) -> {
            dataMap.put(convert.getKey(element), convert.getValue(element));
        });
    }

    public static interface Convert<T, K, V> {
        public K getKey(T value);

        public V getValue(T value);
    }

    public static interface ConvertKey<T, K> extends Convert<T, K, T> {
        @Override
        public default T getValue(T value) {
            return value;
        }
    }

    public static final ConvertKey<String, String> ConvertString2String = new ConvertKey<String, String>() {
        @Override
        public String getKey(String value) {
            return value;
        }
    };
    public static final ConvertKey<String, Integer> ConvertString2Integer = new ConvertKey<String, Integer>() {
        @Override
        public Integer getKey(String value) {
            return Integer.valueOf(value);
        }
    };
    public static final ConvertKey<Integer, String> ConvertInteger2String = new ConvertKey<Integer, String>() {
        @Override
        public String getKey(Integer value) {
            return String.valueOf(value);
        }
    };

    public static <T> String convertList2String(Collection<T> dataList, ConvertKey<T, String> convert) {
        if (Utility.isEmpty(dataList)) {
            return Utility.EMPTY;
        } else {
            StringBuilder sb = new StringBuilder(",");
            for (T value : dataList) {
                sb.append(convert.getKey(value));
                sb.append(",");
            }
            return sb.toString();
        }
    }

    public static <T> List<T> convertString2List(String value, ConvertKey<String, T> convert) {
        if (Utility.isEmpty(value) || ",".equals(value)) {
            return Collections.emptyList();
        } else {
            value = value.substring(1, value.length() - 1);
            String[] values = value.split(",");
            List<T> dataList = new ArrayList<>(values.length);
            for (String v : values) {
                dataList.add(convert.getKey(v));
            }
            return dataList;
        }
    }
}
