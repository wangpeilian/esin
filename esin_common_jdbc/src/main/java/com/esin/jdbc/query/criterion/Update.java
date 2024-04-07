package com.esin.jdbc.query.criterion;

import com.esin.base.utility.AssertUtil;
import com.esin.base.utility.Utility;
import com.esin.jdbc.dao.IDao;
import com.esin.jdbc.entity.IEntity;

import java.util.List;
import java.util.Map;

public class Update<T extends IEntity> extends Query<T> {

    public Update(IDao dao, Class<T> clazz) {
        super(dao, clazz);
        getWhere().withoutUseDefaultFilterExpression();
    }

    private Map<String, String> updatePropertyMap = null;
    private Map<String, Object> updateValueMap = null;

    public Update<T> setUpdatePropertyMap(Map<String, String> updatePropertyMap) {
        this.updatePropertyMap = updatePropertyMap;
        return this;
    }

    public Update<T> setUpdateValueMap(Map<String, Object> updateValueMap) {
        this.updateValueMap = updateValueMap;
        return this;
    }

    public String getSql(List<Object> paramList) {
        AssertUtil.check(Utility.isNotEmpty(updatePropertyMap) || Utility.isNotEmpty(updateValueMap), "更新对象不能为空。");
        StringBuilder sb = new StringBuilder();
        if (Utility.isNotEmpty(updatePropertyMap)) {
            for (String name : updatePropertyMap.keySet()) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(getFrom().getColumnName(getFrom().getClazz(), name));
                sb.append("=");
                sb.append(getFrom().getColumnExpression(updatePropertyMap.get(name)));
            }
        }
        if (Utility.isNotEmpty(updateValueMap)) {
            for (String name : updateValueMap.keySet()) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(getFrom().getColumnName(getFrom().getClazz(), name));
                sb.append("=");
                sb.append(handleSubQuery(updateValueMap.get(name), paramList));
            }
        }
        String sql = getWhere().getSql(paramList);
        return "update " +
                getDao().getTableName(getFrom().getClazz()) +
                " set " + sb.toString()
                + getFrom().getWhereSqlForUpdate(sql);
    }

    private String handleSubQuery(Object param, List<Object> paramList) {
        SubQuery<?> subQuery = null;
        if (param instanceof SubQuery<?>) {
            subQuery = (SubQuery<?>) param;
        } else if (param instanceof Query<?>) {
            subQuery = ((Query<?>) param).asSubQuery(SqlFunc.SubQueryMethod.Null);
        }
        if (subQuery != null) {
            return subQuery.getSql(null, paramList);
        } else {
            paramList.add(param);
            return "?";
        }
    }

}
