package dev.muho.hotel.domain.promotion.service;

import dev.muho.hotel.domain.promotion.dto.command.PromotionCreateCommand;
import dev.muho.hotel.domain.promotion.dto.command.PromotionInfoResult;
import dev.muho.hotel.domain.promotion.dto.command.PromotionUpdateCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PromotionService {

    PromotionInfoResult create(PromotionCreateCommand command);

    PromotionInfoResult findById(Long id);

    PromotionInfoResult update(Long id, PromotionUpdateCommand command);

    void deleteById(Long id);

    Page<PromotionInfoResult> search(Long hotelId, Pageable pageable);
}

