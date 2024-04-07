package com.esin.jdbc.define;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    public static final int string_length_short = 255;
    public static final int string_length_medium = 4095;
    public static final int string_length_long = 65535;

    String name();

    int string_length() default string_length_short;

    int char_length() default 0;

    boolean enum_name() default true;

    boolean null_able() default false;

    boolean insert_able() default true;

    boolean update_able() default true;

    boolean index_able() default false;

    int order() default 100;

    boolean parent() default true;

    // ======================================================

    String label() default "";

    FieldFormat format() default FieldFormat.none;

}
