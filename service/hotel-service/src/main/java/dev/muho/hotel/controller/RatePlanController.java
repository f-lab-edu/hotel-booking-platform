package dev.muho.hotel.controller;

import dev.muho.hotel.domain.RatePlan;
import dev.muho.hotel.dto.request.*;
import dev.muho.hotel.dto.response.RatePlanResponse;
import dev.muho.hotel.service.RatePlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rate-plans")
@RequiredArgsConstructor
public class RatePlanController {

    private final RatePlanService ratePlanService;

    @GetMapping
    public ResponseEntity<Page<RatePlanResponse>> getRatePlans(
            @Valid @ModelAttribute RatePlanSearchRequest request,
            Pageable pageable) {
        Page<RatePlanResponse> ratePlans = ratePlanService.searchRatePlans(request, pageable);
        return ResponseEntity.ok(ratePlans);
    }

    @GetMapping("/{ratePlanId}")
    public ResponseEntity<RatePlanResponse> getRatePlan(@PathVariable Long ratePlanId) {
        RatePlanResponse ratePlan = ratePlanService.findById(ratePlanId);
        return ResponseEntity.ok(ratePlan);
    }

    @PostMapping
    public ResponseEntity<RatePlanResponse> createRatePlan(@Valid @RequestBody RatePlanCreateRequest request) {
        RatePlanResponse createdRatePlan = ratePlanService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRatePlan);
    }

    @PutMapping("/{ratePlanId}")
    public ResponseEntity<RatePlanResponse> updateRatePlan(
            @PathVariable Long ratePlanId,
            @Valid @RequestBody RatePlanUpdateRequest request) {
        RatePlanResponse updatedRatePlan = ratePlanService.update(ratePlanId, request);
        return ResponseEntity.ok(updatedRatePlan);
    }

    @PatchMapping("/{ratePlanId}/status")
    public ResponseEntity<RatePlanResponse> updateRatePlanStatus(
            @PathVariable Long ratePlanId,
            @Valid @RequestBody RatePlanStatusUpdateRequest request) {
        RatePlanResponse updatedRatePlan = ratePlanService.updateStatus(ratePlanId, request);
        return ResponseEntity.ok(updatedRatePlan);
    }

    @DeleteMapping("/{ratePlanId}")
    public ResponseEntity<Void> deleteRatePlan(@PathVariable Long ratePlanId) {
        ratePlanService.delete(ratePlanId);
        return ResponseEntity.noContent().build();
    }
}
