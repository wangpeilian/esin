package com.esin.jdbc.define;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Connection;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {

    public static enum Level {
        Default(Connection.TRANSACTION_NONE),
        None(Connection.TRANSACTION_NONE),
        ReadUncommitted(Connection.TRANSACTION_READ_UNCOMMITTED),
        ReadCommitted(Connection.TRANSACTION_READ_COMMITTED),
        RepeatableRead(Connection.TRANSACTION_REPEATABLE_READ),
        Serializable(Connection.TRANSACTION_SERIALIZABLE);

        public final int level;

        private Level(int level) {
            this.level = level;
        }
    }

    boolean savepoint_able() default false;

    Level level() default Level.Default;

}
