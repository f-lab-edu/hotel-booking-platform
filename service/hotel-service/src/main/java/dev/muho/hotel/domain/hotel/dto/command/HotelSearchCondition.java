package dev.muho.hotel.domain.hotel.dto.command;

public record HotelSearchCondition(
        String name
) {
    public HotelSearchCondition {
        // 추가 검증 필요 시 여기에 작성
    }

    public static HotelSearchCondition of(String name) {
        return new HotelSearchCondition(name);
    }
}

