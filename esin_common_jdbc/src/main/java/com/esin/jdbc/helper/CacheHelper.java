package com.esin.jdbc.helper;

import com.esin.base.executor.IExecutorA;
import com.esin.base.utility.Logger;
import com.esin.base.utility.Utility;
import com.esin.jdbc.entity.IEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CacheHelper {
    private static final Logger logger = Logger.getLogger(CacheHelper.class);

    protected final DaoFactory daoFactory;
    private boolean disableCache = false;
    private final ThreadLocal<Map<Class<?>, Map<Object, Object>>> localCacheEntity = new ThreadLocal<>();
    private final ThreadLocal<Map<Class<?>, Map<Object, Integer>>> localCacheHash = new ThreadLocal<>();

    public CacheHelper(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public CacheHelper disableCache() {
        this.disableCache = true;
        return this;
    }

    public <T extends IEntity> void evict(T entity) {
        if (disableCache) {
            return;
        }
        if (entity == null || entity.isNullId()) {
            return;
        }
        evict(Collections.singletonList(entity));
    }

    public <T extends IEntity> void evict(List<T> entityList) {
        if (disableCache) {
            return;
        }
        if (Utility.isNotEmpty(entityList)) {
            Map<Class<?>, Map<Object, Object>> typeMap = localCacheEntity.get();
            if (typeMap != null) {
                Map<Object, Object> entityMap = typeMap.get(entityList.get(0).getClass());
                if (entityMap != null) {
                    entityMap.keySet().removeAll(entityList.stream().map(IEntity::getId).collect(Collectors.toList()));
                    localCacheHash.get().get(entityList.get(0).getClass()).keySet().retainAll(entityMap.keySet());
                }
            }
        }
    }

    public void clearCache() {
        if (disableCache) {
            return;
        }
//        findChangedEntities(entity -> logger.warn("Cache entity is changed, discard it without update. ("
//                + entity.getClass().getSimpleName() + "=" + entity.getId() + ")"));
        localCacheEntity.remove();
        localCacheHash.remove();
    }

    private void findChangedEntities(IExecutorA<IEntity> callback) {
        if (disableCache) {
            return;
        }
        if (localCacheEntity.get() != null) {
            localCacheEntity.get().values().forEach(entityMap -> entityMap.values().forEach(o -> {
                IEntity entity = (IEntity) o;
                Integer hashCode1 = localCacheHash.get().get(entity.getClass()).get(entity.getId());
                int hashCode2 = entity.toString().hashCode();
                if (hashCode1 != hashCode2) {
                    callback.doExecute(entity);
                }
            }));
        }
    }

    public void updateChangedEntities() {
        if (disableCache) {
            return;
        }
        findChangedEntities(daoFactory.getDaoHelper()::update);
    }

    public <T extends IEntity> T getCacheEntity(T entity) {
        if (disableCache) {
            return null;
        }
        if (entity == null || entity.isNullId()) {
            return null;
        }
        Map<Class<?>, Map<Object, Object>> typeMap = localCacheEntity.get();
        if (typeMap == null) {
            return null;
        }
        Map<Object, Object> entityMap = typeMap.get(entity.getClass());
        if (entityMap == null) {
            return null;
        }
        T cacheEntity = (T) entityMap.get(entity.getId());
        if (cacheEntity != null) {
            Integer hashCode1 = localCacheHash.get().get(entity.getClass()).get(entity.getId());
            int hashCode2 = cacheEntity.toString().hashCode();
            if (hashCode1 != hashCode2) {
                logger.warn("Cache entity is changed, discard it and then use the new entity. ("
                        + entity.getClass().getSimpleName() + "=" + entity.getId() + ")");
                entityMap.remove(entity.getId());
                localCacheHash.get().get(entity.getClass()).remove(entity.getId());
                cacheEntity = null;
            }
        }
        return cacheEntity;
    }

    public <T extends IEntity> void putCacheEntity(T entity) {
        if (disableCache) {
            return;
        }
        if (entity == null || entity.isNullId()) {
            return;
        }
        Map<Class<?>, Map<Object, Object>> typeMap = localCacheEntity.get();
        if (typeMap == null) {
            typeMap = new HashMap<>();
            localCacheEntity.set(typeMap);
            localCacheHash.set(new HashMap<>());
        }
        typeMap.computeIfAbsent(entity.getClass(), type -> new HashMap<>()).put(entity.getId(), entity);
        localCacheHash.get().computeIfAbsent(entity.getClass(), type -> new HashMap<>()).put(entity.getId(), entity.toString().hashCode());
    }
}
