package com.esin.base.exception;

import com.esin.base.utility.Utility;
import org.apache.commons.lang.StringUtils;

public abstract class BaseException extends RuntimeException {
    private final String localizedMessage;

    public BaseException(String message) {
        super(message);
        this.localizedMessage = message;
    }

    public BaseException(String message, Throwable cause) {
        super(cause);
        this.localizedMessage = message;
    }

    @Override
    public String getLocalizedMessage() {
        return localizedMessage;
    }

    public static String joinMessage(String message, Object... args) {
        return Utility.isEmpty(args) ? message : (message + " (" + StringUtils.join(args, ", ") + ")");
    }
}
