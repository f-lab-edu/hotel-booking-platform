package dev.muho.hotel.domain.roomtype.service;

import dev.muho.hotel.domain.roomtype.dto.command.RoomTypeCreateCommand;
import dev.muho.hotel.domain.roomtype.dto.command.RoomTypeInfoResult;
import dev.muho.hotel.domain.roomtype.entity.RoomType;
import dev.muho.hotel.domain.roomtype.error.RoomTypeNotFoundException;
import dev.muho.hotel.domain.roomtype.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository repository;

    @Override
    @Transactional
    public RoomTypeInfoResult create(Long hotelId, RoomTypeCreateCommand command) {
        RoomType r = RoomType.createNew(hotelId, command.name(), command.description(), command.maxOccupancy(), command.standardOccupancy(), command.viewType(), command.bedType());
        RoomType saved = repository.save(r);
        return RoomTypeInfoResult.from(saved);
    }

    @Override
    public RoomTypeInfoResult findById(Long hotelId, Long id) {
        RoomType r = repository.findById(id).orElseThrow(RoomTypeNotFoundException::new);
        if (!r.getHotelId().equals(hotelId)) throw new RoomTypeNotFoundException();
        return RoomTypeInfoResult.from(r);
    }

    @Override
    @Transactional
    public RoomTypeInfoResult update(Long hotelId, Long id, RoomTypeCreateCommand command) {
        RoomType updating = RoomType.builder()
                .id(id)
                .hotelId(hotelId)
                .name(command.name())
                .description(command.description())
                .maxOccupancy(command.maxOccupancy())
                .standardOccupancy(command.standardOccupancy())
                .viewType(command.viewType())
                .bedType(command.bedType())
                .build();
        RoomType updated = repository.update(id, updating);
        return RoomTypeInfoResult.from(updated);
    }

    @Override
    @Transactional
    public void deleteById(Long hotelId, Long id) {
        repository.deleteById(id);
    }

    @Override
    public Page<RoomTypeInfoResult> search(Long hotelId, Pageable pageable) {
        return repository.findByHotelId(hotelId, pageable).map(RoomTypeInfoResult::from);
    }
}

