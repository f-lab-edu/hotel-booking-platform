package dev.muho.hotel.domain.promotion.error;

public class PromotionNotFoundException extends RuntimeException {
    public PromotionNotFoundException() {
        super("프로모션을 찾을 수 없습니다.");
    }

    public PromotionNotFoundException(String message) {
        super(message);
    }
}

