package com.esin.jdbc.query.criterion;

import com.esin.base.utility.ListUtil;
import com.esin.base.utility.Utility;
import com.esin.jdbc.define.Column;
import com.esin.jdbc.entity.IEntity;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Expression extends Criterion.Single {

    private final String name;

    private final String name2;

    private final String sql;

    private final List<Object> params;

    private Expression(String name, String sql, List<Object> params) {
        super(null);
        this.name = name;
        this.name2 = Utility.EMPTY;
        this.sql = sql;
        this.params = params;
    }

    private Expression(String name, String name2, String sql) {
        super(null);
        this.name = name;
        this.name2 = name2;
        this.sql = sql;
        this.params = Collections.emptyList();
    }

    public <T extends IEntity> String getSql(From<T> from, List<Object> paramList) {
        if (Utility.isEmpty(name)) {
            return sql;
        } else if (Utility.isEmpty(name2)) {
            if (Utility.isNotEmpty(this.params)) {
                String subSql = handleSubQuery(from, paramList);
                if (Utility.isNotEmpty(subSql)) {
                    return subSql;
                }
                if (this.params.get(0) instanceof Enum) {
                    Column column = from.getColumn(name);
                    for (Object o : this.params) {
                        if (column.enum_name()) {
                            paramList.add(((Enum<?>) o).name());
                        } else {
                            paramList.add(((Enum<?>) o).ordinal());
                        }
                    }
                } else {
                    paramList.addAll(this.params);
                }
            }
            return sql.replace("_name_", from.getColumnName(name));
        } else {
            return sql.replace("_name_", from.getColumnExpression(name))
                    .replace("_name2_", from.getColumnExpression(name2));
        }
    }

    private <T extends IEntity> String handleSubQuery(From<T> from, List<Object> paramList) {
        SubQuery<?> subQuery = null;
        if (params.get(0) instanceof SubQuery<?>) {
            subQuery = (SubQuery<?>) params.get(0);
        } else if (params.get(0) instanceof Query<?>) {
            subQuery = ((Query<?>) params.get(0)).asSubQuery(SqlFunc.SubQueryMethod.Null);
        }
        if (subQuery != null) {
            return sql.replace("_name_", from.getColumnExpression(name))
                    .replace("(?)", "?")
                    .replace("?", subQuery.getSql(from, paramList));
        } else {
            return null;
        }
    }

    public static Expression is_null(String name) {
        return new Expression(name, "_name_ is null", Collections.emptyList());
    }

    public static Expression is_not_null(String name) {
        return new Expression(name, "_name_ is not null", Collections.emptyList());
    }

    public static Expression expression(String expression) {
        return Utility.isEmpty(expression) ? null : new Expression(Utility.EMPTY, expression, Collections.emptyList());
    }

    public static Expression eq(String name, Object value) {
        return value == null ? is_null(name) : new Expression(name, "_name_=?", Collections.singletonList(value));
    }

    public static Expression eq_ignore_case(String name, String value) {
        return Utility.isEmpty(name) ? is_null(name) :
                new Expression(name, "lower(_name_)=?", Collections.singletonList(value.toLowerCase()));
    }

    public static Expression ne(String name, Object value) {
        return value == null ? is_not_null(name) : new Expression(name, "_name_<>?", Collections.singletonList(value));
    }

    public static Expression ge(String name, Object value) {
        return value == null ? null : new Expression(name, "_name_>=?", Collections.singletonList(value));
    }

    public static Expression gt(String name, Object value) {
        return value == null ? null : new Expression(name, "_name_>?", Collections.singletonList(value));
    }

    public static Expression le(String name, Object value) {
        return value == null ? null : new Expression(name, "_name_<=?", Collections.singletonList(value));
    }

    public static Expression lt(String name, Object value) {
        return value == null ? null : new Expression(name, "_name_<?", Collections.singletonList(value));
    }

    public static Expression ge_le(String name, Object minValue, Object maxValue) {
        return minValue == null || maxValue == null ? null : new Expression(name, "_name_>=? and _name_<=?", Arrays.asList(minValue, maxValue));
    }

    public static Expression ge_lt(String name, Object minValue, Object maxValue) {
        return minValue == null || maxValue == null ? null : new Expression(name, "_name_>=? and _name_<?", Arrays.asList(minValue, maxValue));
    }

    public static Expression gt_le(String name, Object minValue, Object maxValue) {
        return minValue == null || maxValue == null ? null : new Expression(name, "_name_>? and _name_<=?", Arrays.asList(minValue, maxValue));
    }

    public static Expression gt_lt(String name, Object minValue, Object maxValue) {
        return minValue == null || maxValue == null ? null : new Expression(name, "_name_>? and _name_<?", Arrays.asList(minValue, maxValue));
    }

    public static Expression eq_name(String name, String name2) {
        return new Expression(name, name2, "_name_=_name2_");
    }

    public static Expression ne_name(String name, String name2) {
        return new Expression(name, name2, "_name_<>_name2_");
    }

    public static Expression ge_name(String name, String name2) {
        return new Expression(name, name2, "_name_>=_name2_");
    }

    public static Expression gt_name(String name, String name2) {
        return new Expression(name, name2, "_name_>_name2_");
    }

    public static Expression le_name(String name, String name2) {
        return new Expression(name, name2, "_name_<=_name2_");
    }

    public static Expression lt_name(String name, String name2) {
        return new Expression(name, name2, "_name_<_name2_");
    }

    public static Expression like(String name, String value, SqlFunc.LikeMatch likeMatch) {
        return like(name, value, likeMatch, false, false, null);
    }

    public static Expression ilike(String name, String value, SqlFunc.LikeMatch likeMatch) {
        return like(name, value, likeMatch, true, false, null);
    }

    public static Expression not_like(String name, String value, SqlFunc.LikeMatch likeMatch) {
        return like(name, value, likeMatch, false, true, null);
    }

    public static Expression not_ilike(String name, String value, SqlFunc.LikeMatch likeMatch) {
        return like(name, value, likeMatch, true, true, null);
    }

    public static Expression like(String name, String value,
                                  SqlFunc.LikeMatch likeMatch,
                                  boolean ignoreCase,
                                  boolean isNotLike,
                                  Character escapeChar) {
        if (Utility.isEmpty(value)) {
            return null;
        }
        if (escapeChar == null && (value.contains("%") || value.contains("_"))) {
            value = value.replace("%", "\\%").replace("_", "\\_");
            escapeChar = '\\';
        }
        String expression = "_name_";
        if (ignoreCase) {
            // todo : postgresql can use ilike
            expression = "lower(" + expression + ")";
            value = value.toLowerCase();
        }
        expression += (isNotLike ? " not" : "") + " like ?";
        expression += escapeChar == null ? "" : (" escape '" + escapeChar + "'");
        return new Expression(name, expression, Collections.singletonList(likeMatch.sqlFunc.convert(value)));
    }

    public static Expression in(String name, Collection<?> valueList) {
        if (Utility.isEmpty(valueList)) {
            return null;
        } else if (valueList.size() == 1) {
            return eq(name, ListUtil.first(valueList));
        } else {
            return new Expression(name, "_name_ in (" + StringUtils.repeat("?",
                    ",", valueList.size()) + ")", new ArrayList<>(valueList));
        }
    }

    public static Expression in(String name, Object... values) {
        return in(name, Arrays.asList(values));
    }

    public static Expression not_in(String name, Collection<?> valueList) {
        if (Utility.isEmpty(valueList)) {
            return null;
        } else if (valueList.size() == 1) {
            return ne(name, ListUtil.first(valueList));
        } else {
            return new Expression(name, "_name_ not in (" + StringUtils.repeat("?",
                    ",", valueList.size()) + ")", new ArrayList<>(valueList));
        }
    }

    public static Expression not_in(String name, Object... values) {
        return not_in(name, Arrays.asList(values));
    }

    public static Expression exists(Query<?> query) {
        return new Expression(Utility.EMPTY, "exists ?", Collections.singletonList(query));
    }

    public static Expression not_exists(Query<?> query) {
        return new Expression(Utility.EMPTY, "not exists ?", Collections.singletonList(query));
    }

}
