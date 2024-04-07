package com.esin.base.exception;

import com.esin.base.utility.Utility;
import org.apache.commons.lang.StringUtils;

public class LogicException extends BaseException {
    public LogicException(String message, Object... args) {
        super(Utility.isEmpty(args) ? message : (message + " (" + StringUtils.join(args, ", ") + ")"));
    }
}
