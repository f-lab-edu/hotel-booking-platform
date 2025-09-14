package dev.muho.booking.domain.booking.client;

import java.util.Optional;

public interface RatePlanClient {
    Optional<String> findRatePlanName(Long ratePlanId);
}

