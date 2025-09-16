package dev.muho.user.error;

import dev.muho.user.common.error.BusinessException;
import dev.muho.user.common.error.ErrorCode;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
