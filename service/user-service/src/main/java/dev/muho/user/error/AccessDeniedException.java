package dev.muho.user.error;

import dev.muho.user.common.error.BusinessException;
import dev.muho.user.common.error.ErrorCode;

public class AccessDeniedException extends BusinessException {

    public AccessDeniedException() {
        super(ErrorCode.ACCESS_DENIED);
    }
}
