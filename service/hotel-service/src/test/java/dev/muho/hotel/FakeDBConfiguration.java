package dev.muho.hotel;

import dev.muho.hotel.domain.rateplan.FakeRatePlanRepository;
import dev.muho.hotel.domain.roominventory.FakeRoomInventoryRepository;
import dev.muho.hotel.domain.roomtype.FakeRoomTypeRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FakeDBConfiguration {

    @Bean
    public FakeHotelRepository hotelRepository() {
        return new FakeHotelRepository();
    }

    @Bean
    public FakeRoomTypeRepository roomTypeRepository() {
        return new FakeRoomTypeRepository();
    }

    @Bean
    public FakeRoomInventoryRepository roomInventoryRepository() {
        return new FakeRoomInventoryRepository();
    }

    @Bean
    public FakeRatePlanRepository ratePlanRepository() {
        return new FakeRatePlanRepository();
    }

    @Bean
    public FakeRateCalendarRepository rateCalendarRepository() {
        return new FakeRateCalendarRepository();
    }

    @Bean
    public FakePromotionRepository promotionRepository() {
        return new FakePromotionRepository();
    }
}
