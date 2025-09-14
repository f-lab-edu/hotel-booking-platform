package dev.muho.hotel.domain.rateplan;

import dev.muho.hotel.domain.rateplan.dto.RatePlanCreateRequest;
import dev.muho.hotel.domain.rateplan.dto.RatePlanResponse;
import dev.muho.hotel.domain.rateplan.dto.RatePlanUpdateRequest;
 import dev.muho.hotel.domain.rateplan.dto.command.RatePlanCreateCommand;
import dev.muho.hotel.domain.rateplan.dto.command.RatePlanInfoResult;
import dev.muho.hotel.domain.rateplan.service.RatePlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/room-types/{roomTypeId}/rate-plans")
@RequiredArgsConstructor
public class RatePlanController {

    private final RatePlanService service;

    @GetMapping
    public Page<RatePlanResponse> getRatePlans(@PathVariable Long roomTypeId, Pageable pageable) {
        return service.search(roomTypeId, pageable).map(RatePlanResponse::from);
    }

    @GetMapping("/{ratePlanId}")
    public ResponseEntity<RatePlanResponse> getRatePlan(@PathVariable Long roomTypeId, @PathVariable Long ratePlanId) {
        RatePlanInfoResult r = service.findById(roomTypeId, ratePlanId);
        return ResponseEntity.ok(RatePlanResponse.from(r));
    }

    @PostMapping
    public ResponseEntity<RatePlanResponse> createRatePlan(@PathVariable Long roomTypeId, @Valid @RequestBody RatePlanCreateRequest ratePlanCreateRequest) {
        RatePlanCreateCommand cmd = new RatePlanCreateCommand(
                ratePlanCreateRequest.getName(),
                ratePlanCreateRequest.getDescription(),
                ratePlanCreateRequest.getBasePrice(),
                ratePlanCreateRequest.isBreakfastIncluded(),
                ratePlanCreateRequest.isRefundable(),
                ratePlanCreateRequest.getMinNights(),
                ratePlanCreateRequest.getMaxNights()
        );
        RatePlanInfoResult created = service.create(roomTypeId, cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(RatePlanResponse.from(created));
    }

    @PutMapping("/{ratePlanId}")
    public ResponseEntity<RatePlanResponse> updateRatePlan(@PathVariable Long roomTypeId, @PathVariable Long ratePlanId, @Valid @RequestBody RatePlanUpdateRequest ratePlanUpdateRequest) {
        RatePlanCreateCommand cmd = new RatePlanCreateCommand(
                ratePlanUpdateRequest.getName(),
                ratePlanUpdateRequest.getDescription(),
                ratePlanUpdateRequest.getBasePrice(),
                ratePlanUpdateRequest.isBreakfastIncluded(),
                ratePlanUpdateRequest.isRefundable(),
                ratePlanUpdateRequest.getMinNights(),
                ratePlanUpdateRequest.getMaxNights()
        );
        RatePlanInfoResult updated = service.update(roomTypeId, ratePlanId, cmd);
        return ResponseEntity.ok(RatePlanResponse.from(updated));
    }

    @DeleteMapping("/{ratePlanId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRatePlan(@PathVariable Long roomTypeId, @PathVariable Long ratePlanId) {
        service.deleteById(roomTypeId, ratePlanId);
    }
}
