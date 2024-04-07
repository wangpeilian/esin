package com.esin.base.utility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Map;

public class Logger {
    private static final Map<Class<?>, Logger> logMap = new HashMap<Class<?>, Logger>() {
        @Override
        public Logger get(Object key) {
            Logger logger = super.get(key);
            if (logger == null) {
                Class<?> clazz = (Class<?>) key;
                logger = new Logger(clazz);
                put(clazz, logger);
            }
            return logger;
        }
    };

    public static Logger getLogger(Class<?> clazz) {
        return logMap.get(clazz);
    }

    private static Level level = Level.INFO;
    private static boolean useSlf4j = true;

    public static void setLevel(Level level) {
        if (level != null) {
            Logger.level = level;
        }
    }

    public static void setUseCommonLogForReplaceSlf4j() {
        Logger.useSlf4j = false;
    }

    private final org.slf4j.Logger logger;
    private final Log log;

    private Logger(Class<?> clazz) {
        if (useSlf4j) {
            this.logger = LoggerFactory.getLogger(clazz);
            this.log = null;
        } else {
            this.logger = null;
            this.log = LogFactory.getLog(clazz);
        }
    }

    public static boolean checkLevel(Level level) {
        return level.toInt() >= Logger.level.toInt();
    }

    public void trace(String msg) {
        if (checkLevel(Level.TRACE)) {
            if (useSlf4j) {
                logger.info("[" + Level.TRACE.name() + "] " + msg);
            } else {
                log.info("[" + Level.TRACE.name() + "] " + msg);
            }
        }
    }

    public void debug(String msg) {
        if (checkLevel(Level.DEBUG)) {
            if (useSlf4j) {
                logger.info("[" + Level.DEBUG.name() + "] " + msg);
            } else {
                log.info("[" + Level.DEBUG.name() + "] " + msg);
            }
        }
    }

    public void info(String msg) {
        if (checkLevel(Level.INFO)) {
            if (useSlf4j) {
                logger.info(msg);
            } else {
                log.info(msg);
            }
        }
    }

    public void warn(String msg) {
        if (checkLevel(Level.WARN)) {
            if (useSlf4j) {
                logger.warn(msg);
            } else {
                log.warn(msg);
            }
        }
    }

    public void error(String msg, Throwable t) {
        if (checkLevel(Level.ERROR)) {
            if (useSlf4j) {
                logger.error(msg, t);
            } else {
                log.error(msg, t);
            }
        }
    }
}
