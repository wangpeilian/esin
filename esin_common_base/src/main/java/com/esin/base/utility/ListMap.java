package com.esin.base.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListMap<K, V> implements Map<K, List<V>> {
    public final Map<K, List<V>> dataMap;
    public final int initListSize;

    public ListMap(Map<K, List<V>> dataMap, int initListSize) {
        this.dataMap = dataMap;
        this.initListSize = initListSize;
    }

    public List<V> putValue(K key, V value) {
        List<V> valueList = get(key);
        if (valueList == null) {
            put(key, valueList = new ArrayList<V>(initListSize));
        }
        valueList.add(value);
        return valueList;
    }

    @Override
    public int size() {
        return dataMap.size();
    }

    @Override
    public boolean isEmpty() {
        return dataMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return dataMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return dataMap.containsValue(value);
    }

    @Override
    public List<V> get(Object key) {
        return dataMap.get(key);
    }

    public List<V> put(K key, List<V> value) {
        return dataMap.put(key, value);
    }

    @Override
    public List<V> remove(Object key) {
        return dataMap.remove(key);
    }

    public void putAll(Map<? extends K, ? extends List<V>> m) {
        dataMap.putAll(m);
    }

    @Override
    public void clear() {
        dataMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return dataMap.keySet();
    }

    @Override
    public Collection<List<V>> values() {
        return dataMap.values();
    }

    @Override
    public Set<Entry<K, List<V>>> entrySet() {
        return dataMap.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return dataMap.equals(o);
    }

    @Override
    public int hashCode() {
        return dataMap.hashCode();
    }
}
