package com.esin.jdbc.service;

import com.esin.base.utility.Logger;
import com.esin.jdbc.dao.IDao;
import com.esin.jdbc.define.Transactional;
import com.esin.jdbc.helper.JdbcHelper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Savepoint;
import java.util.Arrays;

public final class ServiceInvocationHandler implements InvocationHandler {
    private static final Logger logger = Logger.getLogger(ServiceInvocationHandler.class);

    private final IDao dao;
    private final Object target;

    public ServiceInvocationHandler(IDao dao, Object target) {
        this.dao = dao;
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // todo : 获取Transactional的顺序不能改，否则优先级会不对
        Transactional transactional = method.getAnnotation(Transactional.class);
        if (transactional == null) {
            for (Method _method : target.getClass().getMethods()) {
                if (_method.getName().equals(method.getName()) && Arrays.equals(_method.getParameterTypes(), method.getParameterTypes())) {
                    transactional = _method.getAnnotation(Transactional.class);
                    break;
                }
            }
        }
        if (transactional == null) {
            transactional = method.getDeclaringClass().getAnnotation(Transactional.class);
        }
        if (transactional == null) {
            transactional = target.getClass().getAnnotation(Transactional.class);
        }
        if (transactional == null || Transactional.Level.None.equals(transactional.level())) {
            return invoke(method, args);
        }
        JdbcHelper jdbcHelper = dao.getDaoFactory().getJdbcHelper();
        if (!Transactional.Level.Default.equals(transactional.level())) {
            jdbcHelper.setTransactionIsolation(transactional.level().level);
        }
        Savepoint savepoint = jdbcHelper.doBeginTransaction(transactional.savepoint_able());
        try {
            Object result = invoke(method, args);
            jdbcHelper.doCommitTransaction(savepoint);
            return result;
        } catch (Throwable t) {
            jdbcHelper.doRollbackTransaction(savepoint);
            throw t;
        }
    }

    private Object invoke(Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (Throwable t) {
            if (t instanceof java.lang.reflect.InvocationTargetException) {
                t = ((InvocationTargetException) t).getTargetException();
            }
            throw t;
        }
    }

}
