package com.esin.base.utility;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

public class MapUtil {
    public static <K, V> Map<K, V> of(List<K> kList, List<V> vList) {
        if (Utility.isEmpty(kList) || Utility.isEmpty(vList)) {
            return new LinkedHashMap<>(0);
        }
        if (kList.size() != vList.size()) {
            throw new IllegalArgumentException("k.length(" + kList.size() + ") != v.length(" + vList.size() + ")");
        }
        LinkedHashMap<K, V> dataMap = new LinkedHashMap<>(kList.size());
        for (int i = 0; i < kList.size(); i++) {
            dataMap.put(kList.get(i), vList.get(i));
        }
        return dataMap;
    }

    public static <K, V> Map<K, V> of() {
        return of(null, null);
    }

    public static <K, V> Map<K, V> of(K k, V v) {
        return of(Arrays.asList(k), Arrays.asList(v));
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2) {
        return of(Arrays.asList(k1, k2), Arrays.asList(v1, v2));
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return of(Arrays.asList(k1, k2, k3), Arrays.asList(v1, v2, v3));
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return of(Arrays.asList(k1, k2, k3, k4), Arrays.asList(v1, v2, v3, v4));
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return of(Arrays.asList(k1, k2, k3, k4, k5), Arrays.asList(v1, v2, v3, v4, v5));
    }

    public static <K, V> Map.Entry<K, V> first_entry(Map<K, V> map) {
        if (Utility.isEmpty(map)) {
            return null;
        }
        if (map instanceof NavigableMap) {
            return ((NavigableMap<K, V>) map).firstEntry();
        } else {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                return entry;
            }
            return null;
        }
    }

    public static <K, V> Map.Entry<K, V> last_entry(Map<K, V> map) {
        if (Utility.isEmpty(map)) {
            return null;
        }
        if (map instanceof NavigableMap) {
            return ((NavigableMap<K, V>) map).lastEntry();
        } else {
            Map.Entry<K, V> value = null;
            for (Map.Entry<K, V> entry : map.entrySet()) {
                value = entry;
            }
            return value;
        }
    }

    public static <K, V> K first_key(Map<K, V> map) {
        return Utility.get(first_entry(map), Map.Entry::getKey);
    }

    public static <K, V> K last_key(Map<K, V> map) {
        return Utility.get(last_entry(map), Map.Entry::getKey);
    }

    public static <K, V> V first_value(Map<K, V> map) {
        return Utility.get(first_entry(map), Map.Entry::getValue);
    }

    public static <K, V> V last_value(Map<K, V> map) {
        return Utility.get(last_entry(map), Map.Entry::getValue);
    }

    public static <K, V> Map.Entry<K, V> gt_entry(NavigableMap<K, V> map, K key) {
        return Utility.get(map, _map -> _map.higherEntry(key));
    }

    public static <K, V> Map.Entry<K, V> ge_entry(NavigableMap<K, V> map, K key) {
        return Utility.get(map, _map -> _map.ceilingEntry(key));
    }

    public static <K, V> Map.Entry<K, V> le_entry(NavigableMap<K, V> map, K key) {
        return Utility.get(map, _map -> _map.floorEntry(key));
    }

    public static <K, V> Map.Entry<K, V> lt_entry(NavigableMap<K, V> map, K key) {
        return Utility.get(map, _map -> _map.lowerEntry(key));
    }

    public static <K, V> K gt_key(NavigableMap<K, V> map, K key) {
        return Utility.get(gt_entry(map, key), Map.Entry::getKey);
    }

    public static <K, V> K ge_key(NavigableMap<K, V> map, K key) {
        return Utility.get(ge_entry(map, key), Map.Entry::getKey);
    }

    public static <K, V> K le_key(NavigableMap<K, V> map, K key) {
        return Utility.get(le_entry(map, key), Map.Entry::getKey);
    }

    public static <K, V> K lt_key(NavigableMap<K, V> map, K key) {
        return Utility.get(lt_entry(map, key), Map.Entry::getKey);
    }

    public static <K, V> V gt_value(NavigableMap<K, V> map, K key) {
        return Utility.get(gt_entry(map, key), Map.Entry::getValue);
    }

    public static <K, V> V ge_value(NavigableMap<K, V> map, K key) {
        return Utility.get(ge_entry(map, key), Map.Entry::getValue);
    }

    public static <K, V> V le_value(NavigableMap<K, V> map, K key) {
        return Utility.get(le_entry(map, key), Map.Entry::getValue);
    }

    public static <K, V> V lt_value(NavigableMap<K, V> map, K key) {
        return Utility.get(lt_entry(map, key), Map.Entry::getValue);
    }
}
