package com.esin.base.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * <p>单例模式实现共通类，如果不是静态对象，必须重写destroyOnRemove()方法，否则会产生内存泄露</p><br>
 */
public abstract class SingletonUtil<K, V> {
    private static final Set<SingletonUtil> InstanceSet = new CopyOnWriteArraySet<SingletonUtil>();

    public static void clear() {
        for (SingletonUtil element : InstanceSet) {
            element.remove();
        }
    }

    protected SingletonUtil() {
        InstanceSet.add(this);
    }

    private final Map<K, BeanWrapper<V>> map = getContainer(getInitSize());

    public final V get(K k) {
        onGet(k);
        V v = null;
        BeanWrapper<V> bean = map.get(k);
        if (bean == null) {
            v = newInstance(k);
            map.put(k, new BeanWrapper<V>(v));
        } else {
            v = bean.getValue();
        }
        return v;
    }

    public final Map<K, V> getMap(Collection<K> keyList) {
        if (keyList == null || keyList.size() == 0) {
            Map<K, V> _map = new HashMap<K, V>();
            for (Map.Entry<K, BeanWrapper<V>> entry : map.entrySet()) {
                onGet(entry.getKey());
                _map.put(entry.getKey(), entry.getValue().getValue());
            }
            return _map;
        } else {
            for (K k : keyList) {
                onGet(k);
            }
            Collection<K> keys = new ArrayList<K>(keyList);
            keys.removeAll(map.keySet());
            Map<K, V> _map = null;
            if (keys.isEmpty()) {
                _map = new HashMap<K, V>(keyList.size());
            } else {
                _map = newInstance(keys);
                for (K k : keys) {
                    map.put(k, new BeanWrapper<V>(_map.get(k)));
                }
            }
            for (K k : keyList) {
                if (!_map.containsKey(k)) {
                    _map.put(k, map.get(k).getValue());
                }
            }
            return _map;
        }
    }

    public final boolean contains(K k) {
        return map.containsKey(k);
    }

    public final void remove(K... k) {
        Collection<K> keys = null;
        if (k.length == 0) {
            keys = new ArrayList<K>(map.keySet());
        } else {
            keys = Arrays.asList(k);
        }
        for (K key : keys) {
            onRemove(key);
            map.remove(key);
        }
        if (map.isEmpty() && destroyOnEmpty()) {
            InstanceSet.remove(this);
        }
    }

    public final void put(K k, V v) {
        onPut(k, v);
        map.put(k, new BeanWrapper<V>(v));
    }

    public final void put(Map<K, V> _map) {
        for (Map.Entry<K, V> entry : _map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    protected abstract V newInstance(K k);

    protected Map<K, V> newInstance(Collection<K> keyList) {
        Map<K, V> _map = new HashMap<K, V>(keyList.size());
        for (K k : keyList) {
            _map.put(k, get(k));
        }
        return _map;
    }

    protected void onGet(K k) {
    }

    protected void onRemove(K k) {
    }

    protected void onPut(K k, V v) {
    }

    protected boolean destroyOnEmpty() {
        return false;
    }

    protected Map<K, BeanWrapper<V>> getContainer(int size) {
        return new ConcurrentHashMap<>(size);
    }

    protected int getInitSize() {
        return 16;
    }
}
