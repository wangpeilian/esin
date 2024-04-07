package com.esin.base.constants;

public enum Flag1 implements IEnum {
    False("否"), True("是");

    private final String title;

    private Flag1(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public boolean isTrue() {
        return True.equals(this);
    }

    public boolean isFalse() {
        return False.equals(this);
    }

    public static Flag1 toFlag(Boolean flag) {
        return Boolean.TRUE.equals(flag) ? Flag1.True : Flag1.False;
    }

    public static Flag1 toFlag(Flag1 flag) {
        return flag == null ? Flag1.False : flag;
    }
}
