package dev.muho.hotel.domain;

public enum HotelStatus {
    OPERATING("운영 중"),
    SUSPENDED("중지"),
    CLOSED("미운영");

    private final String description;

    HotelStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
