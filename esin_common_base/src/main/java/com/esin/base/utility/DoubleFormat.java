package com.esin.base.utility;

import java.text.DecimalFormat;
import java.text.FieldPosition;

public class DoubleFormat extends DecimalFormat {
    public DoubleFormat(String pattern) {
        super(pattern);
    }

    @Override
    public final StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
        return format(Long.valueOf(number).doubleValue(), result, fieldPosition);
    }
}
