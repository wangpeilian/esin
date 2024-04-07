package com.esin.jdbc.entity;

import com.esin.base.constants.IEnum;

public enum RecordStatus implements IEnum {
    active("活动"), inactive("无效"), deleted("已删除");

    private final String title;

    private RecordStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
