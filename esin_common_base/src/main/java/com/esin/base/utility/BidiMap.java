package com.esin.base.utility;

import java.util.HashMap;

public class BidiMap<K, V> extends HashMap<K, V> {
    private final HashMap<V, K> inverseMap = new HashMap<V, K>();

    @Override
    public V put(K key, V value) {
        inverseMap.put(value, key);
        return super.put(key, value);
    }

    @Override
    public V remove(Object key) {
        inverseMap.remove(super.get(key));
        return super.remove(key);
    }

    public HashMap<V, K> getInverseMap() {
        return inverseMap;
    }
}
