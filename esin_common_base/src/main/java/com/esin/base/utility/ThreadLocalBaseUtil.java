package com.esin.base.utility;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>线程级别单例模式实现类，如果不是静态对象，必须重写destroyOnRemove()方法，否则会产生内存泄露</p><br>
 */
public abstract class ThreadLocalBaseUtil<T> {
    private static final Set<ThreadLocalBaseUtil> InstanceSet = new LinkedHashSet<ThreadLocalBaseUtil>();

    public static void clear() {
        for (ThreadLocalBaseUtil element : InstanceSet) {
            element.remove();
        }
    }

    private Object target = null;

    protected ThreadLocalBaseUtil(Object target) {
        this.target = target;
        InstanceSet.add(this);
    }

    private final ThreadLocal<BeanWrapper<T>> local = new ThreadLocal<BeanWrapper<T>>() {
        public void remove() {
            onRemove();
            super.remove();
        }
    };

    public final T get() {
        onGet();
        T v = null;
        BeanWrapper<T> bean = local.get();
        if (bean == null) {
            v = newInstance(target);
            local.set(new BeanWrapper<T>(v));
        } else {
            v = bean.getValue();
        }
        return v;
    }

    public final void remove() {
        BeanWrapper bean = local.get();
        if (bean != null) {
            local.remove();
            if (destroyOnRemove()) {
                InstanceSet.remove(this);
            }
        }
    }

    protected void onGet() {
    }

    protected void onRemove() {
    }

    protected boolean destroyOnRemove() {
        return false;
    }

    protected abstract T newInstance(Object target);
}
