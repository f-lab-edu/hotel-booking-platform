package dev.muho.user.error;

import dev.muho.user.common.error.BusinessException;
import dev.muho.user.common.error.ErrorCode;

public class InvalidTokenException extends BusinessException {
    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }
}
