package dev.muho.hotel.domain.roominventory;

import dev.muho.hotel.domain.roominventory.dto.RoomInventoryBulkUpdateRequest;
import dev.muho.hotel.domain.roominventory.dto.RoomInventoryResponse;
import dev.muho.hotel.domain.roominventory.dto.RoomInventoryUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class FakeRoomInventoryRepository {

    private final Map<Long, RoomInventoryResponse> db = new HashMap<>();
    private long currentId = 1L;

    public Page<RoomInventoryResponse> findAll(Long roomTypeId, Pageable pageable) {
        // 1. HashMap은 순서가 없으므로, 값들을 리스트로 변환합니다.
        List<RoomInventoryResponse> sortedRoomTypes = new ArrayList<>(db.values())
                .stream()
                .filter(roomInventoryResponse -> roomInventoryResponse.getRoomTypeId() == roomTypeId)
                .collect(Collectors.toList());

        // 2. 일관된 순서를 보장하기 위해 ID를 기준으로 정렬합니다. (매우 중요)
        // 실제 구현에서는 Pageable 객체의 Sort 정보를 사용해 동적으로 정렬할 수 있습니다.
        sortedRoomTypes.sort(Comparator.comparing(RoomInventoryResponse::getId));

        // 3. 정렬된 리스트를 기준으로 페이징 처리를 합니다.
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), sortedRoomTypes.size());

        // 만약 요청한 페이지가 데이터 범위를 벗어나는 경우 빈 리스트를 반환
        if (start > sortedRoomTypes.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, sortedRoomTypes.size());
        }

        List<RoomInventoryResponse> content = sortedRoomTypes.subList(start, end);

        // 4. PageImpl 객체를 생성하여 반환합니다.
        return new PageImpl<>(content, pageable, sortedRoomTypes.size());
    }

    public RoomInventoryResponse findById(Long roomTypeId, Long roomInventoryId) {
        return db.get(roomInventoryId);
    }

    public void bulkUpdate(Long roomTypeId, RoomInventoryBulkUpdateRequest request) {
        LocalDate currentDate = request.getStartDate();
        while (!currentDate.isAfter(request.getEndDate())) {
            RoomInventoryResponse roomInventory = new RoomInventoryResponse(currentId++, roomTypeId, currentDate, request.getTotalRooms(), request.getAvailableRooms());
            db.put(roomInventory.getId(), roomInventory);
            currentDate = currentDate.plusDays(1);
        }
    }

    public void update(Long roomTypeId, LocalDate date, RoomInventoryUpdateRequest request) {
        // Find existing inventory for the given roomTypeId and date
        RoomInventoryResponse existingInventory = db.values().stream()
                .filter(inventory -> inventory.getRoomTypeId().equals(roomTypeId) && inventory.getDate().equals(date))
                .findFirst()
                .orElse(null);

        if (existingInventory != null) {
            // Update existing inventory
            RoomInventoryResponse updatedInventory = new RoomInventoryResponse(existingInventory.getId(), roomTypeId, date, request.getTotalRooms(), request.getAvailableRooms());
            db.put(updatedInventory.getId(), updatedInventory);
        } else {
            // If not found, create a new inventory entry
            RoomInventoryResponse newInventory = new RoomInventoryResponse(currentId++, roomTypeId, date, request.getTotalRooms(), request.getAvailableRooms());
            db.put(newInventory.getId(), newInventory);
        }
    }

    public void clear() {
        db.clear();
        currentId = 1L;
    }
}
