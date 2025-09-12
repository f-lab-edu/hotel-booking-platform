package dev.muho.hotel.domain.promotion;

import dev.muho.hotel.domain.promotion.dto.PromotionCreateRequest;
import dev.muho.hotel.domain.promotion.dto.PromotionResponse;
import dev.muho.hotel.domain.promotion.dto.PromotionUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final FakePromotionRepository promotionRepository;

    @GetMapping
    public Page<PromotionResponse> getPromotions(Pageable pageable) {
        return promotionRepository.findAll(pageable);
    }

    @GetMapping("/{promotionId}")
    public ResponseEntity<PromotionResponse> getPromotion(@PathVariable Long promotionId) {
        PromotionResponse promotion = promotionRepository.findById(promotionId);
        if (promotion == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(promotion);
    }

    @PostMapping
    public ResponseEntity<PromotionResponse> createPromotion(@Valid @RequestBody PromotionCreateRequest request) {
        PromotionResponse newPromotion = promotionRepository.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newPromotion);
    }

    @PutMapping("/{promotionId}")
    public ResponseEntity<PromotionResponse> updatePromotion(@PathVariable Long promotionId, @Valid @RequestBody PromotionUpdateRequest request) {
        PromotionResponse existingPromotion = promotionRepository.findById(promotionId);
        if (existingPromotion == null) {
            return ResponseEntity.notFound().build();
        }
        PromotionResponse updatedPromotion = promotionRepository.update(promotionId, request);
        return ResponseEntity.ok(updatedPromotion);
    }

    @DeleteMapping("/{promotionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePromotion(@PathVariable Long promotionId) {
        promotionRepository.delete(promotionId);
    }
}
