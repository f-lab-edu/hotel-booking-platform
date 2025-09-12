package dev.muho.user.domain.user.error;

import dev.muho.user.common.error.BusinessException;
import dev.muho.user.common.error.ErrorCode;

public class InvalidUserInputException extends BusinessException {
    public InvalidUserInputException() {
        super(ErrorCode.INVALID_USER_INPUT);
    }

    public InvalidUserInputException(String detail) {
        super(ErrorCode.INVALID_USER_INPUT, detail);
    }
}
