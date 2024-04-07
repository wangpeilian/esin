package com.esin.base.exception;

import java.util.List;

public class ValidateException extends BaseException {
    private final List<String> errorMsgList;

    public ValidateException(List<String> errorMsgList) {
        super("Data validate error");
        this.errorMsgList = errorMsgList;
    }

    public List<String> getErrorMsgList() {
        return errorMsgList;
    }
}
