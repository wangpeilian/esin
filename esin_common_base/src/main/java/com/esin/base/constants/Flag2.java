package com.esin.base.constants;

public enum Flag2 implements IEnum {
    False("否"), True("是"), Null("空");

    private final String title;

    private Flag2(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public boolean isNull() {
        return Null.equals(this);
    }

    public boolean isTrue() {
        return True.equals(this);
    }

    public boolean isFalse() {
        return False.equals(this);
    }

    public boolean isNotNull() {
        return !isNull();
    }

    public boolean isNotTrue() {
        return !isTrue();
    }

    public boolean isNotFalse() {
        return !isFalse();
    }

    public static Flag2 toFlag(Boolean flag) {
        return flag == null ? Flag2.Null : flag ? Flag2.True : Flag2.False;
    }

    public static Flag2 toFlag(Flag2 flag) {
        return flag == null ? Flag2.Null : flag;
    }
}
