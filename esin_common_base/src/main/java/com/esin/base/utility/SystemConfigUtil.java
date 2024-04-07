package com.esin.base.utility;

import org.slf4j.event.Level;

import java.util.Locale;
import java.util.TimeZone;

public class SystemConfigUtil {
    private static boolean debugMode = false;
    private static Locale locale = Locale.getDefault();
    private static TimeZone timeZone = TimeZone.getDefault();
    private static String rootPath = null;

    public static void setRootPath(String rootPath) {
        SystemConfigUtil.rootPath = rootPath;
    }

    static {
        SystemConfigUtil.setDebugMode(true);
    }

    public static boolean isWindows() {
        return Utility.toEmpty(System.getProperty("os.name")).toLowerCase().contains("windows");
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static void setDebugMode(boolean debugMode) {
        SystemConfigUtil.debugMode = debugMode;
        if (debugMode) {
            Logger.setLevel(Level.TRACE);
        } else {
            Logger.setLevel(Level.INFO);
        }
    }

    public static Locale getLocale() {
        return locale;
    }

    public static void setLocale(Locale locale) {
        SystemConfigUtil.locale = locale;
    }

    public static TimeZone getTimeZone() {
        return timeZone;
    }

    public static void setTimeZone(TimeZone timeZone) {
        SystemConfigUtil.timeZone = timeZone;
    }

    public static String getRootPath() {
        return rootPath;
    }
}
