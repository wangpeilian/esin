package com.esin.jdbc.define;

import com.esin.base.constants.IEnum;
import com.esin.base.executor.IExecutorAR;
import com.esin.base.utility.FormatUtil;
import com.esin.base.utility.Utility;

public enum FieldFormat {
    none(value -> value == null ? Utility.EMPTY : String.valueOf(value)),
    date(value -> {
        return FormatUtil.formatDate((Integer) value);
    }),
    security_number(value -> {
        return FormatUtil.formatDouble2Comma(zoom((Number) value, 100d));
    }),
    security_money(value -> {
        return FormatUtil.formatDouble2Comma(zoom((Number) value, 100d));
    }),
    security_price(value -> {
        return FormatUtil.formatDouble3(zoom((Number) value, 10000d));
    }),
    security_cost(value -> {
        return FormatUtil.formatDouble3(zoom((Number) value, 10000d));
    }),
    percent(value -> {
        return FormatUtil.formatPercent(zoom((Number) value, 1000000d));
    }),
    enum_title(value -> {
        return ((IEnum) value).getTitle();
    });

    private final IExecutorAR<Object, String> executor;

    FieldFormat(IExecutorAR<Object, String> executor) {
        this.executor = executor;
    }

    public String format(Object value) {
        return executor.doExecute(value);
    }

    private static Double zoom(Number number, double zoom) {
        if (number == null) {
            return null;
        }
        return number.doubleValue() / zoom;
    }
}
