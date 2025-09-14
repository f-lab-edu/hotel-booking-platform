package dev.muho.hotel.domain.promotion.service;

import dev.muho.hotel.domain.promotion.dto.command.PromotionCreateCommand;
import dev.muho.hotel.domain.promotion.dto.command.PromotionInfoResult;
import dev.muho.hotel.domain.promotion.dto.command.PromotionUpdateCommand;
import dev.muho.hotel.domain.promotion.entity.Promotion;
import dev.muho.hotel.domain.promotion.error.PromotionNotFoundException;
import dev.muho.hotel.domain.promotion.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    @Override
    @Transactional
    public PromotionInfoResult create(PromotionCreateCommand command) {
        Promotion p = Promotion.createNew(
                command.applicableHotelId(),
                command.applicableRoomTypeId(),
                command.applicableRatePlanId(),
                command.name(),
                command.description(),
                command.discountType(),
                command.discountValue(),
                command.minNights(),
                command.bookingWindowMinDays(),
                command.bookingWindowMaxDays()
        );
        Promotion saved = promotionRepository.save(p);
        return PromotionInfoResult.from(saved);
    }

    @Override
    public PromotionInfoResult findById(Long id) {
        Promotion p = promotionRepository.findById(id).orElseThrow(PromotionNotFoundException::new);
        return PromotionInfoResult.from(p);
    }

    @Override
    @Transactional
    public PromotionInfoResult update(Long id, PromotionUpdateCommand command) {
        Promotion updating = Promotion.builder()
                .id(id)
                .name(command.name())
                .description(command.description())
                .discountType(command.discountType())
                .discountValue(command.discountValue())
                .minNights(command.minNights())
                .bookingWindowMinDays(command.bookingWindowMinDays())
                .bookingWindowMaxDays(command.bookingWindowMaxDays())
                .build();

        Promotion updated = promotionRepository.update(id, updating);
        return PromotionInfoResult.from(updated);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        promotionRepository.deleteById(id);
    }

    @Override
    public Page<PromotionInfoResult> search(Long hotelId, Pageable pageable) {
        return promotionRepository.search(hotelId, pageable).map(PromotionInfoResult::from);
    }
}

