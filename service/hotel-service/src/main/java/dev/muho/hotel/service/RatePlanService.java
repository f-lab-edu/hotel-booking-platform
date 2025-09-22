package dev.muho.hotel.service;

import dev.muho.hotel.dto.command.RatePlanCreateCommand;
import dev.muho.hotel.dto.command.RatePlanInfoResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RatePlanService {

    RatePlanInfoResult create(Long roomTypeId, RatePlanCreateCommand command);

    RatePlanInfoResult findById(Long roomTypeId, Long id);

    RatePlanInfoResult update(Long roomTypeId, Long id, RatePlanCreateCommand command);

    void deleteById(Long roomTypeId, Long id);

    Page<RatePlanInfoResult> search(Long roomTypeId, Pageable pageable);
}

