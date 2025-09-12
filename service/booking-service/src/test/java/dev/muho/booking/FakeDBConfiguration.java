package dev.muho.booking;

import dev.muho.booking.domain.booking.FakeBookingRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FakeDBConfiguration {

    @Bean
    public FakeBookingRepository bookingRepository() {
        return new FakeBookingRepository();
    }
}
