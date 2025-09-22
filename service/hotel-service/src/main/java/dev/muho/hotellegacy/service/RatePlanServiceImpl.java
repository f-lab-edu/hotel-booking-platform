package dev.muho.hotellegacy.service;

import dev.muho.hotellegacy.dto.command.RatePlanCreateCommand;
import dev.muho.hotellegacy.dto.command.RatePlanInfoResult;
import dev.muho.hotellegacy.entity.RatePlan;
import dev.muho.hotellegacy.error.RatePlanNotFoundException;
import dev.muho.hotellegacy.repository.RatePlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatePlanServiceImpl implements RatePlanService {

    private final RatePlanRepository repository;

    @Override
    @Transactional
    public RatePlanInfoResult create(Long roomTypeId, RatePlanCreateCommand command) {
        RatePlan r = RatePlan.createNew(roomTypeId, command.name(), command.description(), command.basePrice(), command.breakfastIncluded(), command.refundable(), command.minNights(), command.maxNights());
        RatePlan saved = repository.save(r);
        return RatePlanInfoResult.from(saved);
    }

    @Override
    public RatePlanInfoResult findById(Long roomTypeId, Long id) {
        RatePlan r = repository.findById(id).orElseThrow(RatePlanNotFoundException::new);
        if (!r.getRoomTypeId().equals(roomTypeId)) throw new RatePlanNotFoundException();
        return RatePlanInfoResult.from(r);
    }

    @Override
    @Transactional
    public RatePlanInfoResult update(Long roomTypeId, Long id, RatePlanCreateCommand command) {
        RatePlan updating = RatePlan.builder()
                .id(id)
                .roomTypeId(roomTypeId)
                .name(command.name())
                .description(command.description())
                .basePrice(command.basePrice())
                .breakfastIncluded(command.breakfastIncluded())
                .refundable(command.refundable())
                .minNights(command.minNights())
                .maxNights(command.maxNights())
                .build();
        RatePlan updated = repository.update(id, updating);
        return RatePlanInfoResult.from(updated);
    }

    @Override
    @Transactional
    public void deleteById(Long roomTypeId, Long id) {
        repository.deleteById(id);
    }

    @Override
    public Page<RatePlanInfoResult> search(Long roomTypeId, Pageable pageable) {
        return repository.findByRoomTypeId(roomTypeId, pageable).map(RatePlanInfoResult::from);
    }
}

