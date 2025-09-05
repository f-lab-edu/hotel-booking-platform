package dev.muho.hotel.domain.promotion;

import dev.muho.hotel.domain.promotion.dto.PromotionCreateRequest;
import dev.muho.hotel.domain.promotion.dto.PromotionResponse;
import dev.muho.hotel.domain.promotion.dto.PromotionUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FakePromotionRepository {

    private final Map<Long, PromotionResponse> db = new HashMap<>();
    private long currentId = 1L;

    public Page<PromotionResponse> findAll(Pageable pageable) {
        List<PromotionResponse> sortedPromotions = new ArrayList<>(db.values());

        sortedPromotions.sort(Comparator.comparing(PromotionResponse::getId));

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), sortedPromotions.size());

        if (start > sortedPromotions.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, sortedPromotions.size());
        }

        List<PromotionResponse> content = sortedPromotions.subList(start, end);

        return new PageImpl<>(content, pageable, sortedPromotions.size());
    }

    public PromotionResponse findById(Long id) {
        return db.get(id);
    }

    public PromotionResponse save(PromotionCreateRequest request) {
        PromotionResponse newPromotion = new PromotionResponse(currentId++, request.getApplicableHotelId(), request.getApplicableRoomTypeId(), request.getApplicableRatePlanId(), request.getName(), request.getDescription(), request.getDiscountType(), request.getDiscountValue(), request.getMinNights(), request.getBookingWindowMinDays(), request.getBookingWindowMaxDays());
        db.put(newPromotion.getId(), newPromotion);
        return newPromotion;
    }

    public PromotionResponse update(Long promotionId, PromotionUpdateRequest request) {
        PromotionResponse updatePromotion = new PromotionResponse(promotionId, request.getApplicableHotelId(), request.getApplicableRoomTypeId(), request.getApplicableRatePlanId(), request.getName(), request.getDescription(), request.getDiscountType(), request.getDiscountValue(), request.getMinNights(), request.getBookingWindowMinDays(), request.getBookingWindowMaxDays());
        db.put(promotionId, updatePromotion);
        return updatePromotion;
    }

    public void delete(Long promotionId) {
        db.remove(promotionId);
    }

    public void clear() {
        db.clear();
        currentId = 1L;
    }
}
