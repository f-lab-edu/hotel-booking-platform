package dev.muho.hotel.domain.roomtype;

import dev.muho.hotel.domain.roomtype.dto.RoomTypeCreateRequest;
import dev.muho.hotel.domain.roomtype.dto.RoomTypeResponse;
import dev.muho.hotel.domain.roomtype.dto.RoomTypeUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class FakeRoomTypeRepository {

    private final Map<Long, RoomTypeResponse> db = new HashMap<>();
    private long currentId = 1L;

    public Page<RoomTypeResponse> findAll(Long hotelId, Pageable pageable) {
        // 1. HashMap은 순서가 없으므로, 값들을 리스트로 변환합니다.
        List<RoomTypeResponse> sortedRoomTypes = new ArrayList<>(db.values())
                .stream()
                .filter(roomTypeResponse -> roomTypeResponse.getHotelId() == hotelId)
                .collect(Collectors.toList());

        // 2. 일관된 순서를 보장하기 위해 ID를 기준으로 정렬합니다. (매우 중요)
        // 실제 구현에서는 Pageable 객체의 Sort 정보를 사용해 동적으로 정렬할 수 있습니다.
        sortedRoomTypes.sort(Comparator.comparing(RoomTypeResponse::getId));

        // 3. 정렬된 리스트를 기준으로 페이징 처리를 합니다.
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), sortedRoomTypes.size());

        // 만약 요청한 페이지가 데이터 범위를 벗어나는 경우 빈 리스트를 반환
        if (start > sortedRoomTypes.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, sortedRoomTypes.size());
        }

        List<RoomTypeResponse> content = sortedRoomTypes.subList(start, end);

        // 4. PageImpl 객체를 생성하여 반환합니다.
        return new PageImpl<>(content, pageable, sortedRoomTypes.size());
    }

    public RoomTypeResponse findById(Long hotelId, Long roomTypeId) {
        return db.get(roomTypeId);
    }

    public RoomTypeResponse save(Long hotelId, RoomTypeCreateRequest request) {
        RoomTypeResponse newRoomType = new RoomTypeResponse(currentId++, hotelId, request.getName(), request.getDescription(), request.getMaxOccupancy(), request.getStandardOccupancy(), request.getViewType(), request.getBedType());
        db.put(newRoomType.getId(), newRoomType);
        return newRoomType;
    }

    public RoomTypeResponse update(Long hotelId, Long roomTypeId, RoomTypeUpdateRequest request) {
        RoomTypeResponse updatedRoomType = new RoomTypeResponse(roomTypeId, hotelId, request.getName(), request.getDescription(), request.getMaxOccupancy(), request.getStandardOccupancy(), request.getViewType(), request.getBedType());
        db.put(updatedRoomType.getId(), updatedRoomType);
        return updatedRoomType;
    }

    public void delete(Long roomTypeId) {
        db.remove(roomTypeId);
    }

    public void clear() {
        db.clear();
        currentId = 1L;
    }
}
