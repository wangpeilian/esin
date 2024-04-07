package com.esin.jdbc.lazy;

import com.esin.base.utility.Utility;
import com.esin.jdbc.entity.IEntity;
import com.esin.jdbc.helper.DaoFactory;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

public class LazyEntityJavassist {

    private static final WeakHashMap<Class<? extends IEntity>, Class<? extends IEntity>> proxyClassMap = new WeakHashMap<>();
    public static final List<String> ignoreMethodList = Arrays.asList("getId", "setId", "setLazyLoad", "doLoadLazy", "wait", "finalize");

    private static final MethodHandler lazyLoadHandler = (self, method, proceed, args) -> {
        ((IEntity) self).doLoadLazy();
        return proceed.invoke(self, args);
    };

    private static <T extends IEntity> Class<? extends IEntity> getProxyClass(Class<T> clazz) {
        final ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(clazz);
        proxyFactory.setFilter(method -> !ignoreMethodList.contains(method.getName()));
        return (Class<? extends IEntity>) proxyFactory.createClass();
    }

    public static <K, T extends IEntity<K>> T getProxyEntity(DaoFactory daoFactory, Class<T> type, K id) {
        Class<? extends IEntity> clazz = proxyClassMap.computeIfAbsent(type, key -> getProxyClass(type));
        IEntity entity = Utility.newInstance(clazz);
        entity.setId(id);
        if (!entity.isNullId()) {
            LazyEntityUtil.setLazyLoad(daoFactory, entity);
            ((Proxy) entity).setHandler(lazyLoadHandler);
        }
        return (T) entity;
    }
}
