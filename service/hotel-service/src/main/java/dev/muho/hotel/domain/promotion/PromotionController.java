package dev.muho.hotel.domain.promotion;

import dev.muho.hotel.domain.promotion.dto.api.PromotionCreateRequest;
import dev.muho.hotel.domain.promotion.dto.api.PromotionResponse;
import dev.muho.hotel.domain.promotion.dto.api.PromotionUpdateRequest;
import dev.muho.hotel.domain.promotion.dto.command.PromotionCreateCommand;
import dev.muho.hotel.domain.promotion.dto.command.PromotionUpdateCommand;
import dev.muho.hotel.domain.promotion.dto.command.PromotionInfoResult;
import dev.muho.hotel.domain.promotion.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    public Page<PromotionResponse> getPromotions(Pageable pageable) {
        return promotionService.search(null, pageable).map(PromotionResponse::from);
    }

    @GetMapping("/{promotionId}")
    public ResponseEntity<PromotionResponse> getPromotion(@PathVariable Long promotionId) {
        PromotionInfoResult r = promotionService.findById(promotionId);
        return ResponseEntity.ok(PromotionResponse.from(r));
    }

    @PostMapping
    public ResponseEntity<PromotionResponse> createPromotion(@Valid @RequestBody PromotionCreateRequest request) {
        PromotionCreateCommand cmd = new PromotionCreateCommand(
                request.getApplicableHotelId(),
                request.getApplicableRoomTypeId(),
                request.getApplicableRatePlanId(),
                request.getName(),
                request.getDescription(),
                request.getDiscountType(),
                request.getDiscountValue(),
                request.getMinNights(),
                request.getBookingWindowMinDays(),
                request.getBookingWindowMaxDays()
        );
        PromotionInfoResult created = promotionService.create(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(PromotionResponse.from(created));
    }

    @PutMapping("/{promotionId}")
    public ResponseEntity<PromotionResponse> updatePromotion(@PathVariable Long promotionId, @Valid @RequestBody PromotionUpdateRequest request) {
        PromotionUpdateCommand cmd = new PromotionUpdateCommand(
                request.getName(),
                request.getDescription(),
                request.getDiscountType(),
                request.getDiscountValue(),
                request.getMinNights(),
                request.getBookingWindowMinDays(),
                request.getBookingWindowMaxDays()
        );
        PromotionInfoResult updated = promotionService.update(promotionId, cmd);
        return ResponseEntity.ok(PromotionResponse.from(updated));
    }

    @DeleteMapping("/{promotionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePromotion(@PathVariable Long promotionId) {
        promotionService.deleteById(promotionId);
    }
}
