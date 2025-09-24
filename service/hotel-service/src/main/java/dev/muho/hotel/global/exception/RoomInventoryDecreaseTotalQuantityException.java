package dev.muho.hotel.global.exception;

import java.text.MessageFormat;

public class RoomInventoryDecreaseTotalQuantityException extends CustomException{

    public RoomInventoryDecreaseTotalQuantityException(int reservedQuantity) {
        super(ErrorCode.CANNOT_DECREASE_TOTAL_QUANTITY,
                MessageFormat.format(ErrorCode.CANNOT_DECREASE_TOTAL_QUANTITY.getMessage(),
                        reservedQuantity));
    }
}
