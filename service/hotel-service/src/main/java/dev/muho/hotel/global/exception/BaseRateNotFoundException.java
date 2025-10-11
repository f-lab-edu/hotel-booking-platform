package dev.muho.hotel.global.exception;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BaseRateNotFoundException extends CustomException {

    public BaseRateNotFoundException(LocalDate date) {
        super(ErrorCode.BASE_RATE_NOT_FOUND,
                MessageFormat.format(ErrorCode.BASE_RATE_NOT_FOUND.getMessage(),
                        date.format(DateTimeFormatter.ISO_LOCAL_DATE)));
    }
}
