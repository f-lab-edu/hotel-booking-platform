package dev.muho.hotel.service;

import dev.muho.hotel.domain.Hotel;
import dev.muho.hotel.domain.RoomType;
import dev.muho.hotel.domain.Status;
import dev.muho.hotel.dto.request.RoomTypeCreateRequest;
import dev.muho.hotel.dto.request.RoomTypeSearchRequest;
import dev.muho.hotel.dto.request.RoomTypeStatusUpdateRequest;
import dev.muho.hotel.dto.request.RoomTypeUpdateRequest;
import dev.muho.hotel.dto.response.RoomTypeResponse;
import dev.muho.hotel.global.exception.HotelNotFoundException;
import dev.muho.hotel.global.exception.RoomTypeNotFoundException;
import dev.muho.hotel.repository.HotelRepository;
import dev.muho.hotel.repository.RoomTypeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;

    public Page<RoomTypeResponse> searchRoomTypes(RoomTypeSearchRequest request, Pageable pageable) {
        Long hotelId = request.getHotelId();
        String name = hasText(request.getName()) ? request.getName().trim() : null;
        Status status = request.getStatus();

        Page<RoomType> roomTypes;

        if (name != null && status != null) {
            roomTypes = roomTypeRepository.findByHotelIdAndNameContainingIgnoreCaseAndStatus(
                    hotelId, name, status, pageable);
        } else if (name != null) {
            roomTypes = roomTypeRepository.findByHotelIdAndNameContainingIgnoreCase(
                    hotelId, name, pageable);
        } else if (status != null) {
            roomTypes = roomTypeRepository.findByHotelIdAndStatus(
                    hotelId, status, pageable);
        } else {
            roomTypes = roomTypeRepository.findByHotelId(hotelId, pageable);
        }

        return roomTypes.map(RoomTypeResponse::from);
    }

    public RoomTypeResponse findById(Long roomTypeId) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(RoomTypeNotFoundException::new);

        return RoomTypeResponse.from(roomType);
    }

    @Transactional
    public RoomTypeResponse create(RoomTypeCreateRequest request) {
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(HotelNotFoundException::new);

        RoomType roomType = RoomType.builder()
                .hotel(hotel)
                .name(request.getName())
                .standardCapacity(request.getStandardCapacity())
                .maxCapacity(request.getMaxCapacity())
                .build();

        RoomType saved = roomTypeRepository.save(roomType);

        return RoomTypeResponse.from(saved);
    }

    @Transactional
    public RoomTypeResponse update(Long roomTypeId, RoomTypeUpdateRequest request) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(RoomTypeNotFoundException::new);

        roomType.update(
                request.getName(),
                request.getStandardCapacity(),
                request.getMaxCapacity(),
                request.getStatus()
        );

        return RoomTypeResponse.from(roomType);
    }

    @Transactional
    public void delete(Long roomTypeId) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(RoomTypeNotFoundException::new);

        roomType.changeStatus(Status.INACTIVE);
    }

    @Transactional
    public RoomTypeResponse updateStatus(Long roomTypeId, @Valid RoomTypeStatusUpdateRequest request) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(RoomTypeNotFoundException::new);

        roomType.changeStatus(request.getStatus());

        return RoomTypeResponse.from(roomType);
    }

    // 헬퍼 메서드 추가
    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
