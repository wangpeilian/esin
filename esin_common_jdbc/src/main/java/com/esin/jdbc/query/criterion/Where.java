package com.esin.jdbc.query.criterion;

import com.esin.base.utility.MapUtil;
import com.esin.base.utility.Utility;
import com.esin.jdbc.entity.IEntity;

import java.util.List;
import java.util.Map;

public class Where<T extends IEntity> extends StatementParam<T> {

    protected Where(Query<T> query) {
        super(query);
    }

    private boolean useDefaultFilterExpression = true;
    private final Criterion criterion = Criterion.and();

    public Where<T> withoutUseDefaultFilterExpression() {
        this.useDefaultFilterExpression = false;
        return this;
    }

    public Where<T> addCriterion(Criterion... criteria) {
        criterion.add(criteria);
        return this;
    }

    public Where<T> addCriterion(Map<String, Object> paramMap) {
        return addCriterion(Criterion.of(paramMap));
    }

    public Where<T> addCriterion(String name, Object value) {
        return addCriterion(MapUtil.of(name, value));
    }

    protected String getSql(String prefix, List<Object> paramList) {
        Criterion filterCriterion = null;
        if (useDefaultFilterExpression) {
            filterCriterion = getQuery().getDao().getDaoFactory().getSqlHelper()
                    .getFilterExpression4query(getQuery().getFrom().getClazz());
        }
        if (filterCriterion != null) {
            criterion.add(filterCriterion);
        }
        String sql = criterion.getSql(getQuery().getFrom(), paramList);
        if (Utility.isNotEmpty(sql)) {
            sql = prefix + sql;
        }
        return sql;
    }

    @Override
    public String getSql(List<Object> paramList) {
        return getSql(" where ", paramList);
    }

}
