package dev.muho.hotel.domain.promotion;

import dev.muho.hotel.domain.promotion.dto.PromotionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/promotions")
public class PromotionController {

    @GetMapping
    public List<PromotionResponse> getPromotions() {
        return List.of(
            new PromotionResponse(1L, null, null, "Summer Sale", "20% off for stays in June", DiscountType.PERCENTAGE, BigDecimal.valueOf(20.0), null, 1, 3),
            new PromotionResponse(2L, 1L, null, "Hotel Special", "$50 off for Hotel 1", DiscountType.FIXED_AMOUNT, BigDecimal.valueOf(50.0), 2, 1, 30)
        );
    }

    @GetMapping("/{promotionId}")
    public PromotionResponse getPromotion(@PathVariable Long promotionId) {
        return new PromotionResponse(promotionId, 1L, null, "Summer Sale", "20% off for stays in June", DiscountType.PERCENTAGE, BigDecimal.valueOf(20.0), null, 1, 3);
    }

    @PostMapping
    public PromotionResponse createPromotion() {
        return new PromotionResponse(3L, null, null, "Winter Sale", "15% off for stays in December", DiscountType.PERCENTAGE, BigDecimal.valueOf(15.0), null, 1, 5);
    }

    @PutMapping("/{promotionId}")
    public PromotionResponse updatePromotion(@PathVariable Long promotionId) {
        return new PromotionResponse(promotionId, null, null, "Updated Winter Sale", "10% off for stays in December", DiscountType.PERCENTAGE, BigDecimal.valueOf(10.0), null, 1, 5);
    }

    @DeleteMapping("/{promotionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePromotion(@PathVariable Long promotionId) {
    }
}
