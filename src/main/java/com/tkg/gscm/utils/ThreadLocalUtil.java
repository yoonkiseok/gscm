package com.tkg.gscm.utils;

import java.util.Map;

public class ThreadLocalUtil {

    private static final ThreadLocal<Object> valueThreadLocal = new ThreadLocal<>();

    public static void setThreadLocalValue(Object value) {
        if (value instanceof String ||
                value instanceof Integer ||
                value instanceof Double ||
                value instanceof Float ||
                value instanceof Map) {
            valueThreadLocal.set(value);
        }
    }

    public static Object getThreadLocalValue() {
        return valueThreadLocal.get();
    }

    public static void removeThreadLocalValue() {
        if (valueThreadLocal.get() != null) {
            valueThreadLocal.remove();
        }
    }
}
