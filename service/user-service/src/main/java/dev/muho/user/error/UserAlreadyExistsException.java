package dev.muho.user.error;

import dev.muho.user.common.error.BusinessException;
import dev.muho.user.common.error.ErrorCode;

public class UserAlreadyExistsException extends BusinessException {

    public UserAlreadyExistsException() {
        super(ErrorCode.USER_ALREADY_EXISTS);
    }
}
