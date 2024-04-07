package com.esin.base.utility;

import java.text.FieldPosition;

public class NumberFormatZoom extends DoubleFormat {
    private final double zoom;

    public NumberFormatZoom(String pattern, double zoom) {
        super(pattern);
        this.zoom = zoom;
    }

    @Override
    public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
        return super.format(number / zoom, result, fieldPosition);
    }
}
