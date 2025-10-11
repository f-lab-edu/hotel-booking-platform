package dev.muho.hotel.controller;

import dev.muho.hotel.dto.request.PriceAdjustmentCreateRequest;
import dev.muho.hotel.dto.request.PriceAdjustmentSearchRequest;
import dev.muho.hotel.dto.request.PriceAdjustmentUpdateRequest;
import dev.muho.hotel.dto.response.PriceAdjustmentResponse;
import dev.muho.hotel.service.PriceAdjustmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/adjustments")
@RequiredArgsConstructor
public class PriceAdjustmentController {

    private final PriceAdjustmentService priceAdjustmentService;

    @GetMapping
    public ResponseEntity<Page<PriceAdjustmentResponse>> getPriceAdjustments(
            @Valid @ModelAttribute PriceAdjustmentSearchRequest request,
            Pageable pageable) {
        Page<PriceAdjustmentResponse> adjustments = priceAdjustmentService.search(request, pageable);
        return ResponseEntity.ok(adjustments);
    }

    @GetMapping("/{adjustmentId}")
    public ResponseEntity<PriceAdjustmentResponse> getPriceAdjustment(@PathVariable Long adjustmentId) {
        PriceAdjustmentResponse adjustment = priceAdjustmentService.findById(adjustmentId);
        return ResponseEntity.ok(adjustment);
    }

    @PostMapping
    public ResponseEntity<PriceAdjustmentResponse> createPriceAdjustment(@Valid @RequestBody PriceAdjustmentCreateRequest request) {
        PriceAdjustmentResponse createdAdjustment = priceAdjustmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAdjustment);
    }

    @PutMapping("/{adjustmentId}")
    public ResponseEntity<PriceAdjustmentResponse> updatePriceAdjustment(
            @PathVariable Long adjustmentId,
            @Valid @RequestBody PriceAdjustmentUpdateRequest request) {
        PriceAdjustmentResponse updatedAdjustment = priceAdjustmentService.update(adjustmentId, request);
        return ResponseEntity.ok(updatedAdjustment);
    }
}
