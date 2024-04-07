package com.esin.jdbc.lazy;

import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.helper.DaoFactory;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

public class LazyEntityCglib {

    private static final Enhancer enhancer = new Enhancer();

    private static final MethodInterceptor lazyLoadInterceptor = (obj, method, args, proxy) -> {
        if (!LazyEntityJavassist.ignoreMethodList.contains(method.getName())) {
            ((IEntity) obj).doLoadLazy();
        }
        return proxy.invokeSuper(obj, args);
    };

    private static <T extends IEntity> IEntity createProxyInstance(Class<T> clazz) {
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(lazyLoadInterceptor);
        return (IEntity) enhancer.create();
    }

    public static <K, T extends IEntity<K>> T getProxyEntity(DaoFactory daoFactory, Class<T> type, K id) {
        IEntity entity = createProxyInstance(type);
        entity.setId(id);
        if (!entity.isNullId()) {
            LazyEntityUtil.setLazyLoad(daoFactory, entity);
        }
        return (T) entity;
    }
}
