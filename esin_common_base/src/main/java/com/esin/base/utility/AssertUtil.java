package com.esin.base.utility;

import com.esin.base.exception.LogicException;

import java.io.File;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

public class AssertUtil {
    private static boolean isFalse(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof Boolean) {
            return Boolean.FALSE.equals(value);
        }
        if (value instanceof CharSequence) {
            return Utility.isEmpty((CharSequence) value);
        }
        if (value instanceof Collection) {
            return Utility.isEmpty((Collection) value);
        }
        if (value instanceof Map) {
            return Utility.isEmpty((Map) value);
        }
        if (value instanceof Iterable) {
            return Utility.isEmpty((Iterable) value);
        }
        if (value instanceof Iterator) {
            return Utility.isEmpty((Iterator) value);
        }
        if (value instanceof Enumeration) {
            return Utility.isEmpty((Enumeration) value);
        }
        if (value instanceof Object[]) {
            return Utility.isEmpty((Object[]) value);
        }
        if (value instanceof File) {
            return Utility.isEmpty((File) value);
        }
        return false;
    }

    public static void check(Object value, String message, Object... args) {
        if (isFalse(value)) {
            throw new LogicException(message, args);
        }
    }
}
