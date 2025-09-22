package dev.muho.hotellegacy.service;

import dev.muho.hotellegacy.dto.command.PromotionCreateCommand;
import dev.muho.hotellegacy.dto.command.PromotionInfoResult;
import dev.muho.hotellegacy.dto.command.PromotionUpdateCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PromotionService {

    PromotionInfoResult create(PromotionCreateCommand command);

    PromotionInfoResult findById(Long id);

    PromotionInfoResult update(Long id, PromotionUpdateCommand command);

    void deleteById(Long id);

    Page<PromotionInfoResult> search(Long hotelId, Pageable pageable);
}

