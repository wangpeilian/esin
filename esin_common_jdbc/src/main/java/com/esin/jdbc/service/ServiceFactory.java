package com.esin.jdbc.service;

import com.esin.base.utility.Logger;
import com.esin.base.utility.Utility;
import com.esin.jdbc.dao.Dao;
import com.esin.jdbc.dao.IDao;
import com.esin.jdbc.define.Transactional;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class ServiceFactory {
    private static final Logger logger = Logger.getLogger(ServiceFactory.class);

    private static final Map<IDao, Map<Class<?>, Object>> daoServiceMap = new HashMap<>();
    private static final ICommonService commonService = new CommonService();

    public static ICommonService getCommonService(IDao dao) {
        return (ICommonService) getTransactionService(dao, ICommonService.class, commonService);
    }

    public static Object getTransactionService(IDao dao, Class<?> interfaceType, Object instance) {
        if (interfaceType == null) {
            logger.warn("getTransactionService.interfaceType is null. (" + instance.getClass().getName() + ")");
            return instance;
        }
        Map<Class<?>, Object> serviceMap = daoServiceMap.computeIfAbsent(dao, dao1 -> new HashMap<>());
        return serviceMap.computeIfAbsent(interfaceType, type ->
                Proxy.newProxyInstance(interfaceType.getClassLoader(),
                        new Class[]{interfaceType}, new ServiceInvocationHandler(dao, instance)));
    }

    public static Object getTransactionService(IDao dao, Object instance) {
        if (hasTransactional(instance.getClass())) {
            return ServiceFactory.getTransactionService(Dao.getDao(), getInterface(instance.getClass()), instance);
        } else {
            return instance;
        }
    }

    private static Class<?> getInterface(Class<?> clazz) {
        Class<?>[] interfaces = clazz.getInterfaces();
        if (Utility.isNotEmpty(interfaces)) {
            for (Class<?> interfaceType : interfaces) {
                if (interfaceType.getSimpleName().equals("I" + clazz.getSimpleName())
                        || (interfaceType.getSimpleName() + "Imp").equals(clazz.getSimpleName())
                        || (interfaceType.getSimpleName() + "Impl").equals(clazz.getSimpleName())
                        || (interfaceType.getSimpleName() + "Imp").equals("I" + clazz.getSimpleName())
                        || (interfaceType.getSimpleName() + "Impl").equals("I" + clazz.getSimpleName())) {
                    return interfaceType;
                }
            }
            return interfaces[0];
        } else {
            return null;
        }
    }

    private static boolean hasTransactional(Class<?> clazz) {
        Transactional transactional = clazz.getAnnotation(Transactional.class);
        if (transactional != null && !Transactional.Level.None.equals(transactional.level())) {
            return true;
        }
        if (hasTransactional(clazz.getMethods())) {
            return true;
        }
        Class<?>[] interfaces = clazz.getInterfaces();
        if (Utility.isNotEmpty(interfaces)) {
            for (Class<?> type : interfaces) {
                if (hasTransactional(type)) {
                    return true;
                }
                if (hasTransactional(type.getMethods())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasTransactional(Method[] methods) {
        for (Method method : methods) {
            Transactional transactional = method.getAnnotation(Transactional.class);
            if (transactional != null && !Transactional.Level.None.equals(transactional.level())) {
                return true;
            }
        }
        return false;
    }
}
