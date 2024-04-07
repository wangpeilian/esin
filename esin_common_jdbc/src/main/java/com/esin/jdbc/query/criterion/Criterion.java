package com.esin.jdbc.query.criterion;

import com.esin.base.utility.AssertUtil;
import com.esin.base.utility.Utility;
import com.esin.jdbc.entity.IEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class Criterion implements ICriterion {

    public static final Criterion Empty = new Criterion() {
        @Override
        protected final <T extends IEntity> String getSql(String conj, From<T> from, List<Object> paramList) {
            return Utility.EMPTY;
        }

        @Override
        public final Criterion add(Criterion... criteria) {
            throw new UnsupportedOperationException("Criterion.Empty is unsupported add criteria.");
        }

        @Override
        public final <T extends IEntity> String getSql(From<T> from, List<Object> paramList) {
            return Utility.EMPTY;
        }
    };

    protected final List<Criterion> criterionList = new ArrayList<>();

    public abstract <T extends IEntity> String getSql(From<T> from, List<Object> paramList);

    protected <T extends IEntity> String getSql(String conj, From<T> from, List<Object> paramList) {
        if (criterionList.isEmpty()) {
            return Utility.EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (Criterion criterion : criterionList) {
            String sql = criterion.getSql(from, paramList);
            if (Utility.isNotEmpty(sql)) {
                if (sb.length() != 0) {
                    sb.append(conj);
                }
                sb.append(sql);
            }
        }
        return sb.toString();
    }

    public static class And extends Criterion {
        @Override
        public <T extends IEntity> String getSql(From<T> from, List<Object> paramList) {
            return getSql(" and ", from, paramList);
        }
    }

    public static class Or extends Criterion {
        @Override
        public <T extends IEntity> String getSql(From<T> from, List<Object> paramList) {
            String sql = getSql(" or ", from, paramList);
            if (Utility.isNotEmpty(sql) && criterionList.size() > 1) {
                sql = "(" + sql + ")";
            }
            return sql;
        }
    }

    public static class Single extends Criterion {

        public Single(Criterion criterion) {
            if (criterion != null) {
                criterionList.add(criterion);
            }
        }

        @Override
        public Criterion add(Criterion... criteria) {
            throw new UnsupportedOperationException(getClass().getSimpleName() + " is unsupported add criteria.");
        }

        @Override
        public <T extends IEntity> String getSql(From<T> from, List<Object> paramList) {
            AssertUtil.check(criterionList.size() == 1,
                    "Criterion.Single should has 1 element. (" + from.getClazz().getSimpleName() + ", " + criterionList.size() + ")");
            return criterionList.get(0).getSql(from, paramList);
        }
    }

    public static class Not extends Single {
        public Not(Criterion criterion) {
            super(criterion);
        }

        @Override
        public <T extends IEntity> String getSql(From<T> from, List<Object> paramList) {
            String sql = super.getSql(from, paramList);
            if (Utility.isEmpty(sql)) {
                return Utility.EMPTY;
            }
            return "not (" + sql + ")";
        }
    }

    public Criterion add(Criterion... criteria) {
        criterionList.addAll(Arrays.asList(criteria));
        return this;
    }

    public static Criterion and(Criterion... criteria) {
        return new Criterion.And().add(criteria);
    }

    public static Criterion or(Criterion... criteria) {
        return new Criterion.Or().add(criteria);
    }

    public static ICriterion of(String key, Object value) {
        if (value == null) {
            return Utility.isEmpty(key) ? Criterion.Empty : Expression.is_null(key);
        } else if (value instanceof ICriterion) {
            return (ICriterion) value;
        } else if (value instanceof Collection) {
            return Expression.in(key, (Collection<?>) value);
        } else if (value.getClass().isArray()) {
            return Expression.in(key, (Object[]) value);
        } else {
            return Expression.eq(key, value);
        }
    }

    public static Criterion of(Map<String, Object> paramMap) {
        Criterion criterion = Criterion.and();
        if (Utility.isNotEmpty(paramMap)) {
            paramMap.forEach((key, value) -> {
                if (value instanceof Criterion || !(value instanceof ICriterion)) {
                    criterion.add((Criterion) of(key, value));
                }
            });
        }
        return criterion;
    }

    public static Criterion not(Criterion criterion) {
        return new Criterion.Not(criterion);
    }

}
