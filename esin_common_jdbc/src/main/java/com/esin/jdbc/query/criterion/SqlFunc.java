package com.esin.jdbc.query.criterion;

import com.esin.base.executor.IExecutorAAR;

public class SqlFunc {

    private static final IExecutorAAR<String, String, String> method = (method, name) -> method + "(" + name + ")";
    private static final IExecutorAAR<String, String, String> like_match = (pattern, value) -> pattern.replace("_value_", value);

    private final String name;
    private final IExecutorAAR<String, String, String> executor;

    private SqlFunc(String name, IExecutorAAR<String, String, String> executor) {
        this.name = name;
        this.executor = executor;
    }

    public String convert(String value) {
        return executor.doExecute(name, value);
    }

    public static String Distinct(String name) {
        return "distinct " + name;
    }

    public enum Aggregate {
        Count(new SqlFunc("count", method)),
        Sum(new SqlFunc("sum", method)),
        Avg(new SqlFunc("avg", method)),
        Max(new SqlFunc("max", method)),
        Min(new SqlFunc("min", method));

        public final SqlFunc sqlFunc;

        Aggregate(SqlFunc sqlFunc) {
            this.sqlFunc = sqlFunc;
        }
    }

    public enum LikeMatch {
        Start(new SqlFunc("_value_%", like_match)),
        End(new SqlFunc("%_value_", like_match)),
        Any(new SqlFunc("%_value_%", like_match)),
        Exact(new SqlFunc("_value_", like_match));

        public final SqlFunc sqlFunc;

        LikeMatch(SqlFunc sqlFunc) {
            this.sqlFunc = sqlFunc;
        }
    }

    public enum SubQueryMethod {
        Null, any, all
    }
}
